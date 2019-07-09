/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands.results;

import java.util.List;
import com.google.gson.JsonObject;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerMessageMessage;

public class GetSdksCommandResult extends CommandResult
{

	public GetSdksCommandResult(JsonObject reply, List<ServerMessageMessage> messages)
	{
		super(reply, messages);
	}
	
	public GetSdksCommandResult(Sdk[] sdks)
	{
		super((JsonObject)null, null);
		this.sdks = sdks;
	}
	
	private Sdk[] sdks;
	
	public Sdk[] getSdks()
	{
		return sdks;
	}
	
	public static class Sdk
	{
		private String path;
		
		public String getPath()
		{
			return path;
		}
		
		public Sdk(String path)
		{
			this.path = path;
		}
	}
	
	public static GetSdksCommandResult convertResultToJson(List<String> stdout)
	{
		if(stdout != null)
		{
			Sdk[] sdks = stdout.stream().filter(s -> s.trim().startsWith("-")).map(s -> new Sdk(s.trim().substring(1).trim())).toArray(Sdk[]::new); //$NON-NLS-1$
			
			
			return new GetSdksCommandResult(sdks);
		}
		return null;
	}

}
