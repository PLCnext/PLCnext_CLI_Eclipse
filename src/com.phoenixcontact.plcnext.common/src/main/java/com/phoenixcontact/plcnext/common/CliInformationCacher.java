/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;

import com.phoenixcontact.plcnext.common.commands.GetSettingCommand;
import com.phoenixcontact.plcnext.common.commands.GetTargetsCommand;
import com.phoenixcontact.plcnext.common.commands.results.CommandResult;
import com.phoenixcontact.plcnext.common.commands.results.GetSettingCommandResult;
import com.phoenixcontact.plcnext.common.commands.results.GetSettingCommandResult.Setting;
import com.phoenixcontact.plcnext.common.commands.results.GetTargetsCommandResult;
import com.phoenixcontact.plcnext.common.commands.results.Target;

/**
 * Gets information from cli and saves it in CachedCliInformation
 *
 */
public class CliInformationCacher extends Job
{

	/**
	 * Gets the commandmanager as well as the cache from DIHost
	 */
	public CliInformationCacher()
	{
		super(Messages.Startup_CachingCliInformationJobName);

		setRule(new MutexSchedulingRule());
	}

	/**
	 * Use this constructor if prefix should not be updated
	 * 
	 * @param updatePrefix false if prefix should not be updated, default is true
	 */
	public CliInformationCacher(boolean updatePrefix)
	{
		this();
		this.updatePrefix = updatePrefix;
	}

	private boolean updatePrefix = true;

	@Override
	protected IStatus run(IProgressMonitor monitor)
	{
		IEclipseContext context = EclipseContextHelper.getActiveContext();
		IDIHost host = ContextInjectionFactory.make(IDIHost.class, context);
		ICommandManager commandManager = host.getExport(ICommandManager.class);
		CachedCliInformation cliInformation = host.getExport(CachedCliInformation.class);

		// *******************get all targets***************************
		Map<String, String> options = new HashMap<String, String>();
		options.put(GetTargetsCommand.OPTION_SHORT, null);
		try
		{
			CommandResult result = commandManager
					.executeCommand(commandManager.createCommand(options, GetTargetsCommand.class), false, monitor);

			GetTargetsCommandResult targetsResult = result.convertToTypedCommandResult(GetTargetsCommandResult.class);
			Target[] targets = targetsResult.getTargets();
			
			cliInformation.clearCache();
			cliInformation.setAllTargets(Arrays.asList(targets));

			// *******************get prefix***************************
			if (updatePrefix)
			{
				options.clear();
				options.put(GetSettingCommand.OPTION_AttributePrefix, null);

				CommandResult commandResult = commandManager
						.executeCommand(commandManager.createCommand(options, GetSettingCommand.class), false, monitor);
				GetSettingCommandResult settingResult = commandResult.convertToTypedCommandResult(GetSettingCommandResult.class);
				Setting setting = settingResult.getSetting();
				
				String prefix = setting.getAttributePrefix();
				
				cliInformation.setPortCommentPrefix(prefix);
				
			}
		} catch (ProcessExitedWithErrorException e)
		{
			Activator.getDefault().logError("Error while trying to execute clif command.", e);
			return new Status(Status.ERROR, Activator.PLUGIN_ID, "Could not load plcncli information into cache.", e);
		}

		return Status.OK_STATUS;
	}
}
