/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.internal.plcncliclient;

public class ClientWasRestartedException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8311877540357734652L;
	
	
	public ClientWasRestartedException()
	{
		super("Current client is not up to date since client was restarted.");
	}
}
