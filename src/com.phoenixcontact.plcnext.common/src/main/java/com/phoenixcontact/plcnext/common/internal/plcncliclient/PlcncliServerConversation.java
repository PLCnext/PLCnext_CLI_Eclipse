/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.internal.plcncliclient;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.activity.InvalidActivityException;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.phoenixcontact.plcnext.common.Activator;
import com.phoenixcontact.plcnext.common.CliDescription;
import com.phoenixcontact.plcnext.common.Messages;
import com.phoenixcontact.plcnext.common.PlcncliConsoleWindow;
import com.phoenixcontact.plcnext.common.ProcessExitedWithErrorException;
import com.phoenixcontact.plcnext.common.internal.plcncliclient.jsonmessage.MessageType;
import com.phoenixcontact.plcnext.common.logging.Logger;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerHeartbeatMessage;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerMessage;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerMessageMessage;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerProgressMessage;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerReplyMessage;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerReplyMessage.ReplyType;

@Creatable
@Singleton
public class PlcncliServerConversation implements PipeErrorListener, UncaughtExceptionHandler, MessageListener
{

	private enum HandshakeResult
	{
		Success, Fail, None
	}

	private NamedPipeClient client = null;
	ConcurrentMap<UUID, NamedPipeMessage> notConfirmedMessages = null;
	private Lock clientLock = new ReentrantLock();
	private Object mutex = new Object();
	private long maxWaitTimeForMessage = 520;
	private long waitTimeAfterKill = 520;
	private HandshakeResult handshakeResult = HandshakeResult.None;
	private Object waitForHandshake = new Object();
	private MessageConsoleStream out = null;
	private MessageConsoleStream err = null;
	private MessageConsoleStream warn = null;

	private ExecutorService pool = Executors.newCachedThreadPool();
	private Map<ClientMessage, ConcurrentLinkedQueue<ServerMessage>> receivedMessages = new ConcurrentHashMap<ClientMessage, ConcurrentLinkedQueue<ServerMessage>>();
	private CliDescription cliDescription;

	@Inject
	public PlcncliServerConversation(CliDescription cliDescription)
	{
		PlcncliConsoleWindow console = new PlcncliConsoleWindow();
		out = console.getOutConsole();
		err = console.getErrorConsole();
		warn = console.getWarningConsole();
		this.cliDescription = cliDescription;

		try
		{
			synchronized (mutex)
			{
				client = new NamedPipeClient(this, cliDescription);
				notConfirmedMessages = client.getNotConfirmedMessages();

				client.startReader(this);
			}
			pool.submit(new Runnable()
			{

				@Override
				public void run()
				{
					handshake();
				}
			});

		} catch (IOException e)
		{
			e.printStackTrace();
		} catch (ProcessExitedWithErrorException e)
		{
			synchronized (waitForHandshake)
			{
				handshakeResult = HandshakeResult.Fail;
				waitForHandshake.notifyAll();
			}
			Display.getDefault().syncExec(new Runnable()
			{
				public void run()
				{
					// starting message dialog on ui thread
					MessageDialog.openWarning(null, "Server could not  be started",
							e.getMessage() + "\n" + "Please check cli path and name and restart eclipse.");
					PreferencesUtil.createPreferenceDialogOn(null, Messages.CliPreferencePageId, null, null).open();
				}
			});
		}
	}

	@Override
	public void onThreadThrowsExecutionException(ExecutionException e)
	{
		if (!clientLock.tryLock())
		{
			Logger.log("could not aquire lock, do not restart bec. someone is restarting at the moment");
			return;
		}
		try
		{
			Logger.log("Thread threw exception");
//				e.printStackTrace();
			Logger.log("disconnect and restart");

			synchronized (mutex)
			{
				handshakeResult = HandshakeResult.None;

				receivedMessages.clear();

				if (client != null)
					client.close();
				try
				{
					client = new NamedPipeClient(this, cliDescription);
					notConfirmedMessages = client.getNotConfirmedMessages();
					client.startReader(this);

				} catch (IOException e1)
				{
					e1.printStackTrace();
				} catch (ProcessExitedWithErrorException e1)
				{
					synchronized (waitForHandshake)
					{
						handshakeResult = HandshakeResult.Fail;
						waitForHandshake.notifyAll();
					}
					Display.getDefault().syncExec(new Runnable()
					{
						public void run()
						{
							// starting message dialog on ui thread
							MessageDialog.openWarning(null, "Server could not  be started",
									e.getMessage() + "\n" + "Please check cli path and name and restart eclipse.");
							PreferencesUtil.createPreferenceDialogOn(null, Messages.CliPreferencePageId, null, null)
									.open();
						}
					});
					return;
				}
			}

		} finally
		{
			clientLock.unlock();
		}
		pool.submit(new Runnable()
		{

			@Override
			public void run()
			{
				handshake();
			}
		});
	}

