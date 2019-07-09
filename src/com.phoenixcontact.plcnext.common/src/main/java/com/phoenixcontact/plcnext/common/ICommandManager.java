/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import com.phoenixcontact.plcnext.common.commands.Command;
import com.phoenixcontact.plcnext.common.commands.results.CommandResult;

/**
 * Interface describing a command manager
 *
 */
public interface ICommandManager
{

	/**
	 * Executes the given command
	 * 
	 * @param command the command to execute
	 * @param monitor
	 * @return the CommandResult containing standard and error output
	 * @throws ProcessExitedWithErrorException
	 */
	public CommandResult executeCommand(Command command, IProgressMonitor monitor)
			throws ProcessExitedWithErrorException;

	/**
	 * Executes the given command
	 * 
	 * @param command the command to execute
	 * @param logging whether or not logging is enabled
	 * @param monitor
	 * @return the CommandResult containing standard and error output
	 * @throws ProcessExitedWithErrorException
	 */
	public CommandResult executeCommand(Command command, boolean logging, IProgressMonitor monitor)
			throws ProcessExitedWithErrorException;

	/**
	 * Executes the given command
	 * 
	 * @param command      the command to execute
	 * @param logging      whether or not logging is enabled
	 * @param clearConsole whether or not the console should be cleared
	 * @param monitor
	 * @return the CommandResult containing standard and error output
	 * @throws ProcessExitedWithErrorException
	 */
	public CommandResult executeCommand(Command command, boolean logging, boolean clearConsole,
			IProgressMonitor monitor) throws ProcessExitedWithErrorException;

	/**
	 * @param options
	 * @param commandClass
	 * @return the newly created command
	 */
	public Command createCommand(Map<String, String> options, Class<? extends Command> commandClass);

	public Command createCommand(String command);
}
