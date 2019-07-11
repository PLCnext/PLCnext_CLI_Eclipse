/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands.results;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.phoenixcontact.plcnext.common.Activator;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerMessageMessage;

/**
 * Wrapps stdout and error output of a process
 *
 */
public class CommandResult
{
	protected List<String> stdout = null;
	protected List<String> error = null;
	protected JsonObject reply = null;
	protected List<ServerMessageMessage> messages = null;
	
	/**
	 * @param stdout 
	 * @param error
	 */
	public CommandResult(List<String> stdout, List<String> error)
	{
		this.stdout = stdout;
		this.error = error;
	}
	
	public CommandResult(JsonObject reply, List<ServerMessageMessage> messages) {
		this.reply = reply;
		this.messages = messages;
	}
	
	public List<ServerMessageMessage> getMessages()
	{
		return messages;
	}
	
	public GetCompilerSpecsCommandResult convertToGetCompilerSpecsCommandResult()
	{
		if(reply == null && stdout != null)
			return GetCompilerSpecsCommandResult.convertResultToJson(stdout);
		return convertTo(GetCompilerSpecsCommandResult.class);
	}
	
	public GetIncludePathsCommandResult convertToGetIncludePathsCommandResult()
	{
		if(reply == null && stdout != null)
			return GetIncludePathsCommandResult.convertResultToJson(stdout);
		return convertTo(GetIncludePathsCommandResult.class);
	}
	
	public GetProjectInformationCommandResult convertToGetProjectInformationCommandResult()
	{
		if(reply == null && stdout != null)
			return GetProjectInformationCommandResult.convertResultToJson(stdout);
		return convertTo(GetProjectInformationCommandResult.class);
	}
	
	public GetSdksCommandResult convertToGetSdksCommandResult()
	{
		if(reply == null && stdout != null)
			return GetSdksCommandResult.convertResultToJson(stdout);
		return convertTo(GetSdksCommandResult.class);
	}
	
	public GetSettingCommandResult convertToGetSettingCommandResult()
	{
		if(reply == null && stdout != null)
			return GetSettingCommandResult.convertResultToJson(stdout);
		return convertTo(GetSettingCommandResult.class);
	}
	
	public GetTargetsCommandResult convertToGetTargetsCommandResult()
	{
		if(reply == null && stdout != null)
			return GetTargetsCommandResult.convertResultToJson(stdout);
		return convertTo(GetTargetsCommandResult.class);
	}
	
	private <T> T convertTo(Class<T> clazz)
	{
		Gson gson = new Gson();
		try 
		{
			return gson.fromJson(reply, clazz);
		}catch(JsonSyntaxException e)
		{
			Activator.getDefault().logError("Conversion to "+clazz.getName()+" failed", e);
			return null;
		}
	}
}