	@Override
	public void restartServer(NamedPipeClient clientToRestart)
	{
		if (!clientLock.tryLock())
		{
			Logger.log("could not aquire lock, do not restart bec. someone is restarting at the moment");
			return;
		}
		try
		{
			if (!client.equals(clientToRestart))
			{
				return;
			}
			synchronized (mutex)
			{
				Logger.log("Restarting server ");

				handshakeResult = HandshakeResult.None;

				receivedMessages.clear();

				if (client != null)
					client.close();
				try
				{
					client = new NamedPipeClient(this, cliDescription);
					notConfirmedMessages = client.getNotConfirmedMessages();
					client.startReader(this);

				} catch (IOException e1)
				{
					e1.printStackTrace();
				} catch (ProcessExitedWithErrorException e)
				{
					synchronized (waitForHandshake)
					{
						handshakeResult = HandshakeResult.Fail;
						waitForHandshake.notifyAll();
					}
					Display.getDefault().syncExec(new Runnable()
					{
						public void run()
						{
							// starting message dialog on ui thread
							MessageDialog.openWarning(null, "Server could not  be started",
									e.getMessage() + "\n" + "Please check cli path and name and restart eclipse.");
							PreferencesUtil.createPreferenceDialogOn(null, Messages.CliPreferencePageId, null, null)
									.open();
						}
					});
					return;
				}
			}

		} finally
		{
			clientLock.unlock();
		}
		pool.submit(new Runnable()
		{

			@Override
			public void run()
			{
				handshake();
			}
		});
	}

	private boolean writeMessage(ClientMessage message, NamedPipeClient expectedClient, boolean listenForReply)
			throws InvalidActivityException
	{
		synchronized (mutex)
		{
			if (!client.equals(expectedClient))
				throw new InvalidActivityException("Client was restarted in the meantime.");
			if (listenForReply)
				client.getNotRepliedMessages().put(message, this);
			if (client.writeMessage(message, this))
				return true;
			if (listenForReply)
				client.getNotRepliedMessages().remove(message);
			return false;
		}
	}

	public void handshake()
	{
		int major = 1;
		int minor = 0;
		ClientMessage message = ClientMessage.newHandshakeMessage(major, minor);
		NamedPipeClient client = this.client;

		try
		{
			if (!writeMessage(message, client, true))
			{
				Logger.log("Handshake stopped.");
				synchronized (waitForHandshake)
				{
					handshakeResult = HandshakeResult.Fail;
					waitForHandshake.notifyAll();
				}
				return;
			}

			Map.Entry<ServerReplyMessage, List<ServerMessageMessage>> reply = waitForResponse(message, null, client,
					null, false);

			if (reply.getKey().isSuccess())
			{
				try
				{
					int receivedMajor = reply.getKey().getReply().getAsJsonObject().get("supportedProtocolVersions")
							.getAsJsonArray().get(0).getAsJsonObject().get("major").getAsInt();
					int receivedMinor = reply.getKey().getReply().getAsJsonObject().get("supportedProtocolVersions")
							.getAsJsonArray().get(0).getAsJsonObject().get("minor").getAsInt();
					if (receivedMajor == major && receivedMinor == minor)
					{
						Logger.log("---- Handshake success! ----");

						synchronized (waitForHandshake)
						{
							handshakeResult = HandshakeResult.Success;
							waitForHandshake.notifyAll();
						}
						return;
					}
					Activator.getDefault().logError("Handshake with server failed.", null);
				} catch (Exception e)
				{
					Activator.getDefault().logError("Handshake with server failed. \n"
							+ "Could not retrieve protocol version from handshake reply", null);
				}
			}
			client.close();
			Display.getDefault().asyncExec(new Runnable()
			{

				@Override
				public void run()
				{
					ErrorDialog.openError(null, "Incompatible plcncli server",
							"Protocol version of plcncli server is incompatible with this plugins protocol version.\n"
									+ "Plugin won't work properly.",
							new Status(Status.ERROR, Activator.PLUGIN_ID,
									"Supported version of plugin is " + major + "." + minor));

				}
			});
			synchronized (waitForHandshake)
			{
				handshakeResult = HandshakeResult.Fail;
				waitForHandshake.notifyAll();
			}
		} catch (InvalidActivityException e)
		{
			Logger.log(e.getMessage());
		}

	}

