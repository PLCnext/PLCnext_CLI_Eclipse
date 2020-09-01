/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.project.ui;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.ui.wizards.CDTMainWizardPage;
import org.eclipse.cdt.ui.wizards.EntryDescriptor;

/**
 * Extension of the CDTMainWizardPage which uses FilteredCDTMainWizardPage instead of CDTMainWizardPage
 * such that only plcnext project templates will be shown in wizard page
 */
public class FilteredCDTMainWizardPage extends CDTMainWizardPage {

	/**
	 * @param pageName 
	 * @see org.eclipse.cdt.ui.wizards.CDTMainWizardPage#Constructor
	 */
	public FilteredCDTMainWizardPage(String pageName) {
		super(pageName);
	}

	@Override
	public List<EntryDescriptor> filterItems(List<EntryDescriptor> items) {
		EntryDescriptor axc = null;
		EntryDescriptor acfProjectTemplate = null;
		EntryDescriptor categoryDescriptor = null;
		EntryDescriptor consumableLibraryTemplate = null;
		for(EntryDescriptor item : items) {
			if(item.getId().equals("AXC2152Proj")) {
				axc = item;
			}
			if(item.getId().equals("com.phoenixcontact.plcnext.cplusplus.toolchains.plcnextPropertyValue")) {
				categoryDescriptor = item;
			}
			if(item.getId().equals("AcfProjectTemplate")) {
				acfProjectTemplate = item;
			}
			if(item.getId().equals("ConsumableLibraryTemplate")) {
				consumableLibraryTemplate = item;
			}
		}
		if(axc != null || acfProjectTemplate != null || consumableLibraryTemplate != null)
		{
			items.clear();
			items.add(categoryDescriptor);
			if(axc != null)
			{
				items.add(axc);
			}
			if(acfProjectTemplate != null)
			{
				items.add(acfProjectTemplate);
			}
			if(consumableLibraryTemplate != null)
			{
				items.add(consumableLibraryTemplate);
			}
		}
		return items;
	}
	
	@Override
	protected boolean validatePage() {
		String projectName = getProjectName();
		
//		if(projectName.length() > 0 && Character.isLowerCase(projectName.charAt(0))) {
//			setErrorMessage("Project name cannot start with lowercase character.");
//			return false;
//		}
		
		Pattern pattern = Pattern.compile("^[A-Z](?!.*__)[a-zA-Z0-9_]*$");
		Matcher match = pattern.matcher(projectName);
		if(match.matches())
			return super.validatePage();
		
		setErrorMessage("Project name does not match pattern ^[A-Z](?!.*__)[a-zA-Z0-9_]*$");
		return false;
	}
}
