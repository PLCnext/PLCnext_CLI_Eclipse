/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.project.ui;

import org.eclipse.cdt.ui.templateengine.IPagesAfterTemplateSelectionProvider;
import org.eclipse.cdt.ui.templateengine.IWizardDataPage;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import com.phoenixcontact.plcnext.cplusplus.project.ProjectType;

public class AppProjectPagesAfterTemplateSelectionProvider implements IPagesAfterTemplateSelectionProvider {

	IWizardDataPage[] pages;
	
	@Override
	public IWizardDataPage[] createAdditionalPages(IWorkbenchWizard wizard, IWorkbench workbench,
			IStructuredSelection selection) {
		pages = new IWizardDataPage[2];
		pages[0] = new ProjectPropertiesWizardDataPage(ProjectType.APP);
		pages[1] = new SelectTargetsWizardDataPage("Select Targets");
		return pages;
	}

	@Override
	public IWizardDataPage[] getCreatedPages(IWorkbenchWizard wizard) {
		return pages;
	}
	

}
