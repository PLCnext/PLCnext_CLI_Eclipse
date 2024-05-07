/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Version;
import com.phoenixcontact.plcnext.common.commands.CheckProjectCommand;
import com.phoenixcontact.plcnext.common.commands.Command;

public class PlcncliResourceChangeListener implements IResourceChangeListener
{
	ICommandManager commandManager;
	
	public PlcncliResourceChangeListener()
	{
		IEclipseContext context = EclipseContextHelper.getActiveContext();
		IDIHost host = ContextInjectionFactory.make(IDIHost.class, context);
		commandManager = host.getExport(ICommandManager.class);
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event)
	{
		if(event == null || event.getDelta() == null) 
			return;
		
		IResourceDelta workspace = event.getDelta();
		IResourceDelta[] projects = workspace.getAffectedChildren(IResourceDelta.CHANGED);
		
		for (IResourceDelta resourceDelta : projects)
		{
			if((resourceDelta.getFlags() & IResourceDelta.OPEN) != 0)
			{
				IProject project = resourceDelta.getResource().getProject();
				try
				{
					if(project != null && project.isOpen() && project.hasNature(Messages.PlcProjectNatureId))
					{
						IStatus result = checkOpenProject(project);
						if(!result.isOK())
						{
							Display.getDefault().asyncExec(() -> 
							{
								MessageDialog.openError(null, "Incompatible project version",
										result.getMessage());
							});
						}
					}
				} catch (CoreException e)
				{
					Activator.getDefault().logError("Error while trying to check project.", e);
				}
			}
		}
	}
	
	public IStatus checkOpenProjects()
	{
		IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
		List<IStatus> errorStates = Arrays.stream(workspace.getProjects())
				.filter(project -> {
					try
					{
						return project.isOpen() && project.hasNature(Messages.PlcProjectNatureId);
					} catch (CoreException e)
					{
						return project.isOpen();
					}
				})
				.map(project -> checkOpenProject(project))
				.filter(status -> !status.isOK())
				.collect(Collectors.toList());
				
		if(errorStates.isEmpty()) 
		{
			return Status.OK_STATUS;
		}
		return Status.error(errorStates.stream().map(state -> state.getMessage()).collect(Collectors.joining("\n\n")));
	}
	
	private IStatus checkOpenProject(IProject project)
	{
		Map<String, String> options = new HashMap<String, String>();

		options.put(CheckProjectCommand.OPTION_PATH, project.getLocation().toOSString());
		Command command = commandManager.createCommand(options, CheckProjectCommand.class);
		try {
			commandManager.executeCommand(command, false, null);
		}
		catch(ProcessExitedWithErrorException e)
		{
			return Status.error("Found problem with project "+project.getName()+"\n"+e.getMessages().stream().map(m -> m.getMessage()).collect(Collectors.joining("")));
		}
		
		IScopeContext scope = new ProjectScope(project);
		
		IEclipsePreferences prefsNode = scope.getNode(Messages.ProjectScopeId);
		
		if(prefsNode.get(Messages.ProjectVersionKey, null) == null)
		{
			//if a project is imported from outside the workspace, the prefsNode is not loaded yet and will return the default value
			// in this case the check is outsourced to a job which refereshes the project first to ensure load of the preferences.
			// This has to be done in a separate job which is scheduled using the refreshRule(project) bec. the resourcechangelistener
			// blocks the workspace and does not allow a refresh directly
			Job job = new Job("Check Imported Projects")
			{
				@Override
				protected IStatus run(IProgressMonitor monitor)
				{
					try
					{
						project.refreshLocal(0, null);
					} catch (CoreException e)
					{
						return Status.warning("Checking imported project failed. ",e);
					}
					Version projectVersion = Version.parseVersion(prefsNode.get(Messages.ProjectVersionKey, "1.0.0"));
					Version allowedMaxVersion = Version.parseVersion(Messages.ProjectVersionValue);
					
					if(projectVersion.getMajor() > allowedMaxVersion.getMajor())
					{
						return Status.error("Found problem with project "+project.getName()+
								"\nThe project version ("+projectVersion+") is higher than the currently installed plugin allows ("+allowedMaxVersion+"). Please update the PLCnext Technology plugin!" );
					}
					return Status.OK_STATUS;
				}
			};
			job.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().refreshRule(project));
			job.schedule();
			return Status.OK_STATUS;
		}
		else 
		{
			Version projectVersion = Version.parseVersion(prefsNode.get(Messages.ProjectVersionKey, "1.0.0"));
			Version allowedMaxVersion = Version.parseVersion(Messages.ProjectVersionValue);
		
			if(projectVersion.getMajor() > allowedMaxVersion.getMajor())
			{
				return Status.error("Found problem with project "+project.getName()+
						"\nThe project version ("+projectVersion+") is higher than the currently installed plugin allows ("+allowedMaxVersion+"). Please update the PLCnext Technology plugin!" );
			
			}
		
			return Status.OK_STATUS;
		}
	}
}
