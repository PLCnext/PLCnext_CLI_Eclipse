/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.toolchains;

import java.util.Comparator;

import com.phoenixcontact.plcnext.common.commands.results.Target;


/**
 * Class which compares two ProjectTarget elements.
 */
public class TargetComparator implements Comparator<Target> {

	@Override
	public int compare(Target o1, Target o2) {
		int result = o1.getShortVersion().compareTo(o2.getShortVersion());
		if(result == 0)
		{
			return o2.getName().compareTo(o1.getName());
		}
		return result;
	}
}
