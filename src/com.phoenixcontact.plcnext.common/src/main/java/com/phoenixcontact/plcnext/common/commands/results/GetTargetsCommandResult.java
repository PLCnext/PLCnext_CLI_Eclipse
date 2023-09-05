/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands.results;

import java.util.List;
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
}
