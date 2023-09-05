/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.project.ui;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import com.phoenixcontact.plcnext.common.Messages;
import com.phoenixcontact.plcnext.common.commands.results.GetProjectInformationCommandResult.ProjectTarget;
import com.phoenixcontact.plcnext.common.commands.results.Target;

public class TargetLabelProvider extends ColumnLabelProvider
{
	
	private ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());
	
	
	@Override
	public String getText(Object element)
	{
		if(element instanceof Target)
		{
			Target target = (Target) element;
			return target.getDisplayName();
		}
		return super.getText(element);
	}
	
	@Override
	public Color getForeground(Object element)
	{
		if (element instanceof ProjectTarget)
		{
			ProjectTarget target = (ProjectTarget) element;
			
			if (!target.isAvailable())
			{
				return resourceManager.createColor(new RGB(255, 0, 0));
			}
		}
		return super.getBackground(element);
	}

	@Override
	public String getToolTipText(Object element)
	{
		if (element instanceof ProjectTarget)
		{
			ProjectTarget target = (ProjectTarget) element;
			if (!target.isAvailable())
			{
				return Messages.SupportedTargetsPropertyPage_TooltipNonexistingTarget;
			}
		}
		return super.getToolTipText(element);
	}
}
