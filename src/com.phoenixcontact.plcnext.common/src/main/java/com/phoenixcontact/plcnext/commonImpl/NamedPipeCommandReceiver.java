/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.commonImpl;

import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;

import com.phoenixcontact.plcnext.common.ICommandReceiver;
import com.phoenixcontact.plcnext.common.ProcessExitedWithErrorException;
import com.phoenixcontact.plcnext.common.commands.results.CommandResult;
import com.phoenixcontact.plcnext.common.internal.plcncliclient.PlcncliServerConversation;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerMessageMessage;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerReplyMessage;

/**
 * 
 */
public class NamedPipeCommandReceiver implements ICommandReceiver
{

	PlcncliServerConversation pipeHandler;

	@Inject
	public NamedPipeCommandReceiver(PlcncliServerConversation conversation)
	{
		pipeHandler = conversation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see 
	 * com.phoenixcontact.plcnext.common.ICommandReceiver#executeCommand(java.lang.
	 * String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public CommandResult executeCommand(String command, IProgressMonitor monitor) throws ProcessExitedWithErrorException
	{
		return executeCommand(command, true, monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.phoenixcontact.plcnext.common.ICommandReceiver#executeCommand(java.lang.
	 * String, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public CommandResult executeCommand(String command, boolean logging, IProgressMonitor monitor) throws ProcessExitedWithErrorException
	{
		return executeCommand(command, logging, false, monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.phoenixcontact.plcnext.common.ICommandReceiver#executeCommand(java.lang.
	 * String, boolean, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public CommandResult executeCommand(String command, boolean logging, boolean clearConsole, IProgressMonitor monitor)
			throws ProcessExitedWithErrorException
	{
		Map.Entry<ServerReplyMessage, List<ServerMessageMessage>> reply = pipeHandler.command(command, monitor, logging, clearConsole);
		
		if(reply != null 
				&& reply.getKey() != null 
				&& reply.getKey().isSuccess())
		{
//			if(reply.getValue() != null && reply.getValue().stream().anyMatch(m -> m.getMessageType() == MessageType.error))
//			currently server sends some warnings with messagetype error, so only reply value is important
//				throw new ProcessExitedWithErrorException(reply.getKey().getReply(), command, reply.getValue());
			return new CommandResult(reply.getKey().getReply(), reply.getValue());
			
		}
			
		throw new ProcessExitedWithErrorException(reply != null && reply.getKey()!= null ? reply.getKey().getReply() : null, command, reply != null ? reply.getValue() : null);
	}
	
}
