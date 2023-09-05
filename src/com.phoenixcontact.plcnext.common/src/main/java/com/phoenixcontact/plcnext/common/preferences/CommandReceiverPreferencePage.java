/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.preferences;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.phoenixcontact.plcnext.common.Activator;
import com.phoenixcontact.plcnext.common.Messages;

/**
 * Preference page for settings corresponding to the command receiver
 * Currently the path to the command line tool and the name of the executable can be set
 *
 */
public class CommandReceiverPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#Constructor
	 */
	public CommandReceiverPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(Messages.CommandReceiverPreferencePage_description);
	}
	
	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected void createFieldEditors() {
		addField(new DirectoryFieldEditor(PreferenceConstants.P_CLI_PATH, Messages.CommandReceiverPreferencePage_pathLabel, getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.P_CLI_NAME, Messages.CommandReceiverPreferencePage_nameLabel, getFieldEditorParent()));
	}

}
