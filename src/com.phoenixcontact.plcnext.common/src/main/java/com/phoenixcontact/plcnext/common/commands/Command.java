/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class represents a command.
 * The executionCommand consists of the verb and all options passed via the constructor of this class.
 *
 */
public abstract class Command {
	
	/**
	 * the verb for this command
	 */
	public final String verb;
	
	private Map<String, String> options;
		
	/**
	 * creates a command consisting of a verb to be defined in implementing classes and the options passed as parameters
	 * @param options the options to add to the command
	 * @param verb the verb for the command
	 */
	public Command(Map<String,String> options, String verb) {
		this.verb = verb;
		setExecutionCommand(options);
		this.options = options;
	}
	
	public Command(String command)
	{
		verb = "";
		setExecutionCommand(command);
	}
	
	private String executionCommand = "";
	private String loggableExecutionCommand = "";
		
	/**
	 * @return the execution command consisting of the verb and the options passed via constructor
	 */
	public String getExecutionCommand() {
		return executionCommand;
	}
	
	public String getLoggableExecutionCommand() {
		if(loggableExecutionCommand != null && !loggableExecutionCommand.isBlank()) {
			return loggableExecutionCommand;
		}
		else {
			return executionCommand;
		}
	}
	
	private void setExecutionCommand(Map<String, String> options) {
		List<String> command = new ArrayList<String>();

		command.add(verb);
		
		if (options != null) {
			for (Entry<String, String> entry : options.entrySet()) {
				String[] formattedOption = formatOption(entry.getKey(), entry.getValue());
				command.add(formattedOption[0]); 
				if(formattedOption[1] != null)
					command.add(formattedOption[1]);
			}
		}
		
//		boolean skipNext = false;
		for (String string : command.toArray(new String[0])) {
			executionCommand += " " + string; //$NON-NLS-1$
//			
//			// check for "--password" and skip 2 entries if found
//			if(string != Messages.DeployCommand_optionPassword)
//			{
//				if(!skipNext)
//				{
//					loggableExecutionCommand += " " + string; //$NON-NLS-1$
//				}
//				else 
//				{
//					skipNext = false;
//					loggableExecutionCommand += " *"; //$NON-NLS-1$
//				}
//			}
//			else 
//			{
//				skipNext = true;
//				loggableExecutionCommand += " " + string; //$NON-NLS-1$
//			}
		}
	}
	
	private void setExecutionCommand(String command)
	{
		executionCommand = command;
	}
	
	public void setLoggableExecutionCommand(String loggableExecutionCommand)
	{
		this.loggableExecutionCommand = loggableExecutionCommand;
	}
	
	protected String[] formatOption(String key, String value) {
		String[] result = {key, value};
		return result;
	}
	
	public String getVerb()
	{
		return verb;
	}
	
	public Map<String, String> getOptions()
	{
		return options;
	}
	
}
