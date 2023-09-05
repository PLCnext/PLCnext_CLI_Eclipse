/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands;

import java.util.Map;

import com.phoenixcontact.plcnext.common.Messages;

/**
 * The command line tool command for generation of metacode
 *
 */
public class GenerateCodeCommand extends Command {

	/**
	 * @param options
	 * @param verb 
	 * @see com.phoenixcontact.plcnext.common.commands.Command#Constructor
	 */
	public GenerateCodeCommand(Map<String, String> options) {
		super(options, GENERATE_CODE);
	}
	
	/**
	 * generate code verb
	 */
	public final static String GENERATE_CODE = Messages.GenerateCodeCommand_verb;
	
	/**
	 * option project path
	 */
	public final static String OPTION_PATH = Messages.GenerateCodeCommand_optionpath;
	
	/**
	 * option project source directories
	 */
	public final static String OPTION_SOURCES = Messages.GenerateCodeCommand_optionsources;

	@Override
	protected String[] formatOption(String key, String value) {
		String formattedValue = value;
		
		if(key.equals(OPTION_PATH) || key.equals(OPTION_SOURCES)) {
			formattedValue = "\""+value+"\""; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return super.formatOption(key, formattedValue);
	}
}
