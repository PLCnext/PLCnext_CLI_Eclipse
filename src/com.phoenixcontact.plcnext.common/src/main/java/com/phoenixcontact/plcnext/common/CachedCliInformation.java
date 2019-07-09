/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common;

import java.util.Collections;
import java.util.List;
import java.util.Observable;

import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;

/**
 * Observable object which caches information obtained from the cli
 *
 */
@Creatable
@Singleton
public class CachedCliInformation extends Observable
{
	private List<String> allTargets;
	private String portCommentPrefix = "#";

	/**
	 * @return cached list of all targets
	 */
	public List<String> getAllTargets()
	{
		if (allTargets == null)
			return null;
		return Collections.unmodifiableList(allTargets);
	}

	/**
	 * @param targets list of all targets to be cached
	 */
	public void setAllTargets(List<String> targets)
	{
		allTargets = targets;
		notifyObservers();
	}

	/**
	 * @return port comment prefix
	 */
	public String getPortCommentPrefix()
	{
		return portCommentPrefix;
	}

	/**
	 * Default is '#'.
	 * 
	 * @param prefix value to be cached as port comment prefix
	 */
	public void setPortCommentPrefix(String prefix)
	{
		portCommentPrefix = prefix;
		notifyObservers();
	}

	/**
	 * Clears all saved information inside the cache
	 */
	public void clearCache()
	{
		allTargets = null;
		portCommentPrefix = "#";
	}
}
