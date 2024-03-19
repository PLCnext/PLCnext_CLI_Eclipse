/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands;

import java.util.Map;

import com.phoenixcontact.plcnext.common.Messages;

/**
 * The Command which gets project information via CLI
 *
 */
public class GetProjectInformationCommand extends Command
{
	/**
	 * command verb to get the available targets from the command line tool
	 */
	public final static String GET_PROJECT_INFORMATION = Messages.GetProjectInformationCommand_verb;

	/**
	 * option sources
	 */
	public final static String OPTION_SOURCES = Messages.GetProjectInformationCommand_optionsources;
	
	/**
	 * option path
	 */
	public final static String OPTION_PATH = Messages.GetProjectInformationCommand_optionpath;
	
	/**
	 * option no include path detection
	 */
	public final static String OPTION_NO_INCLUDE_DETECTION = Messages.GetProjectInformationCommand_optionnoincludedetection;
	
	/**
	 * option buildtype
	 */
	public final static String OPTION_BUILDTYPE = Messages.GetProjectInformationCommand_optionbuildtype;
	
	/**
	 * @param options the options to add to the command
	 * @see com.phoenixcontact.plcnext.common.commands.Command#Constructor
	 */
	public GetProjectInformationCommand(Map<String, String> options) {
		super(options, GET_PROJECT_INFORMATION);
	}
	
	@Override
	protected String[] formatOption(String key, String value) {
		String formattedValue = value;
		
		if(key.equals(OPTION_PATH)||key.equals(OPTION_SOURCES)) {
			formattedValue = "\""+value+"\""; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return super.formatOption(key, formattedValue);
	}
}
