/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.toolchains;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

public class FindSourcesUtil
{
	public static List<String> findSourceEntries(IProject project)
	{
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		if (buildInfo != null)
		{
			IConfiguration configuration = buildInfo.getDefaultConfiguration();
			if (configuration != null)
			{
				return findSourceEntries(configuration);
			}
		}
		return null;
	}
	
	public static List<String> findSourceEntries(IConfiguration configuration)
	{
		ICSourceEntry[] entries = configuration.getSourceEntries();
		if (entries != null && entries.length > 0)
		{
			return Arrays.stream(entries).map(e ->
			{
				IPath path = e.getLocation();

				if (path != null)
				{
					if (!path.isEmpty())
					{
						return path.toOSString();
					}
				}

				path = e.getFullPath();
				if (path != null)
				{
					
					//resolve linked resource if available
					IManagedProject managedProject = configuration.getManagedProject();
					if (managedProject != null)
					{
						IResource resource = managedProject.getOwner();
						if (resource != null && resource instanceof IProject)
						{
							IProject project = resource.getProject();
							IFolder folder = project.getFolder(path);
							if(folder.isLinked())
							{
								IPath p = folder.getLocation();
								if(p != null)
									return p.toOSString();
							}
						}
					}
					
					return path.toOSString();
				}

				return null;
			}).collect(Collectors.toList());
		}
		
		return null;
	}
}
