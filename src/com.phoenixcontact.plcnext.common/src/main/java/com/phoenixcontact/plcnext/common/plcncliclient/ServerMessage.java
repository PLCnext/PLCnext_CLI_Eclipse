/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.plcncliclient;

/**
 *
 */
public class ServerMessage
{
	public enum Type
	{
		message, progress, reply, update, heartbeat
	}
	
	private Type type;
	
	public ServerMessage(Type type)
	{
		this.type = type;
	}
	
	public Type getType()
	{
		return type;
	}

}
