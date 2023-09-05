/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common;

/**
 * Exception with message describing, that no cli was found
 *
 */
public class CliNotExistingException extends ProcessExitedWithErrorException {
	
	private static final long serialVersionUID = 8585892059717434554L;

	/**
     * Constructs a new CliNotExistingException.
     */
	public CliNotExistingException() {
		super(null, null, Messages.CliNotExistingExceptionMessage);
	}
}
