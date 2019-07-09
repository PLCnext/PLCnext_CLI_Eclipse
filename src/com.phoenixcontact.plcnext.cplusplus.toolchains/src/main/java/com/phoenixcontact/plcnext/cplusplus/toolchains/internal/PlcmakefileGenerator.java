/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.toolchains.internal;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;

/**
 * This class specifies the build output directory for the PLCncli Builder.
 *
 */
public class PlcmakefileGenerator implements IManagedBuilderMakefileGenerator {

	@Override
	public void generateDependencies() throws CoreException {
	}

	@Override
	public MultiStatus generateMakefiles(IResourceDelta delta) throws CoreException {
		return new MultiStatus(
				ManagedBuilderCorePlugin.getUniqueIdentifier(),
				IStatus.OK,
				"", //$NON-NLS-1$
				null);
	}

	@Override
	public IPath getBuildWorkingDir() {
		return new Path("bin");
	}

	@Override
	public String getMakefileName() {
		return null;
	}

	@Override
	public void initialize(IProject project, IManagedBuildInfo info, IProgressMonitor monitor) {
	}

	@Override
	public boolean isGeneratedResource(IResource resource) {
		return false;
	}

	@Override
	public void regenerateDependencies(boolean force) throws CoreException {
	}

	@Override
	public MultiStatus regenerateMakefiles() throws CoreException {
		return new MultiStatus(
				ManagedBuilderCorePlugin.getUniqueIdentifier(),
				IStatus.OK,
				"", //$NON-NLS-1$
				null);
	}
}
