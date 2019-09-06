/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.toolchains.internal;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import com.phoenixcontact.plcnext.common.ProcessExitedWithErrorException;
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
			try
			{
				configurator.updateIncludesOfExistingProject(project, wrapper.getIncludes(), wrapper.getMacros(), null);
			} catch (ProcessExitedWithErrorException e)
			{
				Activator.getDefault().logError("project configuration error", e);
			}

			project = null;
			wrapper = null;
		}
	}

	@Override
	public void preExecute(String commandId, ExecutionEvent event)
	{
		IPreferencesService preferencesService = Platform.getPreferencesService();
		boolean showDialog = preferencesService.getBoolean(com.phoenixcontact.plcnext.common.Activator.PLUGIN_ID,
				PreferenceConstants.P_CLI_OPEN_INCLUDE_UPDATE_DIALOG, true, null);
		boolean update_includes = preferencesService.getBoolean(com.phoenixcontact.plcnext.common.Activator.PLUGIN_ID,
				PreferenceConstants.P_CLI_UPDATE_INCLUDES, true, null);
		if (showDialog)
		{
			UpdateIncludesDialog dialog = new UpdateIncludesDialog(null);
			int result = dialog.open();
			update_includes = result == Window.OK;
			
			IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(com.phoenixcontact.plcnext.common.Activator.PLUGIN_ID);
			
			prefs.put(PreferenceConstants.P_CLI_UPDATE_INCLUDES, String.valueOf(update_includes));
			
			if(dialog.getButtonSelection())
			{
				prefs.put(PreferenceConstants.P_CLI_OPEN_INCLUDE_UPDATE_DIALOG, "false");
			}
		}

		if (update_includes)
		{
			IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
			if (activeEditor != null)
			{

				IEditorInput editorInput = activeEditor.getEditorInput();
				if (editorInput != null)
				{
					String inputName = editorInput.getName();
					if (inputName != null && inputName.equals(fileName))
					{
						IResource resource = editorInput.getAdapter(IResource.class);
						if (resource != null)
						{
							project = resource.getProject();
							try
							{
								if (configurator == null)
									configurator = new ToolchainConfigurator();

								wrapper = configurator.findMacrosAndIncludes(project, null);
							} catch (ProcessExitedWithErrorException e)
							{
								Activator.getDefault().logError("Fetching project configuration failed", e);
							}
						}
					}
				}
			}
		}
	}

}
