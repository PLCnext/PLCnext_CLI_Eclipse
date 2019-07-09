/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands;

import java.util.Map;

import com.phoenixcontact.plcnext.common.Messages;

/**
 * The Command which gets the compiler specs via plcncli
 *
 */
public class GetCompilerSpecsCommand extends Command {
	
	/**
	 * command verb to get the compiler specs from the plcncli
	 */
	public final static String GET_COMPILERSPECS = Messages.GetCompilerSpecsCommand_verb;
	
	/**
	 * option path
	 */
	public final static String OPTION_PATH = Messages.GetCompilerSpecsCommand_optionpath;
	
	/**
	 * @param options the options to add to the command
	 * @see com.phoenixcontact.plcnext.common.commands.Command#Constructor
	 */
	public GetCompilerSpecsCommand(Map<String, String> options) {
		super(options, GET_COMPILERSPECS);
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
