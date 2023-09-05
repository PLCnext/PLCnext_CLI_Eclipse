/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands;

import java.util.Map;

import com.phoenixcontact.plcnext.common.Messages;

/**
 * The Command which sets the targets for a project via CLI
 *
 */
public class SetTargetCommand extends Command {

	/**
	 * command verb to get the available targets from the command line tool
	 */
	public final static String SET_TARGET = Messages.SetTargetCommand_verb;
	
	/**
	 * option name
	 */
	public final static String OPTION_NAME = Messages.SetTargetCommand_optionname;
	
	/**
	 * option version
	 */
	public final static String OPTION_VERSION = Messages.SetTargetCommand_optionversion;
	
	/**
	 * option add
	 */
	public final static String OPTION_ADD = Messages.SetTargetCommand_optionadd;
	
	/**
	 * option remove
	 */
	public final static String OPTION_REMOVE = Messages.SetTargetCommand_optionremove;
	
	/**
	 * option path
	 */
	public final static String OPTION_PATH = Messages.SetTargetCommand_optionpath;
	
	/**
	 * @param options the options to add to the command
	 * @see com.phoenixcontact.plcnext.common.commands.Command#Constructor
	 */
	public SetTargetCommand(Map<String, String> options) {
		super(options, SET_TARGET);
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
