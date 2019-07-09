/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.plcncliclient;

import java.util.Map;

import com.google.gson.JsonObject;

public class ServerReplyMessage extends ServerMessage
{
	public ServerReplyMessage(Type type)
	{
		super(type);
	}

	private ReplyType inReplyTo;
	private JsonObject reply;
	private boolean success;
	private String command;
	private  Map<String, String> arguments;

	public enum ReplyType
	{
		handshake,
		command,
		cancel
	}
	
	public ReplyType getInReplyTo()
	{
		return inReplyTo;
	}
	
	public JsonObject getReply()
	{
		return reply;
	}
	
	public boolean isSuccess()
	{
		return success;
	}

	public Map<String, String> getArguments()
	{
		return arguments;
	}
	
	public String getCommand()
	{
		return command;
	}
}
