/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands;

import java.util.Map;

import com.phoenixcontact.plcnext.common.Messages;

/**
 * The command line tool command for getting components of a project
 *
 */
public class GetAppComponentsCommand extends Command
{

	private static final String GET_APPCOMPONENTS_VERB = Messages.GetAppComponentsCommand_verb;

	/**
		 * @param options
		 * @see com.phoenixcontact.plcnext.common.commands.Command#Constructor
		 */
		public GetAppComponentsCommand(Map<String, String> options) {
			super(options, GET_APPCOMPONENTS_VERB);
		}

	/**
	 * option path
	 */
	public final static String OPTION_PATH = Messages.GetComponentsCommand_optionpath;

	/**
	 * option name
	 */
	public final static String OPTION_NAME = Messages.GetComponentsCommand_optionname;

	/**
	 * option project source directories
	 */
	public final static String OPTION_SOURCES = Messages.GetComponentsCommand_optionsources;

	@Override
	protected String[] formatOption(String key, String value)
	{
		String formattedValue = value;

		if (key.equals(OPTION_PATH) || key.equals(OPTION_SOURCES))
		{
			formattedValue = "\"" + value + "\""; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return super.formatOption(key, formattedValue);
	}
}
