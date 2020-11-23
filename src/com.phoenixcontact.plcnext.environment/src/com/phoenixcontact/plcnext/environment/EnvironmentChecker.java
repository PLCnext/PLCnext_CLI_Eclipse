/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.environment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;

public class EnvironmentChecker implements IStartup {

	@Override
	public void earlyStartup() 
	{
		try 
		{
			final String version = System.getProperty("java.version");
			Pattern pattern = Pattern.compile("^(?<MajorVersion>\\d+).*");
			Matcher match = pattern.matcher(version);
			match.matches();
			int majorVersion = Integer.parseInt(match.group("MajorVersion"));
			if (majorVersion < 11) 
			{
				Display.getDefault().syncExec(new Runnable() 
				{

					@Override
					public void run() 
					{
						ErrorDialog.openError(null, "Java version not supported",
								"The currently installed version of the PLCnext Technology feature requires java version 11 or higher.\n"
										+ "Please make sure eclipse is started at least with java version 11 otherwise the feature will not work as expected.\n"
										+ "Java versions can be found here: https://jdk.java.net/archive/",
								new Status(Status.ERROR, Activator.PLUGIN_ID,
										"Used java version: " + version + "\nRequired java version: 11 or higher"));
					}
				});
			}

		} catch (Exception e) {
			Activator.getDefault().logError("Exception while trying to get java.version property", e);
		}
	}

}
