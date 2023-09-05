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
import com.phoenixcontact.plcnext.cplusplus.project.ui.ProjectConfigPropertyPage.LibModel;

/**
 *
 */
public class ProjectConfigLibsLabelProvider extends ColumnLabelProvider {

	private ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());
	
	@Override
	public String getText(Object element)
	{
		if(element instanceof LibModel)
		{
			LibModel lib = (LibModel) element;
			return lib.value;
		}
		return super.getText(element);
	}
	
	@Override
	public Color getForeground(Object element)
	{
		if (element instanceof LibModel)
		{
			LibModel lib = (LibModel) element;
			
			if (!lib.valid)
			{
				return resourceManager.createColor(new RGB(119, 119, 119));
			}
		}
		return super.getBackground(element);
	}

	@Override
	public String getToolTipText(Object element)
	{
		if (element instanceof LibModel)
		{
			LibModel lib = (LibModel) element;
			if (!lib.valid)
			{
				return Messages.ProjectConfigLibsLabelProvider_TooltipInvalidLib;
			}
		}
		return super.getToolTipText(element);
	}
}
