/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.project.ui;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.CCProjectWizard;

/**
 * Extension of the CCProjectWizard which uses FilteredCDTMainWizardPage instead of CDTMainWizardPage
 * such that only plc project templates will be shown in wizard page
 */
public class FilteredCCProject extends CCProjectWizard{
	
	@Override
	public void addPages() {
		fMainPage = new FilteredCDTMainWizardPage(CUIPlugin.getResourceString("CProjectWizard"));
		fMainPage.setTitle("New PLCnext C++ Project");
		fMainPage.setDescription("Create a new PLCnext C++ Project");
		addPage(fMainPage);
	}
}
