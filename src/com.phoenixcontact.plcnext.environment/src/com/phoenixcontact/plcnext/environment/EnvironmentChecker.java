/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.environment;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;

public class EnvironmentChecker implements IStartup
{

	@Override
	public void earlyStartup()
	{
		try
		{
			final String version = System.getProperty("java.version");
			if (version != null && version.startsWith("1."))
			{
				Display.getDefault().syncExec(new Runnable()
				{

					@Override
					public void run()
					{
						ErrorDialog.openError(null, "Java version not supported",
								"The currently installed version of the PLCnext Technology feature requires java version 9 or higher.\n"
										+ "Please make sure eclipse is started at least with java version 9 otherwise the feature will not work as expected.",
								new Status(Status.ERROR, Activator.PLUGIN_ID,
										"Used java version: " + version + "\nRequired java version: 9 or higher"));
					}
				});
			}
		} catch (SecurityException e)
		{
			Activator.getDefault().logError("Exception while trying to get java.version property", e);
		}
	}

}
