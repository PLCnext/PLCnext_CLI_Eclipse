/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import com.phoenixcontact.plcnext.common.preferences.PreferenceConstants;

import jakarta.inject.Singleton;

/**
 * Instance of this class holds information about the cli like path or name
 */
@Creatable
@Singleton
public class CliDescription {
	
	private String cliPath;
	private String cliName;
	
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	
	/**
	 * Sets cli path and name according to preferences values, and adds listener to preference changes
	 */
	public CliDescription() {
		super();
		cliName = Platform.getPreferencesService().getString(Activator.PLUGIN_ID, PreferenceConstants.P_CLI_NAME, "", null);
		cliPath = Platform.getPreferencesService().getString(Activator.PLUGIN_ID, PreferenceConstants.P_CLI_PATH, "", null);
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener(){
			
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if(event.getProperty() == PreferenceConstants.P_CLI_PATH) {
					setCliPath( event.getNewValue().toString());
				}
				if(event.getProperty() == PreferenceConstants.P_CLI_NAME) {
					setCliName( event.getNewValue().toString());
				}
			}
		});
	}
	
	/**
	 * @param cliPath path to the directory, where the cli executable is
	 */
	public void setCliPath(String cliPath) {
		String oldValue = this.cliPath;
		this.cliPath = cliPath;
		propertyChangeSupport.firePropertyChange("cliPath", oldValue, cliPath);
	}
	
	/**
	 * @return the path to the directory, where the cli executable is
	 */
	public String getCliPath() {
		return cliPath;
	}
	
	/**
	 * @return the name of the cli executable file
	 */
	public String getCliName() {
		return cliName;
	}
	
	/**
	 * @param name name of the plcncli executable
	 */
	public void setCliName(String name) {
		String oldValue = this.cliName;
		this.cliName = name;
		propertyChangeSupport.firePropertyChange("cliName", oldValue, name);
	}
	
	/**
	 * @return true, if the combination of stored path and name points to an existing file, false otherwise
	 */
	public boolean cliExists() {
		if(cliName == null || cliName.isEmpty()
				|| cliPath == null || cliPath.isEmpty())
			return false;
		return new File(cliPath + Path.SEPARATOR + cliName).exists();
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		propertyChangeSupport.addPropertyChangeListener(listener);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		propertyChangeSupport.removePropertyChangeListener(listener);
	}
}
