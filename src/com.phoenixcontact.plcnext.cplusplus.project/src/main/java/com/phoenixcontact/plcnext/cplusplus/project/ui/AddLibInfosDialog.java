/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.project.ui;

import java.util.Arrays;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.phoenixcontact.plcnext.common.Activator;
import com.phoenixcontact.plcnext.common.ConfigFile.LibraryInfo;

/**
 * Dialog for adding/editing library infos
 */
public class AddLibInfosDialog extends StatusDialog
{
	private Text keyControl;
	private String key = "";
	private Text valueControl;
	private String value = "";
	private String[] existingKeys; 
	private String startKey;
	
	private String infoLabel;
	
	/**
	 * Add dialog
	 * @param parent
	 * @see org.eclipse.jface.dialogs.StatusDialog#Constructor
	 */
	public AddLibInfosDialog(Shell parent, String[] existingKeys)
	{
		super(parent);
		setTitle("Add Library Info");
		setHelpAvailable(false);
		this.existingKeys= existingKeys;
		infoLabel = "Add key value pair to list of library infos.";
	}
	
	/**
	 * Edit dialog
	 * @param parent
	 * @see org.eclipse.jface.dialogs.StatusDialog#Constructor
	 */
	public AddLibInfosDialog(Shell parent, String[] existingKeys, String startKey, String startValue)
	{
		super(parent);
		setTitle("Edit Library Info");
		setHelpAvailable(false);
		this.existingKeys= existingKeys; 
		this.startKey = startKey;
		this.key = startKey;
		this.value = startValue;
		infoLabel = "Edit key value pair of library infos.";
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
		layout.numColumns = 2;
		composite.setLayout(layout);
		
		Label descriptionLabel = new Label(composite, SWT.NONE);
		descriptionLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		descriptionLabel.setText(infoLabel);
		
		Label keyLabel = new Label(composite, SWT.NONE);
		keyLabel.setText("Key:");

		keyControl = new Text(composite, SWT.BORDER);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
		keyControl.setLayoutData(gd);
		keyControl.addListener(SWT.Modify, event -> checkKey());
		keyControl.setText(key);
		
		Label valueLabel = new Label(composite, SWT.NONE);
		valueLabel.setText("Value:");
		valueLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		
		valueControl = new Text(composite, SWT.BORDER | SWT.WRAP);
		gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		gd.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
		gd.heightHint = 50;
		valueControl.setLayoutData(gd);
		valueControl.addListener(SWT.Modify, event -> value = valueControl.getText());
		valueControl.setText(value);

		checkKey();
		return composite;
	}

	private void checkKey()
	{
		key = keyControl.getText();
		if (key == null || key.isEmpty())
		{
			updateStatus(new Status(Status.ERROR, Activator.PLUGIN_ID, "No key is given."));
			return;
		}
		boolean exists = false;
		if(startKey == null || startKey.isEmpty() || !key.equals(startKey)) 
		{
			exists = Arrays.stream(existingKeys).anyMatch(x -> x.equals(key));
		}
		
		if (exists)
		{
			updateStatus(new Status(Status.ERROR, Activator.PLUGIN_ID, "The key " + key + " does already exist."));
		} else
		{
			updateStatus(new Status(Status.OK, Activator.PLUGIN_ID, Util.ZERO_LENGTH_STRING));
		}
	}
	
	/**
	 * @return String containing the entered directory
	 * @see org.eclipse.jface.window.Window#open
	 */
	public LibraryInfo openWithResult()
	{
		if(this.open() == Window.OK)
		{
			LibraryInfo result = new LibraryInfo();
			result.setName(key);
			result.setText(value);
			return result;
		}
		return null;
	}	
}
