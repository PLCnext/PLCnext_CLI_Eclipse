/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.toolchains.internal;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.resources.RefreshScopeManager;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedOptionValueHandler;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.InternalBuildRunner;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import com.phoenixcontact.plcnext.common.CliNotExistingException;
import com.phoenixcontact.plcnext.common.EclipseContextHelper;
import com.phoenixcontact.plcnext.common.ICommandManager;
import com.phoenixcontact.plcnext.common.IDIHost;
import com.phoenixcontact.plcnext.common.Messages;
import com.phoenixcontact.plcnext.common.PasswordPersistFileType;
import com.phoenixcontact.plcnext.common.ProcessExitedWithErrorException;
import com.phoenixcontact.plcnext.common.SetPasswordDialog;
import com.phoenixcontact.plcnext.common.ConfigFile.ConfigFileProvider;
import com.phoenixcontact.plcnext.common.ConfigFile.ProjectConfiguration;
import com.phoenixcontact.plcnext.common.commands.Command;
import com.phoenixcontact.plcnext.common.commands.results.CommandResult;
import com.phoenixcontact.plcnext.common.commands.results.PlcncliMessage;
import com.phoenixcontact.plcnext.common.commands.results.PlcncliMessage.MessageType;
import com.phoenixcontact.plcnext.cplusplus.toolchains.Activator;

/**
 * build which uses the command line interface to execute a custom build
 * consisting of the steps: 1. generate metadata 2. compile and link the
 * codefiles using the sdk-toolchain 3. use the librarybuilder to create a
 * .pcwlx file
 */
public class InternalBuildRunnerExtension extends InternalBuildRunner
{

	ICommandManager commandManager;

	/**
	 * Sets the command manager via dependency injection
	 */
	public InternalBuildRunnerExtension()
	{
		// get commandManager to create and execute commands
		IEclipseContext context = EclipseContextHelper.getActiveContext();
		IDIHost host = ContextInjectionFactory.make(IDIHost.class, context);
		commandManager = host.getExport(ICommandManager.class);
	}

	@Override
	public boolean invokeBuild(int kind, IProject project, IConfiguration configuration, IBuilder builder,
			IConsole console, IMarkerGenerator markerGenerator, IncrementalProjectBuilder projectBuilder,
			IProgressMonitor monitor) throws CoreException
	{

		// delete all cmodel problem marker except for indexer markers
		IMarker[] markers = project.findMarkers(ICModelMarker.INDEXER_MARKER, true, IResource.DEPTH_INFINITE);
		project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
		for (IMarker marker : markers)
			marker.getResource().createMarker(ICModelMarker.INDEXER_MARKER);

		if (kind == IncrementalProjectBuilder.CLEAN_BUILD)
		{
			// this is only executed if a bin folder exists
			return cleanProject(project, monitor, configuration);

		}

		monitor.beginTask("PLCncli Build " + project.getName(), 1000);
		monitor.subTask("calling plcncli build commands for project " + project.getName());
		monitor.worked(100);

		try
		{
			// ******************* first step a: generate code****************
			ITool[] tools = configuration
					.getToolsBySuperClassId("com.phoenixcontact.plcnext.cplusplus.toolchains.metacodetool.base");
			if (tools.length > 0)
			{
				ITool generateTool = tools[0];
				executeToolCommand(generateTool, true, true, configuration, monitor);
			}
			monitor.worked(200);

			// ******************first step b: generate meta*****************
			tools = configuration
					.getToolsBySuperClassId("com.phoenixcontact.plcnext.cplusplus.toolchains.metadatatool.base");
			if (tools.length > 0)
			{
				ITool generateTool = tools[0];
				executeToolCommand(generateTool, true, false, configuration, monitor);
			}
			monitor.worked(200);

			// ******* second step: compile and link the codefiles**************
			tools = configuration.getToolsBySuperClassId("com.phoenixcontact.plcnext.cplusplus.toolchains.buildtool");
			CommandResult output = null;
			if (tools.length > 0)
			{
				ITool buildTool = tools[0];
				output = executeToolCommand(buildTool, true, false, configuration, monitor);
			}
			monitor.worked(300);

			// ***************third step: librarybuilder***********************
			tools = configuration
					.getToolsBySuperClassId("com.phoenixcontact.plcnext.cplusplus.toolchains.librarybuilder.base");
			if (tools.length > 0)
			{
				ITool libraryBuilder = tools[0];
				executeToolCommand(libraryBuilder, true, false, configuration, monitor);
			}
			monitor.worked(100);

			// ****************if process exits normal check for warnings*********
			if (output != null && output.getMessages() != null)
			{
				parseForErrorsOrWarnings(builder, output.getMessages(), project, configuration, markerGenerator);
			}
			
		} catch (ProcessExitedWithErrorException e)
		{
			if (e instanceof ProcessExitedWithErrorException)
			{
				parseForErrorsOrWarnings(builder, ((ProcessExitedWithErrorException) e).getMessages(), project,
						configuration, markerGenerator);
			}
			Activator.getDefault().logError("Error while trying to build project.", e);
		}
		// ******************* finally: refresh project************************
		try
		{
			RefreshScopeManager refreshManager = RefreshScopeManager.getInstance();
			IWorkspaceRunnable runnable = refreshManager.getRefreshRunnable(project, configuration.getName());
			ResourcesPlugin.getWorkspace().run(runnable, null, IWorkspace.AVOID_UPDATE, null);
		} catch (CoreException e)
		{
			Activator.getDefault().logError("Error while refreshing workspace.", e);
		} finally
		{
			monitor.done();
		}
		return false;
	}

