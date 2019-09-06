/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.toolchains.internal;

import org.eclipse.core.commands.Command;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

public class Startup implements IStartup
{

	@Override
	public void earlyStartup()
	{
		ICommandService service = PlatformUI.getWorkbench().getService(ICommandService.class);
		Command command = service.getCommand(IWorkbenchCommandConstants.FILE_SAVE);
		command.addExecutionListener(new SaveCMakeListsListener());

	}

}
