/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands;

/**
 * A generic command class representing a command of the command line tool
 *
 */
public class GenericCommand extends Command {

	/**
	 * @param command
	 * @see com.phoenixcontact.plcnext.common.commands.Command#Constructor
	 */
	public GenericCommand(String command) {
		super(command);
	}
}
