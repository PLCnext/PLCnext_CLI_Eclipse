/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.preferences;

import java.io.File;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.phoenixcontact.plcnext.common.Activator;
import com.phoenixcontact.plcnext.common.Messages;

/**
 * Dialog for selection of sdk to install
 */
public class InstallSdkDialog extends StatusDialog
{
	/**
	 * @param parent
	 * @see org.eclipse.jface.dialogs.StatusDialog#Constructor
	 */
	public InstallSdkDialog(Shell parent)
	{
		super(parent);
		setTitle(Messages.InstallSdkDialog_DialogTitle);
		setHelpAvailable(false);
	}

	private boolean force = false;
	private String installFileText = ""; //$NON-NLS-1$
	private String destinationText = ""; //$NON-NLS-1$
	private Text installFile;
	private Text destination;
	
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
		infoLabel.setText(Messages.InstallSdkDialog_InfoText);
		
		Label archiveLabel = new Label(composite, SWT.NONE);
		archiveLabel.setText(Messages.InstallSdkDialog_ArchiveLabel);
		
		installFile = new Text(composite, SWT.BORDER);
		GridData installGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
		installGD.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
		installFile.setLayoutData(installGD);
		installFile.addListener(SWT.Modify, event -> checkText());

		Button archiveButton = new Button(composite, SWT.PUSH);
		archiveButton.setText(Messages.InstallSdkDialog_ArchiveButton);
		setButtonLayoutData(archiveButton);
		archiveButton.addListener(SWT.Selection, event -> handleArchiveButtonSelected());
				
		Label destinationLabel = new Label(composite, SWT.NONE);
		destinationLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		destinationLabel.setText(Messages.InstallSdkDialog_DestinationLabel);
		
		destination = new Text(composite, SWT.BORDER);
		GridData destinationGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
		destinationGD.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
		destination.setLayoutData(destinationGD);
		destination.addListener(SWT.Modify, event -> checkText());
		
		Button destButton = new Button(composite, SWT.PUSH);
		destButton.setText(Messages.InstallSdkDialog_DestinationButton);
		setButtonLayoutData(destButton);
		destButton.addListener(SWT.Selection, event -> handleDestinationButtonSelected());

		new Label(composite, SWT.NONE);
		
		Button forceButton = new Button(composite, SWT.CHECK);
		setButtonLayoutData(forceButton);
		forceButton.setText(Messages.InstallSdkDialog_ForceButton);
		forceButton.addListener(SWT.Selection, event -> {force = forceButton.getSelection();});
		
		checkText();
		return composite;
	}
	
	private void handleArchiveButtonSelected()
	{
		FileDialog dialog = new FileDialog(getShell());
		
		if(Platform.getOS().equals(Platform.OS_LINUX))
		{
			dialog.setFilterExtensions(new String[] {Messages.InstallSdkDialog_ArchiveFilterExtensionLinux});
		}else 
		{
			dialog.setFilterExtensions(new String[] {Messages.InstallSdkDialog_ArchiveFilterExtensionWin});
		}
		
		String result = dialog.open();
		if (result != null && !result.isEmpty())
		{
			installFile.setText(result);
		}
	}
	
	private void handleDestinationButtonSelected()
	{
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		String result = dialog.open();
		if (result != null && !result.isEmpty())
		{
			destination.setText(result);
		}
	}
	
	private void checkText()
	{
		installFileText = installFile.getText();
		if (installFileText == null || installFileText.isEmpty())
		{
			updateStatus(new Status(Status.ERROR, Activator.PLUGIN_ID, Messages.InstallSdkDialog_NoFileError));
			return;
		}
		boolean exists = new File(installFileText).exists();
		if (!exists)
		{
			updateStatus(new Status(Status.ERROR, Activator.PLUGIN_ID, "The file " + installFileText + " does not exist.")); //$NON-NLS-1$ //$NON-NLS-2$
		} else
		{
			destinationText = destination.getText();
			if (destinationText == null || destinationText.isEmpty())
			{
				updateStatus(new Status(Status.ERROR, Activator.PLUGIN_ID, Messages.InstallSdkDialog_NoDestinationError));
				return;
			}
			
			updateStatus(Status.OK_STATUS);
		}
	}
	
	/**
	 * @return InstallSdkDialogResult object containing the entered destination, archive file and force option
	 * @see org.eclipse.jface.window.Window#open
	 */
	public InstallSdkDialogResult openWithResult()
	{
		if(this.open() == Window.OK)
		{
			return new InstallSdkDialogResult(destinationText, installFileText, force);
		}
		return null;
	}	
	
	protected class InstallSdkDialogResult
	{
		private String destination = ""; //$NON-NLS-1$
		private String archive = ""; //$NON-NLS-1$
		private boolean force = false;
				
		public InstallSdkDialogResult(String destination, String archive, boolean force)
		{
			this.destination= destination;
			this.archive = archive;
			this.force = force;
		}
		
		public String getDestination()
		{
			return destination;
		}
		
		public String getArchive()
		{
			return archive;
		}
		
		public boolean getForce()
		{
			return force;
		}
	}
}
