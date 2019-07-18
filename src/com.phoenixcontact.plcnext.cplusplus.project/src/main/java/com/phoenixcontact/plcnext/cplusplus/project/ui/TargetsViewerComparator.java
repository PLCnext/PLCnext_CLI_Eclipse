/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.project.ui;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import com.phoenixcontact.plcnext.common.commands.results.Target;

public class TargetsViewerComparator extends ViewerComparator
{
	private final int EQUAL = 0;
	
	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		if(e1 instanceof Target
				&& e2 instanceof Target)
		{
			Target t1 = (Target) e1;
			Target t2 = (Target) e2;
			
			int result = t1.getName().compareTo(t2.getName());
			if(result == EQUAL)
			{
				return t1.getVersion().compareTo(t2.getVersion());
			}
			return result;
		}
		
		return super.compare(viewer, e1, e2);
	}
}
