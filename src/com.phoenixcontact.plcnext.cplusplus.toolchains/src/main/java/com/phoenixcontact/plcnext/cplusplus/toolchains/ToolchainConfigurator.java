﻿/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.toolchains;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsGenericProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IMacroEntry;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.ui.newui.CDTPropertyManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.phoenixcontact.plcnext.common.CachedCliInformation;
import com.phoenixcontact.plcnext.common.CliNotExistingException;
import com.phoenixcontact.plcnext.common.EclipseContextHelper;
import com.phoenixcontact.plcnext.common.ICommandManager;
import com.phoenixcontact.plcnext.common.IDIHost;
import com.phoenixcontact.plcnext.common.Messages;
import com.phoenixcontact.plcnext.common.ProcessExitedWithErrorException;
import com.phoenixcontact.plcnext.common.commands.GetCompilerSpecsCommand;
import com.phoenixcontact.plcnext.common.commands.GetIncludePathsCommand;
import com.phoenixcontact.plcnext.common.commands.GetProjectInformationCommand;
import com.phoenixcontact.plcnext.common.commands.results.CommandResult;
import com.phoenixcontact.plcnext.common.commands.results.GetCompilerSpecsCommandResult;
import com.phoenixcontact.plcnext.common.commands.results.GetCompilerSpecsCommandResult.Compiler.Macro;
import com.phoenixcontact.plcnext.common.commands.results.GetIncludePathsCommandResult;
import com.phoenixcontact.plcnext.common.commands.results.GetIncludePathsCommandResult.IncludePath;
import com.phoenixcontact.plcnext.common.commands.results.GetProjectInformationCommandResult;
import com.phoenixcontact.plcnext.common.commands.results.GetProjectInformationCommandResult.ProjectTarget;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerMessageMessage.MessageType;

/**
 * Class which implements needed configurations for the toolchain
 */
public class ToolchainConfigurator
{

	ICommandManager commandManager;
	CachedCliInformation cache;
	private final String plcNextErrorParserId = com.phoenixcontact.plcnext.cplusplus.toolchains.Activator.PLUGIN_ID
			+ ".plcnextErrorParser";

	/**
	 * @param commandManager
	 */
	public ToolchainConfigurator()
	{
		IEclipseContext context = EclipseContextHelper.getActiveContext();
		IDIHost host = ContextInjectionFactory.make(IDIHost.class, context);
		commandManager = host.getExport(ICommandManager.class);
		cache = host.getExport(CachedCliInformation.class);
	}

	/**
	 * This method is called at project creation.
	 * 
	 * @param projectName
	 * @param monitor
	 * @throws CliNotExistingException
	 * @throws ProcessExitedWithErrorException
	 */
	public void configureProject(String projectName, IProgressMonitor monitor) throws ProcessExitedWithErrorException
	{
		IWorkspaceRoot wspRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = wspRoot.getProject(projectName);

		if (!project.exists())
			return;

		ICProjectDescription projectDescription = CoreModel.getDefault().getProjectDescription(project);

		setIncludes(project, projectDescription, null, null, monitor);
		setErrorParsers(projectDescription);
		addConfigurations(project, projectDescription, monitor);

		try
		{
			CoreModel.getDefault().setProjectDescription(project, projectDescription);
		} catch (CoreException e)
		{
			Activator.getDefault().logError("Error while setting include paths.", e);
		}
	}

	/**
	 * This method is called if target added/removed
	 * 
	 * @param project
	 * @param oldIncludePaths
	 * @param oldMacros
	 * @param monitor
	 * @throws CliNotExistingException
	 * @throws ProcessExitedWithErrorException
	 */
	public void configureProject(IProject project, List<String> oldIncludePaths, Map<String, String> oldMacros,
			IProgressMonitor monitor) throws ProcessExitedWithErrorException
	{
		ICProjectDescription projectDescription = CDTPropertyManager.getProjectDescription(project);

		setIncludes(project, projectDescription, oldIncludePaths, oldMacros, monitor);
		addConfigurations(project, projectDescription, monitor);

		try
		{
			CoreModel.getDefault().setProjectDescription(project, projectDescription);
		} catch (CoreException e)
		{
			Activator.getDefault().logError("Error while setting include paths.", e);
		}
	}

