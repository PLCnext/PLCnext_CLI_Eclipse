/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.ConfigFile;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name="LibraryInfo", namespace = "http://www.phoenixcontact.com/schema/projectconfiguration")
public class LibraryInfo
{
	private String name = "";
	private String text = "";
	
	public LibraryInfo(String name, String text)
	{
		this.name = name;
		this.text = text;
	}
	
	public LibraryInfo() 
	{
	}
	
	@XmlAttribute(required = true, name="name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@XmlValue
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
}
