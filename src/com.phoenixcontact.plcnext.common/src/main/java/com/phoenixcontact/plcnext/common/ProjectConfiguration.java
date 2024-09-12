/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common;

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
	private boolean sign = false;
	private String pkcs12 = "";
	private String privateKey = "";
	private String publicKey = "";
	private Certificates certificates = null;
	private String timestampConfiguration = "";
	private boolean timestamp = false;
	private boolean noTimestamp = false;
		
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
	
	@XmlElement(name="Sign", namespace = "http://www.phoenixcontact.com/schema/projectconfiguration")
	public boolean getSign() {
		return sign;
	}
	public void setSign(boolean sign) {
		this.sign = sign;
	}
	
	@XmlElement(name="Pkcs12", namespace = "http://www.phoenixcontact.com/schema/projectconfiguration")
	public String getPkcs12() {
		return pkcs12;
	}
	public void setPkcs12(String pkcs12) {
		this.pkcs12 = pkcs12;
	}
	
	@XmlElement(name="PrivateKey", namespace = "http://www.phoenixcontact.com/schema/projectconfiguration")
	public String getPrivateKey() {
		return privateKey;
	}
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}
	
	@XmlElement(name="PublicKey", namespace = "http://www.phoenixcontact.com/schema/projectconfiguration")
	public String getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	
	@XmlElement(name="Certificates", namespace = "http://www.phoenixcontact.com/schema/projectconfiguration")
	public Certificates getCertificates() {
		return certificates;
	}
	public void setCertificates(Certificates certificates) {
		this.certificates = certificates;
	}
	
	@XmlElement(name="TimestampConfiguration", namespace = "http://www.phoenixcontact.com/schema/projectconfiguration")
	public String getTimestampConfiguration() {
		return timestampConfiguration;
	}
	public void setTimestampConfiguration(String timestampConfiguration) {
		this.timestampConfiguration = timestampConfiguration;
	}
	
	@XmlElement(name="Timestamp", namespace = "http://www.phoenixcontact.com/schema/projectconfiguration")
	public boolean getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(boolean timestamp) {
		this.timestamp = timestamp;
	}
	
	@XmlElement(name="NoTimestamp", namespace = "http://www.phoenixcontact.com/schema/projectconfiguration")
	public boolean getNoTimestamp() {
		return noTimestamp;
	}
	public void setNoTimestamp(boolean noTimestamp) {
		this.noTimestamp = noTimestamp;
	}
}
