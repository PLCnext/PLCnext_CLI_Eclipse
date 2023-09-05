/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.project.Import;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

/**
 * 
 *
 */
public class ImportPlcProjectWizard extends Wizard implements IImportWizard
{
	private WizardImportPlcProjectPage mainPage;

	/**
	 * 
	 */
	public ImportPlcProjectWizard()
	{
		setWindowTitle("Import");
		setNeedsProgressMonitor(true);
	}
	
	@Override
	public void addPages()
	{
		super.addPages();
		mainPage = new WizardImportPlcProjectPage();
		addPage(mainPage);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish()
	{
		return mainPage.importProject();
	}

}