	public Map.Entry<ServerReplyMessage, List<ServerMessageMessage>> command(String command, IProgressMonitor monitor,
			boolean logging, boolean clearConsole)
	{
		if (clearConsole)
			out.getConsole().clearConsole();

		ClientMessage message = ClientMessage.newCommandMessage(command);
		NamedPipeClient client = this.client;
		boolean restarted = false;

		do
		{
			restarted = false;
			while (handshakeResult == HandshakeResult.None && (monitor == null || !monitor.isCanceled()))
			{
				try
				{
					synchronized (waitForHandshake)
					{
						waitForHandshake.wait(1000);
					}
				} catch (InterruptedException e)
				{
				}
			}
			if (handshakeResult == HandshakeResult.Fail)
			{
				if (monitor != null)
					monitor.done();
				Activator.getDefault().logError(
						"Command " + command + " could not be started because handshake with plcncli server failed.",
						null);
				return null;
			}
			try
			{
				if (monitor == null || !monitor.isCanceled())
				{
					if (!writeMessage(message, client, true))
					{
						restarted = true;
						client = this.client;
						continue;
					}

					Map.Entry<ServerReplyMessage, List<ServerMessageMessage>> reply = waitForResponse(message, command,
							client, monitor, logging);

					Logger.log("---- Command finished! ----");
					return reply;
				}

			} catch (InvalidActivityException e)
			{
				restarted = true;
				client = this.client;
				message.resetCounter();
			}
		} while (restarted);

		return null;
	}

