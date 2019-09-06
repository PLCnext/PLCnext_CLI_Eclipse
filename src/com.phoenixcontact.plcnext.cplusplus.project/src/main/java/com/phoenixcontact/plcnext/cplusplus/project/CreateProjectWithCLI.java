/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.project;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;

import com.phoenixcontact.plcnext.common.CliNotExistingException;
import com.phoenixcontact.plcnext.common.EclipseContextHelper;
import com.phoenixcontact.plcnext.common.ICommandManager;
import com.phoenixcontact.plcnext.common.IDIHost;
import com.phoenixcontact.plcnext.common.ProcessExitedWithErrorException;
import com.phoenixcontact.plcnext.common.commands.Command;
import com.phoenixcontact.plcnext.common.commands.GenerateCodeCommand;
import com.phoenixcontact.plcnext.common.commands.NewAcfProjectCommand;
import com.phoenixcontact.plcnext.common.commands.NewProjectCommand;
import com.phoenixcontact.plcnext.common.commands.SetTargetCommand;
import com.phoenixcontact.plcnext.common.commands.results.Target;
import com.phoenixcontact.plcnext.cplusplus.project.acfproject.PlcnextAcfProjectNature;
import com.phoenixcontact.plcnext.cplusplus.project.ui.ProjectPropertiesWizardDataPage;
import com.phoenixcontact.plcnext.cplusplus.project.ui.SelectTargetsWizardDataPage;
import com.phoenixcontact.plcnext.cplusplus.toolchains.ToolchainConfigurator;

/**
 * Extension of cdt extension point org.eclipse.cdt.core.templateProcessTypes A
 * custom Process Runner used while creating a new plcnext project. This process
 * executes the command line tool to create a new project.
 */
public class CreateProjectWithCLI extends ProcessRunner
{

	@Override
	public void process(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor)
			throws ProcessFailureException
	{
		createProjectWithCLI(args[0].getSimpleValue(), args[1].getSimpleValue(), monitor);
	}

