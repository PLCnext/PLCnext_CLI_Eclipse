/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands.results;

import java.util.List;

import com.google.gson.JsonObject;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerMessageMessage;

public class GetProjectNamespaceCommandResult extends CommandResult
{
	public GetProjectNamespaceCommandResult(JsonObject reply, List<ServerMessageMessage> messages)
	{
		super(reply, messages);
	}
	
	public GetProjectNamespaceCommandResult(String namespace)
	{
		super((JsonObject)null,null);
		this.namespace = namespace;
	}
	
	private String namespace;
	
	public String getNamespace()
	{
		return namespace;
	}
	
	public static GetProjectNamespaceCommandResult convertResultToJson(List<String> stdout)
	{
		if (stdout != null)
		{
			return new GetProjectNamespaceCommandResult(stdout.get(0));
		}
		return null;
	}
}
