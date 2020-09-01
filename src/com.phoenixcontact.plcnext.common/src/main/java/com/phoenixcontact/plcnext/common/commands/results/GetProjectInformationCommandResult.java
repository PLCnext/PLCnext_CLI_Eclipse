/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands.results;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerMessageMessage;

public class GetProjectInformationCommandResult extends CommandResult
{
	public enum PLCnCLIProjectType
	{
		project, acfproject, consumablelibrary
	}

	public GetProjectInformationCommandResult(JsonObject reply, List<ServerMessageMessage> messages)
	{
		super(reply, messages);
	}

	private String name;

	private String namespace;

	private PLCnCLIProjectType type;

	private ProjectTarget[] targets; 

	public ProjectTarget[] getTargets()
	{
		return targets;
	}

	public String getName()
	{
		return name;
	}

	public String getNamespace()
	{
		return namespace;
	}

	public PLCnCLIProjectType getType()
	{
		return type;
	}

	public static class ProjectTarget extends Target
	{
		private boolean available;

		public boolean isAvailable()
		{
			return available;
		}
	}

	private Entity[] entities;

	public List<Entity> getComponents()
	{
		return Arrays.stream(entities).filter(e -> e.getType().contains("component")).collect(Collectors.toList());
	}

	public List<Entity> getPrograms()
	{
		return Arrays.stream(entities).filter(e -> e.getType().equals("program")).collect(Collectors.toList());
	}

	public static class Entity
	{
		private String name = "";
		private String namespace = "";
		private String type = "";
		private String[] relatedEntity = null;

		public String getName()
		{
			return name;
		}

		public String getNamespace()
		{
			return namespace;
		}

		public String getType()
		{
			return type;
		}

		public String[] getRelatedEntity()
		{
			return relatedEntity;
		}

		public Entity(String name)
		{
			this.name = name;
		}
	}
	
	private IncludePath[] includePaths;

	public IncludePath[] getIncludePaths()
	{
		return includePaths;
	}

	public static class IncludePath
	{
		private String path;
		
		private boolean exists;

		public String getPath()
		{
			return path;
		}
		
		public boolean exists()
		{
			return exists;
		}
		
		public IncludePath(String path, boolean exists)
		{
			this.path = path;
			this.exists = exists;
		}
	}
}
