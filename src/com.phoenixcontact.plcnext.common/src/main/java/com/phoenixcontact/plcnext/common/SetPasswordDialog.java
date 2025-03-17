/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 *
 */
public class SetPasswordDialog extends StatusDialog
{
	private String password;
	private char echoCharHidden;
	private final char echoCharVisible = '\0';
	private String okButtonText;
	private String additionalInformation;
	
	/**
	 * @param parent
	 * @see org.eclipse.jface.dialogs.StatusDialog#Constructor
	 */
	public SetPasswordDialog(Shell parent, String okButtonText, String dialogTitle, String additionalInformation)
	{
		super(parent);
		setTitle(dialogTitle);
		setHelpAvailable(false);
		this.okButtonText = okButtonText;
		this.additionalInformation = additionalInformation;
	}
	
	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = (Composite) super.createDialogArea(parent);
		initializeDialogUnits(composite);
		
		
		Composite container = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.horizontalSpacing = 0;
		layout.marginLeft = 0;
		layout.marginRight = 0;
		layout.marginWidth = 0;
		container.setLayout(layout);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Label infoLabel = new Label(container, SWT.NONE);
		infoLabel.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false, 2, 1));
		infoLabel.setText(Messages.SetPasswordDialog_InfoText);
		
		Text passwordText = new Text(container, SWT.BORDER | SWT.PASSWORD);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
		passwordText.setLayoutData(gd);
		passwordText.setText(password);
		passwordText.addListener(SWT.Modify, event -> password = passwordText.getText());
		echoCharHidden = passwordText.getEchoChar();
		
		Button showPasswordButton = new Button(container, SWT.PUSH|SWT.TRANSPARENT);
		showPasswordButton.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));
		showPasswordButton.setToolTipText("Show Password");
		showPasswordButton.setImage(new Image(parent.getDisplay(), Activator.class.getResourceAsStream("/icons/ShowPasswordIcon.png")));
		showPasswordButton.addListener(SWT.MouseDown,  event -> passwordText.setEchoChar(echoCharVisible));
		showPasswordButton.addListener(SWT.MouseUp,  event -> passwordText.setEchoChar(echoCharHidden));
		
		Label additionalInformationLabel = new Label(container, SWT.NONE);
		additionalInformationLabel.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false, 2, 1));
		additionalInformationLabel.setText(additionalInformation);
		
		return composite;
	}
	
	@Override
	protected Control createContents(Composite parent)
	{
		Control composite = super.createContents(parent);
		
		getButton(IDialogConstants.OK_ID).setText(okButtonText);
		
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
