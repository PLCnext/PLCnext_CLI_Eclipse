/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.project.comfortfunction;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Default handler for the Generate Port Comment Command
 *
 */
public class GeneratePortCommentCommandHandler extends AbstractHandler
{
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
	 * ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		IEditorPart part = HandlerUtil.getActiveEditor(event);
		if (part == null || !(part instanceof AbstractTextEditor)) {
			Display.getDefault().syncExec(new Runnable()
			{
				@Override
				public void run()
				{
					MessageDialog.openInformation(null, "Generate port comments",
							"Please open a .hpp file and set the cursor to the variable you want to generate port comments for.");
				}
			});
			return null;
		}

		ITextEditor editor = (ITextEditor) part;
		IDocumentProvider provider = editor.getDocumentProvider();
		IDocument document = provider.getDocument(editor.getEditorInput());
		ISelection selection = editor.getSelectionProvider().getSelection();
		if (selection instanceof TextSelection)
		{
			TextSelection textSelection = (TextSelection) selection;
			if (textSelection.getStartLine() >= 0)
			{
				SelectPortAttributesWizardDialog portWizard = new SelectPortAttributesWizardDialog(null, document, textSelection);
				portWizard.open();
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#isEnabled()
	 */
	@Override
	public boolean isEnabled()
	{
		return true;
	}
}
