/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.phoenixcontact.plcnext.common.commands.results.ErrorResult;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerMessageMessage;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerMessageMessage.MessageType;

/**
 * Exception with message describing, that a process stopped with error
 *
 */
public class ProcessExitedWithErrorException extends Exception
{

	private static final long serialVersionUID = 8477895725966036900L;
	private List<ServerMessageMessage> messages = null;
	private JsonObject reply = null;

	/**
	 * Constructs a new ProcessExitedWithErrorException and appends the command to
	 * the message.
	 * 
	 * @param command     the command which started the process
	 * @param outputLines the output lines which were produced by the process
	 * @param errorLines  the error lines which were produced by the process
	 */
	public ProcessExitedWithErrorException(String command, List<String> outputLines, List<String> errorLines)
	{
		this(outputLines, errorLines, Messages.ProcessExitedWithErrorExceptionMessage + command +
				(errorLines != null && errorLines.size() > 0 
				? "\n" + errorLines.stream().filter(w -> w instanceof String).map(w -> (String) w).collect(Collectors.joining("\n")) 
				: (outputLines != null && outputLines.size() > 0) 
					? "\n" + outputLines.stream().filter(w -> w instanceof String).map(w -> (String) w)
							.collect(Collectors.joining("\n"))
					: ""
				)
			);
	}
	
	public ProcessExitedWithErrorException(List<String> outputLines, List<String> errorLines, String message)
	{
		super(message);
		if(messages == null)
			messages = new ArrayList<ServerMessageMessage>();
		if(outputLines != null)
			outputLines.stream().forEach(l -> messages.add(new ServerMessageMessage(l, MessageType.information)));
		if(errorLines != null)
			errorLines.stream().forEach(l -> messages.add(new ServerMessageMessage(l, MessageType.error)));
	}
	
	public ProcessExitedWithErrorException(JsonObject reply, String command, List<ServerMessageMessage> messages)
	{
		super(messages != null 
				? Messages.ProcessExitedWithErrorExceptionMessage + command + "\n" + messages.stream().filter(m -> m.getMessageType() == MessageType.error).map(m -> m.getMessage()).collect(Collectors.toList()) 
				: Messages.ProcessExitedWithErrorExceptionMessage + command);
		this.reply = reply;
		this.messages = messages;
	}
	
	public ProcessExitedWithErrorException(String message)
	{
		super(message);
	}
	
	public List<ServerMessageMessage> getMessages()
	{
		return messages == null ? new ArrayList<ServerMessageMessage>() : messages;
	}
	
	public JsonObject getReply()
	{
		return reply;
	}
	
	@Override
	public String getMessage() 
	{
		try 
		{
			return new Gson().fromJson(messages.stream()
											   .map(m -> m.getMessage())
											   .dropWhile(x -> !x.startsWith("{"))
											   .collect(Collectors.joining("")),
									   ErrorResult.class).getError();
		}
		catch(Exception e) 
		{
			return super.getMessage();
		}
	}
}
