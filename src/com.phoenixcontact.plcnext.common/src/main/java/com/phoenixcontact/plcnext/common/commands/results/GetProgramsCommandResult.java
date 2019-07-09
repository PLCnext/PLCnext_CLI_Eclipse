/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands.results;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerMessageMessage;

public class GetProgramsCommandResult extends CommandResult
{

	public GetProgramsCommandResult(JsonObject reply, List<ServerMessageMessage> messages)
	{
		super(reply, messages);
	}
	
	public GetProgramsCommandResult(Program[] programs)
	{
		super((JsonObject)null, null);
		this.programs = programs;
	}
	
	private Program[] programs;
	
	public Program[] getPrograms()
	{
		return programs;
	}
	
	public static class Program
	{
		private String name;
		private String namespace;
		private String parent;
		private String parentNamespace;
		
		public String getName()
		{
			return name;
		}
		
		public String getNamespace()
		{
			return namespace;
		}
		
		public String getParent()
		{
			return parent;
		}
		
		public String getParentNamespace()
		{
			return parentNamespace;
		}
		
		public Program(String name)
		{
			this.name = name;
		}
	}
	
	public static GetProgramsCommandResult convertResultToJson(List<String> stdout)
	{
		if (stdout != null)
		{
			List<Program> programs = new ArrayList<Program>();
			for (String result : stdout) {
				if (result.trim().startsWith("-")) {
					String[] nameparts = result.split("::");
					String name = nameparts[nameparts.length - 1].trim();
					if(name.startsWith("-"))
						name = name.substring(1).trim();
					
					programs.add(new Program(name));
				}
			}
			
			return new GetProgramsCommandResult(programs.toArray(new Program[0]));
		}
		return null;
	}

}