	private void setIncludes(IProject project, ICProjectDescription projectDescription, List<String> oldIncludePaths,
			Map<String, String> oldMacros, IProgressMonitor monitor)
			throws CliNotExistingException, ProcessExitedWithErrorException
	{

		MacrosAndIncludesWrapper macrosAndIncludes = findMacrosAndIncludes(project, monitor);
		boolean languageSettingsAdded = false;

		List<ICLanguageSetting> languageSettings = new ArrayList<ICLanguageSetting>();

		for (ICConfigurationDescription description : projectDescription.getConfigurations())
		{
			if (description instanceof ILanguageSettingsProvidersKeeper)
			{
				List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>(
						((ILanguageSettingsProvidersKeeper) description).getLanguageSettingProviders());

				ILanguageSettingsProvider plcnextLanguageProvider = providers.stream()
						.filter(p -> p.getId().contains("com.phoenixcontact.plcnext")).findFirst().orElse(null);
				if (plcnextLanguageProvider != null
						&& plcnextLanguageProvider instanceof LanguageSettingsGenericProvider)
				{
					LanguageSettingsGenericProvider languageProvider = (LanguageSettingsGenericProvider) plcnextLanguageProvider;

//					LanguageSettingsManager.setStoringEntriesInProjectArea(languageProvider, true);

					languageProvider.setSettingEntries(description, null, "org.eclipse.cdt.core.g++",
							macrosAndIncludes.getAllEntries());
					languageSettingsAdded = true;
				} else
				{
					ILanguageSettingsProvider userSettingsProvider = providers.stream()
							.filter(p -> p.getId().contains("org.eclipse.cdt.ui.UserLanguageSettingsProvider"))
							.findFirst().orElse(null);
					if (userSettingsProvider != null && userSettingsProvider instanceof LanguageSettingsGenericProvider)
					{
						LanguageSettingsGenericProvider languageProvider = (LanguageSettingsGenericProvider) userSettingsProvider;

						List<ICLanguageSettingEntry> existingEntries = languageProvider.getSettingEntries(description,
								project, "org.eclipse.cdt.core.g++");
						List<ICLanguageSettingEntry> x = macrosAndIncludes.getAllEntries();

						if (existingEntries != null)
						{
							List<ICLanguageSettingEntry> entriesToKeep = null;
							if (oldIncludePaths != null)
							{
								entriesToKeep = existingEntries.stream()
										.filter(e -> e.getKind() == ICSettingEntry.INCLUDE_PATH
												&& !oldIncludePaths.contains(e.getValue()))
										.collect(Collectors.toList());
							} else
							{
								entriesToKeep = existingEntries.stream()
										.filter(e -> (e.getKind() == ICSettingEntry.INCLUDE_PATH))
										.collect(Collectors.toList());
							}
							x.addAll(entriesToKeep);
							if (oldMacros != null)
							{
								entriesToKeep = existingEntries.stream()
										.filter(e -> (e.getKind() == ICSettingEntry.MACRO
												&& (!oldMacros.containsKey(e.getName()) || !(e.getValue() != null
														&& e.getValue().equals(oldMacros.get(e.getName()))))))
										.collect(Collectors.toList());
							} else
							{
								entriesToKeep = existingEntries.stream()
										.filter(e -> (e.getKind() == ICSettingEntry.MACRO))
										.collect(Collectors.toList());
							}
							x.addAll(entriesToKeep);

							entriesToKeep = existingEntries.stream()
									.filter(e -> (e.getKind() != ICSettingEntry.MACRO
											&& e.getKind() != ICSettingEntry.INCLUDE_PATH))
									.collect(Collectors.toList());
							x.addAll(entriesToKeep);
						}
						languageProvider.setSettingEntries(description, project, "org.eclipse.cdt.core.g++", x);
						languageSettingsAdded = true;
					}
				}

			} else
			{
				Activator.getDefault()
						.logWarning("Invalid old include paths might not be removed properly, please remove manually.");
			}

			for (ICLanguageSetting languageSetting : description.getRootFolderDescription().getLanguageSettings())
			{
				if (languageSetting.getLanguageId() != null
						&& languageSetting.getLanguageId().equals("org.eclipse.cdt.core.g++"))
				{
					languageSettings.add(languageSetting);
				}
			}
		}
		if (!languageSettings.isEmpty())
		{
			Map<String, String> options = new HashMap<String, String>();
			options.put(GetIncludePathsCommand.OPTION_PATH, project.getLocation().toOSString());

			IncludePath[] includePaths = null;
			try
			{
				includePaths = commandManager
						.executeCommand(commandManager.createCommand(options, GetIncludePathsCommand.class), false,
								monitor)
						.convertToTypedCommandResult(GetIncludePathsCommandResult.class).getIncludePaths();

			} catch (ProcessExitedWithErrorException e)
			{
				JsonObject reply = e.getReply();
				if (reply != null)
				{
					Gson gson = new Gson();
					try
					{
						includePaths = gson.fromJson(reply, GetIncludePathsCommandResult.class).getIncludePaths();

					} catch (JsonSyntaxException ex)
					{
						Activator.getDefault().logError("Could not determine include paths", e);
						return;
					}
				} else
				{
					List<String> output = e.getMessages().stream()
							.filter(m -> m.getMessageType() == MessageType.information).map(m -> m.getMessage())
							.collect(Collectors.toList());
					if (output != null)
					{
						includePaths = CommandResult.convertToTypedCommandResult(GetIncludePathsCommandResult.class, output).getIncludePaths();
					}
				}
			}
			List<String> newIncludePaths = new ArrayList<String>();
			List<ICLanguageSettingEntry> includePathEntries = new ArrayList<ICLanguageSettingEntry>();

			if (includePaths != null)
			{
				for (IncludePath includePath : includePaths)
				{
					String path = includePath.getPath();
					newIncludePaths.add(path);
					includePathEntries.add(new CIncludePathEntry(path, ICSettingEntry.BUILTIN));
					if (oldIncludePaths != null && !oldIncludePaths.isEmpty())
						oldIncludePaths.remove(path);
				}
			}

			includePathEntries.add(new CIncludePathEntry(File.separator + project.getName() + File.separator
					+ Messages.ToolchainConfigurator_intermediateFolder + File.separator
					+ Messages.ToolchainConfigurator_codeFolder, ICSettingEntry.VALUE_WORKSPACE_PATH));

			for (ICLanguageSetting languageSetting : languageSettings)
			{

				List<ICLanguageSettingEntry> includeEntries = languageSetting
						.getSettingEntriesList(ICSettingEntry.INCLUDE_PATH);

				List<ICLanguageSettingEntry> macroEntries = languageSetting.getSettingEntriesList(ICSettingEntry.MACRO);

				// remove includes which are not needed anymore
				if (oldIncludePaths != null && !oldIncludePaths.isEmpty())
				{
					List<ICLanguageSettingEntry> entriesToRemove = new ArrayList<ICLanguageSettingEntry>();
					for (String includePath : oldIncludePaths)
					{
						for (ICLanguageSettingEntry entry : includeEntries)
						{
							// if a workspace path was added in the same operation, it is not resolved at this point, 
							// so don't remove unresolved includes
							if (entry.getValue().equals(includePath))
							{
								entriesToRemove.add(entry);
							}
						}
					}

					includeEntries.removeAll(entriesToRemove);
				}
				if (oldMacros != null)
				{
					List<ICLanguageSettingEntry> entriesToRemove = new ArrayList<ICLanguageSettingEntry>();
					for (Entry<String, String> oldEntry : oldMacros.entrySet())
					{
						for (ICLanguageSettingEntry entry : macroEntries)
						{
							if (entry.getName().equals(oldEntry.getKey())
									&& entry.getValue().equals(oldEntry.getValue()))
							{
								entriesToRemove.add(entry);
							}
						}
					}
					macroEntries.removeAll(entriesToRemove);
				}

				for (String includePath : newIncludePaths)
				{
					includeEntries.add(new CIncludePathEntry(includePath, ICSettingEntry.LOCAL));
				}

				// fallback implementation if description is not
				// ILanguageSettingsProvidersKeeper
				if (!languageSettingsAdded)
				{
					includeEntries.addAll(macrosAndIncludes.getIncludeEntries());
					macroEntries.addAll(macrosAndIncludes.getMacroEntries());
					languageSetting.setSettingEntries(ICSettingEntry.MACRO, macroEntries);
				}

				includeEntries.add(new CIncludePathEntry(File.separator + project.getName() + File.separator
						+ Messages.ToolchainConfigurator_intermediateFolder + File.separator
						+ Messages.ToolchainConfigurator_codeFolder, ICSettingEntry.VALUE_WORKSPACE_PATH));

				languageSetting.setSettingEntries(ICSettingEntry.INCLUDE_PATH, includeEntries);
			}
		}

	}

