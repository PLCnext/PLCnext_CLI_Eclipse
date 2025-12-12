/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.ConfigFile;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.eclipse.core.runtime.IPath;

import com.phoenixcontact.plcnext.common.Activator;


public class ConfigFileProvider
{
	private final static String configFileName = "PLCnextSettings.xml";
	
	private static File GetConfigFile(IPath projectDirectory) 
	{
		IPath path = projectDirectory.append(configFileName);
		return path.toFile();
	}
	
	public static ProjectConfiguration LoadFromConfig(IPath projectDirectory) 
	{
		File file = GetConfigFile(projectDirectory);
		if (file.exists())
		{
			try
			{
				JAXBContext context = JAXBContext.newInstance(ProjectConfiguration.class);
				Unmarshaller unmarshaller = context.createUnmarshaller();
				ProjectConfiguration configuration = (ProjectConfiguration) unmarshaller.unmarshal(file);
				
				return configuration;
				
			} catch (JAXBException e)
			{
				Activator.getDefault().logError("Project configuration file could not be loaded.", e);
			}
		}
		return null;
	}
	
	public static void WriteConfigFile(ProjectConfiguration config, IPath projectDirectory) 
	{
		File file = GetConfigFile(projectDirectory);
		
		if (!config.hasContent())
		{
			if (file.exists())
			{
				file.delete();
			}
		} else
		{
			try
			{
				JAXBContext context = JAXBContext.newInstance(ProjectConfiguration.class);
				Marshaller marshaller = context.createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				config.setEmptyPropertiesToNull();
				marshaller.marshal(config, file);
			} 
			catch (JAXBException e)
			{
				Activator.getDefault().logError("Project configuration file could not be saved.", e);
			}			
		}
	}
}
