/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common;

import java.io.IOException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.ui.IStartup;

/**
 * This class starts the check-cli-job at eclipse startup
 *
 */
public class Startup implements IStartup {
	
	@Override
	public void earlyStartup() {
		
//		Logger.log("-------------------- STARTING NEW SESSION --------------------");
		
		Job job = new Job("Check projects") {
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				PlcncliResourceChangeListener resourceChangeListener = new PlcncliResourceChangeListener();
				ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener);
				return resourceChangeListener.checkOpenProjects();
			}
		};
		job.setRule(new MutexSchedulingRule());
		job.schedule();
		
		
		
		Job cachingJob = new CliInformationCacher();
		cachingJob.schedule();
		
		
		
		Job updateSecureStorageJob = new Job("UpdateSecureStorage")
		{
			
			@Override
			protected IStatus run(IProgressMonitor arg0)
			{
				ISecurePreferences securePreferences = SecurePreferencesFactory.getDefault();
				ISecurePreferences rootNode = securePreferences.node(Messages.SecureStorageNodeName);
				ISecurePreferences wspNode = rootNode.node(Messages.SecureStorageWorkspacesKey);
				
				boolean nodeRemoved = false;
				
				for(String key : wspNode.keys())
				{
					if(!keyIsExistingWorkspaceLocation(key))
					{
						wspNode.node(key).removeNode();
						nodeRemoved = true;
					}
				}
				
				if(nodeRemoved)
				{
					try
					{
						securePreferences.flush();
					} catch (IOException e)
					{
						return Status.warning("Problem during writing of secure preferences.", e);
					}
				}
				
				
				return Status.OK_STATUS;
			}
			
			private boolean keyIsExistingWorkspaceLocation(String key)
			{
				if(new Path(key).append(".metadata").toFile().exists())
				{
					return true;
				}
				
				return false;
			}
		};
		updateSecureStorageJob.setRule(new MutexSchedulingRule());
		updateSecureStorageJob.schedule();
	}
}
