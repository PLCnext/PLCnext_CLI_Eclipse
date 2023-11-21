/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands.results;

import java.util.List;
import com.google.gson.JsonObject;

public class GetTargetsCommandResult extends CommandResult
{
	private Target[] targets;

	public GetTargetsCommandResult(JsonObject reply, List<PlcncliMessage> messages)
	{
		super(reply, messages);
	}

	public Target[] getTargets()
	{
		return targets;
	}
}
