/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands;

import java.util.Map;

import com.phoenixcontact.plcnext.common.Messages;

/**
 * The command line tool command for checking the project version
 */
public class CheckProjectCommand extends Command
{

	public CheckProjectCommand(Map<String, String> options)
	{
		super(options, CHECK_PROJECT);
	}

	
	public final static String CHECK_PROJECT = Messages.CheckProjectCommand_verb;
	
	/**
	 * option project path
	 */
	public final static String OPTION_PATH = Messages.CheckProjectCommand_optionpath;
	
	@Override
	protected String[] formatOption(String key, String value) {
		String formattedValue = value;
		
		if(key.equals(OPTION_PATH)) {
			formattedValue = "\""+value+"\""; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return super.formatOption(key, formattedValue);
	}
}
