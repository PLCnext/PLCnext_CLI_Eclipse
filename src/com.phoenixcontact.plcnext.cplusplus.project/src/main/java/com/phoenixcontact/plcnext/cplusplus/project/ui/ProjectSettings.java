/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.project.ui;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "target",
    "id",
    "version",
    "name",
    "extension",
    "type",
    "generateDTArrayNameByType",
    "cSharpProjectPath",
    "generateNamespaces"
})
@XmlRootElement(name = "ProjectSettings", namespace = "http://www.phoenixcontact.com/schema/cliproject")
public class ProjectSettings {

    @XmlElement(name = "Target", namespace = "http://www.phoenixcontact.com/schema/cliproject")
    protected List<String> target;
    @XmlElement(name = "Id", namespace = "http://www.phoenixcontact.com/schema/cliproject")
    protected String id;
    @XmlElement(name = "Version", namespace = "http://www.phoenixcontact.com/schema/cliproject")
    protected String version;
    @XmlElement(name = "Name", required = true, namespace = "http://www.phoenixcontact.com/schema/cliproject")
    protected String name;
    @XmlElement(name = "Extension", namespace = "http://www.phoenixcontact.com/schema/cliproject")
    protected List<Extension> extension;
    @XmlElement(name = "Type", defaultValue = "project", namespace = "http://www.phoenixcontact.com/schema/cliproject")
    protected String type;
    @XmlElement(name = "GenerateDTArrayNameByType", defaultValue = "false", namespace = "http://www.phoenixcontact.com/schema/cliproject")
    protected boolean generateDTArrayNameByType;
    @XmlElement(name = "CSharpProjectPath", namespace = "http://www.phoenixcontact.com/schema/cliproject")
    protected String cSharpProjectPath;
    @XmlElement(name = "GenerateNamespaces", namespace = "http://www.phoenixcontact.com/schema/cliproject")
    protected boolean generateNamespaces;

    
    public List<String> getTarget() {
        if (target == null) {
            target = new ArrayList<String>();
        }
        return this.target;
    }
    public void setId(String value) {
        this.id = value;
    }
    public void setVersion(String value) {
        this.version = value;
    }
    public void setName(String value) {
        this.name = value;
    }
    public void setType(String value) {
        this.type = value;
    }
    public void setGenerateDTArrayNameByType(boolean value) {
        this.generateDTArrayNameByType = value;
    }
    public void setCSharpProjectPath(String value) {
        this.cSharpProjectPath = value;
    }
    public void setGenerateNamespaces(boolean value) {
        this.generateNamespaces = value;
    }
    public boolean getGenerateNamespaces() {
        return generateNamespaces;
    }

}
