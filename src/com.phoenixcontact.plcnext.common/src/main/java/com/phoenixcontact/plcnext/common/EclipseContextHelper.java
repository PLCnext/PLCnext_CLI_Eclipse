/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.ui.PlatformUI;

/**
 * Helper class for accessing the eclipse context
 * (needed for dependency injection)
 *
 */
public final class EclipseContextHelper {
	
	/**
	 * @return the active eclipse context, or null if it can't be found
	 */
	public static IEclipseContext getActiveContext() {
		IEclipseContext context = getWorkbenchContext();
		return context == null ? null : context.getActiveLeaf();
	}
	
	private static IEclipseContext getWorkbenchContext() {
		return PlatformUI.getWorkbench().getService(IEclipseContext.class);
	}
}
