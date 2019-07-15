/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common;

import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.di.*;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.phoenixcontact.plcnext.common.internal.plcncliclient.PlcncliServerConversation;
import com.phoenixcontact.plcnext.commonImpl.Cli;
import com.phoenixcontact.plcnext.commonImpl.CommandManager;
import com.phoenixcontact.plcnext.commonImpl.DIHost;
import com.phoenixcontact.plcnext.commonImpl.NamedPipeCommandReceiver;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin
{

	/**
	 * The plugin id
	 */
	public static final String PLUGIN_ID = Messages.PLUGIN_ID;

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator()
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext)
	 */

	public void start(BundleContext bundleContext) throws Exception
	{
		super.start(bundleContext);
		plugin = this;

		InjectorFactory.getDefault().addBinding(IDIHost.class).implementedBy(DIHost.class);
		InjectorFactory.getDefault().addBinding(ICommandManager.class).implementedBy(CommandManager.class);
		InjectorFactory.getDefault().addBinding(ICommandReceiver.class).implementedBy(Cli.class);
//		InjectorFactory.getDefault().addBinding(ICommandReceiver.class).implementedBy(NamedPipeCommandReceiver.class);
		InjectorFactory.getDefault().addBinding(CliDescription.class);
		InjectorFactory.getDefault().addBinding(PlcncliServerConversation.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception
	{
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault()
	{
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