	private CommandResult executeToolCommand(ITool tool, boolean logging, boolean clearConsole, IConfiguration config,
			IProgressMonitor monitor) throws CliNotExistingException, ProcessExitedWithErrorException
	{
		String commandline = "";
		try
		{
			// update the list of source directories
			ManagedBuildManager.performValueHandlerEvent(config, IManagedOptionValueHandler.EVENT_LOAD);

			commandline = tool.getCommandLineGenerator()
					.generateCommandLineInfo(tool, tool.getToolCommand(),
							tool.getToolCommandFlags(new Path("inp"), new Path("outp")), tool.getOutputFlag(),
							tool.getOutputPrefix(), "", tool.getAllInputExtensions(), tool.getCommandLinePattern())
					.getCommandLine();

		} catch (BuildException e)
		{
			Activator.getDefault().logError("Error while getting tool command flags.", e);
		}
		
		String commandlineWithoutPassword = commandline;
		if(tool.getBaseId().contains("com.phoenixcontact.plcnext.cplusplus.toolchains.librarybuilder"))
		{
			String password = getDeployPasswordForCommandLine(config);
			if(password != null && !password.isEmpty()) {
				commandlineWithoutPassword += " " + Messages.DeployCommand_optionPassword + " *";
				commandline += password;
			}
			
		}

		Command command = commandManager.createCommand(commandline);
		command.setLoggableExecutionCommand(commandlineWithoutPassword);
		return commandManager.executeCommand(command, logging, clearConsole, monitor);
	}
	
	private String getDeployPasswordForCommandLine(IConfiguration buildConfiguration)
	{
		if (buildConfiguration != null)
		{
			if (buildConfiguration instanceof IConfiguration)
			{
				IConfiguration conf = (IConfiguration) buildConfiguration;
				IManagedProject managedProject = conf.getManagedProject();
				if (managedProject != null)
				{
					IResource resource = managedProject.getOwner();
					if (resource != null && resource instanceof IProject)
					{
						IProject project = resource.getProject();
						ProjectConfiguration config = ConfigFileProvider.LoadFromConfig(project.getLocation());
						if(config != null && config.getSign())
						{
							String password = null;
							if(config.getPkcs12() != null && config.getPkcs12().isBlank())
							{
								password = getPassword(PasswordPersistFileType.PEMKeyFile, project);
							}
							else 
							{
								password = getPassword(PasswordPersistFileType.PKCS12, project);
							}
							if(password != null && !password.isBlank())
							{
								return " " +Messages.DeployCommand_optionPassword + " " + password;
							}
						}
					}
				}
			}
		}
		
		return "";
	}
	
