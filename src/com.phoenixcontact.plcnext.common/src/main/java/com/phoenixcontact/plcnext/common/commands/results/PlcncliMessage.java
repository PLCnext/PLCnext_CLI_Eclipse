/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands.results;


public class PlcncliMessage
{
	public PlcncliMessage(String message, MessageType type)
	{
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
	
	private String message;
	private MessageType messageType;
	
	
	public String getMessage()
	{
		return message;
	}
	
	public MessageType getMessageType()
	{
		return messageType;
	}
	
}
