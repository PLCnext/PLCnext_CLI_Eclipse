/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.project.Import;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.resources.RefreshScopeManager;
import org.eclipse.cdt.core.settings.model.CSourceEntry;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

import com.phoenixcontact.plcnext.common.EclipseContextHelper;
import com.phoenixcontact.plcnext.common.ICommandManager;
import com.phoenixcontact.plcnext.common.IDIHost;
import com.phoenixcontact.plcnext.common.ProcessExitedWithErrorException;
import com.phoenixcontact.plcnext.common.commands.Command;
import com.phoenixcontact.plcnext.common.commands.GenerateCodeCommand;
import com.phoenixcontact.plcnext.common.commands.GetProjectInformationCommand;
import com.phoenixcontact.plcnext.common.commands.results.GetProjectInformationCommandResult;
import com.phoenixcontact.plcnext.common.commands.results.GetProjectInformationCommandResult.PLCnCLIProjectType;
import com.phoenixcontact.plcnext.cplusplus.project.Activator;
import com.phoenixcontact.plcnext.cplusplus.project.PlcProjectNature;
import com.phoenixcontact.plcnext.cplusplus.project.acfproject.PlcnextAcfProjectNature;
import com.phoenixcontact.plcnext.cplusplus.toolchains.ToolchainConfigurator;

/**
 *
 */
public class WizardImportPlcProjectPage extends WizardPage
{
	private final String standardMessage = "Import existing PLCnCLI project into workspace";
//	private final String errorEmptyName = "Project name cannot be empty";
	private final String errorEmptyFile = "PLCnCLI project file cannot be empty";
	private final String warningFilesNotCopied = "Project files not copied into workspace. Modifications will be done directly on project.";
	Text rootFile;
//	Text projectName;
	Button copyFilesButton;

	protected WizardImportPlcProjectPage()
	{
		super("Page Name");
		setTitle("Import PLCnCLI Projects");
		setMessage(standardMessage);
	}

	@Override
	public void createControl(Composite parent)
	{
		initializeDialogUnits(parent);

		Composite control = new Composite(parent, SWT.NONE);
		setControl(control);

		control.setLayout(new GridLayout());
		control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// name
//		Group nameGroup = new Group(control, SWT.NONE);
//		nameGroup.setLayout(new GridLayout());
//		nameGroup.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
//		nameGroup.setText("Project Name");
//
//		projectName = new Text(nameGroup, SWT.BORDER);
//		projectName.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

		// root
		Group rootGroup = new Group(control, SWT.NONE);
		rootGroup.setLayout(new GridLayout(3, false));
		rootGroup.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		rootGroup.setText("Select PLCnCLI Project");

		Label rootLabel = new Label(rootGroup, SWT.NONE);
		rootLabel.setText("PLCnCLI Project File:");

		rootFile = new Text(rootGroup, SWT.BORDER);
		rootFile.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

		Button browseButton = new Button(rootGroup, SWT.PUSH);
		browseButton.setText("B&rowse...");
		setButtonLayoutData(browseButton);

		// options
		Group optionsGroup = new Group(control, SWT.NONE);
		optionsGroup.setLayout(new RowLayout(SWT.VERTICAL));
		optionsGroup.setText("Options");

		copyFilesButton = new Button(optionsGroup, SWT.CHECK);
		copyFilesButton.setText("&Copy files into workspace");
		copyFilesButton.setSelection(true);

		copyFilesButton.addListener(SWT.Selection, event -> validatePage());

		browseButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				handleBrowseButtonPressed();
			}
		});

//		projectName.addModifyListener(new ModifyListener()
//		{
//			@Override
//			public void modifyText(ModifyEvent e)
//			{
//				handleProjectNameModified();
//			}
//		});

		rootFile.addModifyListener(new ModifyListener()
		{

			@Override
			public void modifyText(ModifyEvent e)
			{
				handleRootDirectoryModified();
			}
		});

		setErrorMessage(errorEmptyFile);
	}

	private void handleBrowseButtonPressed()
	{
		FileDialog dialog = new FileDialog(getShell(), SWT.SHEET);
		dialog.setText("Select PLCnCLI project file");

		String selectedFile = dialog.open();
		if (selectedFile != null)
		{
			rootFile.setText(selectedFile);
		}
	}

