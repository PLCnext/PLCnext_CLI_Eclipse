/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.commonImpl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;

import com.phoenixcontact.plcnext.common.CachedCliInformation;
import com.phoenixcontact.plcnext.common.CliDescription;
import com.phoenixcontact.plcnext.common.ICommandManager;
import com.phoenixcontact.plcnext.common.IDIHost;

/**
 * Implements the interface IDIHost
 *
 */
@Creatable
@Singleton
public class DIHost implements IDIHost
{

	private ICommandManager commandManager;
	private CliDescription cliInformation;
	private CachedCliInformation cachedInformation;

	/**
	 * injecting constructor
	 * 
	 * @param commandManager
	 * @param cliInformation
	 * @param cachedInformation
	 */
	@Inject
	public DIHost(ICommandManager commandManager, CliDescription cliInformation, CachedCliInformation cachedInformation)
	{
		this.commandManager = commandManager;
		this.cliInformation = cliInformation;
		this.cachedInformation = cachedInformation;
	}

	@SuppressWarnings("unchecked")
	public <T> T getExport(Class<T> clazz)
	{
		if (ICommandManager.class == clazz)
		{
			return (T) commandManager;
		}
		if (CliDescription.class == clazz)
		{
			return (T) cliInformation;
		}
		if (CachedCliInformation.class == clazz)
		{
			return (T) cachedInformation;
		}
		return null;
	}
}
