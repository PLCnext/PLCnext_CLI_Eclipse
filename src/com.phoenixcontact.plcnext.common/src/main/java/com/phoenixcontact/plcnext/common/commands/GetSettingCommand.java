/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands;

import java.util.Map;

import com.phoenixcontact.plcnext.common.Messages;

/**
 * The Command which gets settings via CLI
 *
 */
public class GetSettingCommand extends Command {

	/**
	 * command verb to get settings from the command line tool
	 */
	public final static String GET_SETTING = Messages.GetSettingCommand_verb;
	
	/**
	 * option CliRepositorySignatureFileName
	 */
	public final static String OPTION_CliRepositorySignatureFileName = Messages.GetSettingCommand_optionCliRepositorySignatureFileName;
	
	/**
	 * option CliRepositoryFileName
	 */
	public final static String OPTION_CliRepositoryFileName = Messages.GetSettingCommand_optionCliRepositoryFileName;
	
	/**
	 * option AttributePrefix
	 */
	public final static String OPTION_AttributePrefix = Messages.GetSettingCommand_optionAttributePrefix;
	
	/**
	 * @param options the options to add to the command
	 * @see com.phoenixcontact.plcnext.common.commands.Command#Constructor
	 */
	public GetSettingCommand(Map<String, String> options) {
		super(options, GET_SETTING);
	}
}
