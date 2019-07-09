/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands.results;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.phoenixcontact.plcnext.common.commands.results.GetTargetsCommandResult.Target;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerMessageMessage;

public class GetProjectTargetsCommandResult extends CommandResult
{

	public GetProjectTargetsCommandResult(JsonObject reply, List<ServerMessageMessage> messages)
	{
		super(reply, messages);
	}
	
	public GetProjectTargetsCommandResult(ProjectTarget[] targets)
	{
		super((JsonObject)null, null);
		this.targets = targets;
	}

	private ProjectTarget[] targets;

	public ProjectTarget[] getTargets()
	{
		return targets;
	}
	
	public static class ProjectTarget extends Target
	{
		public ProjectTarget(String displayname)
		{
			super(displayname);
		}

		private boolean available;

		public boolean isAvailable()
		{
			return available;
		}
	}

	public static GetProjectTargetsCommandResult convertResultToJson(List<String> stdout)
	{
		if (stdout != null
				&& stdout.stream().anyMatch(x -> x.startsWith("Project ") && x.endsWith(" supports targets:")))
		{
			String startElement = stdout.stream().filter(x -> x.startsWith("Project ") && x.endsWith(" supports targets:")).findFirst().orElse(null);
			if(startElement != null)
			{
				int startIndex = stdout.indexOf(startElement) + 1;
				List<ProjectTarget> results = new ArrayList<ProjectTarget>();
				for (String result : stdout.subList(startIndex, stdout.size()))
				{
					String resultTrimmed = result.trim();
					if (resultTrimmed.startsWith("-"))
					{
						results.add(new ProjectTarget(resultTrimmed.substring(1, resultTrimmed.length()).trim()));
					}
				}
				return new GetProjectTargetsCommandResult(results.toArray(new ProjectTarget[0]));
			}
					

			
		}
		return null;
	}

}
