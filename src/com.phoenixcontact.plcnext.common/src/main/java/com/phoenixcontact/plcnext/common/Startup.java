/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IStartup;

import com.phoenixcontact.plcnext.common.clicheck.CliAvailabilityChecker;
import com.phoenixcontact.plcnext.common.logging.Logger;

/**
 * This class starts the check-cli-job at eclipse startup
 *
 */
public class Startup implements IStartup {
	
	@Override
	public void earlyStartup() {
		
		Logger.log("-------------------- STARTING NEW SESSION --------------------");
		
		Job job = new Job(Messages.Startup_CheckCliJobName) {
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				return new CliAvailabilityChecker().checkAvailability();
			}
		};
		job.setRule(new MutexSchedulingRule());
		job.schedule();
		
		Job cachingJob = new CliInformationCacher();
		cachingJob.schedule();
	}
}
