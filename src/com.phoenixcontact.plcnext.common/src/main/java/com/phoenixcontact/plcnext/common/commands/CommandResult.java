/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands;

import java.util.List;

/**
 * Wrapps stdout and error output of a process
 *
 */
public class CommandResult
{
	List<String> stdout;
	List<String> error;
	
	/**
	 * @param stdout 
	 * @param error
	 */
	public CommandResult(List<String> stdout, List<String> error)
	{
		this.stdout = stdout;
		this.error = error;
	}
	
	/**
	 * @return the standard output
	 */
	public List<String> getStandardOutput(){
		return stdout;
	}
	
	/**
	 * @return the error output
	 */
	public List<String> getErrorOutput(){
		return error;
	}
}
