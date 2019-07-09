/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.internal.plcncliclient.jsonmessage;

public enum MessageType
{
	handshake,
	command,
	cancel,
	kill
}
