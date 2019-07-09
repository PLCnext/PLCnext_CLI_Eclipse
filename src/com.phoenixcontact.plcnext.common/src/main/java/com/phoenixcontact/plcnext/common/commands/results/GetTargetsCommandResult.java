/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands.results;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.phoenixcontact.plcnext.common.Messages;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerMessageMessage;

public class GetTargetsCommandResult extends CommandResult
{
	private Target[] targets;

	public GetTargetsCommandResult(JsonObject reply, List<ServerMessageMessage> messages)
	{
		super(reply, messages);
	}
	
	public GetTargetsCommandResult(Target[] targets) {
		super((JsonObject)null,null);
		this.targets = targets;
	}
	
	public Target[] getTargets()
	{
		return targets;
	}
	
	public static class Target
	{
		public Target(String displayname)
		{
			this.name = displayname;
		}
		
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
			if(shortVersion != null)
				return name + "," + version;
			return name;
		}
	}
	
	public static GetTargetsCommandResult convertResultToJson(List<String> stdout)
	{
		if(stdout != null && stdout.contains(Messages.GetTargetsCommand_outputdescription))
		{
			int startIndex = stdout.indexOf(Messages.GetTargetsCommand_outputdescription) + 1;
			List<Target> results = new ArrayList<Target>();
			for (String result : stdout.subList(startIndex, stdout.size()))
			{
				String resultTrimmed = result.trim();
				if (resultTrimmed.startsWith("-"))
				{
					results.add(new Target(resultTrimmed.substring(1, resultTrimmed.length()).trim()));
				}
			}
			
			return new GetTargetsCommandResult(results.toArray(new Target[0]));
		}
		return null;
	}
}