	private Map.Entry<ServerReplyMessage, List<ServerMessageMessage>> waitForResponse(ClientMessage sentMessage,
			String command, NamedPipeClient expectedClient, IProgressMonitor monitor, boolean logging)
			throws InvalidActivityException
	{
		SubMonitor subMonitor = SubMonitor.convert(monitor, 1000);
		List<ServerMessageMessage> messages = new ArrayList<ServerMessageMessage>();

		messageloop:
		while (true)
		{

			// at first check that message was confirmed
			while (notConfirmedMessages.containsValue(sentMessage))
			{
				try
				{
					synchronized (notConfirmedMessages)
					{
						notConfirmedMessages.wait(1000);
					}
				} catch (InterruptedException e)
				{
				}
				Logger.log(sentMessage.getId() + " Waiting for message confirmation");
			}
			
			Logger.log(sentMessage.getId() + " Waiting for message");
			
			if (subMonitor != null && subMonitor.isCanceled())
			{
				Logger.log("user cancelled command - starting cancel routine 1");
				ClientMessage cancelMessage = ClientMessage.newCancelMessage(command);
				commandCanceledByUser(subMonitor, cancelMessage, expectedClient, logging);
				client.getNotRepliedMessages().remove(sentMessage);
				receivedMessages.remove(sentMessage);
				return null;
			}
			if (receivedMessages.get(sentMessage) == null || receivedMessages.get(sentMessage).isEmpty())
			{

				synchronized (sentMessage)
				{
					try
					{
						sentMessage.wait(maxWaitTimeForMessage);
					} catch (InterruptedException e)
					{
					}
				}
				if (subMonitor != null && subMonitor.isCanceled())
				{
					Logger.log("user cancelled command - starting cancel routine 2");
					ClientMessage cancelMessage = ClientMessage.newCancelMessage(command);
					commandCanceledByUser(subMonitor, cancelMessage, expectedClient, logging);
					client.getNotRepliedMessages().remove(sentMessage);
					receivedMessages.remove(sentMessage);
					return null;
				}
			}

			ServerMessage message = null;
			if (receivedMessages.get(sentMessage) != null)
				message = receivedMessages.get(sentMessage).poll();
			if (message == null)
			{
				// case no message
				Logger.log(sentMessage.getMessage() + " no message found");
				if (sentMessage.getJsonMessage().getType().equals(MessageType.command))
				{
					Logger.log(sentMessage.getMessage() + "send cancel bec no message arrived");
					// send cancel, if current message is command
					ClientMessage cancelMessage = ClientMessage.newCancelMessage(command);
					writeMessage(cancelMessage, expectedClient, true);
					Logger.log("Waiting for cancel confirmation");
					while (true)
					{
						synchronized (notConfirmedMessages)
						{
							while (notConfirmedMessages.containsValue(cancelMessage))
							{

								try
								{

									notConfirmedMessages.wait(1000);

								} catch (InterruptedException e)
								{
								}

							}
						}
						Logger.log("Waiting for message after cancel was sent");
						synchronized (cancelMessage)
						{
							try
							{
								// wait 500ms for cancel reply
								cancelMessage.wait(maxWaitTimeForMessage);
							} catch (InterruptedException e)
							{
							}
						}

						if (receivedMessages.get(cancelMessage) != null)
							message = receivedMessages.get(cancelMessage).poll();

						if (message != null)
						{
							Logger.log(sentMessage.getMessage() + " message arrived: " + message.getType());
							// reply arrived: restart command
							if (message instanceof ServerReplyMessage)
							{
								if (((ServerReplyMessage) message).getInReplyTo().equals(ReplyType.cancel))
								{
									client.getNotRepliedMessages().remove(sentMessage);
									receivedMessages.remove(sentMessage);
									receivedMessages.remove(cancelMessage);
									if (subMonitor != null && subMonitor.isCanceled())
									{
										Logger.log("user cancelled command - 3");
										return null;
									}
									messages.clear();
									writeMessage(sentMessage, expectedClient, true);
									continue messageloop;
								}
								// command reply arrived in the meantime, accept it
								if (((ServerReplyMessage) message).getInReplyTo().equals(ReplyType.command))
								{
									client.getNotRepliedMessages().remove(cancelMessage);
									receivedMessages.remove(sentMessage);
									receivedMessages.remove(cancelMessage);
									ServerReplyMessage reply = (ServerReplyMessage) message;
									if (subMonitor != null)
										subMonitor.done();
									return new AbstractMap.SimpleEntry<ServerReplyMessage, List<ServerMessageMessage>>(
											reply, messages);
								}
							}
						}
						break;
					}
					// no reply: remove cancel from not responded
					client.getNotRepliedMessages().remove(cancelMessage);
				}

				// no reply: send kill
				Logger.log("No message arrived within the expected time -> kill");
				writeMessage(ClientMessage.killMessage(), expectedClient, false);
				try
				{
					// give server time to shutdown
					Thread.sleep(waitTimeAfterKill);
				} catch (InterruptedException ex)
				{
				}

				restartServer(expectedClient);
				if (subMonitor != null && subMonitor.isCanceled())
				{
					Logger.log("user cancelled command - 4");
					client.getNotRepliedMessages().remove(sentMessage);
					return null;
				}
				messages.clear();
				writeMessage(sentMessage, expectedClient, true);
			}
			Logger.log(sentMessage.getMessage() + " message arrived: " + (message != null ? message.getType() : ""));
			if (message instanceof ServerReplyMessage)
			{
				ServerReplyMessage reply = (ServerReplyMessage) message;
				if (subMonitor != null)
					subMonitor.done();
				receivedMessages.remove(sentMessage);
				return new AbstractMap.SimpleEntry<ServerReplyMessage, List<ServerMessageMessage>>(reply, messages);
			} else if (message instanceof ServerProgressMessage)
			{
				ServerProgressMessage progress = (ServerProgressMessage) message;

				if (subMonitor != null)
				{
					String progress_message = progress.getProgressMessage();
					if (progress_message != null && !progress_message.isEmpty())
						subMonitor.setTaskName(progress_message);

					int max_progress = progress.getProgressMaximum();
					int current_progress = progress.getProgress();

					// check for infinite progress
					if (max_progress == Integer.MIN_VALUE)
					{
						max_progress = 100;
						current_progress = 1;
					}

					subMonitor.setWorkRemaining(max_progress);
					subMonitor.worked(current_progress);

				}
			} else if (message instanceof ServerMessageMessage)
			{

				ServerMessageMessage m = (ServerMessageMessage) message;
				Logger.log(m.getMessageType() + ": " + m.getMessage());
				if (logging)
				{
					switch (m.getMessageType()) {
					case error:
						err.println(m.getMessage());
						break;
					case information:
						out.println(m.getMessage());
						break;
					case verbose:
						break;
					case warning:
						warn.println(m.getMessage());
						break;
					default:
						Activator.getDefault().logWarning("A new message type was detected: "
								+ m.getMessageType().name() + "\nMessageContent: " + m.getMessage());
						break;
					}
				}
				messages.add(m);
			}
		}
	}

