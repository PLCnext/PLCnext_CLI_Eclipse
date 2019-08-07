/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.project.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsGenericProvider;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.phoenixcontact.plcnext.common.ICommandManager;
import com.phoenixcontact.plcnext.common.MutexSchedulingRule;
import com.phoenixcontact.plcnext.common.ProcessExitedWithErrorException;
import com.phoenixcontact.plcnext.common.commands.Command;
import com.phoenixcontact.plcnext.common.commands.GetIncludePathsCommand;
import com.phoenixcontact.plcnext.common.commands.SetTargetCommand;
import com.phoenixcontact.plcnext.common.commands.results.CommandResult;
import com.phoenixcontact.plcnext.common.commands.results.GetIncludePathsCommandResult;
import com.phoenixcontact.plcnext.common.commands.results.GetIncludePathsCommandResult.IncludePath;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerMessageMessage.MessageType;
import com.phoenixcontact.plcnext.cplusplus.project.Activator;
import com.phoenixcontact.plcnext.cplusplus.toolchains.ToolchainConfigurator;
import com.phoenixcontact.plcnext.cplusplus.toolchains.ToolchainConfigurator.MacrosAndIncludesWrapper;

/**
 * Add or remove target(s) from project in background job
 *
 */
public class SupportedTargetsPerformOKJob extends Job
{
	private List<String> targetsToAdd;
	private List<String> targetsToRemove;
	private IProject project;
	private ICommandManager commandManager;
	private ToolchainConfigurator configurator;

	/**
	 * Creates new job which will add or remove the given targets for the given
	 * project
	 * 
	 * @param name            jobname
	 * @param targetsToAdd
	 * @param targetsToRemove
	 * @param project
	 * @param commandManager
	 * @param cache
	 */
	public SupportedTargetsPerformOKJob(String name, List<String> targetsToAdd, List<String> targetsToRemove,
			IProject project, ICommandManager commandManager)
	{
		super(name);
		setUser(true);
		this.targetsToAdd = targetsToAdd;
		this.targetsToRemove = targetsToRemove;
		this.project = project;
		this.commandManager = commandManager;
		configurator = new ToolchainConfigurator();
		setRule(new MutexSchedulingRule());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.
	 * IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor)
	{

		Map<String, String> options = new HashMap<String, String>();

		if (project != null)
		{

			List<String> includePaths = new ArrayList<String>();
			MacrosAndIncludesWrapper entries = null;

			if (!targetsToRemove.isEmpty())
			{

				// get include paths for project before executing set target (needed to find out
				// which includes should be deleted)
				options.put(GetIncludePathsCommand.OPTION_PATH, project.getLocation().toOSString());

				IncludePath[] results = null;

				try
				{
					CommandResult commandResult = commandManager.executeCommand(
							commandManager.createCommand(options, GetIncludePathsCommand.class), false, monitor);

					results = commandResult.convertToTypedCommandResult(GetIncludePathsCommandResult.class).getIncludePaths();

				} catch (ProcessExitedWithErrorException e)
				{
					JsonObject reply = e.getReply();
					if (reply != null)
					{
						Gson gson = new Gson();
						try
						{
							results = gson.fromJson(reply, GetIncludePathsCommandResult.class).getIncludePaths();

						} catch (JsonSyntaxException ex)
						{
							Activator.getDefault().logError("Could not determine include paths", e);
						}
					} else
					{
						List<String> output = e.getMessages().stream()
								.filter(m -> m.getMessageType() == MessageType.information).map(m -> m.getMessage())
								.collect(Collectors.toList());
						if (output != null)
						{
							try
							{
								results = CommandResult.convertToTypedCommandResult(GetIncludePathsCommandResult.class, output).getIncludePaths();
							} catch (ProcessExitedWithErrorException e1)
							{
								Activator.getDefault().logError("Could not determine include paths", e);
							}
						}
					}
				}

				if (results != null)
				{
					for (IncludePath result : results)
					{
						includePaths.add(result.getPath());
					}
				}

				// fallback if plcnextLanguagesettingsProvider cannot be accessed via cast of
				// configuration (see ToolchainConfigurator) or if provider not instance of
				// LanguageSettingsGenericProvider
				boolean fallbackNeeded = false;
				for (ICConfigurationDescription desc : CoreModel.getDefault().getProjectDescription(project)
						.getConfigurations())
				{
					if (desc instanceof ILanguageSettingsProvidersKeeper)
					{
						List<ILanguageSettingsProvider> x = ((ILanguageSettingsProvidersKeeper) desc)
								.getLanguageSettingProviders();
						if (x != null)
						{
							ILanguageSettingsProvider result = x.stream()
									.filter(p -> p.getId().contains("com.phoenixcontact.plcnext")).findFirst()
									.orElse(null);
							if (result != null && !(result instanceof LanguageSettingsGenericProvider))
							{
								fallbackNeeded = true;
								break;
							}
						}
					}

					if (!(desc instanceof ILanguageSettingsProvidersKeeper))
					{
						fallbackNeeded = true;
						break;
					}
				}
				if (fallbackNeeded)
				{
					try
					{
						entries = configurator.findMacrosAndIncludes(project, monitor);
					} catch (ProcessExitedWithErrorException e)
					{
						Activator.getDefault().logWarning("Could not update include paths properly.", e);
					}
				}

			}

			try
			{
				for (String target : targetsToAdd)
				{
					options.clear();
					options.put(SetTargetCommand.OPTION_ADD, null);
					options.put(SetTargetCommand.OPTION_PATH, project.getLocation().toOSString());
					String[] name_version = target.split(",");
					if (name_version.length > 0)
					{
						options.put(SetTargetCommand.OPTION_NAME, name_version[0]);
						if (name_version.length > 1)
						{
							options.put(SetTargetCommand.OPTION_VERSION, name_version[1]);
						}
						Command setTargetCommand = commandManager.createCommand(options, SetTargetCommand.class);
						commandManager.executeCommand(setTargetCommand, monitor);
					}
				}
				for (String target : targetsToRemove)
				{
					options.clear();
					options.put(SetTargetCommand.OPTION_PATH, project.getLocation().toOSString());
					options.put(SetTargetCommand.OPTION_REMOVE, null);
					String[] name_version = target.split(",");
					if (name_version.length > 0)
					{
						options.put(SetTargetCommand.OPTION_NAME, name_version[0]);
						if (name_version.length > 1)
						{
							options.put(SetTargetCommand.OPTION_VERSION, name_version[1]);
						}
						Command setTargetCommand = commandManager.createCommand(options, SetTargetCommand.class);
						commandManager.executeCommand(setTargetCommand, monitor);
					}
				}

				if (!targetsToAdd.isEmpty() || !targetsToRemove.isEmpty())
				{
					Map<String, String> macros = new HashMap<String, String>();
					if (entries != null)
					{
						includePaths.addAll(entries.getIncludes());
						macros.putAll(entries.getMacros());
					}
					configurator.configureProject(project, includePaths, macros, monitor);
				}
			} catch (ProcessExitedWithErrorException e)
			{
				Activator.getDefault().logError("Error while trying to set target for project.", e);
			}
		}
		return Status.OK_STATUS;

	}
}
