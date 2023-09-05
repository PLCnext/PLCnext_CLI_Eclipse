/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.internal.plcncliclient.jsonmessage;

import java.util.Map;

public class ParsedCommand 
{
	private String command;
	private Map<String, String> arguments;
	
	public Map<String, String> getArguments()
	{
		return arguments;
	}
	
	public String getCommand()
	{
		return command;
	}
	
}
