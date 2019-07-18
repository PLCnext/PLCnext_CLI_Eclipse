/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands.results;

public class Target
{
	private String name;
	private String version;
	private String longVersion;
	private String shortVersion;

	public String getName()
	{
		return name;
	}

	public String getVersion()
	{
		return version;
	}

	public String getLongVersion()
	{
		return longVersion;
	}

	public String getShortVersion()
	{
		return shortVersion;
	}

	public String getDisplayName()
	{
		if (shortVersion != null)
			return name + "," + version;
		return name;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof Target)
		{
			Target target = (Target) obj;
			return target.getName().equals(this.getName()) && target.getLongVersion().equals(this.getLongVersion());
		}
		return super.equals(obj);
	}
}