/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.project.ui;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

import com.phoenixcontact.plcnext.cplusplus.project.Activator;


/**
 *
 */
public class ProjectConfigPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {

	private IPath path;
	private Pattern pattern = Pattern.compile("^(?<major>\\d+)\\.\\d+(.\\d+)?(.\\d+)?$");
	private final String groupName = "major";
	private final String ConfigFileName = "PLCnextSettings.xml";
	private Text libraryDescription;
	private Text libraryVersion;
	private Text engineerVersion;

	@Override
	protected Control createContents(Composite parent) {

		noDefaultAndApplyButton();
		
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label description = new Label(container, SWT.NONE);
		description.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		description.setText("Project properties for PLCnext C++ Projects.\r\n"
				+ "Rebuild the project after saving your changes to transfer the configuration to the library.");

		Label libraryDescriptionLabel = new Label(container, SWT.NONE);
		libraryDescriptionLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		libraryDescriptionLabel.setText("Library Description");

		libraryDescription = new Text(container, SWT.MULTI|SWT.BORDER|SWT.V_SCROLL);
		libraryDescription.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Label libraryVersionLabel = new Label(container, SWT.NONE);
		libraryVersionLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		libraryVersionLabel.setText("Library Version");
		
		libraryVersion = new Text(container, SWT.SINGLE|SWT.BORDER);
		libraryVersion.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		
		Label engineerVersionLabel = new Label(container, SWT.NONE);
		engineerVersionLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		engineerVersionLabel.setText("PLCnext Engineer Version");
		
		engineerVersion = new Text(container, SWT.SINGLE|SWT.BORDER);
		engineerVersion.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		engineerVersion.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				Text widget = (Text) e.widget;
				String text = widget.getText();
				if(text != null && !text.isEmpty()) 
				{
					Matcher matcher = pattern.matcher(text);
					if(!matcher.matches()) 
					{
						setMessage("No valid version! Please use format: 202x.x or 202x.x.x", ERROR);
						setValid(false);
						return;
					}
					else
					{
						String major = matcher.group(groupName);
						if(Integer.parseInt(major)<2020
								|| Integer.parseInt(major) >= 2030)
						{
							setMessage("No valid version! Please use format: 202x.x or 202x.x.x", ERROR);
							setValid(false);
							return;
						}
					}
				}
				setMessage(null);
				setValid(true);
			}
		});
		
		LoadFileContent();
		
		return container;
	}

	private void LoadFileContent() {
		IAdaptable element = getElement();
		if (element instanceof IProject)
		{
			IProject project = (IProject) element;
			path = project.getLocation().append(ConfigFileName);
			File file = path.toFile();
			if(file.exists())
			{
				try {
					JAXBContext context = JAXBContext.newInstance(ProjectConfiguration.class);
					Unmarshaller unmarshaller = context.createUnmarshaller();
					ProjectConfiguration configuration = (ProjectConfiguration) unmarshaller.unmarshal(file);
					engineerVersion.setText(configuration.getEngineerVersion());
					libraryDescription.setText(configuration.getLibraryDescription());
					libraryVersion.setText(configuration.getLibraryVersion());
				} catch (JAXBException e) {
					Activator.getDefault().logError("Project configuration file could not be loaded.",e);
				}
			}
		}
		
	}
	
	@Override
	public boolean performOk() 
	{
		String engineerText = engineerVersion.getText();
		String description = libraryDescription.getText();
		String libVersion = libraryVersion.getText();
		if((engineerText == null || engineerText.isBlank())
				&& (description == null || description.isBlank())
				&& (libVersion == null || libVersion.isBlank()))
		{
			File file = path.toFile();
			if(file.exists())
			{
				file.delete();
			}
		}else {
			ProjectConfiguration config = new ProjectConfiguration();
			description = description.replaceAll("\r", "");
			config.setLibraryDescription(description);
			config.setLibraryVersion(libVersion);
			config.setEngineerVersion(engineerText);
			WriteFile(config);
		}
		return super.performOk();
	}

	private void WriteFile(ProjectConfiguration config)
	{
		try {
			JAXBContext context = JAXBContext.newInstance(ProjectConfiguration.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			
			marshaller.marshal(config, path.toFile());
		} catch (JAXBException e) {
			Activator.getDefault().logError("Project configuration file could not be saved.",e);
		}
		
	}
}
