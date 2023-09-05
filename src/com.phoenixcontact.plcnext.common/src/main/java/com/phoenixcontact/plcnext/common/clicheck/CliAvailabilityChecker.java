/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.clicheck;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.widgets.Display;
import com.phoenixcontact.plcnext.common.Activator;
import com.phoenixcontact.plcnext.common.CliDescription;
import com.phoenixcontact.plcnext.common.EclipseContextHelper;
import com.phoenixcontact.plcnext.common.IDIHost;
import com.phoenixcontact.plcnext.common.Messages;
import com.phoenixcontact.plcnext.common.preferences.PreferenceConstants;

/**
 * Background job to check cli availability and download / update cli if
 * necessary
 *
 */
public class CliAvailabilityChecker {

	private String cliName;
	private CliDescription cliInformation;
	boolean downloadCli = false;

	/**
	 * This constructor is used at tool startup
	 */
	public CliAvailabilityChecker() {
		IEclipseContext context = EclipseContextHelper.getActiveContext();
		IDIHost host = ContextInjectionFactory.make(IDIHost.class, context);
		cliInformation = host.getExport(CliDescription.class);

		downloadCli = true;
	}

	/**
	 * @param info
	 * 
	 *             this constructor is used for the commandreceiver
	 */
	public CliAvailabilityChecker(CliDescription info) {
		cliInformation = info;
	}

	/**
	 * Searches for cli in preferences, path and workspace
	 * 
	 * @return ok if cli was found, error otherwise
	 * 
	 */
	public IStatus checkAvailability() {
		/*
		 * 
		 * 
		 * 1. search preferences 2. search path (update preferences?) 3. search
		 * workspace (update preferences?)
		 * 
		 * check if available before each command execution and if not available do this
		 * search again
		 * 
		 * search for cli in workspace -> if more than one found-> selection which to
		 * take? or first that was found) name currently taken from preferences CLIName
		 * 
		 * 
		 * 
		 */

		// 1. search preferences
		cliName = cliInformation.getCliName();
		if (checkPreferences()) {
			return Status.OK_STATUS;
		}

		// 2. search path
		if (checkPath()) {
			return Status.OK_STATUS;
		}

		// 3. search workspace
		IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();

		try {
			List<File> files = searchRecursiveForFile(workspace.getLocation().toOSString(), new ArrayList<File>());
			if (!files.isEmpty()) {
				// take first found
				IPath path = Path.fromOSString(files.get(0).getAbsolutePath());
				cliInformation.setCliPath(path.removeLastSegments(1).toOSString());
				return Status.OK_STATUS;
			}
		} catch (CoreException e) {
			Activator.getDefault().logError(Messages.CliAvailabilityChecker_errorSearchingWorkspace + cliName, e);
			e.printStackTrace();
			return new Status(Status.ERROR, Activator.PLUGIN_ID,
					Messages.CliAvailabilityChecker_errorSearchingWorkspace + cliName);
		}

		// 4. if not found open dialog to select download cli or edit preferences
		if (downloadCli) {
			suggestDownload();
			return Status.OK_STATUS;
		}

		return new Status(Status.ERROR, Activator.PLUGIN_ID, Messages.CliAvailabilityChecker_errorCLINotFound);
	}

	private boolean checkPreferences() 
	{
		String cliName = Platform.getPreferencesService().getString(Activator.PLUGIN_ID, PreferenceConstants.P_CLI_NAME, "", null);
		if(cliInformation.getCliName() != cliName)
		{
			cliInformation.setCliName(cliName);
		}
		String cliPath = Platform.getPreferencesService().getString(Activator.PLUGIN_ID, PreferenceConstants.P_CLI_PATH, "", null);
		if (new File(cliPath + Path.SEPARATOR + cliName).exists() && cliName != null && !cliName.isEmpty()
				&& cliPath != null && !cliPath.isEmpty()) 
		{
			if(!cliInformation.getCliPath().equals(cliPath))
			{
				cliInformation.setCliPath(cliPath);
			}
			return true;
		}
		return false;
	}

	private boolean checkPath() {
		String path = System.getenv("Path");
		if (path != null) {
			String[] pathVar = path.split(";"); //$NON-NLS-1$ 
			for (String var : pathVar) {

				File root = new File(var);
				File[] members = root.listFiles();

				if (members == null)
					continue;

				for (File member : members) {
					if (!member.isDirectory() && member.getName().equals(cliName)) {
						cliInformation.setCliPath(var);
						return true;
					}
				}
			}
		}
		return false;
	}

	private List<File> searchRecursiveForFile(String path, List<File> files) throws CoreException {
		File root = new File(path);
		File[] members = root.listFiles();

		if (members == null)
			return files;

		for (File member : members) {
			if (member.isDirectory()) {
				files = searchRecursiveForFile(member.getAbsolutePath(), files);
			} else {
				if (member.getName().equals(cliName)) {
					files.add(member);
				}
			}
		}
		return files;
	}

	private void suggestDownload() {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				CliSourceDialog dialog = new CliSourceDialog(null, cliInformation);
				dialog.open();
			}
		});
	}
}