/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands.results;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.phoenixcontact.plcnext.common.ProcessExitedWithErrorException;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerMessageMessage;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerMessageMessage.MessageType;

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

		messages = new ArrayList<ServerMessageMessage>();
		if (stdout != null)
			stdout.stream().forEach(l -> messages.add(new ServerMessageMessage(l, MessageType.information)));
		if (error != null)
			error.stream().forEach(l -> messages.add(new ServerMessageMessage(l, MessageType.error)));
	}

	public CommandResult(JsonObject reply, List<ServerMessageMessage> messages)
	{
		this.reply = reply;
		this.messages = messages;
	}

	public List<ServerMessageMessage> getMessages()
	{
		return messages;
	}

	public static <T extends CommandResult> T convertToTypedCommandResult(Class<T> clazz, List<String> stdout) throws ProcessExitedWithErrorException
	{
		if (stdout != null)
		{
			try
			{
				return new Gson().fromJson(stdout.stream().collect(Collectors.joining("")), clazz);
			} catch (JsonSyntaxException e)
			{
				String result = stdout.stream().dropWhile(x -> !x.startsWith("{")).collect(Collectors.joining(""));
				if (result != null && !result.isBlank()) 
				{
					try 
					{
						return new Gson().fromJson(result, clazz);
					}catch(JsonSyntaxException e1)
					{
						throw new ProcessExitedWithErrorException(stdout, null, "Could not convert result to json");
					}
				}
				throw new ProcessExitedWithErrorException(stdout, null, "Could not convert result to json");
			}
		}
		return null;
	}
	
	public <T extends CommandResult> T convertToTypedCommandResult(Class<T> clazz) throws ProcessExitedWithErrorException
	{
		if(reply == null)
		{
			return CommandResult.convertToTypedCommandResult(clazz, stdout);
		}
		
		try
		{
			return new Gson().fromJson(reply, clazz);
		} catch (JsonSyntaxException e)
		{
			throw new ProcessExitedWithErrorException(stdout, null, "Conversion to " + clazz.getName() + " failed");
		}
	}
}
