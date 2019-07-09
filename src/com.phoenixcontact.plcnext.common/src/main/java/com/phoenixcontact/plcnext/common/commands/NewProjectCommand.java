/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands;

import java.util.Map;

import com.phoenixcontact.plcnext.common.Messages;

/**
 * The command line tool command for creation of a new project
 *
 */
public class NewProjectCommand extends Command {

	/**
	 * @param options 
	 * @see com.phoenixcontact.plcnext.common.commands.Command#Constructor
	 */
	public NewProjectCommand(Map<String, String> options) {
		super(options, NEW_PROJECT);
	}

	/**
	 * new project verb
	 */
	public final static String NEW_PROJECT = Messages.NewProjectCommand_verb;
	/**
	 * option project name flag
	 */
	public final static String OPTION_NAME = Messages.NewProjectCommand_optionname;
	/**
	 * option output directory flag
	 */
	public final static String OPTION_OUTPUT = Messages.NewProjectCommand_optionoutput;
	/**
	 * option force flag
	 */
	public final static String OPTION_FORCE = Messages.NewProjectCommand_optionforce;
	/**
	 * option component name flag
	 */
	public final static String OPTION_CNAME = Messages.NewProjectCommand_optioncname;
	/**
	 * option program name flag
	 */
	public final static String OPTION_PNAME = Messages.NewProjectCommand_optionpname;
	/**
	 * option namespace flag
	 */
	public final static String OPTION_NAMESPACE = Messages.NewProjectCommand_optionnamespace;
	
	@Override
	protected String[] formatOption(String key, String value) {
		
		if(key.equals(OPTION_OUTPUT)||key.equals(OPTION_NAME)) {
			value = "\""+value+"\""; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return super.formatOption(key, value);
	}
}
