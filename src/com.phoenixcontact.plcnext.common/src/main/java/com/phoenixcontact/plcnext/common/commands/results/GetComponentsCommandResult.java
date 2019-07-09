/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands.results;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerMessageMessage;

public class GetComponentsCommandResult extends CommandResult
{

	public GetComponentsCommandResult(JsonObject reply, List<ServerMessageMessage> messages)
	{
		super(reply, messages);
	}
	
	private Component[] components;
	
	public Component[] getComponents()
	{
		return components;
	}
	
	public GetComponentsCommandResult(Component[] components)
	{
		super((JsonObject)null, null);
		this.components = components;
	}
	
	public static class Component
	{
		private String name = "";
		private String namespace = "";
		
		public String getName()
		{
			return name;
		}
		
		public String getNamespace()
		{
			return namespace;
		}
		
		public Component(String name)
		{
			this.name = name;
		}
	}
	
	public static GetComponentsCommandResult convertResultToJson(List<String> stdout)
	{
		if (stdout != null)
		{
			List<Component> components = new ArrayList<Component>();
			for (String result : stdout) {
				if (result.trim().startsWith("-")) {
					String[] nameparts = result.split("::");
					String name = nameparts[nameparts.length - 1].trim();
					if(name.startsWith("-"))
						name = name.substring(1).trim();
					components.add(new Component(name));
				}
			}
			
			return new GetComponentsCommandResult(components.toArray(new Component[0]));
		}
		return null;
	}
}
