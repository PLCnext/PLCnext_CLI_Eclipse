/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.internal.plcncliclient;

import com.google.gson.Gson;
import com.phoenixcontact.plcnext.common.internal.plcncliclient.jsonmessage.JsonCancelMessage;
import com.phoenixcontact.plcnext.common.internal.plcncliclient.jsonmessage.JsonCommandMessage;
import com.phoenixcontact.plcnext.common.internal.plcncliclient.jsonmessage.JsonHandshakeMessage;
import com.phoenixcontact.plcnext.common.internal.plcncliclient.jsonmessage.JsonKillMessage;
import com.phoenixcontact.plcnext.common.internal.plcncliclient.jsonmessage.JsonMessage;

public class ClientMessage extends NamedPipeMessage
{
	
	private ClientMessage(JsonMessage message)
	{
		super(gson.toJson(message));
		this.jsonMessage = message;
	}

	private JsonMessage jsonMessage;
	private static Gson gson = new Gson();
	
	public static ClientMessage newCancelMessage(String command)
	{
		return new ClientMessage(new JsonCancelMessage(command));
	}
	
	public static ClientMessage newHandshakeMessage(int major, int minor)
	{
		return new ClientMessage(new JsonHandshakeMessage(major, minor));
	}
	
	public static ClientMessage newCommandMessage(String command)
	{
		return new ClientMessage(new JsonCommandMessage(command));
	}
	
	public static ClientMessage killMessage()
	{
		return new ClientMessage(new JsonKillMessage());
	}
	
	public JsonMessage getJsonMessage()
	{
		return jsonMessage;
	}
}
