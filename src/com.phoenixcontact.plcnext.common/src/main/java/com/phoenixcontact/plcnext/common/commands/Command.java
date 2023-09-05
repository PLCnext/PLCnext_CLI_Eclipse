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
		
	/**
	 * @return the execution command consisting of the verb and the options passed via constructor
	 */
	public String getExecutionCommand() {
		return executionCommand;
	}
	
	private void setExecutionCommand(Map<String, String> options) {
		List<String> command = new ArrayList<String>();
//		String[] splited = verb.split("\\s+");
//		for (String part : splited) {
//			command.add(part);
//		}
		command.add(verb);
		if (options != null) {
			for (Entry<String, String> entry : options.entrySet()) {
				String[] formattedOption = formatOption(entry.getKey(), entry.getValue());
				command.add(formattedOption[0]); 
				if(formattedOption[1] != null)
					command.add(formattedOption[1]);
			}
		}
		
		for (String string : command.toArray(new String[0])) {
			executionCommand += " " + string; //$NON-NLS-1$
		}
	}
	
	private void setExecutionCommand(String command)
	{
		executionCommand = command;
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
