/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands;

import java.util.Map;

import com.phoenixcontact.plcnext.common.Messages;

/**
 * The command line tool command for getting the name of a project
 *
 */
public class GetNameCommand extends Command {

	private static final String GET_NAME_VERB = Messages.GetNameCommand_verb;
	
	/**
	 * @param options
	 * @see com.phoenixcontact.plcnext.common.commands.Command#Constructor
	 */
	public GetNameCommand(Map<String, String> options) {
		super(options, GET_NAME_VERB);
	}
	
	/**
	 * option path
	 */
	public final static String OPTION_PATH = Messages.GetNameCommand_optionpath;
	
	
	@Override
	protected String[] formatOption(String key, String value) {
		String formattedValue = value;
		
		if(key.equals(OPTION_PATH)) {
			formattedValue = "\""+value+"\""; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return super.formatOption(key, formattedValue);
	}
}
