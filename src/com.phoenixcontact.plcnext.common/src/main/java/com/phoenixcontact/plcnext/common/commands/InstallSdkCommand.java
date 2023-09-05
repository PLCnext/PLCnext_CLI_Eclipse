/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands;

import java.util.Map;

import com.phoenixcontact.plcnext.common.Messages;

/**
 * The command line tool command for installing an sdk
 */
public class InstallSdkCommand extends Command
{
	/**
	 * @param options 
	 * @see com.phoenixcontact.plcnext.common.commands.Command#Constructor
	 */
	public InstallSdkCommand(Map<String, String> options) {
		super(options, INSTALL_SDK);
	}

	/**
	 * install sdk verb
	 */
	public final static String INSTALL_SDK = Messages.InstallSdkCommand_verb;
	/**
	 * option path to packed sdk flag
	 */
	public final static String OPTION_PATH = Messages.InstallSdkCommand_optionpath;
	/**
	 * option destination folder to install sdk into flag
	 */
	public final static String OPTION_DESTINATION = Messages.InstallSdkCommand_optiondestination;
	/**
	 * option force flag
	 */
	public final static String OPTION_FORCE = Messages.InstallSdkCommand_optionforce;
	
	@Override
	protected String[] formatOption(String key, String value) {
		
		if(key.equals(OPTION_PATH)||key.equals(OPTION_DESTINATION)) {
			value = "\""+value+"\""; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return super.formatOption(key, value);
	}
}
