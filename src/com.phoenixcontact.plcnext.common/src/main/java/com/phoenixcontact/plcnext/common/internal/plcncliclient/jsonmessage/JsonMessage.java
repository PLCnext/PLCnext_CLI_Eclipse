/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.internal.plcncliclient.jsonmessage;


public class JsonMessage
{
	private MessageType type;
	
	public JsonMessage(MessageType type)
	{
		this.type = type;
	}
	
	public MessageType getType()
	{
		return type;
	}
	
}
