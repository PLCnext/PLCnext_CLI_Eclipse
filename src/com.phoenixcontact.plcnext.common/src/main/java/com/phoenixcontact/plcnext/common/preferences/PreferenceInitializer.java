/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.preferences;

import java.nio.file.Paths;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import com.phoenixcontact.plcnext.common.Activator;
import com.phoenixcontact.plcnext.common.Messages;

/**
 * This class initializes the preference page for the command line tool
 *
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer
{

	/**
	 * The default path to the command line tool
	 */
	public static final String P_CLI_PATH_DEFAULT = Paths
			.get(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString(),
					Messages.PreferenceInitializer_cliFilenameLinux)
			.toString();
	/**
	 * The default name of the command line tool on windows
	 */
	public static final String P_CLI_NAME_DEFAULT_WIN = Messages.PreferenceInitializer_cliFilenameWin;
	/**
	 * The default name of the command line tool on linux
	 */
	public static final String P_CLI_NAME_DEFAULT_LINUX = Messages.PreferenceInitializer_cliFilenameLinux;
	
	public static final String P_CLI_UPDATE_INCLUDES_DEFAULT = "true";
	
	public static final String P_CLI_OPEN_INCLUDE_UPDATE_DIALOG_DEFAULT = "true";

	@Override
	public void initializeDefaultPreferences()
	{
		IEclipsePreferences node = DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		node.put(PreferenceConstants.P_CLI_PATH, P_CLI_PATH_DEFAULT);
		if (Platform.getOS().equals(Platform.OS_LINUX))
		{
			node.put(PreferenceConstants.P_CLI_NAME, P_CLI_NAME_DEFAULT_LINUX);
		} else
		{
			node.put(PreferenceConstants.P_CLI_NAME, P_CLI_NAME_DEFAULT_WIN);
		}
		node.put(PreferenceConstants.P_CLI_UPDATE_INCLUDES, P_CLI_UPDATE_INCLUDES_DEFAULT);
		node.put(PreferenceConstants.P_CLI_OPEN_INCLUDE_UPDATE_DIALOG, P_CLI_OPEN_INCLUDE_UPDATE_DIALOG_DEFAULT);
	}
}
