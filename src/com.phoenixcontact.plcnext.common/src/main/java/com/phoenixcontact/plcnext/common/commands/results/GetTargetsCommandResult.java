/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands.results;

import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerMessageMessage;

public class GetTargetsCommandResult extends CommandResult
{
	private Target[] targets;

	public GetTargetsCommandResult(JsonObject reply, List<ServerMessageMessage> messages)
	{
		super(reply, messages);
	}

	public Target[] getTargets()
	{
		return targets;
	}

	public static GetTargetsCommandResult convertResultToJson(List<String> stdout)
	{
		if (stdout != null)
		{
			return new Gson().fromJson(stdout.stream().collect(Collectors.joining("")), GetTargetsCommandResult.class);
		}
		return null;
	}
}
