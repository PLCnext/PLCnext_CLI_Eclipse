/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.internal.plcncliclient;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharsetDecoder;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import com.phoenixcontact.plcnext.common.Activator;
import com.phoenixcontact.plcnext.common.logging.Logger;

public class NamedPipeReader implements Runnable
{
	private RandomAccessFile readFile;
	private RandomAccessFile writeFile;
	private final static int bufferSize = 4096;
	private CharsetDecoder decoder;
	private Timer timer;

	private Deque<NamedPipeMessage> messageStack;
	private ConcurrentMap<UUID, NamedPipeMessage> notConfirmedMessages;
	private ConcurrentMap<UUID, TimerTask> confirmationTimer;

	private PipeErrorListener listener = null;
	private NamedPipeClient client = null;
	private Map<UUID, String> messageBufferForSplitMessages = new HashMap<UUID, String>();

	/**
	 * reads messages forever and puts received messages onto stack
	 * 
	 * @param client
	 * @param readFile             the file to read from
	 * @param writeFile            the file to write to
	 * @param messageStack         the stack to put messages onto
	 * @param notConfirmedMessages
	 * @param confirmationTimer
	 */
	public NamedPipeReader(NamedPipeClient client, RandomAccessFile readFile, RandomAccessFile writeFile,
			Deque<NamedPipeMessage> messageStack, ConcurrentMap<UUID, NamedPipeMessage> notConfirmedMessages,
			ConcurrentMap<UUID, TimerTask> confirmationTimer)
	{
		this.readFile = readFile;
		this.writeFile = writeFile;
		this.decoder = NamedPipeClient.charset.newDecoder();
		this.timer = client.getTimer();
		this.client = client;

		this.messageStack = messageStack;
		this.notConfirmedMessages = notConfirmedMessages;
		this.confirmationTimer = confirmationTimer;
	}

	@Override
	public void run()
	{
		try
		{
			Logger.log("start reader");
			FileChannel channel = readFile.getChannel();
			while (!Thread.interrupted())
			{

				if (readFile.length() == 0 || !channel.isOpen())
				{
					try {
						Thread.sleep(20);
					}
					catch (InterruptedException e) {
					}
					continue;
				}
				if (readFile.length() < NamedPipeMessage.headerLength)
				{
					throw new IllegalArgumentException("Expected message of length " + NamedPipeMessage.headerLength
							+ " but found " + readFile.length());
				}

				// **************read header **************************
//				Logger.log(System.currentTimeMillis()+": start reading header");
				int headerLength = NamedPipeMessage.headerLength;

				int bytesToRead = headerLength;
				int bytesRead = 0;
				byte[] header = new byte[headerLength];

				do
				{
					int result = readFile.read(header, bytesRead, bytesToRead);
					if (result == -1)
					{
						throw new IOException("Reached end of file while reading");
					}
					bytesRead += result;
					bytesToRead -= result;
				} while (bytesToRead > 0);

				// *************** Message length ************************

				ByteBuffer headerBuffer = ByteBuffer.wrap(header);
				int length = headerBuffer.getInt();

				// *************** Message or Confirmation? ************************

				// TODO test if split message works as expected
//				Logger.log("Finished reading header length="+length);
				if (length > 0)
				{
					messageBufferForSplitMessages.clear();
					String message = readMessage(length, header, false);

					UUID id = NamedPipeMessage.getIdFromHeader(header);
					if (messageBufferForSplitMessages.containsKey(id))
					{
						message = messageBufferForSplitMessages.remove(id) + message;
					}

					messageStack.push(new NamedPipeMessage(header, message));
					synchronized (messageStack)
					{
						messageStack.notifyAll();
					}
				} else if (length == -0x80000000)
				{
					String message = readMessage(length, header, true);
					UUID id = NamedPipeMessage.getIdFromHeader(header);
					if (messageBufferForSplitMessages.containsKey(id))
					{
						message = messageBufferForSplitMessages.get(id) + message;
					}
					messageBufferForSplitMessages.put(id, message);
				} else
					processConfirmationFlag(new NamedPipeMessage(header, null));

			}
		} catch (IOException e)
		{
			if (listener == null)
			{
				Activator.getDefault().logError("No listener registered", e);
			} else
			{
				listener.onThreadThrowsExecutionException(new ExecutionException(e));
				Logger.log("Killing reader");
			}
		}
	}

