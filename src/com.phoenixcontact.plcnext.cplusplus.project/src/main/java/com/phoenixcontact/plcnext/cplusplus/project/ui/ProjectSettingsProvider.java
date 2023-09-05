/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.project.ui;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.eclipse.core.runtime.IPath;

import com.phoenixcontact.plcnext.cplusplus.project.Activator;

/**
 *
 */
public class ProjectSettingsProvider
{

	private IPath projectFilePath;
	private ProjectSettings value;
	private final String projectFileName = "plcnext.proj";
	
	public ProjectSettingsProvider(IPath projectPath)
	{
		projectFilePath = projectPath.append(projectFileName);;
		readProjectFile();
	}
	
	protected void readProjectFile()
	{
		File file = projectFilePath.toFile();
		if (file.exists())
		{
			try
			{
				JAXBContext context = JAXBContext.newInstance(ProjectSettings.class);
				Unmarshaller unmarshaller = context.createUnmarshaller();
				value = (ProjectSettings) unmarshaller.unmarshal(file);
				
			} catch (JAXBException e)
			{
				Activator.getDefault().logError("Project file could not be loaded.", e);
			}
		}
	}
	
	protected void writeProjectFile()
	{
		if(value == null)
			return;
		
		try
		{
			JAXBContext context = JAXBContext.newInstance(ProjectSettings.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			marshaller.marshal(value, projectFilePath.toFile());
		} catch (JAXBException e)
		{
			Activator.getDefault().logError("Project file could not be saved.", e);
		}
	}
	
	protected boolean getGenerateNamespaces()
	{
		if(value == null)
			return true;
		
		return value.getGenerateNamespaces();
	}
	
	protected void setGenerateNamespaces(boolean generateNamespaces)
    {
		if(value ==null)
			return;
		
		value.setGenerateNamespaces(generateNamespaces);
    }
}
