/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class PlcncliConsoleWindow
{
	private MessageConsoleStream out = null;
	private MessageConsoleStream err = null;
	private MessageConsoleStream warn = null;

	public PlcncliConsoleWindow()
	{
		MessageConsole myConsole = findConsole(Messages.CliConsoleName);
		out = myConsole.newMessageStream();
		err = myConsole.newMessageStream();
		warn = myConsole.newMessageStream();
		out.setActivateOnWrite(true);
		warn.setActivateOnWrite(true);
		err.setActivateOnWrite(true);
		Display.getDefault().syncExec(new Runnable()
		{
			public void run()
			{
				err.setColor(new Color(Display.getCurrent(), 255, 0, 0));
				warn.setColor(new Color(Display.getCurrent(), 255, 255, 0));
			}
		});
	}
	
	private MessageConsole findConsole(String name)
	{
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}
	
	public MessageConsoleStream getOutConsole()
	{
		return out;
	}
	
	public MessageConsoleStream getErrorConsole()
	{
		return err;
	}
	
	public MessageConsoleStream getWarningConsole()
	{
		return warn;
	}
}
