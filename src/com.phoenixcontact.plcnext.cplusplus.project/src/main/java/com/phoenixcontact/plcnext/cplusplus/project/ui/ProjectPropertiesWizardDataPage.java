/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.project.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.cdt.ui.templateengine.AbstractWizardDataPage;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.phoenixcontact.plcnext.cplusplus.project.ProjectType;

/**
 * 
 */
public class ProjectPropertiesWizardDataPage extends AbstractWizardDataPage
{

	/**
	 * The page-id
	 */
	public static final String PAGE_ID = "com.phoenixcontact.plcnext.cplusplus.project.ProjectPropertiesWizardDataPage";
	/**
	 * The component name key
	 */
	public static final String KEY_COMPONENTNAME = "COMPONENTNAME";
	/**
	 * The program name key
	 */
	public static final String KEY_PROGRAMNAME = "PROGRAMNAME";
	/**
	 * The namespace key
	 */
	public static final String KEY_PROJECTNAMESPACE = "PROJECTNAMESPACE";

	private Text componentText;
	private Text programText;
	private Text projectNamespace;

	private ProjectType projectType;

	/**
	 * @param projectType 
	 * @param pageName
	 * @see org.eclipse.cdt.ui.templateengine.AbstractWizardDataPage#Constructor
	 */
	public ProjectPropertiesWizardDataPage(ProjectType projectType)
	{
		super("Project Properties");
		this.projectType = projectType;
		setTitle("PLCnext C++ Project Properties");
		setDescription("Select properties of the new project.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.templateengine.IWizardDataPage#getPageData()
	 */
	@Override
	public Map<String, String> getPageData()
	{
		return new HashMap<String, String>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.
	 * Composite)
	 */
	@Override
	public void createControl(Composite parent)
	{
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.verticalSpacing = 15;
		gridLayout.marginHeight = 15;

		container.setLayout(gridLayout);
		container.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		setControl(container);

		if(projectType != ProjectType.CONSUMABLELIBRARY) 
		{
			Label componentLabel = new Label(container, SWT.NONE);
			componentLabel.setText("&Component name:");

			componentText = new Text(container, SWT.BORDER);
			componentText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		}
		
		if (projectType == ProjectType.STANDARD)
		{
			Label programLabel = new Label(container, SWT.NONE);
			programLabel.setText("&Program name:");

			programText = new Text(container, SWT.BORDER);
			programText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		}
		// **** project namespace ****
		Label namespaceLabel = new Label(container, SWT.NONE);
		namespaceLabel.setText("Project name&space:");

		projectNamespace = new Text(container, SWT.BORDER);
		projectNamespace.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

		String projectName = "My";
		IWizardPage previousPage = getPreviousPage();
		if (previousPage instanceof FilteredCDTMainWizardPage)
		{
			projectName = ((FilteredCDTMainWizardPage) previousPage).getProjectName();
		}

		if(componentText != null)
		{
			componentText.setText(projectName + "Component");
		}
		if (programText != null)
		{
			programText.setText(projectName + "Program");
		}
		projectNamespace.setText(projectName);

		if(componentText != null)
		{
			MBSCustomPageManager.addPageProperty(PAGE_ID, KEY_COMPONENTNAME, componentText.getText());
		}
		if (programText != null)
		{
			MBSCustomPageManager.addPageProperty(PAGE_ID, KEY_PROGRAMNAME, programText.getText());
		}
		MBSCustomPageManager.addPageProperty(PAGE_ID, KEY_PROJECTNAMESPACE, projectNamespace.getText());

		validatePage();

		if(componentText != null)
		{
			componentText.addModifyListener(new ModifyListener()
			{

				@Override
				public void modifyText(ModifyEvent e)
				{
					if (validatePage())
					{
						MBSCustomPageManager.addPageProperty(PAGE_ID, KEY_COMPONENTNAME, componentText.getText());
					}
					;
				}
			});
		}

		if (programText != null)
		{
			programText.addModifyListener(new ModifyListener()
			{

				@Override
				public void modifyText(ModifyEvent e)
				{
					if (validatePage())
					{
						MBSCustomPageManager.addPageProperty(PAGE_ID, KEY_PROGRAMNAME, programText.getText());
					}
				}
			});
		}

		projectNamespace.addListener(SWT.Modify, event ->
		{
			if (validatePage())
			{
				MBSCustomPageManager.addPageProperty(PAGE_ID, KEY_PROJECTNAMESPACE, projectNamespace.getText());
			}
		});

	}

	private boolean validatePage()
	{
		// **** check component name ****
		Pattern pattern = Pattern.compile("^[A-Z](?!.*__)[a-zA-Z0-9_]*$");
		Matcher match = null;
		if(componentText != null)
		{
			match = pattern.matcher(componentText.getText());
		}
		String errorMessage = null;
		if (match == null || match.matches())
		{
			// **** check program name ****
			if (programText != null)
			{
				match = pattern.matcher(programText.getText());
			}
			if (match == null || match.matches())
			{
				// **** check project namespace ****
				Pattern namespacePattern = Pattern
						.compile("^(?:[a-zA-Z][a-zA-Z0-9_]*\\.)*[a-zA-Z](?!.*__)[a-zA-Z0-9_]*$");
				Matcher namespaceMatch = namespacePattern.matcher(projectNamespace.getText());
				if (namespaceMatch.matches())
				{
					setPageComplete(true);
					setErrorMessage(null);
					return true;
				} else
					errorMessage = "Namespace does not match pattern ^(?:[a-zA-Z][a-zA-Z0-9_]*\\.)*[a-zA-Z](?!.*__)[a-zA-Z0-9_]*$";
			} else
				errorMessage = "Program name does not match pattern ^[A-Z](?!.*__)[a-zA-Z0-9_]*$";
		} else
			errorMessage = "Component name does not match pattern ^[A-Z](?!.*__)[a-zA-Z0-9_]*$";

		setPageComplete(false);
		setErrorMessage(errorMessage);
		return false;
	}
}
