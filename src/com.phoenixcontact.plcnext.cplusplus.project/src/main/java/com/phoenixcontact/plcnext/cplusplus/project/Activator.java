/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.project;


import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	
	/**
	 * The plugin id
	 */
	public static final String PLUGIN_ID = "com.phoenixcontact.plcnext.cplusplus.project"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * logs the message as error in the eclipse workspace log
	 * @param msg
	 * @param e
	 */
	public void logError(String msg, Throwable e)
	{
		if(e != null)
			getLog().log(new Status(Status.ERROR, PLUGIN_ID, msg, e));
		else
			getLog().log(new Status(Status.ERROR, PLUGIN_ID, msg));
	}

	/**
	 * logs the message as info in the eclipse workspace log
	 * @param msg
	 */
	public void logInfo(String msg)
	{
		getLog().log(new Status(Status.INFO, PLUGIN_ID, msg));
	}
	
	/**
	 * logs the message as info in the eclipse workspace log
	 * @param msg
	 */
	public void logWarning(String msg)
	{
		getLog().log(new Status(Status.WARNING, PLUGIN_ID, msg));
	}

	/**
	 * logs the message as info in the eclipse workspace log
	 * @param msg
	 * @param e 
	 */
	public void logWarning(String msg, Throwable e)
	{
		getLog().log(new Status(Status.WARNING, PLUGIN_ID, msg, e));
	}
}

