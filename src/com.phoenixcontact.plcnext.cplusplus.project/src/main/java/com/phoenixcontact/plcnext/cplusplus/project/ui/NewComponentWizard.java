/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.project.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.cdt.core.resources.RefreshScopeManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.phoenixcontact.plcnext.common.EclipseContextHelper;
import com.phoenixcontact.plcnext.common.ICommandManager;
import com.phoenixcontact.plcnext.common.IDIHost;
import com.phoenixcontact.plcnext.common.ProcessExitedWithErrorException;
import com.phoenixcontact.plcnext.common.commands.Command;
import com.phoenixcontact.plcnext.common.commands.GenerateCodeCommand;
import com.phoenixcontact.plcnext.common.commands.NewAcfComponentCommand;
import com.phoenixcontact.plcnext.common.commands.NewComponentCommand;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerMessageMessage.MessageType;
import com.phoenixcontact.plcnext.cplusplus.project.Activator;
import com.phoenixcontact.plcnext.cplusplus.project.acfproject.PlcnextAcfProjectNature;
import com.phoenixcontact.plcnext.cplusplus.toolchains.FindSourcesUtil;

/**
 * Wizard for creation of new component with command line interface
 *
 */
public class NewComponentWizard extends Wizard implements INewWizard
{

	private NewComponentWizardPage page;
	private ICommandManager commandManager;
	private IStructuredSelection selection;
	@Override
	public boolean performFinish()
	{
		BusyIndicator.showWhile(null, new Runnable()
		{

			@Override
			public void run()
			{
				Map<String, String> options = new HashMap<String, String>();
				IProject project = page.getProject();
				// add project path
				options.put(NewComponentCommand.OPTION_PATH, project.getLocation().toOSString());

				// add component name
				options.put(NewComponentCommand.OPTION_NAME, page.getComponentName());

				// add component namespace
				String namespace = page.getComponentNamespace();
				if (namespace != null && !namespace.isEmpty())
				{
					options.put(NewComponentCommand.OPTION_NAMESPACE, page.getComponentNamespace());
				}

				Command command;
				try
				{
					if(project.hasNature(PlcnextAcfProjectNature.NATURE_ID))
					{
						command = commandManager.createCommand(options, NewAcfComponentCommand.class);
					}
					else
					{
						command = commandManager.createCommand(options, NewComponentCommand.class);
					}
				} catch (CoreException e1)
				{
					command = commandManager.createCommand(options, NewComponentCommand.class);
				}
				try
				{
					commandManager.executeCommand(command, false, null);

					// execute generate code command
					Map<String, String> generateOptions = new HashMap<>();
					generateOptions.put(GenerateCodeCommand.OPTION_PATH, project.getLocation().toOSString());

					// get source directories for generate command
					List<String> entries = FindSourcesUtil.findSourceEntries(project);
					if (entries != null && !entries.isEmpty())
					{
						String sourceEntries = entries.stream().collect(Collectors.joining(","));
						generateOptions.put(GenerateCodeCommand.OPTION_SOURCES, sourceEntries);
					}

					Command generateCommand = commandManager.createCommand(generateOptions, GenerateCodeCommand.class);
					commandManager.executeCommand(generateCommand, false, null);
				} catch (ProcessExitedWithErrorException e)
				{
					if (e.getMessages().stream().anyMatch(
							m -> m.getMessageType() == MessageType.error && m.getMessage().contains("exists already")))
					{
						page.setExistsError();
					}
					
					String errormessage = e.getMessages().stream().filter(m -> m.getMessageType() == MessageType.error)
							.map(m -> m.getMessage()).collect(Collectors.joining("\n"));
					
					Activator.getDefault().logError("Error while trying to execute clif command:\n" + errormessage, e);
					ErrorDialog.openError(getShell(), "Could not create new component",
							"The component could not be created. See log for more details."
							, new Status(Status.ERROR, Activator.PLUGIN_ID, errormessage));
				}
				// refreshWorkspace
				try
				{
					RefreshScopeManager refreshManager = RefreshScopeManager.getInstance();
					IWorkspaceRunnable runnable = refreshManager.getRefreshRunnable(project);
					ResourcesPlugin.getWorkspace().run(runnable, null, IWorkspace.AVOID_UPDATE, null);
				} catch (CoreException e)
				{
					Activator.getDefault().logError("Error when refreshing workspace.", e);
				}

			}
		});

		if (page.isPageComplete())
			return true;
		return false;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection)
	{
		this.selection = selection;

		setWindowTitle("PLCnext C++ Component");

		// get commandManager to create and execute commands
		IEclipseContext context = EclipseContextHelper.getActiveContext();
		IDIHost host = ContextInjectionFactory.make(IDIHost.class, context);
		commandManager = host.getExport(ICommandManager.class);
	}

	@Override
	public void addPages()
	{
		page = new NewComponentWizardPage(commandManager, selection);
		addPage(page);
	}
}
