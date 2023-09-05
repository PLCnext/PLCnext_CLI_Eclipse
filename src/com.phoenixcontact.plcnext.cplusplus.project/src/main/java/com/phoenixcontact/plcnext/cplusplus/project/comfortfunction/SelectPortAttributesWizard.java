/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.project.comfortfunction;

import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.wizard.Wizard;

import com.phoenixcontact.plcnext.cplusplus.project.Activator;

/**
 * Wizard for Port Comment Creation
 *
 */
public class SelectPortAttributesWizard extends Wizard
{

	private SelectPortAttributesWizardPage page;
	private IDocument document;
	private TextSelection textSelection;
	protected static final String KEY_PREFIX = "Prefix";

	/**
	 * Creates new wizard for inserting comment in the document at position
	 * selection
	 * 
	 * @param document  the document in which the comment shall be inserted
	 * @param selection the comment will be added in a new line above the start of
	 *                  the selection
	 */
	public SelectPortAttributesWizard(IDocument document, TextSelection selection)
	{
		try
		{
			this.document = document;
			this.textSelection = selection;
			int startLine = textSelection.getStartLine();
			
			int lineoffset = document.getLineOffset(startLine);
			String text = document.get(lineoffset,
					document.getLineOffset(startLine + 1) - lineoffset);

			page = new SelectPortAttributesWizardPage(text);
			addPage(page);
			setDialogSettings(DialogSettings.getOrCreateSection(Activator.getDefault().getDialogSettings(),
					"PortAttributesWizard"));
			

		} catch (BadLocationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish()
	{
		String prefix = page.getPrefix();
		try
		{
			document.replace(document.getLineOffset(textSelection.getStartLine()), 0, page.getPortComment());
		} catch (BadLocationException e)
		{
			Activator.getDefault().logError("Error while trying to generate port comments.", e);
			return false;
		}
		getDialogSettings().put(KEY_PREFIX, prefix);
		return true;
	}
}
