/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.internal.plcncliclient.jsonmessage;

public class JsonCommandMessage extends JsonMessage
{
	private String command;
	private ParsedCommand parsedCommand;
	
	public JsonCommandMessage(String command)
	{
		super(MessageType.command);
		this.command = command;
	}

	public String getCommand()
	{
		return command;
	}
	
	public ParsedCommand getParsedCommand()
	{
		return parsedCommand;
	}
	
}
