/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands;

import java.util.Map;

import com.phoenixcontact.plcnext.common.Messages;

/**
 * The command line tool command for creating new components
 *
 */
public class NewAppComponentCommand extends Command {

	private static final String NEW_APPCOMPONENT_VERB = Messages.NewAppComponentCommand_verb;
	
	/**
	 * @param options
	 * @see com.phoenixcontact.plcnext.common.commands.Command#Constructor
	 */
	public NewAppComponentCommand(Map<String, String> options) {
		super(options, NEW_APPCOMPONENT_VERB);
	}
	
	/**
	 * option path
	 */
	public final static String OPTION_PATH = Messages.NewAppComponentCommand_optionpath;
	
	/**
	 * option name
	 */
	public final static String OPTION_NAME = Messages.NewAppComponentCommand_optionname;
	
	/**
	 * option namespace
	 */
	public final static String OPTION_NAMESPACE = Messages.NewAppComponentCommand_optionnamespace;
	
	@Override
	protected String[] formatOption(String key, String value) {
		String formattedValue = value;
		
		if(key.equals(OPTION_PATH)||key.equals(OPTION_NAME)) {
			formattedValue = "\""+value+"\""; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return super.formatOption(key, formattedValue);
	}
}