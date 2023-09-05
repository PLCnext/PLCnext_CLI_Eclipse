/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.preferences;

import java.util.List;
import java.util.stream.Collectors;

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

import com.phoenixcontact.plcnext.common.Messages;

public class RemoveSdkDialog extends IconAndMessageDialog
{

	private List<String> sdkNames;
	private Button deleteFromFileSystem;
	private boolean buttonSelection = false;
	private final String dialogMessage = Messages.RemoveSdkDialog_dialogMessage;
	private final String buttonText = Messages.RemoveSdkDialog_buttonText;
	private final String dialogTitle = Messages.RemoveSdkDialog_dialogTitle;
	
	public RemoveSdkDialog(Shell parentShell, List<String> sdkName)
	{
		super(parentShell);
		this.sdkNames = sdkName;
	}
	
	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		if ((newShell != null) && !newShell.isDisposed()) {
			newShell.setText(dialogTitle);
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
		
		if(sdkNames != null && sdkNames.size() == 1)
			this.message = String.format(dialogMessage, "", sdkNames.get(0), "y");
		else if (sdkNames != null && sdkNames.size() > 1)
			this.message = String.format(dialogMessage, "s", sdkNames.stream().collect(Collectors.joining("'\n'")), "ies");
		else 
			return null;
		
		createMessageArea(composite);	
		
		
		new Label(composite, SWT.NONE);
		
		deleteFromFileSystem = new Button(composite, SWT.CHECK);
		if(sdkNames != null && sdkNames.size() > 1)
			deleteFromFileSystem.setText(String.format(buttonText, "ies"));
		else 
			deleteFromFileSystem.setText(String.format(buttonText, "y"));
		
		deleteFromFileSystem.addListener(SWT.Selection, event -> {buttonSelection = deleteFromFileSystem.getSelection();});
		
		return composite;
	}

	@Override
	protected Image getImage()
	{
		return getQuestionImage();
	}
	
	public boolean getButtonSelection()
	{
		return buttonSelection;
	}
}
