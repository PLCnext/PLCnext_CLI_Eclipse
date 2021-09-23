/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands.results;

import java.util.List;

import com.google.gson.JsonObject;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerMessageMessage;

public class GetSdksCommandResult extends CommandResult {

	public GetSdksCommandResult(JsonObject reply, List<ServerMessageMessage> messages)
	{
		super(reply, messages);
	}
	
	public GetSdksCommandResult()
	{
		super((JsonObject)null, null);
		
	}
	
	private SdkPath[] sdks;
	
	public SdkPath[] getSdkPaths() 
	{
		return sdks;
	}
	
	public static class SdkPath
	{
		private String path;
		
		private Target[] targets;

		public String getPath()
		{
			return path;
		}
		
		public Target[] getTargets()
		{
			return targets;
		}
		
		public SdkPath(String path, Target[] targets)
		{
			this.path = path;
			this.targets = targets;
		}
	}
}
