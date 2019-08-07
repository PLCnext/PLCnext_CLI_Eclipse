/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.commands.results;

import java.util.List;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerMessageMessage;

public class GetSettingCommandResult extends CommandResult
{

	public GetSettingCommandResult(JsonObject reply, List<ServerMessageMessage> messages)
	{
		super(reply, messages);
	}
	
	public GetSettingCommandResult(Setting setting)
	{
		super((JsonObject)null, null);
		this.setting = setting;
	}

	private Setting setting;
	
	public Setting getSetting()
	{
		return setting;
	}
	
	public class Setting
	{
		@SerializedName("UseSystemCommands")
		private boolean useSystemCommands;
		
		@SerializedName("SdkPaths")
		private String[] sdkPaths;
		
		@SerializedName("TemplateLocations")
		private String[] templateLocations;
		
		@SerializedName("AttributePrefix")
		private String attributePrefix;
		
		@SerializedName("CliRepositoryRoot")
		private String cliRepositoryRoot;
		
		@SerializedName("CliRepositoryFileName")
		private String cliRepositoryFileName;
		
		@SerializedName("CliRepositorySignatureFileName")
		private String cliRepositorySignatureFileName;
		
		@SerializedName("HttpProxy")
		private String httpProxy;
		
		@SerializedName("LogFilePath")
		private String logFilePath;
		
		public boolean getUseSystemCommands()
		{
			return useSystemCommands;
		}
		
		public String[] getSdkPaths()
		{
			return sdkPaths;
		}
		
		public String[] getTemplateLocations()
		{
			return templateLocations;
		}
		
		public String getAttributePrefix()
		{
			return attributePrefix;
		}
		
		public String getCliRepositoryFileName()
		{
			return cliRepositoryFileName;
		}
		
		public String getCliRepositoryRoot()
		{
			return cliRepositoryRoot;
		}
		
		public String getCliRepositorySignatureFileName()
		{
			return cliRepositorySignatureFileName;
		}
		
		public String getHttpProxy()
		{
			return httpProxy;
		}
		
		public String getLogFilePath()
		{
			return logFilePath;
		}
	}
}
