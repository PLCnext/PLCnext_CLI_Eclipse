/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.toolchains.internal;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ManagedOptionValueHandler;

import com.phoenixcontact.plcnext.cplusplus.toolchains.Activator;
import com.phoenixcontact.plcnext.cplusplus.toolchains.FindSourcesUtil;

public class SourceDirectoriesOptionValueHandler extends ManagedOptionValueHandler
{

	@Override
	public boolean handleValue(IBuildObject configuration, IHoldsOptions holder, IOption option, String extraArgument,
			int event)
	{
		if (event == EVENT_LOAD || event == EVENT_OPEN)
		{
			if (configuration != null)
			{
				if (configuration instanceof IConfiguration)
				{
					IConfiguration conf = (IConfiguration) configuration;

					List<String> sourceEntries = FindSourcesUtil.findSourceEntries(conf);
					if(sourceEntries != null) {
						String value = sourceEntries.stream().collect(Collectors.joining(",", "\"", "\""));
						try
						{
							option.setValue(value);

							return true;
						} catch (BuildException e)
						{
							Activator.getDefault().logError("Could not set build property: sources", e);
						}
					}
				}
			}
		}
		return false;
	}
}