	private void createProjectWithCLI(String projectName, String projectType, IProgressMonitor monitor)
	{

		Object componentNameProperty = MBSCustomPageManager.getPageProperty(ProjectPropertiesWizardDataPage.PAGE_ID,
				ProjectPropertiesWizardDataPage.KEY_COMPONENTNAME);
		Object programNameProperty = MBSCustomPageManager.getPageProperty(ProjectPropertiesWizardDataPage.PAGE_ID,
				ProjectPropertiesWizardDataPage.KEY_PROGRAMNAME);
		Object projectNamespaceProperty = MBSCustomPageManager.getPageProperty(ProjectPropertiesWizardDataPage.PAGE_ID,
				ProjectPropertiesWizardDataPage.KEY_PROJECTNAMESPACE);
		String componentName = componentNameProperty != null ? componentNameProperty.toString() : null;
		String programName = programNameProperty != null ? programNameProperty.toString() : null;
		String projectNamespace = projectNamespaceProperty != null ? projectNamespaceProperty.toString() : null;

		Map<String, String> options = new HashMap<String, String>();

		if (programName != null && !programName.isEmpty())
		{
			options.put(NewProjectCommand.OPTION_PNAME, programName);
		}
		if (projectName != null && !projectName.isEmpty())
		{
			options.put(NewProjectCommand.OPTION_NAME, projectName);
		}
		if (componentName != null && !componentName.isEmpty())
		{
			options.put(NewProjectCommand.OPTION_CNAME, componentName);
		}
		if (projectNamespace != null && !projectNamespace.isEmpty())
		{
			options.put(NewProjectCommand.OPTION_NAMESPACE, projectNamespace);
		}
		addLocation(projectName, options);

		// get commandManager to create and execute the NewProject-command
		IEclipseContext context = EclipseContextHelper.getActiveContext();
		IDIHost host = ContextInjectionFactory.make(IDIHost.class, context);
		ICommandManager commandManager = host.getExport(ICommandManager.class);

		Command command;
		if (ProjectType.valueOf(projectType) == ProjectType.STANDARD)
		{
			command = commandManager.createCommand(options, NewProjectCommand.class);
		} else
		{
			command = commandManager.createCommand(options, NewAcfProjectCommand.class);
		}

		try
		{
			commandManager.executeCommand(command, true, true, monitor);

			String projectPath = options.get(NewProjectCommand.OPTION_OUTPUT);

			Map<String, String> generateOptions = new HashMap<>();
			generateOptions.put(GenerateCodeCommand.OPTION_PATH, projectPath);
			Command generateCommand = commandManager.createCommand(generateOptions, GenerateCodeCommand.class);
			commandManager.executeCommand(generateCommand, false, monitor);

			setProjectNature(ResourcesPlugin.getWorkspace().getRoot().getProject(projectName), projectType);

			addTargets(projectPath, commandManager, monitor);

			new ToolchainConfigurator().configureProject(projectName, monitor, true);

		} catch (ProcessExitedWithErrorException e)
		{
			Activator.getDefault().logError("Error while trying to create project.", e);
			Display.getDefault().asyncExec(new Runnable()
			{

				@Override
				public void run()
				{
					ErrorDialog.openError(null, "Error while trying to create project.",
							"PLCnCLI project could not be created.",
							new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage()));

				}
			});
			try
			{
				ResourcesPlugin.getWorkspace().getRoot().getProject(projectName).delete(true, monitor);
			} catch (CoreException | NullPointerException e1)
			{
				// do nothing since this is just a try to clean up after failure
			}
		}
	}

	private void setProjectNature(IProject project, String projectType)
	{
		try
		{
			IProjectDescription description = project.getDescription();
			String[] natures = description.getNatureIds();
			String[] newNatures = new String[natures.length + 1];
			System.arraycopy(natures, 0, newNatures, 0, natures.length);
			newNatures[natures.length] = PlcProjectNature.NATURE_ID;

			if (ProjectType.valueOf(projectType) == ProjectType.ACF)
			{
				natures = newNatures;
				newNatures = new String[natures.length + 1];
				System.arraycopy(natures, 0, newNatures, 0, natures.length);
				newNatures[natures.length] = PlcnextAcfProjectNature.NATURE_ID;
			}
			description.setNatureIds(newNatures);
			project.setDescription(description, null);

		} catch (CoreException e)
		{
			Activator.getDefault().logError("Error while trying to set project nature.", e);
		}
	}

	private void addLocation(String projectName, Map<String, String> options)
	{
		IWorkspaceRoot workspaceroot = ResourcesPlugin.getWorkspace().getRoot();
		String key = NewProjectCommand.OPTION_OUTPUT;
		String value = workspaceroot.getProject(projectName).getLocation().toOSString();
		options.put(key, value);
	}

	private void addTargets(String projectPath, ICommandManager commandManager, IProgressMonitor monitor)
			throws CliNotExistingException, ProcessExitedWithErrorException
	{

		Map<String, String> setTargetOptions = new HashMap<>();

		setTargetOptions.put(SetTargetCommand.OPTION_PATH, projectPath);
		String addKey = SetTargetCommand.OPTION_ADD;
		setTargetOptions.put(addKey, null);

		Object property = MBSCustomPageManager.getPageProperty(SelectTargetsWizardDataPage.PAGE_ID,
				SelectTargetsWizardDataPage.KEY_TARGETS);
		if (property != null && property instanceof Target[])
		{
			Target[] targets = (Target[]) property;
			for (Target target : targets)
			{
				setTargetOptions.put(SetTargetCommand.OPTION_NAME, target.getName());
				if (target.getVersion() != null)
					setTargetOptions.put(SetTargetCommand.OPTION_VERSION, target.getVersion());
				else if (target.getLongVersion() != null)
					setTargetOptions.put(SetTargetCommand.OPTION_VERSION, target.getLongVersion());
				else if (target.getShortVersion() != null)
					setTargetOptions.put(SetTargetCommand.OPTION_VERSION, target.getShortVersion());
				Command setTargetCommand = commandManager.createCommand(setTargetOptions, SetTargetCommand.class);
				commandManager.executeCommand(setTargetCommand, monitor);
			}
		}
	}
}
