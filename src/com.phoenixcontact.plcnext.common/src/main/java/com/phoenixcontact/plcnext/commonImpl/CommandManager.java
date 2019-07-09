/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.commonImpl;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.di.annotations.Creatable;

import com.phoenixcontact.plcnext.common.ICommandManager;
import com.phoenixcontact.plcnext.common.ICommandReceiver;
import com.phoenixcontact.plcnext.common.ProcessExitedWithErrorException;
import com.phoenixcontact.plcnext.common.commands.Command;
import com.phoenixcontact.plcnext.common.commands.GenericCommand;
import com.phoenixcontact.plcnext.common.commands.results.CommandResult;

/**
 * This class manages the creation and execution of commands
 *
 */
@Creatable
@Singleton
public class CommandManager implements ICommandManager
{

	ICommandReceiver receiver;

	/**
	 * @param receiver the command line tool which executes the commands
	 */
	@Inject
	public CommandManager(ICommandReceiver receiver)
	{
		this.receiver = receiver;
	}

	public CommandResult executeCommand(Command command, IProgressMonitor monitor)
			throws ProcessExitedWithErrorException
	{

		return receiver.executeCommand(command.getExecutionCommand(), monitor);
	}

	public CommandResult executeCommand(Command command, boolean logging, IProgressMonitor monitor)
			throws ProcessExitedWithErrorException
	{

		return receiver.executeCommand(command.getExecutionCommand(), logging, monitor);
	}

	public CommandResult executeCommand(Command command, boolean logging, boolean clearConsole,
			IProgressMonitor monitor) throws ProcessExitedWithErrorException
	{

		return receiver.executeCommand(command.getExecutionCommand(), logging, clearConsole, monitor);
	}

	public Command createCommand(Map<String, String> options, Class<? extends Command> commandClass)
	{
		if (options == null)
		{
			options = new HashMap<String, String>();
		}

		try
		{
			return commandClass.getDeclaredConstructor(Map.class).newInstance(options);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e)
		{
			return null;
		}
	}

	public Command createCommand(String command)
	{
		return new GenericCommand(command);
	}
}
