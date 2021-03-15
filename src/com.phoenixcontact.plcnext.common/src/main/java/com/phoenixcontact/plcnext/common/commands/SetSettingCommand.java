/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands;

import java.util.Map;

import com.phoenixcontact.plcnext.common.Messages;

/**
 * The Command which sets settings via CLI
 */
public class SetSettingCommand extends Command
{
	/**
	 * command verb to set settings from plcncli
	 */
	public final static String SET_SETTING = Messages.SetSettingCommand_verb;
	
	/**
	 * option sdkPaths
	 */
	public final static String OPTION_SdkPaths = Messages.SetSettingCommand_optionSdkPaths;
	
	/**
	 * option add
	 */
	public final static String OPTION_ADD = Messages.SetSettingCommand_optionAdd;
	
	/**
	 * option remove
	 */
	public final static String OPTION_REMOVE = Messages.SetSettingCommand_optionRemove;
	
	/**
	 * @param options the options to add to the command
	 * @see com.phoenixcontact.plcnext.common.commands.Command#Constructor
	 */
	public SetSettingCommand(Map<String, String> options) {
		super(options, SET_SETTING);
	}
	
	@Override
	protected String[] formatOption(String key, String value) {
		
		if(key.equals(OPTION_SdkPaths)) {
			if(value.endsWith("\\") && !value.endsWith("\\\\"))
			{
				value = value + "\\";
			}
			value = "\""+value+"\""; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return super.formatOption(key, value);
	}
}
