/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands;

import java.util.Map;

import com.phoenixcontact.plcnext.common.Messages;

/**
 * The command line tool command for creation of a new acf project
 *
 */
public class NewAcfProjectCommand extends Command
{

	/**
		 * @param options 
		 * @see com.phoenixcontact.plcnext.common.commands.Command#Constructor
		 */
		public NewAcfProjectCommand(Map<String, String> options) {
			super(options, NEW_ACFPROJECT);
		}

	/**
	 * new project verb
	 */
	public final static String NEW_ACFPROJECT = Messages.NewAcfProjectCommand_verb;
	/**
	 * option project name flag
	 */
	public final static String OPTION_NAME = Messages.NewAcfProjectCommand_optionname;
	/**
	 * option output directory flag
	 */
	public final static String OPTION_OUTPUT = Messages.NewAcfProjectCommand_optionoutput;
	/**
	 * option force flag
	 */
	public final static String OPTION_FORCE = Messages.NewAcfProjectCommand_optionforce;
	/**
	 * option component name flag
	 */
	public final static String OPTION_CNAME = Messages.NewAcfProjectCommand_optioncname;
	/**
	 * option namespace flag
	 */
	public final static String OPTION_NAMESPACE = Messages.NewAcfProjectCommand_optionnamespace;

	@Override
	protected String[] formatOption(String key, String value)
	{

		if (key.equals(OPTION_OUTPUT) || key.equals(OPTION_NAME))
		{
			value = "\"" + value + "\""; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return super.formatOption(key, value);
	}

}
