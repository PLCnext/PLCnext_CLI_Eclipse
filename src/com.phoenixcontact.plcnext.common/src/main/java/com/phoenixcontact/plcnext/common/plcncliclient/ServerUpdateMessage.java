/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.plcncliclient;

/**
 *
 */
public class ServerUpdateMessage extends ServerMessage
{
	
	public final static String topic_sdks = "sdks";
	public final static String topic_project_targets = "project-settings";
	public final static String topic_settings = "settings";
	
	public ServerUpdateMessage(Type type)
	{
		super(type);
	}

	private String updateTopic;
	private String project;
	
	public String getProject()
	{
		return project;
	}
	
	public String getUpdateTopic()
	{
		return updateTopic;
	}
}