	private String readMessage(int length, byte[] header, boolean messageIsSplit) throws IOException
	{
		Logger.log("Received message header, start reading message of length " + length);
		int bytesToRead = length;
		CharBuffer charBuffer = CharBuffer.allocate(length);
		decoder.reset();

		while (bytesToRead > 0)
		{
			if (readFile.length() == 0)
			{

				continue;
			}
			byte[] buffer = new byte[Math.min(bytesToRead, bufferSize)];

			int result = readFile.read(buffer);
			if (result == -1)
			{
				throw new IOException("Reached end of file while reading");
			}
			bytesToRead -= result;
			ByteBuffer messageByteBuffer = ByteBuffer.wrap(buffer);

			if (bytesToRead > 0)
				decoder.decode(messageByteBuffer, charBuffer, false);
			else
				decoder.decode(messageByteBuffer, charBuffer, true);
		}
		if (!messageIsSplit)
			sendConfirmation(header);

		decoder.flush(charBuffer);
		charBuffer.flip();
		return charBuffer.toString();
	}

	private void sendConfirmation(byte[] header)
	{
//		Logger.log("Schedule confirmation " + NamedPipeMessage.getIdFromHeader(header));
		timer.schedule(new TimerTask()
		{

			@Override
			public void run()
			{
//				Logger.log("Starting confirmation task");
				try
				{
					byte[] confirmHeader = new byte[21];
					ByteBuffer confirmBuffer = ByteBuffer.wrap(header);
					confirmBuffer.getInt();
					confirmBuffer.get(confirmHeader, 4, 16);
					confirmHeader[20] = 1;
					writeFile.write(confirmHeader);
					Logger.log("Sent confirmation for " + NamedPipeMessage.getIdFromHeader(header));
				} catch (IOException e)
				{
					Logger.log("Could not send confirmation for " + NamedPipeMessage.getIdFromHeader(header)
							+ e.getMessage());
					if (listener == null)
					{
						Activator.getDefault().logError("No listener registered", e);
					} else
					{
						listener.onThreadThrowsExecutionException(new ExecutionException(e));
					}
				}
//				Logger.log("finish confirmation task");
			}
		}, 0);
	}

	private void processConfirmationFlag(NamedPipeMessage message) throws IOException
	{
		Logger.log("Received confirmation flag");
		UUID id = message.getId();
		byte confirmation = message.getConfirmation();

		switch (confirmation) {
		case NamedPipeClient.successConfirmationFlag:
			NamedPipeMessage m = notConfirmedMessages.remove(id);
			TimerTask timer = confirmationTimer.remove(id);

			if (timer != null)
			{
				timer.cancel();
			}

			if (m != null)
			{
				m.resetCounter();
				Logger.log("Confirmed message " + m.getId().toString());
			}
			synchronized(notConfirmedMessages)
			{
				notConfirmedMessages.notifyAll();
			}
			break;
		case NamedPipeClient.errorConfirmationFlag:
			checkAndResendMessage(id);
			break;
		default:
			// ignoring message
			break;
		}
	}

	private void checkAndResendMessage(UUID messageID) throws IOException
	{
		NamedPipeMessage message = notConfirmedMessages.get(messageID);
		if (message != null)
		{
			TimerTask task = confirmationTimer.remove(messageID);
			task.cancel();
			int counter = message.increaseCounter();
			if (counter < NamedPipeClient.maxRetrySendingCount)
			{
				timer.schedule(new TimerTask()
				{

					@Override
					public void run()
					{
						try
						{
							synchronized (writeFile)
							{
								writeFile.write(message.getHeader());

								writeFile.write(message.getMessage().getBytes());
							}
							NamedPipeWriter writeTask = new NamedPipeWriter(writeFile, message, notConfirmedMessages,
									confirmationTimer, client);
							writeTask.registerListener(listener);
							timer.schedule(writeTask, NamedPipeClient.maxConfirmationResponseTime);
						} catch (IOException e)
						{
							if (listener == null)
							{
								Activator.getDefault().logError("No listener registered", e);
							} else
							{
								listener.onThreadThrowsExecutionException(new ExecutionException(e));
							}
						}
					}
				}, 0);

				return;
			}
			Logger.log("Restarting server bec. received error confirmation after sending message " + messageID
					+ " 3 times");
			listener.restartServer(client);
			return;
			// unsuccessfully tried to send message max times -> disconnect
		}
		// should not happen, something went wrong
		throw new IllegalArgumentException("Could not find old message to resend.");
	}

	public void registerListener(PipeErrorListener listener)
	{
		this.listener = listener;
	}

	public void setUncaughtExceptionHandler(UncaughtExceptionHandler handler)
	{
		Thread.setDefaultUncaughtExceptionHandler(handler);
	}
}