	/**
	 * Wrapper for includes and macros as list/map or as ICLanguageSettingEntry
	 *
	 */
	public class MacrosAndIncludesWrapper
	{
		private List<String> includes;
		private Map<String, String> macros;

		/**
		 * Creates wrapper for includes and macros
		 * 
		 * @param includes
		 * @param macros
		 */
		public MacrosAndIncludesWrapper(List<String> includes, Map<String, String> macros)
		{
			this.includes = includes;
			this.macros = macros;
		}

		public List<String> getIncludes()
		{
			return includes;
		}

		public Map<String, String> getMacros()
		{
			return macros;
		}

		/**
		 * @return list of CIncludePathEntry elements and CMacroEntry elements
		 */
		public List<ICLanguageSettingEntry> getAllEntries()
		{
			List<ICLanguageSettingEntry> entries = getIncludeEntries();
			entries.addAll(getMacroEntries());
			return entries;
		}

		/**
		 * @return list of CIncludePathEntry elements
		 */
		public List<ICLanguageSettingEntry> getIncludeEntries()
		{
			List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
			for (String entry : includes)
			{
				entries.add(CDataUtil.createCIncludePathEntry(entry, ICSettingEntry.BUILTIN));
			}
			return entries;
		}

		/**
		 * @return list of CMacroEntry elements
		 */
		public List<ICLanguageSettingEntry> getMacroEntries()
		{
			List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
			for (Entry<String, String> entry : macros.entrySet())
			{
				entries.add(CDataUtil.createCMacroEntry(entry.getKey(), entry.getValue(), IMacroEntry.CDT_MACRO));
			}
			return entries;
		}
	}

