/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.project.ui;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

import com.phoenixcontact.plcnext.common.ConfigFileProvider;
import com.phoenixcontact.plcnext.common.EclipseContextHelper;
import com.phoenixcontact.plcnext.common.ExcludedFiles;
import com.phoenixcontact.plcnext.common.ICommandManager;
import com.phoenixcontact.plcnext.common.IDIHost;
import com.phoenixcontact.plcnext.common.ProcessExitedWithErrorException;
import com.phoenixcontact.plcnext.common.ProjectConfiguration;
import com.phoenixcontact.plcnext.common.commands.GetProjectInformationCommand;
import com.phoenixcontact.plcnext.common.commands.results.CommandResult;
import com.phoenixcontact.plcnext.common.commands.results.GetProjectInformationCommandResult;
import com.phoenixcontact.plcnext.common.commands.results.GetProjectInformationCommandResult.PLCnCLIProjectType;
import com.phoenixcontact.plcnext.common.commands.results.GetProjectInformationCommandResult.Path;
import com.phoenixcontact.plcnext.cplusplus.project.Activator;


/**
 *
 */
public class ProjectConfigPropertyPage extends PropertyPage implements IWorkbenchPropertyPage
{

	private Pattern pattern = Pattern.compile("^(?<major>\\d+)\\.\\d+(.\\d+)?(.\\d+)?$");
	private final String groupName = "major";
	
	private Text libraryDescription;
	private Text libraryVersion;
	private Text engineerVersion;
	private CheckboxTableViewer libsViewer;
	private Button generateNamespaces;
	private String[] savedExcludedFiles = null;
	private IProject project;
	private GetProjectInformationCommandResult projectInformation = null;
	private ProjectSettingsProvider settingsProvider;

	private boolean updated = false;
	private Path[] externalLibs = null;
	private final LibModel allLibs = new LibModel("Select/Deselect all libraries");

	@Override
	protected Control createContents(Composite parent)
	{

		noDefaultAndApplyButton();

		IAdaptable element = getElement();
		if (element instanceof IProject)
		{
			project = (IProject) element;
		}

		Job informationFetchingJob = new Job("Fetching project information")
		{

			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				if (project != null)
				{
					Map<String, String> options = new HashMap<String, String>();

					options.put(GetProjectInformationCommand.OPTION_PATH, project.getLocation().toOSString());
					
					// get buildconfiguration to set build type option
					IConfiguration config =  ManagedBuildManager.getBuildInfo(project).getDefaultConfiguration();
					if(config != null)
					{
						ITool[] tools = config.getToolChain().getToolsBySuperClassId(
								"com.phoenixcontact.plcnext.cplusplus.toolchains.buildtool");
						if (tools.length == 1)
						{
							IOption optionBuildType = tools[0].getOptionBySuperClassId(
									"com.phoenixcontact.plcnext.cplusplus.toolchains.buildtool.optionbuildtype");
							if(optionBuildType != null) 
							{
								try
								{
									String buildType = optionBuildType.getStringValue();
									if(buildType != null) {
										buildType = optionBuildType.getEnumName(buildType);
									
										if(buildType != null && !buildType.isBlank())
										{
											options.put(GetProjectInformationCommand.OPTION_BUILDTYPE, buildType);
										}
									}
								} catch (BuildException e)
								{
									Activator.getDefault().logError("Error while trying to get active build configuration.", e); //$NON-NLS-1$
								}
							}
						}
					}
					
					
					
					
					try
					{
						IEclipseContext context = EclipseContextHelper.getActiveContext();
						IDIHost host = ContextInjectionFactory.make(IDIHost.class, context);
						ICommandManager commandManager = host.getExport(ICommandManager.class);
						CommandResult result = commandManager.executeCommand(
								commandManager.createCommand(options, GetProjectInformationCommand.class), false, null);
						projectInformation = result
								.convertToTypedCommandResult(GetProjectInformationCommandResult.class);
						externalLibs = projectInformation.getExternalLibraries();

					} catch (ProcessExitedWithErrorException e)
					{
						Display.getDefault().syncExec(new Runnable()
						{
							public void run()
							{
								// starting message dialog on ui thread
								MessageDialog.openError(null, "Problem encountered",
										"Dialog might show incorrect data because of the following error:\n"
												+ e.getMessage());
							}
						});
						Activator.getDefault().logError("Error while trying to execute clif command.", e); //$NON-NLS-1$
					}
				}
				return Status.OK_STATUS;
			}
		};
		informationFetchingJob.schedule();
		informationFetchingJob.addJobChangeListener(new JobChangeAdapter()
		{
			public void done(IJobChangeEvent event)
			{
				if (event.getResult().isOK())
				{
					Display.getDefault().syncExec(new Runnable()
					{
						@Override
						public void run()
						{
							if (libsViewer != null)
								fillLibsViewer();
							updated = true;
						}
					});
				}
			}
		});

		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label description = new Label(container, SWT.NONE);
		description.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		description.setText("Project properties for PLCnext C++ Projects.\r\n"
				+ "Rebuild the project after saving your changes to transfer the configuration to the library.");

