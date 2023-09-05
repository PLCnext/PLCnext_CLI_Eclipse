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
		if (longVersion != null)
			return name + "," + longVersion;
		return name;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof Target)
		{
			Target target = (Target) obj;
			String tLongVersion = getLongVersion();
			String oLongVersion = target.getLongVersion();
			if(tLongVersion == null && oLongVersion != null
					|| tLongVersion != null && oLongVersion == null)
				return false;
			return target.getName().equals(this.getName()) && ((oLongVersion!= null && tLongVersion != null)?oLongVersion.equals(tLongVersion):true);
		}
		return super.equals(obj);
	}
}