/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.logging;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.resources.ResourcesPlugin;
import com.phoenixcontact.plcnext.common.Activator;

public class Logger
{
	private static final boolean DEBUG = true;
	private static File logFile = null;
	private static Path logFilePath = Paths.get(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString(), ".metadata", ".plugins",
			Activator.PLUGIN_ID, ".log");

	private static void initializeLogger()
	{
		try
		{
			Files.createDirectories(logFilePath.getParent());
			logFile = logFilePath.toFile();
			if(!logFile.exists())
				Files.createFile(logFilePath);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

	public static void log(String message)
	{
		if (DEBUG)
		{
			if (logFile == null)
				initializeLogger();
			
			if (logFile.length() > 1000000)
			{
				Path bakFilePath = Paths.get(logFilePath.getParent().toString(), ".log.bak.txt");
				File bakFile = bakFilePath.toFile();
				if(bakFile.exists()) {
					bakFile.delete();
				}
				logFile.renameTo(bakFile);
				logFile = logFilePath.toFile();
				if(!logFile.exists())
					try
					{
						Files.createFile(logFilePath);
					} catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
			}
			
			String entry = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss:SSS").format(new Date()) + " " + message + "\n";

			try
			{
				Files.write(logFile.toPath(), entry.getBytes(), StandardOpenOption.APPEND);
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
//				e.printStackTrace();
			}
			
			System.out.print(entry);
		}
	}

}