		Label libraryDescriptionLabel = new Label(container, SWT.NONE);
		libraryDescriptionLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		libraryDescriptionLabel.setText("Library Description");

		libraryDescription = new Text(container, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		libraryDescription.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label libraryVersionLabel = new Label(container, SWT.NONE);
		libraryVersionLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		libraryVersionLabel.setText("Library Version");

		libraryVersion = new Text(container, SWT.SINGLE | SWT.BORDER);
		libraryVersion.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		Label engineerVersionLabel = new Label(container, SWT.NONE);
		engineerVersionLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		engineerVersionLabel.setText("PLCnext Engineer Version");

		engineerVersion = new Text(container, SWT.SINGLE | SWT.BORDER);
		engineerVersion.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		engineerVersion.addModifyListener(new ModifyListener()
		{

			@Override
			public void modifyText(ModifyEvent e)
			{
				Text widget = (Text) e.widget;
				String text = widget.getText();
				if (text != null && !text.isEmpty())
				{
					Matcher matcher = pattern.matcher(text);
					if (!matcher.matches())
					{
						setMessage("No valid version! Please use format: 202x.x or 202x.x.x", ERROR);
						setValid(false);
						return;
					} else
					{
						String major = matcher.group(groupName);
						if (Integer.parseInt(major) < 2020 || Integer.parseInt(major) >= 2030)
						{
							setMessage("No valid version! Please use format: 202x.x or 202x.x.x", ERROR);
							setValid(false);
							return;
						}
					}
				}
				setMessage(null);
				setValid(true);
			}
		});

		Label excludedFilesLabel = new Label(container, SWT.NONE);
		excludedFilesLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		excludedFilesLabel.setText("Excluded Files");

		Label excludeDescription = new Label(container, SWT.NONE);
		excludeDescription.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		excludeDescription.setText("The checked elements will not be added to the library.");

		libsViewer = CheckboxTableViewer.newCheckList(container,
				SWT.PUSH | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		GridData layout = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2);
		layout.minimumWidth = 220;
		libsViewer.getTable().setLayoutData(layout);
		libsViewer.setLabelProvider(new ProjectConfigLibsLabelProvider());
		libsViewer.setContentProvider(ArrayContentProvider.getInstance());
		ColumnViewerToolTipSupport.enableFor(libsViewer);
		libsViewer.addCheckStateListener(new ICheckStateListener()
		{

			@Override
			public void checkStateChanged(CheckStateChangedEvent event)
			{
				LibModel element = (LibModel) event.getElement();
				if (element.equals(allLibs))
				{
					libsViewer.setAllChecked(event.getChecked());
				}
				else
				{
					if(libsViewer.getChecked(allLibs) 
							&& libsViewer.getCheckedElements().length < libsViewer.getTable().getItemCount())
					{	
						libsViewer.setChecked(allLibs, false);
					}
					else if (!libsViewer.getChecked(allLibs)
							&& libsViewer.getCheckedElements().length == libsViewer.getTable().getItemCount()-1)
					{
						libsViewer.setChecked(allLibs, true);
					}
				}
			}
		});

		LoadConfigFile();

		if (!updated)
		{
			libsViewer.add("Fetching project information..."); //$NON-NLS-1$
			libsViewer.getTable().setEnabled(false);
		} else
		{
			fillLibsViewer();
		}
		
		IPath projectPath = project.getLocation();
		settingsProvider = new ProjectSettingsProvider(projectPath);
		
		generateNamespaces = new Button(container, SWT.CHECK);
		generateNamespaces.setText("Generate Namespaces in Datatypes Worksheet");
		generateNamespaces.setSelection(settingsProvider.getGenerateNamespaces());
		

		return container;
	}
	
	

