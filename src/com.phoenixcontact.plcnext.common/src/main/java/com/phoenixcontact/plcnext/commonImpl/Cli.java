/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.commonImpl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.phoenixcontact.plcnext.common.Activator;
import com.phoenixcontact.plcnext.common.CliDescription;
import com.phoenixcontact.plcnext.common.CliNotExistingException;
import com.phoenixcontact.plcnext.common.ICommandReceiver;
import com.phoenixcontact.plcnext.common.Messages;
import com.phoenixcontact.plcnext.common.ProcessExitedWithErrorException;
import com.phoenixcontact.plcnext.common.clicheck.CliAvailabilityChecker;
import com.phoenixcontact.plcnext.common.commands.Command;
import com.phoenixcontact.plcnext.common.commands.results.CommandResult;

/**
 * Describes the command line tool which can be set via eclipse preferences The
 * tool can execute commands via executeCommand and returns the console output
 * to the eclipse console of the development platform
 *
 */
@Creatable
@Singleton
public class Cli implements ICommandReceiver, PropertyChangeListener {

	private String cliLocation;
	private String cliName;
	private CliDescription cliInformation;
	private CliAvailabilityChecker cliChecker;

	/**
	 * @param cliInfo 
	 * 
	 */
	@Inject
	public Cli(CliDescription cliInfo) {
		this.cliInformation = cliInfo;
		cliInformation.addPropertyChangeListener(this);
		cliLocation = cliInformation.getCliPath();
		cliName = cliInformation.getCliName();
		cliChecker = new CliAvailabilityChecker(cliInformation);
	}

	public CommandResult executeCommand(Command command, IProgressMonitor monitor) throws ProcessExitedWithErrorException {
		return executeCommand(command, true, monitor);
	}
	
	public CommandResult executeCommand(Command command, boolean logging, IProgressMonitor monitor) throws ProcessExitedWithErrorException {
		return executeCommand(command, logging, false, monitor);
	}

	public CommandResult executeCommand(Command c, boolean logging, boolean clearConsole, IProgressMonitor monitor) throws ProcessExitedWithErrorException {
		if(monitor == null)
			monitor = new NullProgressMonitor();
		
		if (!cliInformation.cliExists()) {

			IStatus checkerStatus = cliChecker.checkAvailability();
			if (!checkerStatus.isOK()) 
			{
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						// starting message dialog on ui thread
						MessageDialog.openWarning(null, Messages.CliNotExistingExceptionMessage,
								Messages.CliNotExistingExceptionCheckPreferencesMessage);
						PreferencesUtil.createPreferenceDialogOn(null,
								Messages.CliPreferencePageId, null,
								null).open();
					}
				});
			}
		}
		
		if (!cliInformation.cliExists()) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					// starting message dialog on ui thread
					MessageDialog.openError(null, Messages.CliNotExistingExceptionMessage,
							Messages.CliNotExistingExceptionPluginCorruptMessage);
				}
			});
			throw new CliNotExistingException();
		}
		
		
		String command = "\"" + cliLocation + Path.SEPARATOR + cliName + "\" " + c.getExecutionCommand(); //$NON-NLS-1$ //$NON-NLS-2$
		String loggedCommand = "\"" + cliLocation + Path.SEPARATOR + cliName + "\" " + c.getLoggableExecutionCommand(); //$NON-NLS-1$ //$NON-NLS-2$


		MessageConsole myConsole = findConsole(Messages.CliConsoleName);
		if(clearConsole)
			myConsole.clearConsole();
		
		
		MessageConsoleStream out = myConsole.newMessageStream();
		MessageConsoleStream err = myConsole.newMessageStream();
		out.setActivateOnWrite(true);
		err.setActivateOnWrite(true);
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				err.setColor(new Color(Display.getCurrent(), 255, 0, 0));
			}});

		try {
			Process proc;
			ProcessBuilder builder;
			if (Platform.getOS().equals(Platform.OS_LINUX)) {
				if (logging) {
					out.println();
					out.println("/bin/sh -c " + loggedCommand); //$NON-NLS-1$
				}
				builder = new ProcessBuilder("/bin/sh", "-c", command); //$NON-NLS-1$ //$NON-NLS-2$

			} else {
				if (logging) {
					out.println();
					out.println("cmd.exe /c \"" + loggedCommand + "\""); //$NON-NLS-1$ //$NON-NLS-2$
				}
				builder = new ProcessBuilder("cmd.exe", "/c", "\"" + command + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

			}
			Activator.getDefault().logInfo("Executing "+loggedCommand);
			
			builder.directory(new Path(cliLocation).toFile());
			proc = builder.start();

			List<String> outputLines = new ArrayList<String>();
			List<String> errorLines = new ArrayList<String>();
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			BufferedReader error = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

			
			// *******consume stdout********************
			new Thread(new Runnable()
			{
				
				@Override
				public void run()
				{
					try
					{
						String s = null;
						while ((s = stdInput.readLine()) != null) {
							outputLines.add(s);
							if (logging)
								out.println(s);
						}
					} catch (IOException e)
					{
						Activator.getDefault().logError(Messages.errorExecutingCli, e);
					}
				}
			}).start();
			
			// *******consume error output********************
			new Thread(new Runnable()
			{
				
				@Override
				public void run()
				{
					try
					{
						String s = null;
						while ((s = error.readLine()) != null) {
							errorLines.add(s);
							if (logging)
								err.println(s);
						}
					} catch (IOException e)
					{
						Activator.getDefault().logError(Messages.errorExecutingCli, e);
					}
				}
			}).start();
			
			try {
				while(!proc.waitFor(100, TimeUnit.MILLISECONDS))
				{
					if(monitor.isCanceled())
					{
						proc.descendants().forEach(p -> p.destroy());
						proc.destroy();
						err.println("canceled command execution");
						break;
					}
				}
				
				
				if (proc.exitValue() != 0) {
					throw new ProcessExitedWithErrorException(loggedCommand, outputLines, errorLines);
				}
			} catch (InterruptedException e) {
				Activator.getDefault().logError("Error while waiting for cli to finish", e);
			}
			
			return new CommandResult(outputLines, errorLines);

		} catch (IOException e) {
			Activator.getDefault().logError(Messages.errorExecutingCli, e);
		}

		return null;
	}

	private MessageConsole findConsole(String name) {
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

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		cliLocation = cliInformation.getCliPath();
		cliName = cliInformation.getCliName();
	}
}