	private void commandCanceledByUser(SubMonitor subMonitor, ClientMessage cancelMessage,
			NamedPipeClient expectedClient, boolean logging) throws InvalidActivityException
	{
		// send cancel

		if (writeMessage(cancelMessage, expectedClient, true))
		{
			while (true)
			{
				while (notConfirmedMessages.containsValue(cancelMessage))
				{
					try
					{
						synchronized (notConfirmedMessages)
						{
							notConfirmedMessages.wait(1000);
						}

					} catch (InterruptedException e)
					{
					}
					Logger.log("Waiting for cancel confirmation");
				}
				Logger.log("Waiting for message after cancel was sent");
				try
				{
					// wait 500ms for cancel reply
					synchronized (cancelMessage)
					{
						cancelMessage.wait(maxWaitTimeForMessage);
					}

				} catch (InterruptedException e)
				{
				}

				ServerMessage message = null;
				if (receivedMessages.get(cancelMessage) != null)
					message = receivedMessages.get(cancelMessage).poll();

				if (message != null)
				{
					Logger.log(cancelMessage.getMessage() + " message arrived: " + message.getType());
					// cancel reply arrived
					if (message instanceof ServerReplyMessage)
					{
						if (((ServerReplyMessage) message).getInReplyTo().equals(ReplyType.cancel))
						{
							return;
						}
					} else if (message instanceof ServerHeartbeatMessage)
					{
						continue;
					} else if (message instanceof ServerMessageMessage)
					{

						ServerMessageMessage m = (ServerMessageMessage) message;
						Logger.log(m.getMessageType() + ": " + m.getMessage());
						if (logging)
						{
							switch (m.getMessageType()) {
							case error:
								err.println(m.getMessage());
								break;
							case information:
								out.println(m.getMessage());
								break;
							case verbose:
								break;
							case warning:
								warn.println(m.getMessage());
								break;
							default:
								Activator.getDefault().logWarning("A new message type was detected: "
										+ m.getMessageType().name() + "\nMessageContent: " + m.getMessage());
								break;
							}
						}
						continue;
					}
					Logger.log("This should not happen, received message :" + message.getType());
				}

				// did not receive cancel
				Logger.log("Tried to cancel command but did not get a reply so kill.");
				if (writeMessage(ClientMessage.killMessage(), expectedClient, false))
				{
					try
					{
						// wait for 500ms
						Thread.sleep(waitTimeAfterKill);
					} catch (InterruptedException ex)
					{
					}
					restartServer(expectedClient);
				}
			}
		}
	}

	@Override
	public void uncaughtException(Thread t, Throwable e)
	{
//		if (Constants.DEBUG)
//		{
//			Logger.log("Thread " + t + "threw an uncaught exception");
//			e.printStackTrace();
//		} else
		Activator.getDefault().logError("Thread " + t + "threw an uncaught exception", e);
	}

	@Override
	public void onMessageReceived(ClientMessage clientMessage, ServerMessage serverMessage)
	{
		synchronized (clientMessage)
		{
			if (!receivedMessages.containsKey(clientMessage))
				receivedMessages.put(clientMessage, new ConcurrentLinkedQueue<>());
			this.receivedMessages.get(clientMessage).add(serverMessage);
			clientMessage.notifyAll();
		}
	}
}
