/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands.results;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerMessageMessage;

public class GetIncludePathsCommandResult extends CommandResult
{

	public GetIncludePathsCommandResult(JsonObject reply, List<ServerMessageMessage> messages)
	{
		super(reply, messages);
	}

	public GetIncludePathsCommandResult(IncludePath[] includePaths)
	{
		super((JsonObject) null, null);
		this.includePaths = includePaths;
	}

	private IncludePath[] includePaths;

	public IncludePath[] getIncludePaths()
	{
		return includePaths;
	}
	
	public static class IncludePath
	{
		private String path;

		public String getPath()
		{
			return path;
		}

		public IncludePath(String path)
		{
			this.path = path;
		}
	}

	public static GetIncludePathsCommandResult convertResultToJson(List<String> stdout)
	{
		if (stdout != null && stdout.stream()
				.anyMatch(x -> x.startsWith("Project ") && x.endsWith(" has the following include paths:")))
		{
			String startElement = stdout.stream()
					.filter(x -> x.startsWith("Project ") && x.endsWith(" has the following include paths:"))
					.findFirst().orElse(null);
			if (startElement != null)
			{
				List<IncludePath> includes = new ArrayList<IncludePath>();
				int startIndex = stdout.indexOf(startElement);
				for (String result : stdout.subList(startIndex + 1, stdout.size()))
				{
					includes.add(new IncludePath(result.trim()));
				}

				return new GetIncludePathsCommandResult(includes.toArray(new IncludePath[0]));

			}

		}
		return null;
	}
}
