/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.internal.plcncliclient;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.phoenixcontact.plcnext.common.Activator;
import com.phoenixcontact.plcnext.common.CliDescription;
import com.phoenixcontact.plcnext.common.CliNotExistingException;
import com.phoenixcontact.plcnext.common.Messages;
import com.phoenixcontact.plcnext.common.ProcessExitedWithErrorException;
import com.phoenixcontact.plcnext.common.clicheck.CliAvailabilityChecker;
import com.phoenixcontact.plcnext.common.logging.Logger;

public class NamedPipeClient implements Closeable, PropertyChangeListener
{
	private Process process = null;
	private ExecutorService pool;
	private RandomAccessFile readFile = null;
	private RandomAccessFile writeFile = null;
	private ConcurrentLinkedDeque<NamedPipeMessage> messageStack = null;
	private Future<?> readFuture = null;
	private Future<?> messageStackConsumerFuture = null;
	private Timer timer = new Timer();
	private MessageStackConsumer messageConsumer = null;

	public final static int maxRetrySendingCount = 3;
	public final static int maxConfirmationResponseTime = 120;
	public final static Charset charset = StandardCharsets.UTF_8;
	public final static byte successConfirmationFlag = 0x01;
	public final static byte errorConfirmationFlag = (byte) 0xFF;
	public final static byte noConfirmationFlag = 0x00;

	private ConcurrentMap<UUID, NamedPipeMessage> notConfirmedMessages = new ConcurrentHashMap<>();
	private ConcurrentMap<UUID, TimerTask> confirmationTimer = new ConcurrentHashMap<>();
	private PipeErrorListener listener;
	private ConcurrentMap<ClientMessage, MessageListener> notRepliedMessages = new ConcurrentHashMap<>();
	private CliDescription cliInformation;
	private String cliLocation;
	private String cliName;
	private CliAvailabilityChecker cliChecker;

	public NamedPipeClient(PipeErrorListener listener, CliDescription plcncliInfo) throws IOException, ProcessExitedWithErrorException
	{
		this.listener = listener;
		this.cliInformation = plcncliInfo;
		cliInformation.addPropertyChangeListener(this);
		cliLocation = cliInformation.getCliPath();
		cliName = cliInformation.getCliName();
		cliChecker = new CliAvailabilityChecker(cliInformation);
		messageStack = new ConcurrentLinkedDeque<NamedPipeMessage>();

		// *********create thread pool**************
		pool = Executors.newCachedThreadPool();

		try
		{

			String pipeName = startServer();

			connectAsClient(pipeName);

		} catch (IOException e)
		{
			close();
			throw e;
		}
	}

	private String startServer() throws IOException, CliNotExistingException
	{
		String pipeName = UUID.randomUUID().toString() + "-plcncli-server";
		String pipeFullName = pipeName;
		if (!Platform.getOS().equals(Platform.OS_LINUX))
		{
			pipeFullName = "\\\\.\\pipe\\" + pipeName;
		}
		
		// check for cli availability
		if (!cliInformation.cliExists())
		{

			IStatus checkerStatus = cliChecker.checkAvailability();
			if (!checkerStatus.isOK())
			{
				Display.getDefault().syncExec(new Runnable()
				{
					public void run()
					{
						// starting message dialog on ui thread
						MessageDialog.openWarning(null, Messages.CliNotExistingExceptionMessage,
								Messages.CliNotExistingExceptionCheckPreferencesMessage);
						PreferencesUtil.createPreferenceDialogOn(null, Messages.CliPreferencePageId, null, null).open();
					}
				});
			}
		}

		if (!cliInformation.cliExists())
		{
			Display.getDefault().syncExec(new Runnable()
			{
				public void run()
				{
					// starting message dialog on ui thread
					MessageDialog.openError(null, Messages.CliNotExistingExceptionMessage,
							Messages.CliNotExistingExceptionPluginCorruptMessage);
				}
			});
			throw new CliNotExistingException();
		}

		String serverExecutablePath = new File(cliLocation, cliName).getPath();

		ProcessBuilder builder = new ProcessBuilder(serverExecutablePath, "start-server", "-h", "--verbose", "-t", "-n",
				pipeName);

		command = builder.command().stream().collect(Collectors.joining(" "));
		process = builder.start();
		return pipeFullName;
	}
	
