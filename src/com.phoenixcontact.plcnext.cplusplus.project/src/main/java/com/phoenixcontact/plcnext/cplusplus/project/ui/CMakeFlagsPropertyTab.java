/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.project.ui;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import com.phoenixcontact.plcnext.cplusplus.project.Activator;
import com.phoenixcontact.plcnext.cplusplus.project.PlcProjectNature;

/**
 *
 */
public class CMakeFlagsPropertyTab extends AbstractCPropertyTab {

	private IProject project;
	private final String cmakeFlagsFileName = "CMakeFlags.txt";
	private String topLabel = "CMake flags can be entered here. \n"
			+ "They will be used whenever the PLCnCLI calls cmake.";
	
	private Text text;
	private Color grey = new Color(Display.getCurrent(), 190, 190, 190);
	private Color black = new Color(Display.getCurrent(), 0, 0, 0);
	private boolean exampleIsShown = false;
	private File cmakeFlagsFile;
	
	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		project = page.getProject();
		IPath directory = project.getLocation();
		cmakeFlagsFile = new File(directory.toOSString(), cmakeFlagsFileName);
		
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginBottom = 5;
		gridLayout.marginTop = 5;
		gridLayout.marginLeft = 5;
		gridLayout.marginRight = 5;
		usercomp.setLayout(gridLayout);
				
		setupLabel(usercomp, topLabel, 1, SWT.MULTI);
		
		
		text = new Text(usercomp, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		GridData layout = new GridData(SWT.FILL, SWT.FILL, true, true);
		layout.verticalIndent = 5;
		text.setLayoutData(layout);
		
		initializeText();
	}

	private void initializeText() {
		
		loadTextFromFile();
		setExampleIfEmpty();		
				
		text.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				setExampleIfEmpty();
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				clearIfExampleWasShown();
			}
		});
		
	}
	
	private void loadTextFromFile()
	{
		if (!cmakeFlagsFile.exists()) {
			return;
		}
		
		try {
			String fileContent = Files.readString(cmakeFlagsFile.toPath());
			text.setText(fileContent);
			
		} catch (IOException e) {
			Activator.getDefault().logError("Error while trying to read "+cmakeFlagsFileName, e);
		}
	}
	
	private void writeFileWithTextContent()
	{
		if(!exampleIsShown)
		{
			try 
			{
				Files.writeString(cmakeFlagsFile.toPath(), text.getText(), StandardOpenOption.WRITE, 
						StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
			} catch (IOException e) {
				Activator.getDefault().logError("Error while trying to write "+cmakeFlagsFileName, e);
			}
		}
		else 
		{
			if(cmakeFlagsFile.exists())
			{
				cmakeFlagsFile.delete();
			}
		}
	}
	
	private void setExampleIfEmpty() 
	{
		if(text.getText().isBlank())
		{
			text.setForeground(grey);
			text.setText("Example:\r\n"
					+ "-G \"Unix Makefiles\"\r\n"
					+ "-DCMAKE_MAKE_PROGRAM=\"mymakepath\"");
			exampleIsShown = true;
		}
	}
	
	private void clearIfExampleWasShown() {
		if(exampleIsShown)
		{
			text.setForeground(black);
			text.setText("");
			exampleIsShown = false;
		}
	}

	@Override
	public boolean canBeVisible() {
		
		if(project != null)
		{
			try {
				if(project.hasNature(PlcProjectNature.NATURE_ID))
				{
					return true;
				}
			} catch (CoreException e) {
			}
		}
		return false;
	}


	@Override
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) {
		writeFileWithTextContent();
	}
	
	@Override
	protected void performOK() {
		writeFileWithTextContent();
	}


	@Override
	protected void performDefaults() {
	}


	@Override
	protected void updateData(ICResourceDescription cfg) {
	}


	@Override
	protected void updateButtons() {
	}

}
