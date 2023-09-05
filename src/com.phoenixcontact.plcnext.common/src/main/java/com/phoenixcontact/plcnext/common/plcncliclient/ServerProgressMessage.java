/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.plcncliclient;

import java.util.Map;

public class ServerProgressMessage extends ServerMessage
{
	
	public ServerProgressMessage(Type type)
	{
		super(type);
		// TODO Auto-generated constructor stub
	}
	private String command;
	private  Map<String, String> arguments;
	private int progress;
	
	private int progressMaximum;
	private int progressMinimum;
	private String progressMessage;
	private int progressId;
	
	public String getCommand()
	{
		return command;
	}
	
	public Map<String, String> getArguments()
	{
		return arguments;
	}
	
	public int getProgress()
	{
		return progress;
	}
	
	public String getProgressMessage()
	{
		return progressMessage;
	}
	
	public int getProgressMaximum()
	{
		return progressMaximum;
	}
	
	public int getProgressMinimum()
	{
		return progressMinimum;
	}
	
	public int getProgressId()
	{
		return progressId;
	}
}