//	private void handleProjectNameModified()
//	{
//		validatePage();
//	}

	private void handleRootDirectoryModified()
	{
		validatePage();
	}

	private void validatePage()
	{
//		String name = projectName.getText();
//		if (name == null || name.isEmpty())
//		{
//			setErrorMessage(errorEmptyName);
//			return;
//		}

		String directory = rootFile.getText();
		File projectFile = new File(directory);
		if (directory == null || directory.isEmpty())
		{
			setErrorMessage(errorEmptyFile);
			return;
		}
		if (!projectFile.exists())
		{
			setErrorMessage(directory + " does not exist.");
			return;
		}

		setErrorMessage(null);

		if (!copyFilesButton.getSelection())
		{
			setMessage(warningFilesNotCopied, WARNING);
			return;
		}
		setMessage(standardMessage);
	}

	protected boolean importProject()
	{
		WorkspaceModifyOperation operation = new WorkspaceModifyOperation()
		{
			@Override
			protected void execute(IProgressMonitor monitor)
					throws CoreException, InvocationTargetException, InterruptedException
			{
				IStatus result = createPLCProject(monitor);
				if (!result.isOK())
				{
					throw new InvocationTargetException(new CoreException(result));
				}
			}
		};

		try
		{
			getContainer().run(false, true, operation);
		} catch (InvocationTargetException e)
		{
			IStatus status;
			Throwable throwable = e.getTargetException();
			if (throwable instanceof CoreException)
			{
				status = ((CoreException) throwable).getStatus();
			} else
			{
				status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Problem occured during import", throwable);
			}

			ErrorDialog.openError(getShell(), "Problem occured during import", null, status);
			if (status.getSeverity() == IStatus.WARNING)
				return true;
			return false;
		} catch (InterruptedException e)
		{
			return false;
		}
		return true;
	}

	private IStatus createPLCProject(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
	{
		SubMonitor subMonitor = SubMonitor.convert(monitor);
		subMonitor.beginTask("Import PLCnCLI Project", 20);
		subMonitor.worked(2);

		String projectFileLocation = rootFile.getText();
		boolean copyFiles = copyFilesButton.getSelection();
		IOverwriteQuery overwriteQuery = new IOverwriteQuery()
		{
			@Override
			public String queryOverwrite(String pathString)
			{
				// TODO let user choose
				return IOverwriteQuery.NO;
			}
		};

		IEclipseContext context = EclipseContextHelper.getActiveContext();
		IDIHost host = ContextInjectionFactory.make(IDIHost.class, context);
		ICommandManager commandManager = host.getExport(ICommandManager.class);

		Map<String, String> options = new HashMap<>();
		options.put(GetProjectInformationCommand.OPTION_PATH, projectFileLocation);
		options.put(GetProjectInformationCommand.OPTION_NO_INCLUDE_DETECTION, null);

		String name;
		PLCnCLIProjectType type = PLCnCLIProjectType.project;
		try
		{
			GetProjectInformationCommandResult projectInfo = commandManager
					.executeCommand(commandManager.createCommand(options, GetProjectInformationCommand.class), false,
							monitor)
					.convertToTypedCommandResult(GetProjectInformationCommandResult.class);
			name = projectInfo.getName();
			type = projectInfo.getType();
		} catch (ProcessExitedWithErrorException e1)
		{
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"The specified location does not contain a valid plcncli project.", e1);
		}
		subMonitor.worked(2);

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(name);

		// check if project with this name exists already in workspace
		if (project.exists())
		{
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"A project with name '" + name + "' already exists in the workspace.");
		}

		IProjectDescription description = workspace.newProjectDescription(name);

		// validate new project location

		IPath projectRootDirectory = new Path(projectFileLocation).removeLastSegments(1);
		IStatus result = ResourcesPlugin.getWorkspace().validateProjectLocation(project, projectRootDirectory);

		if (!result.isOK())
		{
			return result;
		}

		String infoMessage = null;
		if (!copyFiles)
		{
			// set project location
			description.setLocation(projectRootDirectory);
			infoMessage = "Project files were not copied into workspace. "
					+ "All changes will be done directly on "+projectRootDirectory;
		} else
		{
			// import files into project
			File importRoot = projectRootDirectory.toFile();
			ImportOperation importop = new ImportOperation(project.getFullPath(), importRoot,
					FileSystemStructureProvider.INSTANCE, overwriteQuery,
					FileSystemStructureProvider.INSTANCE.getChildren(importRoot));
			importop.setCreateContainerStructure(false);
			importop.run(null);
			infoMessage = "Copied project files from "+importRoot.getAbsolutePath()+" to "+project.getLocation().toOSString();
			
			// clean copied project -> if cmake cache exists, it is invalid now for copied project
			try
			{
				// *********************delete intermediate folder*********************
				IFolder intermediateFolder = project.getFolder("intermediate"); //$NON-NLS-1$
				intermediateFolder.delete(true, null);

				// *********************delete bin folder*****************************
				IFolder binFolder = project.getFolder("bin"); //$NON-NLS-1$
				binFolder.delete(true, null);
				binFolder.create(false, false, subMonitor.split(1));

			} catch (CoreException e)
			{
				Activator.getDefault().logError("Error while trying to clean project during import.", e);
			}
		}

		try
		{
			subMonitor.worked(1);
			// create c project and add ccnature
			CCorePlugin.getDefault().createCProject(description, project, subMonitor.split(2), project.getName());
			CCProjectNature.addCCNature(project, subMonitor.split(2));

			// add managed build nature and plprojectnature
			ManagedBuildManager.createBuildInfo(project);
			ManagedCProjectNature.addManagedNature(project, subMonitor.split(2));
			if (type == PLCnCLIProjectType.acfproject)
				ManagedCProjectNature.addNature(project, PlcnextAcfProjectNature.NATURE_ID, subMonitor.split(2));
			
			ManagedCProjectNature.addNature(project, PlcProjectNature.NATURE_ID, subMonitor.split(2));
			ManagedCProjectNature.addManagedBuilder(project, subMonitor.split(2));

			IProjectType projectType;
			if (type == PLCnCLIProjectType.acfproject)
				projectType = ManagedBuildManager
						.getProjectType("com.phoenixcontact.plcnext.cplusplus.toolchains.acfprojectType");
			else
				projectType = ManagedBuildManager
						.getProjectType("com.phoenixcontact.plcnext.cplusplus.toolchains.projectType");

			IManagedProject managedProject = ManagedBuildManager.createManagedProject(project, projectType);
			subMonitor.worked(1);

			// copy configurations and mark src as source folder
			IFolder srcFolder = project.getFolder("src");
			ICSourceEntry sourceEntry = new CSourceEntry(srcFolder, null, 0);

			IConfiguration[] configurations = projectType.getConfigurations();
			for (IConfiguration configuration : configurations)
			{
				IConfiguration newConfiguration = managedProject.createConfiguration(configuration,
						ManagedBuildManager.calculateChildId(configuration.getId(), null));

				newConfiguration.setSourceEntries(new ICSourceEntry[] { sourceEntry });
			}

			IConfiguration cfgs[] = managedProject.getConfigurations();
			if (cfgs.length > 0)
			{
				ManagedBuildManager.setDefaultConfiguration(project, cfgs[0]);
			}

			ManagedBuildManager.getBuildInfo(project).setValid(true);
			subMonitor.worked(1);

		} catch (BuildException e)
		{
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Problem occured while importing PLCnCLI project", e);
		} catch (CoreException e)
		{
			return e.getStatus();
		}

		Map<String, String> generateOptions = new HashMap<>();
		generateOptions.put(GenerateCodeCommand.OPTION_PATH, project.getLocation().toOSString());
		Command generateCommand = commandManager.createCommand(generateOptions, GenerateCodeCommand.class);
		try
		{
			commandManager.executeCommand(generateCommand, false, monitor);
			
			new ToolchainConfigurator().configureProject(name, monitor);
			subMonitor.worked(3);
			
		} catch (ProcessExitedWithErrorException e)
		{
			return new Status(IStatus.WARNING, Activator.PLUGIN_ID,
					"PLCnCLI code generation could not be executed. \n Project might have unresolved inclusions.", e);
		}

		try
		{
			RefreshScopeManager refreshManager = RefreshScopeManager.getInstance();
			IWorkspaceRunnable runnable = refreshManager.getRefreshRunnable(project);
			ResourcesPlugin.getWorkspace().run(runnable, null, IWorkspace.AVOID_UPDATE, null);
		} catch (CoreException e)
		{
			Activator.getDefault().logError("Error while refreshing workspace.", e);
		}
		
		Display.getDefault().asyncExec(new ShowInfoRunnable(infoMessage));
			

		subMonitor.done();
		monitor.done();
		return Status.OK_STATUS;
	}
	class ShowInfoRunnable implements Runnable
	{
		private String message;
		public ShowInfoRunnable(String infoMessage)
		{
			message = infoMessage;
		}
		@Override
		public void run()
		{
			MessageDialog.openInformation(null, "Successfully imported project", message);
		}
	}
}
