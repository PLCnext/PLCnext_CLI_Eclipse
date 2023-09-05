/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.clicheck;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.phoenixcontact.plcnext.common.CliDescription;
import com.phoenixcontact.plcnext.common.Messages;

/**
 * This dialog provides a message that the cli was not found and provides a download button and a button which opens the preferences
 *
 */
public class CliSourceDialog extends MessageDialog {

	/**
	 * @see org.eclipse.jface.dialogs.MessageDialog#Constructor
	 * 
	 * @param parentShell
	 * @param cliInfo
	 */
	public CliSourceDialog(Shell parentShell, CliDescription cliInfo) {
		super(parentShell
				, Messages.CliSourceDialog_title
				, null
				, Messages.CliSourceDialog_message 
				, MessageDialog.CONFIRM, new String[] {Messages.CliSourceDialog_buttondownload, Messages.CliSourceDialog_buttonpreferences}, 0);
		this.shell = getShell();
		this.cliInfo = cliInfo;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getButton(Download).setVisible(false);
		getButton(Download).setEnabled(false);
	}
	
	protected static final int Download = OK;
	protected static final int editPreferences = CANCEL;
	private Shell shell;
	private boolean buttonPressed = false;
	private CliDescription cliInfo;
	
	@Override
	protected void buttonPressed(int buttonId) {
		buttonPressed = true;
		if(buttonId == Download) {
//			downloadPressed();
		}
		else if(buttonId == editPreferences) {
			editPreferencesPressed();
		}
		super.buttonPressed(buttonId);
	}
	
//	private void downloadPressed() {
//		DownloadWizardDialog wizard = new DownloadWizardDialog(shell);
//		wizard.open();
//	}
	
	private void editPreferencesPressed() {
		PreferenceDialog dialog = PreferencesUtil
		.createPreferenceDialogOn(shell,
				Messages.CliPreferencePageId, null, null);
		dialog.setBlockOnOpen(true);
		dialog.open();
		if (!cliInfo.cliExists()) {
			cliNotFoundMessage();
		}
	}
	
	@Override
	public boolean close() {
		if(!buttonPressed) {
			cliNotFoundMessage();
		}
		return super.close();
	}
	
	private void cliNotFoundMessage() {
		MessageDialog.openError(shell, Messages.CliNotExistingExceptionMessage,
				Messages.CliNotExistingExceptionPluginCorruptMessage);
	}
	
}