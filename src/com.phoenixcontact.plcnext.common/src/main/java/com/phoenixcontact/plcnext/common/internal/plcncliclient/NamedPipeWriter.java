/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.internal.plcncliclient;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import com.phoenixcontact.plcnext.common.Activator;
import com.phoenixcontact.plcnext.common.logging.Logger;

public class NamedPipeWriter extends TimerTask /* implements Callable<Boolean> */
{
	private RandomAccessFile pipe;
	private NamedPipeMessage message = null;
	private ConcurrentMap<UUID, NamedPipeMessage> notConfirmedMessages;
	private ConcurrentMap<UUID, TimerTask> confirmationTimer;
	private Timer timer;
	NamedPipeClient client = null;

	private PipeErrorListener listener;

	protected NamedPipeWriter(RandomAccessFile pipe, NamedPipeMessage message,
			ConcurrentMap<UUID, NamedPipeMessage> notConfirmedMessages,
			ConcurrentMap<UUID, TimerTask> confirmationTimer, NamedPipeClient client)
	{
		this.pipe = pipe;
		this.message = message;
		this.notConfirmedMessages = notConfirmedMessages;
		this.confirmationTimer = confirmationTimer;
		this.timer = client.getTimer();
		this.client = client;
	}

	@Override
	public void run()
	{

		if (!notConfirmedMessages.containsKey(message.getId()))
			notConfirmedMessages.put(message.getId(), message);

		int counter = message.increaseCounter();

//		Logger.log(System.currentTimeMillis()+": start write task - from timer "+ message.getId().toString() + message.getMessage()+ " Attempt " + counter);

		if (counter > NamedPipeClient.maxRetrySendingCount)
		{
			Logger.log("No confirmation after " + (counter - 1) + " attempts to write message: " + message.getId().toString());

			new Thread(new Runnable()
			{

				@Override
				public void run()
				{
					listener.restartServer(client);
				}
			}).start();
			return;
		}
		NamedPipeWriter resendTask = new NamedPipeWriter(pipe, message, notConfirmedMessages, confirmationTimer,
				client);
		resendTask.registerListener(listener);
		confirmationTimer.put(message.getId(), resendTask);
		try
		{
			Logger.log("Wait for write lock: " + message.getId().toString() + " Attempt " + counter);
			synchronized (pipe)
			{
				Logger.log("Start write message: " + message.getId().toString() + message.getMessage() + " Attempt "
						+ counter);
				pipe.write(message.getHeader());
				Logger.log("wrote header");
				pipe.write(message.getMessage().getBytes());
				Logger.log("wrote message");
			}
			try
			{
				timer.schedule(resendTask, NamedPipeClient.maxConfirmationResponseTime);
//				Logger.log("scheduled write task");
			} catch (IllegalStateException e)
			{
				// timer was cancelled in the meantime, do nothing
			}

			Logger.log("End write message: " + message.getId().toString() + " Attempt " + counter);
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
//		Logger.log(System.currentTimeMillis()+": end write task - from timer");
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
