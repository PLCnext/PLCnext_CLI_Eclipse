/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands;

import java.util.Map;

import com.phoenixcontact.plcnext.common.Messages;

/**
 * The Command which gets the list of sdks via CLI
 *
 */
public class GetSdksCommand extends Command {

	/**
	 * command verb to get sdks from the command line tool
	 */
	public final static String GET_SDKS = Messages.GetSdksCommand_verb;
	
	/**
	 * @param options the options to add to the command
	 * @see com.phoenixcontact.plcnext.common.commands.Command#Constructor
	 */
	public GetSdksCommand(Map<String, String> options) {
		super(options, GET_SDKS);
	}
}