	/**
	 * uses plcncli to get compiler specs and extracts all include paths and macros
	 * if same macro is defined multiple times, the first occurence wins and all
	 * others will be ignored
	 * 
	 * @param project
	 * @param monitor
	 * @return a wrapper containing the macros and includes
	 * @throws ProcessExitedWithErrorException
	 */
	public MacrosAndIncludesWrapper findMacrosAndIncludes(IProject project, IProgressMonitor monitor)
			throws ProcessExitedWithErrorException
	{
		List<String> includes = new ArrayList<String>();
		Map<String, String> macros = new HashMap<String, String>();

		Map<String, String> options = new HashMap<String, String>();
		options.put(GetCompilerSpecsCommand.OPTION_PATH, project.getLocation().toOSString());

		GetCompilerSpecsCommandResult.Compiler[] compiler = null;
		try
		{
			compiler = commandManager
					.executeCommand(commandManager.createCommand(options, GetCompilerSpecsCommand.class), false,
							monitor)
					.convertToTypedCommandResult(GetCompilerSpecsCommandResult.class).getCompiler();

		} catch (ProcessExitedWithErrorException e)
		{
			try {
			compiler = CommandResult.convertToTypedCommandResult(GetCompilerSpecsCommandResult.class,
					e.getMessages().stream().filter(m -> m.getMessageType() == MessageType.information)
							.map(m -> m.getMessage()).collect(Collectors.toList())).getCompiler();
			}catch (JsonSyntaxException e1) {
				throw e;
			}
		}

		if (compiler.length == 0)
			return new MacrosAndIncludesWrapper(includes, macros);

		Collection<String> macroNames = new ArrayList<String>();

		for (GetCompilerSpecsCommandResult.Compiler result : compiler)
		{
			includes.addAll(Arrays.stream(result.getIncludePaths()).map(p -> p.getPath()).collect(Collectors.toList()));

			List<Macro> macroSubResults = Arrays.asList(result.getMacros());

			for (Macro macro : macroSubResults)
			{
				String name = macro.getName();
				String value = "";
				if (macro.getValue() != null)
				{
					value = macro.getValue();
				}
				if (!macroNames.contains(name))
				{
					macroNames.add(name);
					macros.put(name, value);
				}
			}
		}

		return new MacrosAndIncludesWrapper(includes, macros);

	}

