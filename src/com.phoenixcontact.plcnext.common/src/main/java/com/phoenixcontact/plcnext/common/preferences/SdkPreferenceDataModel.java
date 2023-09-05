/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.preferences;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DataModel for the SdkPreferencePage
 */
public class SdkPreferenceDataModel
{
	private List<InstallSdk> installSdks = new ArrayList<InstallSdk>();
	
	private List<String> setSdks = new ArrayList<String>();
	
	private List<String> removeSdks = new ArrayList<String>();
	
	protected void addSdkForInstall(String archive, String destination, boolean force)
	{
		if(removeSdks.remove(destination)) return;
		
		installSdks.add(new InstallSdk(archive, destination, force));
	}
	
	protected void addSdkForSet(String sdk)
	{
		if(removeSdks.remove(sdk)) return;
		
		setSdks.add(sdk);
	}
	
	protected void removeSdk(String sdk)
	{
		if(installSdks.removeIf(x -> x.getDestination().equals(sdk))) return;
		if(setSdks.remove(sdk)) return;
		
		removeSdks.add(sdk);
	}
	
	protected List<InstallSdk> getInstallSdks()
	{
		return installSdks;
	}

	protected List<String> getSetSdks()
	{
		return setSdks.stream().distinct().collect(Collectors.toList());
	}
	
	protected List<String> getRemoveSdks()
	{
		return removeSdks.stream().distinct().collect(Collectors.toList());
	}
	
	protected class InstallSdk
	{
		private String archive;
		private String destination;
		private boolean force;
		
		protected InstallSdk(String archive, String destination, boolean force)
		{
			this.archive = archive;
			this.destination = destination;
			this.force = force;
		}
		
		protected String getArchive()
		{
			return archive;
		}
		
		protected String getDestination()
		{
			return destination;
		}
		
		protected boolean getForce()
		{
			return force;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if(obj instanceof InstallSdk) {
				return ((InstallSdk)obj).destination.equals(this.destination);
			}
			if(obj instanceof String) {
				return ((String)obj).equals(this.destination);
			}
			return super.equals(obj);
		}
	}
}
