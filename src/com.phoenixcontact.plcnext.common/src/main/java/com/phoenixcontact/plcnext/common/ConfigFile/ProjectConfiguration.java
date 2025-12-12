/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.ConfigFile;

import javax.xml.bind.annotation.*;

import com.phoenixcontact.plcnext.common.Activator;

/**
 * Model for the project configuration file
 */
@XmlRootElement(name="ProjectConfiguration", namespace = "http://www.phoenixcontact.com/schema/projectconfiguration")
public class ProjectConfiguration {

	private String libraryVersion = "";
	private String libraryDescription = "";
	private String engineerVersion = "";
	private LibraryInfo[] libraryInfos = null;
	private ExcludedFiles excludedFiles = null;
	private boolean sign = false;
	private String pkcs12 = "";
	private String privateKey = "";
	private String signingCertificate = "";
	private String publicKey = "";
	private Certificates certificates = null;
	private CertificateChain certificateChain = null;
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
	
	@XmlElements(value = { @XmlElement(name = "LibraryInfo", type=LibraryInfo.class, namespace = "http://www.phoenixcontact.com/schema/projectconfiguration") })
	public LibraryInfo[] getLibraryInfos() {
		return libraryInfos;
	}
	public void setLibraryInfos(LibraryInfo[] libraryInfos) {
		this.libraryInfos = libraryInfos;
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
	
	@XmlElement(name="SigningCertificate", namespace = "http://www.phoenixcontact.com/schema/projectconfiguration")
	public String getSigningCertificate() {
		if((signingCertificate == null || signingCertificate.isEmpty())
				&& publicKey != null && !publicKey.isEmpty())
		{
			return publicKey;
		}
		return signingCertificate;
	}
	public void setSigningCertificate(String signingCertificate) {
		this.signingCertificate = signingCertificate;
	}
	@XmlElement(name="PublicKey", namespace = "http://www.phoenixcontact.com/schema/projectconfiguration")
	public String getPublicKey() {
		return null;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	
	@XmlElement(name="CertificateChain", namespace = "http://www.phoenixcontact.com/schema/projectconfiguration")
	public CertificateChain getCertificateChain() {
		if((certificateChain == null || certificateChain.getFiles() == null
				|| certificateChain.getFiles().length < 1)
				&& certificates != null && certificates.getFiles() != null)			
		{
			CertificateChain result = new CertificateChain();
			result.setFiles(certificates.getFiles());
			return result;
		}
		
		return certificateChain;
	}
	public void setCertificateChain(CertificateChain certificateChain) {
		this.certificateChain = certificateChain;
	}
	
	@XmlElement(name="Certificates", namespace = "http://www.phoenixcontact.com/schema/projectconfiguration")
	public Certificates getCertificates() {
		return null;
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
	
	public boolean validateAndUpdateFile()
	{
		if(publicKey != null && !publicKey.isEmpty()
				&& signingCertificate != null && !signingCertificate.isEmpty())
		{
			Activator.getDefault().logWarning("PublicKey and SigningCertificate cannot both be inside the Config File.");
			return false;
		}
		
		if(certificates != null && certificates.getFiles() != null 
				&& certificates.getFiles().length > 0
				&& certificateChain != null && certificateChain.getFiles() != null
				&& certificateChain.getFiles().length > 0)
		{
			Activator.getDefault().logWarning("Certificates and CertificateChain cannot both be inside the Config File.");
			return false;
		}
		
		
		return true;
	}
	
	public boolean hasContent()
	{
		return !((engineerVersion == null || engineerVersion.isBlank()) 
		&& (libraryDescription == null || libraryDescription.isBlank())
		&& (libraryVersion == null || libraryVersion.isBlank()) 
		&& (libraryInfos == null || libraryInfos.length < 1)
		&& (excludedFiles == null || excludedFiles.getFiles().length < 1)
		&& sign == false
		&& (pkcs12 == null || pkcs12.isBlank())
		&& (privateKey == null || privateKey.isBlank())
		&& (publicKey == null || publicKey.isBlank())
		&& (signingCertificate == null || signingCertificate.isBlank())
		&& (certificates == null || certificates.getFiles().length < 1)
		&& (certificateChain == null || certificateChain.getFiles().length < 1)
		&& (timestamp == false)
		&& (timestampConfiguration == null || timestampConfiguration.isBlank()));
	}
	
	public void setEmptyPropertiesToNull()
	{
		libraryVersion = setEmptyStringToNull(libraryVersion);
		libraryDescription = setEmptyStringToNull(libraryDescription);
		engineerVersion = setEmptyStringToNull(engineerVersion);
		pkcs12 = setEmptyStringToNull(pkcs12);
		privateKey = setEmptyStringToNull(privateKey);
		signingCertificate = setEmptyStringToNull(signingCertificate);
		publicKey = setEmptyStringToNull(publicKey);
		timestampConfiguration = setEmptyStringToNull(timestampConfiguration);
		
//		if(libraryInfos != null && libraryInfos.length == 0)
//			libraryInfos = null;
		
		if(excludedFiles != null)
		{
			if(excludedFiles.getFiles() == null
					|| excludedFiles.getFiles().length == 0)
			{
				excludedFiles = null;
			}
		}
		
		if(certificateChain != null)
		{
			if(certificateChain.getFiles() == null
					|| certificateChain.getFiles().length == 0)
			{
				certificateChain = null;
			}
		}
		
		if(certificates != null)
		{
			if(certificates.getFiles() == null
					|| certificates.getFiles().length == 0)
			{
				certificates = null;
			}
		}
	}
	
	private static String setEmptyStringToNull(String input)
	{
		if(input == null) return null;
		
		if(input.isEmpty())return null;
		
		return input;
	}
	
}
