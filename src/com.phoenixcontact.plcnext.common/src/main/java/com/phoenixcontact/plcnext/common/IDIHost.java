/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common;


/**
 * Host class which provides classes of this project for dependency injection
 *
 */
public abstract interface IDIHost {
	
	/**
	 * @param clazz the class from which an instance shall be exported
	 * @return instance of the specified class 
	 */
	public <T> T getExport(Class<T> clazz);	
}