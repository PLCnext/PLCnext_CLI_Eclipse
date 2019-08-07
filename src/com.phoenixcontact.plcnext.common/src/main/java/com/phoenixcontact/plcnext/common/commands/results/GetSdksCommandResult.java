/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands.results;

import java.util.List;
import com.google.gson.JsonObject;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerMessageMessage;

public class GetSdksCommandResult extends CommandResult
{

	public GetSdksCommandResult(JsonObject reply, List<ServerMessageMessage> messages)
	{
		super(reply, messages);
	}

	private Sdk[] sdks;

	public Sdk[] getSdks()
	{
		return sdks;
	}

	public static class Sdk
	{
		private String path;

		public String getPath()
		{
			return path;
		}
	}
}
