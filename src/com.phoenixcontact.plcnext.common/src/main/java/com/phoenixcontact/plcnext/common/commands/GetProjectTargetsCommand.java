/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands;

import java.util.Map;

import com.phoenixcontact.plcnext.common.Messages;

/**
 * The Command which gets the targets for a project via CLI
 *
 */
public class GetProjectTargetsCommand extends Command {
	
	/**
	 * command verb to get the available targets from the command line tool
	 */
	public final static String GET_PROJECT_TARGETS = Messages.GetProjectTargetsCommand_verb;

	/**
	 * option path
	 */
	public final static String OPTION_PATH = Messages.GetProjectTargetsCommand_optionpath;
	
	/**
	 * option short
	 */
	public final static String OPTION_SHORT = Messages.GetProjectTargetsCommand_optionshort;
	
	/**
	 * @param options the options to add to the command
	 * @see com.phoenixcontact.plcnext.common.commands.Command#Constructor
	 */
	public GetProjectTargetsCommand(Map<String, String> options) {
		super(options, GET_PROJECT_TARGETS);
	}
	
	@Override
	protected String[] formatOption(String key, String value) {
		String formattedValue = value;
		
		if(key.equals(OPTION_PATH)) {
			formattedValue = "\""+value+"\""; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return super.formatOption(key, formattedValue);
	}
}
