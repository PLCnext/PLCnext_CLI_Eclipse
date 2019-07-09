/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.project.componentproject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class PlcnextAppProjectNature implements IProjectNature
{
	private IProject project;
	/**
	 * The ID of this project nature
	 */
	public static final String NATURE_ID = "com.phoenixcontact.plcnext.cplusplus.project.plcnextappprojectnature";

	@Override
	public void configure() throws CoreException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void deconfigure() throws CoreException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public IProject getProject()
	{
		return project;
	}

	@Override
	public void setProject(IProject project)
	{
		this.project = project;
	}

}
