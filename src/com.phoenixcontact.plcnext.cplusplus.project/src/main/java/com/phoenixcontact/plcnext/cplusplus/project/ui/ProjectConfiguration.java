/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.project.ui;

import javax.xml.bind.annotation.*;
/**
 * Model for the project configuration file
 */
@XmlRootElement(name="ProjectConfiguration", namespace = "http://www.phoenixcontact.com/schema/projectconfiguration")
public class ProjectConfiguration {

	private String libraryVersion = "";
	private String libraryDescription = "";
	private String engineerVersion = "";
	private ExcludedFiles excludedFiles = null;
		
	@XmlElement(name="LibraryVersion", namespace = "http://www.phoenixcontact.com/schema/projectconfiguration")
	public String getLibraryVersion() {
		return libraryVersion;
	}
	public void setLibraryVersion(String libraryVersion) {
		this.libraryVersion = libraryVersion;
	}
	
	@XmlElement(name="LibraryDescription", namespace = "http://www.phoenixcontact.com/schema/projectconfiguration")
	public String getLibraryDescription() {
		return libraryDescription;
	}
	public void setLibraryDescription(String libraryDescription) {
		this.libraryDescription = libraryDescription;
	}
	
	@XmlElement(name="EngineerVersion", namespace = "http://www.phoenixcontact.com/schema/projectconfiguration")
	public String getEngineerVersion() {
		return engineerVersion;
	}
	public void setEngineerVersion(String engineerVersion) {
		this.engineerVersion = engineerVersion;
	}
	
	@XmlElement(name="ExcludedFiles", namespace = "http://www.phoenixcontact.com/schema/projectconfiguration")
	public ExcludedFiles getExcludedFiles() {
		return excludedFiles;
	}
	public void setExcludedFiles(ExcludedFiles excludedFiles) {
		this.excludedFiles = excludedFiles;
	}
}