	private String command = "";

	private void connectAsClient(String pipeName) throws ProcessExitedWithErrorException
	{
		Logger.log("Waiting for server to start...");
		while (readFile == null)
		{
			if (!process.isAlive())
			{
				throw new ProcessExitedWithErrorException("Server could not be started with the following command: "+command);
			}
			try
			{
				readFile = new RandomAccessFile(pipeName + "\\server-output", "r");
				Logger.log("Connected to read pipe " + pipeName);

			} catch (FileNotFoundException e)
			{
				try
				{
					Thread.sleep(100);
				} catch (InterruptedException e1)
				{
				}
			}
		}
		while (writeFile == null)
		{
			try
			{
				writeFile = new RandomAccessFile(pipeName + "\\server-input", "rw");
				Logger.log("Connected to write pipe " + pipeName);

			} catch (FileNotFoundException e)
			{
				try
				{
					Thread.sleep(100);
				} catch (InterruptedException e1)
				{
				}
			}
		}
	}

	public void startReader(UncaughtExceptionHandler handler)
	{
		NamedPipeReader reader = new NamedPipeReader(this, readFile, writeFile, messageStack, notConfirmedMessages,
				confirmationTimer);
		reader.registerListener(listener);
		reader.setUncaughtExceptionHandler(handler);
		readFuture = pool.submit(reader);

		startMessageConsumer();
	}

	public boolean writeMessage(NamedPipeMessage message, UncaughtExceptionHandler handler)
	{
		NamedPipeWriter writer = new NamedPipeWriter(writeFile, message, notConfirmedMessages, confirmationTimer, this);
		writer.registerListener(listener);
		writer.setUncaughtExceptionHandler(handler);

		Future<?> writeFuture = pool.submit(writer);
		try
		{
			// TODO enter correct timeout
			writeFuture.get(4000, TimeUnit.MILLISECONDS);
			return true;

		} catch (ExecutionException | TimeoutException | InterruptedException e)
		{
			writeFuture.cancel(true);
			if (e instanceof ExecutionException)
			{
				Activator.getDefault().logError("could not write to server due to the following exception", e);
			}
			if (e instanceof TimeoutException)
			{
				Logger.log("TIMEOUT for writing: " + message.getId().toString()
							+ message.getMessage() + " Attempt " + message.getCounter());
			}
			return false;
		}
	}

	public Timer getTimer()
	{
		return timer;
	}

	public void close()
	{

		if (readFuture != null)
		{
			readFuture.cancel(true);
		}
		if (messageStackConsumerFuture != null)
		{
			messageStackConsumerFuture.cancel(true);
		}

		if (process != null)
			process.destroy();

		if (readFile != null)
		{
			try
			{
				readFile.close();
			} catch (IOException e)
			{
//				Activator.getDefault().logError("Could not close read file", e);
				e.printStackTrace();
			}
		}

		if (writeFile != null)
		{
			try
			{
				writeFile.close();
			} catch (IOException e)
			{
//				Activator.getDefault().logError("Could not close file", e);
				e.printStackTrace();
			}
		}

		if (pool != null)
			pool.shutdownNow();
		if (timer != null)
			timer.cancel();
		
		cliInformation.removePropertyChangeListener(this);
	}

	public ConcurrentLinkedDeque<NamedPipeMessage> getMessageStack()
	{
		return messageStack;
	}

	private void startMessageConsumer()
	{
		messageConsumer = new MessageStackConsumer(messageStack, listener, notRepliedMessages);
		messageStackConsumerFuture = pool.submit(messageConsumer);
	}

	public ConcurrentMap<ClientMessage, MessageListener> getNotRepliedMessages()
	{
		return notRepliedMessages;
	}
	
	public ConcurrentMap<UUID, NamedPipeMessage> getNotConfirmedMessages()
	{
		return notConfirmedMessages;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		cliLocation = cliInformation.getCliPath();
		cliName = cliInformation.getCliName();
	}
}
