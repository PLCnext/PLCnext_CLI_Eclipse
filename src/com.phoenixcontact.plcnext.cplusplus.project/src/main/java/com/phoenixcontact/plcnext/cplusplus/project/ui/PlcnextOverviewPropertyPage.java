/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.project.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

import com.phoenixcontact.plcnext.common.ProcessExitedWithErrorException;
import com.phoenixcontact.plcnext.cplusplus.project.Activator;
import com.phoenixcontact.plcnext.cplusplus.toolchains.ToolchainConfigurator;
import com.phoenixcontact.plcnext.cplusplus.toolchains.ToolchainConfigurator.MacrosAndIncludesWrapper;

public class PlcnextOverviewPropertyPage extends PropertyPage implements IWorkbenchPropertyPage
{
	@Override
	protected Control createContents(Composite parent)
	{
		noDefaultAndApplyButton();
				
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, false);
		container.setLayout(gridLayout);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label description = new Label(container, SWT.NONE);
		description.setText("Project settings for PLCnext Technology C++ Projects.");
		GridData descriptionLayout = new GridData();
		descriptionLayout.horizontalSpan = 1;
		description.setLayoutData(descriptionLayout);
		
		GridData buttonGridData = new GridData();
		buttonGridData.verticalIndent = 20;
		buttonGridData.verticalAlignment = SWT.CENTER;
		buttonGridData.horizontalIndent = 0;
		Button button = new Button(container, SWT.PUSH);
		button.setText("Update includes");
		button.addListener(SWT.Selection, event -> updateIncludes());
		button.setLayoutData(buttonGridData);
		
		Text buttonDescription = new Text(container, SWT.MULTI);
		buttonDescription.setEditable(false);
		buttonDescription.setText("Update includes and macros to fix missing include paths e.g. after importing project into workspace.");
		
		return container;
	}
	
	private void updateIncludes()
	{
		IAdaptable element = getElement();
		if (element instanceof IProject)
		{
			IProject project = (IProject) element;
			new Job("Update includes for project " + project.getName())
			{
				@Override
				protected IStatus run(IProgressMonitor monitor)
				{
					
					ToolchainConfigurator configurator = new ToolchainConfigurator();
					MacrosAndIncludesWrapper wrapper = null;
					try
					{
						wrapper = configurator.findMacrosAndIncludes(project, null);
						configurator.updateIncludesOfExistingProject(project, wrapper.getIncludes(),
								wrapper.getMacros(), null);
						return Status.OK_STATUS;
					} catch (ProcessExitedWithErrorException e)
					{
						Activator.getDefault().logError("error during update of project macros and includes", e);
						return new Status(Status.ERROR, Activator.PLUGIN_ID,
								"error during update of project macros and includes", e);
					}
				}
			}.schedule();
		}
	}
}