	private void fillLibsViewer()
	{
		if(projectInformation.getType() != PLCnCLIProjectType.project)
		{
			libsViewer.setInput(new LibModel[]{new LibModel("Only available for PLM projects")});
			return;
		}
		if (externalLibs != null)
		{
			LibModel[] availableLibs = Arrays.stream(externalLibs)
					.map(lib -> new LibModel(Paths.get(lib.getPath()).getFileName().toString()))
					.toArray(LibModel[]::new);
			List<LibModel> input = new ArrayList<LibModel>();
			List<LibModel> checkedElements = new ArrayList<LibModel>();
			if (savedExcludedFiles != null)
			{
				for (String file : savedExcludedFiles)
				{
					if (Arrays.stream(availableLibs).noneMatch(e -> e.value.equals(file)))
					{
						LibModel element = new LibModel(file, false);
						input.add(element);
						checkedElements.add(element);
					} else
					{
						checkedElements.add(Arrays.stream(availableLibs).filter(e -> e.value.equals(file)).findFirst()
								.orElse(null));
					}
				}
			}

			input.addAll(Arrays.asList(availableLibs));
			input.add(0, allLibs);
			if (input.size() > 1)
			{
				libsViewer.setInput(input);
				if (checkedElements.size() == input.size() - 1)
				{
					checkedElements.add(allLibs);
				}
				libsViewer.setCheckedElements(checkedElements.toArray(LibModel[]::new));
				libsViewer.getTable().setEnabled(true);
			} else
			{
				libsViewer.setInput(new LibModel[]{new LibModel("No libraries found")});
			}
		}
	}

	private void LoadConfigFile()
	{
		ProjectConfiguration configuration = ConfigFileProvider.LoadFromConfig(project.getLocation());
		if(configuration != null )
		{
			engineerVersion.setText(configuration.getEngineerVersion());
			libraryDescription.setText(configuration.getLibraryDescription());
			libraryVersion.setText(configuration.getLibraryVersion());
			savedExcludedFiles = configuration.getExcludedFiles() != null ? configuration.getExcludedFiles().getFiles() : null;
		}		
	}

	@Override
	public boolean performOk()
	{
		String engineerText = engineerVersion.getText();
		String description = libraryDescription.getText();
		String libVersion = libraryVersion.getText();
		Object[] checkedLibs = libsViewer.getCheckedElements();
		
		ProjectConfiguration config = new ProjectConfiguration();
		description = description.replaceAll("\r", "");
		String[] excludedFiles = Arrays.stream(checkedLibs).filter(l -> !((LibModel)l).equals(allLibs))
				.map(l -> ((LibModel)l).value).toArray(String[]::new);
		config.setLibraryDescription(description);
		config.setLibraryVersion(libVersion);
		config.setEngineerVersion(engineerText);
		config.setExcludedFiles(new ExcludedFiles(excludedFiles));
		
		ConfigFileProvider.WriteConfigFile(config, project.getLocation());		
		
		updateProjectFile();
		
		return super.performOk();
	}

	private void updateProjectFile()
	{
		settingsProvider.setGenerateNamespaces(generateNamespaces.getSelection());
		settingsProvider.writeProjectFile();
	}
	
	protected class LibModel
	{
		boolean valid;
		String value;

		public LibModel(String value)
		{
			this.value = value;
			valid = true;
		}

		public LibModel(String value, boolean valid)
		{
			this.value = value;
			this.valid = valid;
		}
	}
}
