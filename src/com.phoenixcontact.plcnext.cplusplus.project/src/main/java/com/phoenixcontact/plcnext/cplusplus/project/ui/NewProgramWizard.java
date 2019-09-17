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
import com.phoenixcontact.plcnext.common.commands.NewProgramCommand;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerMessageMessage.MessageType;
import com.phoenixcontact.plcnext.cplusplus.project.Activator;
import com.phoenixcontact.plcnext.cplusplus.toolchains.FindSourcesUtil;

/**
 * Wizard for creation of new program with command line interface
 *
 */
public class NewProgramWizard extends Wizard implements INewWizard
{

	private NewProgramWizardPage page;
	private ICommandManager commandManager;
	private IStructuredSelection selection;

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
	public boolean performFinish()
	{
		BusyIndicator.showWhile(null, new Runnable()
		{

			@Override
			public void run()
			{
				Map<String, String> options = new HashMap<String, String>();

				// add project path
				options.put(NewProgramCommand.OPTION_PATH, page.getProject().getLocation().toOSString());

				// add parent component name
				options.put(NewProgramCommand.OPTION_COMPONENT, page.getComponentName());

				// add program name
				options.put(NewProgramCommand.OPTION_NAME, page.getProgramName());

				// add component namespace
				String namespace = page.getProgramNamespace();
				if (namespace != null && !namespace.isEmpty())
				{
					options.put(NewProgramCommand.OPTION_NAMESPACE, page.getProgramNamespace());
				}

				String sourceEntries = null;

				// add source folder list
				List<String> entries = FindSourcesUtil.findSourceEntries(page.getProject());
				if (entries != null && !entries.isEmpty())
				{
					sourceEntries = entries.stream().collect(Collectors.joining(","));
					options.put(NewProgramCommand.OPTION_SOURCES, sourceEntries);
				}

				Command command = commandManager.createCommand(options, NewProgramCommand.class);
				try
				{
					commandManager.executeCommand(command, false, null);

					// execute generate code command
					Map<String, String> generateOptions = new HashMap<>();
					generateOptions.put(GenerateCodeCommand.OPTION_PATH, page.getProject().getLocation().toOSString());

					// get source directories for generate command
					if (sourceEntries != null && !sourceEntries.isEmpty())
					{
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
							
					ErrorDialog.openError(getShell(), "Could not create new program", "The program could not be created.", 
								new Status(Status.ERROR, Activator.PLUGIN_ID, errormessage));
					
				}

				// refreshWorkspace
				try
				{
					RefreshScopeManager refreshManager = RefreshScopeManager.getInstance();
					IWorkspaceRunnable runnable = refreshManager.getRefreshRunnable(page.getProject());
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
	public void addPages()
	{
		page = new NewProgramWizardPage(commandManager, selection);
		addPage(page);
	}

}
