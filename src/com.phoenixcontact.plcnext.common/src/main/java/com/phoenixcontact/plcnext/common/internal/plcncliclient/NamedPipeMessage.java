/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.internal.plcncliclient;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * 
 */
public class NamedPipeMessage
{

	public final static int messageLengthSize = 4;
	public final static int messageIDSize = 16;
	public final static int messageReceivedSize = 1;
	public final static int headerLength = messageLengthSize + messageIDSize + messageReceivedSize;
	private String message = null;
	private byte[] header = null;
	private int retryCounter = 0;
	private UUID id = null;
	private byte confirmation;
	
	public NamedPipeMessage(byte[] header, String message)
	{
		this.header = header;
		this.message = message;
		
		ByteBuffer headerByteBuffer = ByteBuffer.wrap(header);
		//skip 4 length bytes
		headerByteBuffer.position(headerByteBuffer.position() + NamedPipeMessage.messageLengthSize);

//		*************** Message ID ************************
		long mostSigBits = headerByteBuffer.getLong();
		long leastSigBits = headerByteBuffer.getLong();
		this.id = new UUID(mostSigBits, leastSigBits);
		
		this.confirmation = headerByteBuffer.get();
	}
	
	public NamedPipeMessage(String message)
	{
		this.message = message;
		this.confirmation = NamedPipeClient.noConfirmationFlag;
		
		//create message header
		ByteBuffer buffer = ByteBuffer.allocate(21);
		//length
		buffer.putInt(message.getBytes(NamedPipeClient.charset).length);
		//id
		this.id = UUID.randomUUID();
		buffer.putLong(id.getMostSignificantBits());
		buffer.putLong(id.getLeastSignificantBits());
		//
		buffer.put(confirmation);
		this.header = buffer.array();
	}
	
	public byte[] getHeader()
	{
		return header;
	}
	
	public String getMessage()
	{
		return message;
	}
	
	public int increaseCounter()
	{
		this.retryCounter++;
		return retryCounter;
	}
	
	public UUID getId()
	{
		return id;
	}
	
	public byte getConfirmation()
	{
		return confirmation;
	}
	
	public void resetCounter()
	{
		this.retryCounter = 0;
	}
	
	public int getCounter()
	{
		return retryCounter;
	}
	
	public static UUID getIdFromHeader(byte[] header)
	{
		ByteBuffer headerByteBuffer = ByteBuffer.wrap(header);
		//skip 4 length bytes
		headerByteBuffer.position(headerByteBuffer.position() + NamedPipeMessage.messageLengthSize);

//		*************** Message ID ************************
		long mostSigBits = headerByteBuffer.getLong();
		long leastSigBits = headerByteBuffer.getLong();
		return new UUID(mostSigBits, leastSigBits);
	}
}