	private String getPassword(PasswordPersistFileType type, IProject project)
	{
		try
		{
			String workspaceLocation = project.getWorkspace().getRoot().getLocation().toOSString();
			ISecurePreferences securePreferences = SecurePreferencesFactory.getDefault();
			ISecurePreferences node = securePreferences.node(Messages.SecureStorageNodeName);
			ISecurePreferences wspNode = node.node(Messages.SecureStorageWorkspacesKey).node(workspaceLocation);
			ISecurePreferences projectNode = null;
			if(wspNode.nodeExists(project.getName()))
			{
				projectNode = wspNode.node(project.getName());
			}else 
			{
				projectNode = node.node(project.getName());
			}
			String password  = projectNode.get(type.toString(), "");
			
			if(password != null && !password.isBlank())
				return password;
			
			
			
			class RunnableResult implements Runnable
			{
				private String result = "";
				@Override
				public void run()
				{
					SetPasswordDialog passwordDialog = 
							new SetPasswordDialog(null, 
									"Apply", 
									Messages.DeployWithPasswordDialog_DialogTitle,
									Messages.DeployWithPasswordDialog_AdditionalInformation);
					passwordDialog.setPassword("");
					result = passwordDialog.openWithResult();
				}
				public String getResult()
				{
					return result;
				}
			}
			RunnableResult runnable = new RunnableResult();
			
			Display.getDefault().syncExec(runnable);
			password = runnable.getResult();
			
			return password;
			
			
		} 
		catch (StorageException e1)
		{
			e1.printStackTrace();
			return null;
		}
	}

	private boolean clean_generate = true;

	private boolean cleanProject(IProject project, IProgressMonitor monitor, IConfiguration configuration)
	{
		Runnable op = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					// *********************delete intermediate folder*********************
					IFolder intermediateFolder = project.getFolder("intermediate"); //$NON-NLS-1$
					intermediateFolder.delete(true, null);

					// *********************delete bin folder*****************************
					IFolder binFolder = project.getFolder("bin"); //$NON-NLS-1$
					ResourceAttributes attributes = binFolder.getResourceAttributes();

					if (attributes != null && !attributes.isReadOnly())
					{
						for(IResource res : binFolder.members())
						{
							res.delete(true, monitor);
						}
					}

				} catch (CoreException e)
				{
					clean_generate = false;
					Activator.getDefault().logError("Error while trying to clean project.", e);

					ErrorDialog.openError(null, null, null,
							new Status(Status.ERROR, Activator.PLUGIN_ID, "Error while trying to clean project.", e));
					// ***********************refresh project*****************************
					try
					{
						RefreshScopeManager refreshManager = RefreshScopeManager.getInstance();
						IWorkspaceRunnable runnable = refreshManager.getRefreshRunnable(project,
								configuration.getName());
						ResourcesPlugin.getWorkspace().run(runnable, null, IWorkspace.AVOID_UPDATE, null);
					} catch (CoreException e1)
					{
						Activator.getDefault().logError("Error while refreshing workspace.", e1);
					}
				}
			}
		};
		
		clean_generate = true;
		
		Display.getDefault().syncExec(op);
		
		if (!clean_generate)
			return false; // no generate if deleting or creating folder threw exception

		// **********************generate code**********************************
		try
		{
			ITool[] tools = configuration
					.getToolsBySuperClassId("com.phoenixcontact.plcnext.cplusplus.toolchains.metacodetool.base");
			if (tools.length > 0)
			{
				ITool generateTool = tools[0];
				executeToolCommand(generateTool, false, false, configuration, monitor);
			}
		} catch (ProcessExitedWithErrorException e)
		{
			Activator.getDefault().logError("Error while trying to generate code after clean.", e);
		}

		// ***********************refresh project*****************************
		try
		{
			RefreshScopeManager refreshManager = RefreshScopeManager.getInstance();
			IWorkspaceRunnable runnable = refreshManager.getRefreshRunnable(project, configuration.getName());
			ResourcesPlugin.getWorkspace().run(runnable, null, IWorkspace.AVOID_UPDATE, null);
		} catch (CoreException e)
		{
			Activator.getDefault().logError("Error while refreshing workspace.", e);
		}
		return true;
	}

	private void parseForErrorsOrWarnings(IBuilder builder, List<PlcncliMessage> messages, IProject project,
			IConfiguration configuration, IMarkerGenerator markerGenerator)
	{
		if (messages != null)
		{

			List<PlcncliMessage> errorsAndWarnings = messages.stream()
					.filter(m -> m.getMessageType() == MessageType.error || m.getMessageType() == MessageType.warning)
					.collect(Collectors.toList());

			String[] errorParsers = builder.getErrorParsers();
			try
			{
				try (ErrorParserManager epm = new ErrorParserManager(project,
						ManagedBuildManager.getBuildLocationURI(configuration, builder), markerGenerator, errorParsers))
				{

					for (PlcncliMessage message : errorsAndWarnings)
					{
						epm.processLine(message.getMessage());
					}
				}
			} catch (IOException e1)
			{
				Activator.getDefault().logError("Error while trying to close ErrorParserManager.", e1);
			}
		}
	}
}
