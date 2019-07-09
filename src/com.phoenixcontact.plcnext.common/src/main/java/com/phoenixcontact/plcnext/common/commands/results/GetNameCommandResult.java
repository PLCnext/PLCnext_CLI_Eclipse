/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands.results;

import java.util.List;

import com.google.gson.JsonObject;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerMessageMessage;

public class GetNameCommandResult extends CommandResult
{

	public GetNameCommandResult(JsonObject reply, List<ServerMessageMessage> messages)
	{
		super(reply, messages);
	}
	
	public GetNameCommandResult(String name)
	{
		super((JsonObject)null,null);
		this.name = name;
	}
	
	private String name;
	
	public String getName()
	{
		return name;
	}
	
	public static GetNameCommandResult convertResultToJson(List<String> stdout)
	{
		if (stdout != null)
		{
			return new GetNameCommandResult(stdout.get(0));
		}
		return null;
	}

}
