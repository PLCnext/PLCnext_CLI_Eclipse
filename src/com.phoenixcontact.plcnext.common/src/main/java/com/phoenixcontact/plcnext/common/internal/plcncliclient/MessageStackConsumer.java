/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.internal.plcncliclient;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.phoenixcontact.plcnext.common.Activator;
import com.phoenixcontact.plcnext.common.internal.plcncliclient.jsonmessage.*;
import com.phoenixcontact.plcnext.common.logging.Logger;
import com.phoenixcontact.plcnext.common.plcncliclient.*;
import com.phoenixcontact.plcnext.common.plcncliclient.ServerReplyMessage.ReplyType;

public class MessageStackConsumer implements Runnable
{
	private ConcurrentLinkedDeque<NamedPipeMessage> messageStack;
	private ConcurrentMap<ClientMessage, MessageListener> notRepliedMessages;

	private PipeErrorListener errorListener;

	Gson gson = new Gson();

	public MessageStackConsumer(ConcurrentLinkedDeque<NamedPipeMessage> messageStack, PipeErrorListener errorListener,
			ConcurrentMap<ClientMessage, MessageListener> notRepliedMessages)
	{
		this.messageStack = messageStack;
		this.notRepliedMessages = notRepliedMessages;
		this.errorListener = errorListener;
	}

	@Override
	public void run()
	{

		try
		{
			while (!Thread.interrupted())
			{
				
					NamedPipeMessage message = messageStack.pollFirst();
					if (message == null)
					{
						synchronized(messageStack)
						{
							messageStack.wait();
						}
						continue;
					}

					String messageString = message.getMessage();
					Logger.log("parsing message "+message.getId()+messageString);
					parseMessage(messageString);
				
			}
			Logger.log("message consumer stops working now");
		} catch (JsonSyntaxException e)
		{
			errorListener.onThreadThrowsExecutionException(new ExecutionException(e));
		} catch (InterruptedException e)
		{
			Logger.log("message consumer was interrupted and stops working now");
		}

	}

	public void parseMessage(String message) throws JsonSyntaxException
	{
		ServerMessage reply = null;
		try {
			reply = gson.fromJson(message, ServerMessage.class);
		}catch(JsonSyntaxException e)
		{
			Logger.log("Following reply could not be parsed:\n"+message);
			throw e;
		}
		List<ClientMessage> messagesToNotify = null;
		switch (reply.getType())
		{
		case reply:
			ServerReplyMessage replyMessage = gson.fromJson(message, ServerReplyMessage.class);
			ReplyType replyType = replyMessage.getInReplyTo();
			MessageType receivedInReplyTo = MessageType.valueOf(replyType.name());
			messagesToNotify = notRepliedMessages.keySet().stream().filter(m ->
			{
				JsonMessage mes = m.getJsonMessage();
				if (mes.getType().equals(receivedInReplyTo))
				{
					if (mes instanceof JsonCommandMessage)
					{
						JsonCommandMessage commandMessage = (JsonCommandMessage) mes;
						if (replyMessage.getCommand().trim().equals(commandMessage.getCommand().trim()))
						{
							return true;
						}
					} else if(mes instanceof JsonCancelMessage)
					{
						JsonCancelMessage cancelMessage = (JsonCancelMessage) mes;
						if (replyMessage.getCommand().trim().equals(cancelMessage.getCommand().trim()))
							{
								return true;
							}
						return false;
					}else if(mes instanceof JsonHandshakeMessage)
					{
						return true;
					}
				}
				return false;
			}).collect(Collectors.toList());
			for (ClientMessage m : messagesToNotify)
			{
				MessageListener listener = notRepliedMessages.remove(m);
				if (listener != null)
				{
					listener.onMessageReceived(m, replyMessage);
				}
			}

			break;
		case message:
			ServerMessageMessage messageMessage = gson.fromJson(message, ServerMessageMessage.class);
			messagesToNotify = notRepliedMessages.keySet().stream().filter(m ->
			{
				JsonMessage mes = m.getJsonMessage();
				if (mes instanceof JsonCommandMessage)
				{
					JsonCommandMessage commandMessage = (JsonCommandMessage) mes;
					if (messageMessage.getCommand().trim().equals(commandMessage.getCommand().trim()))
					{
						return true;
					}
				}
				return false;
			}).collect(Collectors.toList());
			for (ClientMessage m : messagesToNotify)
			{
				MessageListener listener = notRepliedMessages.get(m);
				if (listener != null)
				{
					listener.onMessageReceived(m, messageMessage);
				}
			}

			break;
		case progress:
			ServerProgressMessage progressMessage = gson.fromJson(message, ServerProgressMessage.class);
			messagesToNotify = notRepliedMessages.keySet().stream().filter(m ->
			{
				JsonMessage mes = m.getJsonMessage();
				if (mes instanceof JsonCommandMessage)
				{
					JsonCommandMessage commandMessage = (JsonCommandMessage) mes;
					if (progressMessage.getCommand().trim().equals(commandMessage.getCommand().trim()))
					{
						return true;
					}
				}
				return false;
			}).collect(Collectors.toList());
			for (ClientMessage m : messagesToNotify)
			{
				MessageListener listener = notRepliedMessages.get(m);
				if (listener != null)
				{
					listener.onMessageReceived(m, progressMessage);
				}
			}
			break;
		case update:
			ServerUpdateMessage updateMessage = gson.fromJson(message, ServerUpdateMessage.class);
			
			new UpdateMessageConsumer(updateMessage).schedule();
			break;
		case heartbeat:
			ServerHeartbeatMessage heartbeatMessage = gson.fromJson(message, ServerHeartbeatMessage.class);
			for(ClientMessage m : notRepliedMessages.keySet())
			{
				notRepliedMessages.get(m).onMessageReceived(m , heartbeatMessage);
			}
			break;
		default:
			Activator.getDefault().logWarning("Unknown message type will be ignored: " + reply.getType());
			break;

		}
	}

}
