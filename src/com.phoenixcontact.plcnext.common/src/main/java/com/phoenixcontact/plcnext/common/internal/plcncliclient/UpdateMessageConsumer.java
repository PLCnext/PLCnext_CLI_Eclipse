/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.internal.plcncliclient;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;

import com.phoenixcontact.plcnext.common.Activator;
import com.phoenixcontact.plcnext.common.CachedCliInformation;
import com.phoenixcontact.plcnext.common.EclipseContextHelper;
import com.phoenixcontact.plcnext.common.ICommandManager;
import com.phoenixcontact.plcnext.common.IDIHost;
import com.phoenixcontact.plcnext.common.ProcessExitedWithErrorException;
import com.phoenixcontact.plcnext.common.commands.GetProjectInformationCommand;
import com.phoenixcontact.plcnext.common.commands.GetSettingCommand;
import com.phoenixcontact.plcnext.common.commands.GetTargetsCommand;
import com.phoenixcontact.plcnext.common.commands.results.CommandResult;
import com.phoenixcontact.plcnext.common.commands.results.GetProjectInformationCommandResult;
import com.phoenixcontact.plcnext.common.commands.results.GetSettingCommandResult;
import com.phoenixcontact.plcnext.common.commands.results.GetSettingCommandResult.Setting;
import com.phoenixcontact.plcnext.common.commands.results.GetTargetsCommandResult;
import com.phoenixcontact.plcnext.common.commands.results.Target;
import com.phoenixcontact.plcnext.common.logging.Logger;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerUpdateMessage;

public class UpdateMessageConsumer extends Job
{
	private ServerUpdateMessage message;

	public UpdateMessageConsumer(ServerUpdateMessage message)
	{
		super("Processing server update message");
		this.message = message;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor)
	{
		IEclipseContext context = EclipseContextHelper.getActiveContext();
		IDIHost host = ContextInjectionFactory.make(IDIHost.class, context);
		CachedCliInformation cache = host.getExport(CachedCliInformation.class);
		ICommandManager commandManager = host.getExport(ICommandManager.class);

		String topic = message.getUpdateTopic();
		Logger.log("Received update message with topic:"+topic);

		switch (topic)
		{
		case ServerUpdateMessage.topic_sdks:
			
			try
			{
				CommandResult result = commandManager
						.executeCommand(commandManager.createCommand(null, GetTargetsCommand.class), monitor);
				GetTargetsCommandResult commandResult = result.convertToTypedCommandResult(GetTargetsCommandResult.class);
				Target[] targets = commandResult.getTargets();
				
				cache.setAllTargets(Arrays.asList(targets));
				//TODO test this behavior

			} catch (ProcessExitedWithErrorException e)
			{
				IStatus status = new Status(Status.ERROR, Activator.PLUGIN_ID, "Error while trying to execute command.",
						e);
				Activator.getDefault().getLog().log(status);
				return status;
			}

			break;
		case ServerUpdateMessage.topic_project_targets:

			try
			{
				Map<String, String> options = new HashMap<String, String>();
				options.put(GetProjectInformationCommand.OPTION_PATH, message.getProject());
				CommandResult result = commandManager
						.executeCommand(commandManager.createCommand(options, GetProjectInformationCommand.class), monitor);
				GetProjectInformationCommandResult commandResult = result.convertToTypedCommandResult(GetProjectInformationCommandResult.class);
				Target[] targets = commandResult.getTargets();
				
				// TODO currently nothing is done with the information, that the project target has changed

			} catch (ProcessExitedWithErrorException e)
			{
				IStatus status = new Status(Status.ERROR, Activator.PLUGIN_ID, "Error while trying to execute command.",
						e);
				Activator.getDefault().getLog().log(status);
				return status;
			}
			break;
		case ServerUpdateMessage.topic_settings:
			try
			{
				Map<String, String> options = new HashMap<String, String>();
				options.put(GetSettingCommand.OPTION_AttributePrefix, null);
				CommandResult result = commandManager
						.executeCommand(commandManager.createCommand(options, GetSettingCommand.class), false, monitor);

				GetSettingCommandResult settingResult = result.convertToTypedCommandResult(GetSettingCommandResult.class);
				Setting setting = settingResult.getSetting();

				String prefix = "#";
				
				prefix = setting.getAttributePrefix();
				
				cache.setPortCommentPrefix(prefix);

			} catch (ProcessExitedWithErrorException e)
			{
				IStatus status = new Status(Status.ERROR, Activator.PLUGIN_ID, "Error while trying to execute command.",
						e);
				Activator.getDefault().getLog().log(status);
				return status;
			}
			break;
		default:
			// unknown update topic
			IStatus status = new Status(Status.ERROR, Activator.PLUGIN_ID,
					"Received server message with unknown update topic: " + topic);
			Activator.getDefault().getLog().log(status);
			return status;
		}
		return Status.OK_STATUS;
	}

}
