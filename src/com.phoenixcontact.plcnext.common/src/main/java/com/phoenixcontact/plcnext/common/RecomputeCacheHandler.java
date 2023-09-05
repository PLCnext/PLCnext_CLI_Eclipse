/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.jobs.Job;



/**
 * Handler for the recompute command
 *
 */
public class RecomputeCacheHandler extends AbstractHandler
{
	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		Job cachingJob = new CliInformationCacher();
		cachingJob.schedule();
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#isEnabled()
	 */
	@Override
	public boolean isEnabled()
	{
		return true;
	}
}
