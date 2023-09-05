/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands;

import java.util.Map;

import com.phoenixcontact.plcnext.common.Messages;

/**
 * The command line tool command for creating new programs
 *
 */
public class NewProgramCommand extends Command {

	private static final String NEW_PROGRAM_VERB = Messages.NewProgramCommand_verb;
	
	/**
	 * @param options
	 * @see com.phoenixcontact.plcnext.common.commands.Command#Constructor
	 */
	public NewProgramCommand(Map<String, String> options) {
		super(options, NEW_PROGRAM_VERB);
	}
	
	/**
	 * option path
	 */
	public final static String OPTION_PATH = Messages.NewProgramCommand_optionpath;
	
	/**
	 * option component
	 */
	public final static String OPTION_COMPONENT = Messages.NewProgramCommand_optioncomponent;
	
	/**
	 * option name
	 */
	public final static String OPTION_NAME = Messages.NewProgramCommand_optionname;
	
	/**
	 * option namespace
	 */
	public final static String OPTION_NAMESPACE = Messages.NewProgramCommand_optionnamespace;
	
	/**
	 * option sources
	 */
	public final static String OPTION_SOURCES = Messages.NewProgramCommand_optionsources;
	
	@Override
	protected String[] formatOption(String key, String value) {
		String formattedValue = value;
		
		if(key.equals(OPTION_PATH)||key.equals(OPTION_NAME)||key.equals(OPTION_COMPONENT)||key.equals(OPTION_SOURCES)) {
			formattedValue = "\""+value+"\""; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return super.formatOption(key, formattedValue);
	}
}
