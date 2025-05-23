/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.ConfigFile;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Certificates", namespace = "http://www.phoenixcontact.com/schema/projectconfiguration")
public class Certificates
{
	private String[] files = new String[0];
	
	public Certificates(String[] files)
	{
		this.files = files;
	}
	
	public Certificates() 
	{
	}
	
	@XmlElement(name="File", namespace = "http://www.phoenixcontact.com/schema/projectconfiguration")
	public String[] getFiles() {
		return files;
	}
	public void setFiles(String[] files) {
		this.files = files;
	}
}
