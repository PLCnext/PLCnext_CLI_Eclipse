/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands;

import java.util.Map;

import com.phoenixcontact.plcnext.common.Messages;

/**
 *
 */
public class NewConsumableLibraryCommand extends Command {

	/**
	 * @param options 
	 * @see com.phoenixcontact.plcnext.common.commands.Command#Constructor
	 */
	public NewConsumableLibraryCommand(Map<String, String> options) {
		super(options, NEW_CONSUMABLELIBRARY);
	}

/**
 * new project verb
 */
public final static String NEW_CONSUMABLELIBRARY = Messages.NewConsumableLibraryCommand_verb;

@Override
protected String[] formatOption(String key, String value)
{

	if (key.equals(NewProjectCommand.OPTION_OUTPUT) || key.equals(NewProjectCommand.OPTION_NAME))
	{
		value = "\"" + value + "\""; //$NON-NLS-1$ //$NON-NLS-2$
	}
	return super.formatOption(key, value);
}
}