	private void addConfigurations(IProject project, ICProjectDescription projectDescription, IProgressMonitor monitor)
	{

		try
		{
			ICConfigurationDescription configDescription = projectDescription
					.getConfigurationById("com.phoenixcontact.plcnext.cplusplus.toolchains.configuration.debug");

			if (configDescription == null)
			{
				// create configurartion and set options of tools
				configDescription = projectDescription.createConfiguration(
						"com.phoenixcontact.plcnext.cplusplus.toolchains.configuration.debug", "Debug all targets",
						projectDescription.getConfigurationByName("Release all targets"));
				IConfiguration config = ManagedBuildManager.getConfigurationForDescription(configDescription);
				ITool[] tools = config.getToolChain()
						.getToolsBySuperClassId("com.phoenixcontact.plcnext.cplusplus.toolchains.buildtool");
				if (tools.length == 1)
				{
					IOption optionBuildType = tools[0].getOptionBySuperClassId(
							"com.phoenixcontact.plcnext.cplusplus.toolchains.buildtool.optionbuildtype");
					if (optionBuildType != null)
					{
						ManagedBuildManager.setOption(config, tools[0], optionBuildType, "Debug");
					}
				}
			}

			// *******try to get project targets from cache*********************

			// use command line tool to get list of available targets for selected project
			Map<String, String> options = new HashMap<String, String>();
			options.put(GetProjectInformationCommand.OPTION_PATH, project.getLocation().toOSString());

			ProjectTarget[] projectTargets = commandManager
					.executeCommand(commandManager.createCommand(options, GetProjectInformationCommand.class), false,
							monitor)
					.convertToTypedCommandResult(GetProjectInformationCommandResult.class).getTargets();

			for (ProjectTarget target : projectTargets)
			{
				String targetName = target.getDisplayName();

				// configuration release <targetname>
				configDescription = projectDescription.getConfigurationById(
						"com.phoenixcontact.plcnext.cplusplus.toolchains.configuration.release" + targetName);

				if (configDescription == null)
				{
					configDescription = projectDescription.createConfiguration(
							"com.phoenixcontact.plcnext.cplusplus.toolchains.configuration.release" + targetName,
							"Release " + targetName, projectDescription.getConfigurationByName("Release all targets"));

					IConfiguration config = ManagedBuildManager.getConfigurationForDescription(configDescription);

					ITool[] tools = config.getToolChain()
							.getToolsBySuperClassId("com.phoenixcontact.plcnext.cplusplus.toolchains.buildtool");
					if (tools.length == 1)
					{
						IOption optionBuildType = tools[0].getOptionBySuperClassId(
								"com.phoenixcontact.plcnext.cplusplus.toolchains.buildtool.optionbuildtype");
						if (optionBuildType != null)
						{
							ManagedBuildManager.setOption(config, tools[0], optionBuildType, "Release");
						}

						IOption optionTarget = tools[0].getOptionBySuperClassId(
								"com.phoenixcontact.plcnext.cplusplus.toolchains.buildtool.optiontarget");
						if (optionTarget != null)
						{
							ManagedBuildManager.setOption(config, tools[0], optionTarget, "\"" + targetName + "\"");
						}
					}
					// set options for generate library command
					tools = config.getToolChain()
							.getToolsBySuperClassId("com.phoenixcontact.plcnext.cplusplus.toolchains.librarybuilder");
					if (tools.length == 1)
					{
						IOption optionTarget = tools[0].getOptionBySuperClassId(
								"com.phoenixcontact.plcnext.cplusplus.toolchains.librarybuildtool.optiontarget");
						if (optionTarget != null)
						{
							ManagedBuildManager.setOption(config, tools[0], optionTarget, "\"" + targetName + "\"");
						}
					}
				}

				// configuration debug <targetname>
				configDescription = projectDescription.getConfigurationById(
						"com.phoenixcontact.plcnext.cplusplus.toolchains.configuration.debug" + targetName);
				if (configDescription == null)
				{
					configDescription = projectDescription.createConfiguration(
							"com.phoenixcontact.plcnext.cplusplus.toolchains.configuration.debug" + targetName,
							"Debug " + targetName, projectDescription.getConfigurationByName("Release all targets"));
					IConfiguration config = ManagedBuildManager.getConfigurationForDescription(configDescription);

					// set options for build command
					ITool[] tools = config.getToolChain()
							.getToolsBySuperClassId("com.phoenixcontact.plcnext.cplusplus.toolchains.buildtool");
					if (tools.length == 1)
					{
						IOption optionBuildType = tools[0].getOptionBySuperClassId(
								"com.phoenixcontact.plcnext.cplusplus.toolchains.buildtool.optionbuildtype");
						if (optionBuildType != null)
						{
							ManagedBuildManager.setOption(config, tools[0], optionBuildType, "Debug");
						}

						IOption optionTarget = tools[0].getOptionBySuperClassId(
								"com.phoenixcontact.plcnext.cplusplus.toolchains.buildtool.optiontarget");
						if (optionTarget != null)
						{
							ManagedBuildManager.setOption(config, tools[0], optionTarget, "\"" + targetName + "\"");
						}
					}
					// set options for generate library command
					tools = config.getToolChain()
							.getToolsBySuperClassId("com.phoenixcontact.plcnext.cplusplus.toolchains.librarybuilder");
					if (tools.length == 1)
					{
						IOption optionTarget = tools[0].getOptionBySuperClassId(
								"com.phoenixcontact.plcnext.cplusplus.toolchains.librarybuildtool.optiontarget");
						if (optionTarget != null)
						{
							ManagedBuildManager.setOption(config, tools[0], optionTarget, "\"" + targetName + "\"");
						}
					}
				}

			}

		} catch (Exception e)
		{
			Activator.getDefault().logError("Could not create default configurations.", e);
		}
	}

	private void setErrorParsers(ICProjectDescription projectDescription)
	{
		for (ICConfigurationDescription configDescription : projectDescription.getConfigurations())
		{

			IBuilder builder = ManagedBuildManager.getConfigurationForDescription(configDescription).getToolChain()
					.getBuilder();
			String[] parsers = builder.getErrorParsers();

			// create arrayList from Arrays.asList to make the resulting list modifyable
			List<String> parserList = new ArrayList<String>(Arrays.asList(parsers));

			try
			{
				if (parserList.contains(plcNextErrorParserId))
				{
					parserList.remove(plcNextErrorParserId);
					parserList.add(0, plcNextErrorParserId);

					builder.setErrorParsers(parserList.toArray(new String[0]));
				} else
				{
					Activator.getDefault().logInfo("Could not find error parser " + plcNextErrorParserId);
				}
			} catch (CoreException e)
			{
				Activator.getDefault().logWarning(
						"Could not set error parser " + plcNextErrorParserId + ". Please move to top manually.");
			}
		}
	}
}
