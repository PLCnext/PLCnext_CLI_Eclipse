/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * Scheduling rule to allow only one job with this rule to run at a time
 *
 */
public class MutexSchedulingRule implements ISchedulingRule
{

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(org.eclipse.core.runtime.jobs.ISchedulingRule)
	 */
	@Override
	public boolean contains(ISchedulingRule rule)
	{
		ISchedulingRule wspRule = ResourcesPlugin.getWorkspace().getRoot();
		return rule instanceof MutexSchedulingRule
				|| rule.equals(wspRule);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(org.eclipse.core.runtime.jobs.ISchedulingRule)
	 */
	@Override
	public boolean isConflicting(ISchedulingRule rule)
	{
		return contains(rule);
	}

}
