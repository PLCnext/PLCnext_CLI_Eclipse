/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.plcncliclient;

import java.util.Map;


public class ServerMessageMessage extends ServerMessage
{
	public ServerMessageMessage(String message, MessageType type)
	{
		super(Type.message);
		this.message = message;
		this.messageType = type;
	}
	public enum MessageType
	{
		information,
		error,
		warning,
		verbose
	}
	
	private String command;
	private Map<String, String> arguments;
	private String message;
	private MessageType messageType;
	
	public String getCommand()
	{
		return command;
	}
	
	public Map<String, String> getArguments()
	{
		return arguments;
	}
	
	public String getMessage()
	{
		return message;
	}
	
	public MessageType getMessageType()
	{
		return messageType;
	}
	
}
