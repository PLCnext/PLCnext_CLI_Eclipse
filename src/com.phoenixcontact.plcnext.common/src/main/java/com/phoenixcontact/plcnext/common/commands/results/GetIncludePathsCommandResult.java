/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands.results;

import java.util.List;
import com.google.gson.JsonObject;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerMessageMessage;

public class GetIncludePathsCommandResult extends CommandResult
{

	public GetIncludePathsCommandResult(JsonObject reply, List<ServerMessageMessage> messages)
	{
		super(reply, messages);
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
}
