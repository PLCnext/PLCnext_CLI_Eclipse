/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.internal.plcncliclient;

import com.phoenixcontact.plcnext.common.plcncliclient.ServerMessage;

public interface MessageListener
{
	public void onMessageReceived(ClientMessage clientMessage, ServerMessage serverMessage);
}

