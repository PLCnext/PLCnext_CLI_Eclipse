/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.phoenixcontact.plcnext.common.Activator;
import com.phoenixcontact.plcnext.common.Messages;

/**
 * Page representing the PLCnext category in the preferences
 *
 */
public class PLCnextPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{

	/**
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#Constructor
	 */
	public PLCnextPreferencePage()
	{
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(Messages.PLCnextPreferencePage_description);
	}

	@Override
	public void init(IWorkbench workbench)
	{
	}

	@Override
	protected void createFieldEditors()
	{
		addField(new BooleanFieldEditor(PreferenceConstants.P_CLI_OPEN_INCLUDE_UPDATE_DIALOG,
				"Ask for includes-update every time CMakeLists.txt is saved", getFieldEditorParent()));
		addField(new BooleanFieldEditor(PreferenceConstants.P_CLI_UPDATE_INCLUDES,
				"Update includes (use this preference if not asked every time)", getFieldEditorParent()));

	}

}
