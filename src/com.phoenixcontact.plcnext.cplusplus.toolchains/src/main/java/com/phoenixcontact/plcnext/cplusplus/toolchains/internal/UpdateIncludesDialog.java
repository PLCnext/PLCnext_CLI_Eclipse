/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.toolchains.internal;

import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

public class UpdateIncludesDialog extends IconAndMessageDialog
{
	private boolean buttonSelection = false;

	
	public UpdateIncludesDialog(Shell parentShell)
	{
		super(parentShell);
	}

	@Override
	protected Image getImage()
	{
		return getQuestionImage();
	}
	
	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		if ((newShell != null) && !newShell.isDisposed()) {
			newShell.setText("Update includes");
		}
	}
	
	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = (Composite) super.createDialogArea(parent);
		
		GridLayout layout = new GridLayout();
		Layout l = composite.getLayout();
		if (l instanceof GridLayout)
		{
			layout = (GridLayout) l;
		}
		layout.numColumns = 2;
		layout.verticalSpacing = 20;
		composite.setLayout(layout);
		
		this.message = "CMakeLists.txt has changed, the project's includes might need an update.\n"
				+ "Selecting 'OK' will check and update the includes while saving the file.\n"
				+ "Selecting 'Cancel' will perform no check and update while saving the file.";
		
		
		createMessageArea(composite);	
		
		
		new Label(composite, SWT.NONE);
		
		Button rememberDecision = new Button(composite, SWT.CHECK);
		rememberDecision.setText("Remember decision and do not ask again");
		
		rememberDecision.addListener(SWT.Selection, event -> {buttonSelection = rememberDecision.getSelection();});
		
		return composite;
	}
	

	public boolean getButtonSelection()
	{
		return buttonSelection;
	}

}
