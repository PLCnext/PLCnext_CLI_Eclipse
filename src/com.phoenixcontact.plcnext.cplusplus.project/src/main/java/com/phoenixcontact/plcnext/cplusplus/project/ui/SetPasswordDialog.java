/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.project.ui;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.phoenixcontact.plcnext.common.Messages;

/**
 *
 */
public class SetPasswordDialog extends StatusDialog
{
	private String password;
	
	/**
	 * @param parent
	 * @see org.eclipse.jface.dialogs.StatusDialog#Constructor
	 */
	public SetPasswordDialog(Shell parent)
	{
		super(parent);
		setTitle(Messages.SetPasswordDialog_DialogTitle);
		setHelpAvailable(false);
	}
	
	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = (Composite) super.createDialogArea(parent);
		initializeDialogUnits(composite);
		
		Label infoLabel = new Label(composite, SWT.NONE);
		infoLabel.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		infoLabel.setText(Messages.SetPasswordDialog_InfoText);
		
		Text passwordText = new Text(composite, SWT.BORDER | SWT.PASSWORD);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
		passwordText.setLayoutData(gd);
		passwordText.setText(password);
		passwordText.addListener(SWT.Modify, event -> password = passwordText.getText());
		
		return composite;
	}
	
	@Override
	protected Control createContents(Composite parent)
	{
		Control composite = super.createContents(parent);
		
		getButton(IDialogConstants.OK_ID).setText("Save");
		
		return composite;
	}
	
	public void setPassword(String password)
	{
		this.password = password;
	}
	
	
	public String openWithResult()
	{
		if(this.open() == Window.OK)
		{
			return password;
		}
		return null;
	}
}
