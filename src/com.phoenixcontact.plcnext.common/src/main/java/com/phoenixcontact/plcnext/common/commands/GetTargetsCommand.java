/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands;

import java.util.Map;

import com.phoenixcontact.plcnext.common.Messages;

/**
 * The Command which gets all available targets via CLI
 *
 */
public class GetTargetsCommand extends Command {
	
	/**
	 * command verb to get the available targets from the command line tool
	 */
	public final static String GET_TARGETS = Messages.GetTargetsCommand_verb;

	/**
	 * option short
	 */
	public final static String OPTION_SHORT = Messages.GetTargetsCommand_optionshort;
	
	/**
	 * @param options the options to add to the command
	 * @see com.phoenixcontact.plcnext.common.commands.Command#Constructor
	 */
	public GetTargetsCommand(Map<String, String> options) {
		super(options, GET_TARGETS);
	}
}
