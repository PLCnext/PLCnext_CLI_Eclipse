/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.internal.plcncliclient;

import java.util.concurrent.ExecutionException;

/**
 *
 */
public interface PipeErrorListener
{
	void onThreadThrowsExecutionException(ExecutionException e);
	
	void restartServer(NamedPipeClient expectedClient);
}
