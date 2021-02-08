/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.preferences;

import java.io.File;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.phoenixcontact.plcnext.common.Activator;
import com.phoenixcontact.plcnext.common.Messages;

/**
 * Dialog for selection of sdk to add
 */
public class AddSdkDialog extends StatusDialog
{
	private Text localDirectory;
	private String localDirectoryText = ""; //$NON-NLS-1$
	
	/**
	 * @param parent
	 * @see org.eclipse.jface.dialogs.StatusDialog#Constructor
	 */
	public AddSdkDialog(Shell parent)
	{
		super(parent);
		setTitle(Messages.AddSdkDialog_DialogTitle);
		setHelpAvailable(false);
		
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = (Composite) super.createDialogArea(parent);
		initializeDialogUnits(composite);

		GridLayout layout = new GridLayout();
		Layout l = composite.getLayout();
		if (l instanceof GridLayout)
		{
			layout = (GridLayout) l;
		}
		layout.numColumns = 3;
		composite.setLayout(layout);
		
		Label infoLabel = new Label(composite, SWT.NONE);
		infoLabel.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false, 3, 1));
		infoLabel.setText(Messages.AddSdkDialog_InfoText);
		
		Label locationLabel = new Label(composite, SWT.NONE);
		locationLabel.setText(Messages.AddSdkDialog_LocationLabel);

		localDirectory = new Text(composite, SWT.BORDER);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
		localDirectory.setLayoutData(gd);
		localDirectory.addListener(SWT.Modify, event -> checkText());

		Button localButton = new Button(composite, SWT.PUSH);
		localButton.setText(Messages.AddSdkDialog_SelectButton);
		setButtonLayoutData(localButton);
		localButton.addListener(SWT.Selection, event -> handleLocalButtonSelected());

		checkText();
		return composite;
	}

	private void handleLocalButtonSelected()
	{
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		String result = dialog.open();
		if (result != null && !result.isEmpty())
		{
			localDirectory.setText(result);
		}
	}

	private void checkText()
	{
		localDirectoryText = localDirectory.getText();
		if (localDirectoryText == null || localDirectoryText.isEmpty())
		{
			updateStatus(new Status(Status.ERROR, Activator.PLUGIN_ID, Messages.AddSdkDialog_NoDirectoryError));
			return;
		}
		boolean exists = new File(localDirectoryText).exists();
		if (!exists)
		{
			updateStatus(new Status(Status.ERROR, Activator.PLUGIN_ID, "The directory " + localDirectoryText + " does not exist."));
		} else
		{
			updateStatus(new Status(Status.OK, Activator.PLUGIN_ID, Util.ZERO_LENGTH_STRING));
		}
	}
	
	/**
	 * @return String containing the entered directory
	 * @see org.eclipse.jface.window.Window#open
	 */
	public String openWithResult()
	{
		if(this.open() == Window.OK)
		{
			return localDirectoryText;
		}
		return null;
	}	
}
