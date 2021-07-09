/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.toolchains.internal;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCommandGenerator;
import org.eclipse.cdt.utils.cdtvariables.IVariableSubstitutor;

import com.phoenixcontact.plcnext.cplusplus.toolchains.Activator;

/**
 *
 */
public class OptionWithValueCommandGenerator implements IOptionCommandGenerator {

	/**
	 * 
	 */
	public OptionWithValueCommandGenerator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String generateCommand(IOption option, IVariableSubstitutor macroSubstitutor) {
		
		try {
			String value = option.getStringValue();
			if(value != null && !value.isBlank())
			{
				return option.getCommand()+" \""+value+"\"";
			}
			return null;
		} catch (BuildException e) {
			Activator.getDefault().logError("Could not set build property: "+option.getName(), e);
			return null;
		}
	}

}
