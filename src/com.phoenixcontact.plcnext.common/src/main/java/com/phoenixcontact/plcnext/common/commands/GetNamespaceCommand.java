/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands;

import java.util.Map;

import com.phoenixcontact.plcnext.common.Messages;

/**
 * The command line tool command for getting the namespace of a project
 *
 */
public class GetNamespaceCommand extends Command {

	private static final String GET_NAMESPACE_VERB = Messages.GetNamespaceCommand_verb;
	
	/**
	 * @param options
	 * @see com.phoenixcontact.plcnext.common.commands.Command#Constructor
	 */
	public GetNamespaceCommand(Map<String, String> options) {
		super(options, GET_NAMESPACE_VERB);
	}
	
	/**
	 * option path
	 */
	public final static String OPTION_PATH = Messages.GetNamespaceCommand_optionpath;
	
	/**
	 * option sources
	 */
	public final static String OPTION_SOURCES = Messages.GetNamespaceCommand_optionsources;
	
	
	@Override
	protected String[] formatOption(String key, String value) {
		String formattedValue = value;
		
		if(key.equals(OPTION_PATH) || key.equals(OPTION_SOURCES)) {
			formattedValue = "\""+value+"\""; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return super.formatOption(key, formattedValue);
	}
}
