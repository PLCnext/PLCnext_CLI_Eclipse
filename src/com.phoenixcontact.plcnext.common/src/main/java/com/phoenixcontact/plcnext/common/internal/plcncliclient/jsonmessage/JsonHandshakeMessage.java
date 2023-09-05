/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.internal.plcncliclient.jsonmessage;


public class JsonHandshakeMessage extends JsonMessage
{
//	private ProtocolVersion protocolVersion;
	
	public JsonHandshakeMessage(int major, int minor)
	{
		super(MessageType.handshake);
//		this.protocolVersion = new ProtocolVersion(major, minor);
	}
	
//	class ProtocolVersion
//	{
//		private int major;
//		private int minor;
//		
//		public ProtocolVersion(int major, int minor)
//		{
//			this.major = major;
//			this.minor = minor;
//		}
//	}

}