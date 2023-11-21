/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.toolchains.internal;

import java.util.stream.Collectors;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.phoenixcontact.plcnext.common.ProcessExitedWithErrorException;
import com.phoenixcontact.plcnext.common.commands.results.PlcncliMessage.MessageType;
import com.phoenixcontact.plcnext.common.preferences.PreferenceConstants;
import com.phoenixcontact.plcnext.cplusplus.toolchains.Activator;
import com.phoenixcontact.plcnext.cplusplus.toolchains.ToolchainConfigurator;
import com.phoenixcontact.plcnext.cplusplus.toolchains.ToolchainConfigurator.MacrosAndIncludesWrapper;

public class SaveCMakeListsListener implements IExecutionListener
{
	private final String fileName = "CMakeLists.txt";
	private IProject project = null;
	private MacrosAndIncludesWrapper wrapper = null;
	private ToolchainConfigurator configurator = null;

	@Override
	public void notHandled(String commandId, NotHandledException exception)
	{
		project = null;
		wrapper = null;
	}

	@Override
	public void postExecuteFailure(String commandId, ExecutionException exception)
	{
		project = null;
		wrapper = null;
	}

	@Override
	public void postExecuteSuccess(String commandId, Object returnValue)
	{
		if (project != null && wrapper != null)
		{
			new Job("Update includes for project " + project.getName())
			{

				@Override
				protected IStatus run(IProgressMonitor monitor)
				{
					try
					{
						configurator.updateIncludesOfExistingProject(project, wrapper.getIncludes(),
								wrapper.getMacros(), null);

						project = null;
						wrapper = null;
						return Status.OK_STATUS;
					} catch (ProcessExitedWithErrorException e)
					{
						Activator.getDefault().logError("project configuration error", e);

						project = null;
						wrapper = null;
						return new Status(Status.ERROR, Activator.PLUGIN_ID,
								"Error while trying to execute plcncli command\n"
										+ e.getMessages().stream().filter(m -> m.getMessageType() == MessageType.error)
												.map(m -> m.getMessage()).collect(Collectors.joining("\n")),
								e);
					}
				}
			}.schedule();
		}
	}

	@Override
	public void preExecute(String commandId, ExecutionEvent event)
	{
		String editorName = "";
		IEditorInput editorInput = null;
		if (commandId.equals(IWorkbenchCommandConstants.FILE_SAVE_ALL))
		{
			IWorkbenchWindow workbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);
			if (workbenchWindow != null)
			{
				IWorkbenchPage[] workbenchPages = workbenchWindow.getPages();
				if (workbenchPages != null)
				{
					pagesloop:
					for (IWorkbenchPage workbenchPage : workbenchPages)
					{
						IEditorPart[] dirtyEditors = workbenchPage.getDirtyEditors();
						for (IEditorPart editor : dirtyEditors)
						{
							editorInput = editor.getEditorInput();
							if(editorInput != null && editorInput.getName().equals(fileName))
							{
								editorName = editorInput.getName();
								break pagesloop;
							}
						}
					}
				}
			}
		} else if (commandId.equals(IWorkbenchCommandConstants.FILE_SAVE))
		{

			IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
			if (activeEditor != null)
			{
				editorInput = activeEditor.getEditorInput();
				if (editorInput != null)
				{
					editorName = editorInput.getName();
				}
			}
		}

		if (editorName != null && editorName.equals(fileName))
		{
			IPreferencesService preferencesService = Platform.getPreferencesService();
			boolean showDialog = preferencesService.getBoolean(com.phoenixcontact.plcnext.common.Activator.PLUGIN_ID,
					PreferenceConstants.P_CLI_OPEN_INCLUDE_UPDATE_DIALOG, true, null);
			boolean update_includes = preferencesService.getBoolean(
					com.phoenixcontact.plcnext.common.Activator.PLUGIN_ID, PreferenceConstants.P_CLI_UPDATE_INCLUDES,
					true, null);
			if (showDialog)
			{
				UpdateIncludesDialog dialog = new UpdateIncludesDialog(null);
				int result = dialog.open();
				update_includes = result == Window.OK;

				IEclipsePreferences prefs = InstanceScope.INSTANCE
						.getNode(com.phoenixcontact.plcnext.common.Activator.PLUGIN_ID);

				prefs.put(PreferenceConstants.P_CLI_UPDATE_INCLUDES, String.valueOf(update_includes));

				if (dialog.getButtonSelection())
				{
					prefs.put(PreferenceConstants.P_CLI_OPEN_INCLUDE_UPDATE_DIALOG, "false");
				}
			}

			if (update_includes)
			{

				IResource resource = editorInput.getAdapter(IResource.class);
				if (resource != null)
				{
					project = resource.getProject();

					if (configurator == null)
						configurator = new ToolchainConfigurator();
					BusyIndicator.showWhile(null, new Runnable()
					{
						@Override
						public void run()
						{
							try
							{
								wrapper = configurator.findMacrosAndIncludes(project, null);
							} catch (ProcessExitedWithErrorException e)
							{
								Activator.getDefault().logError("Fetching project configuration failed", e);
							}

						}
					});

				}
			}
		}
	}
}