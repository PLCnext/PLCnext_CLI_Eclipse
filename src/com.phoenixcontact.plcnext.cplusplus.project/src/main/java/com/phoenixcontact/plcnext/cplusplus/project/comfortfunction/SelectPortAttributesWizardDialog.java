/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.project.comfortfunction;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;


/**
 * Wizard dialog for Port Comment Creation
 *
 */
public class SelectPortAttributesWizardDialog extends WizardDialog
{
	
	/**
	 * Creates new wizard dialog for inserting comment in the document at position selection
	 * 
	 * @param parentShell
	 * @param document the document in which the comments shall be added
	 * @param selection the comment will be added in a new line above the start of the selection 
	 */
	public SelectPortAttributesWizardDialog(Shell parentShell, IDocument document, TextSelection selection)
	{
		super(parentShell, new SelectPortAttributesWizard(document, selection));
	}
}
