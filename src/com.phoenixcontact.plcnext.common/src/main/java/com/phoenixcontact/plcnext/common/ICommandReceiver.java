/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common;

import org.eclipse.core.runtime.IProgressMonitor;

import com.phoenixcontact.plcnext.common.commands.results.CommandResult;

/**
 * Implementing classes will receive commands, execute them and can return some message
 *
 */
public interface ICommandReceiver {

	/**
	 * @param command the command to execute
	 * @param monitor 
	 * @return the CommandResult containing standard and error output
	 * @throws ProcessExitedWithErrorException 
	 */
	public CommandResult executeCommand(String command, IProgressMonitor monitor) throws ProcessExitedWithErrorException;
	
	/**
	 * @param command the command to execute
	 * @param logging whether or not logging is enabled
	 * @param monitor 
	 * @return the CommandResult containing standard and error output
	 * @throws ProcessExitedWithErrorException 
	 */
	public CommandResult executeCommand(String command, boolean logging, IProgressMonitor monitor) throws ProcessExitedWithErrorException;
	
	/**
	 * @param command the command to execute
	 * @param logging whether or not logging is enabled
	 * @param clearConsole whether or not the console should be cleared
	 * @param monitor 
	 * @return the CommandResult containing standard and error output
	 * @throws ProcessExitedWithErrorException 
	 */
	public CommandResult executeCommand(String command, boolean logging, boolean clearConsole, IProgressMonitor monitor) throws ProcessExitedWithErrorException;
}
