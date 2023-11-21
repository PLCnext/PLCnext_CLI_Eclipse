/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.project.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.phoenixcontact.plcnext.common.CliNotExistingException;
import com.phoenixcontact.plcnext.common.ICommandManager;
import com.phoenixcontact.plcnext.common.ProcessExitedWithErrorException;
import com.phoenixcontact.plcnext.common.commands.Command;
import com.phoenixcontact.plcnext.common.commands.GetProjectInformationCommand;
import com.phoenixcontact.plcnext.common.commands.results.CommandResult;
import com.phoenixcontact.plcnext.common.commands.results.GetProjectInformationCommandResult;
import com.phoenixcontact.plcnext.common.commands.results.PlcncliMessage.MessageType;
import com.phoenixcontact.plcnext.cplusplus.project.Activator;
import com.phoenixcontact.plcnext.cplusplus.project.PlcProjectNature;
import com.phoenixcontact.plcnext.cplusplus.project.consumablelibrary.PlcnextConsumableLibraryNature;
import com.phoenixcontact.plcnext.cplusplus.toolchains.FindSourcesUtil;

/**
 * Wizard page for creation of new component with command line interface
 *
 */
public class NewComponentWizardPage extends WizardPage
{

	private Text nameText;
	private Combo projectCombo;
	private IProject[] projects;
	private List<String> programAndComponentNames;
	private Text namespaceText;

	private ICommandManager commandManager;
	private IStructuredSelection selection;

	private String temp = "";
	private Color grey = new Color(Display.getCurrent(), 190, 190, 190);
	private Color black = new Color(Display.getCurrent(), 0, 0, 0);

	protected NewComponentWizardPage(ICommandManager commandManager, IStructuredSelection selection)
	{
		super("new wizard");
		setTitle("Create New PLCnext C++ Component");
		setDescription("This wizard creates a new component.");
		this.commandManager = commandManager;
		this.selection = selection;
	}

	@Override
	public void createControl(Composite parent)
	{
		BusyIndicator.showWhile(null, new Runnable()
		{

			@Override
			public void run()
			{
				setPageComplete(false);
				Composite container = new Composite(parent, SWT.NONE);
				setControl(container);

				GridLayout containerLayout = new GridLayout();
				container.setLayout(containerLayout);
				containerLayout.numColumns = 2;

				// ************Name*****************
				Label label = new Label(container, SWT.NONE);
				label.setText("Component &name:");
				label.setToolTipText("Name of the new component");

				nameText = new Text(container, SWT.BORDER | SWT.SINGLE);
				nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

				// ************Namespace***************
				Label nsLabel = new Label(container, SWT.NONE);
				nsLabel.setText("Component name&space:");
				nsLabel.setToolTipText("Namespace of the new component");

				namespaceText = new Text(container, SWT.BORDER | SWT.SINGLE);
				namespaceText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

				// ************Project*****************
				Label projectLabel = new Label(container, SWT.NONE);
				projectLabel.setText("Project:");

				projectCombo = new Combo(container, SWT.PUSH);
				if(!initializeProjectCombo())
					return;
				
				// ************Listeners*****************
				nameText.addListener(SWT.Modify, event -> checkName());

				namespaceText.addListener(SWT.Modify, event -> namespaceModified());

				try
				{
					setComponentsAndPrograms();
				} catch (CliNotExistingException e1)
				{
					Activator.getDefault().logError("Error while trying to execute clif command.", e1);
					MessageDialog.openError(getShell(), "Could not create new component wizard",
							"The wizard could not be created. See log for more details.");
					getContainer().getShell().close();
				} catch (ProcessExitedWithErrorException e1)
				{
					Activator.getDefault()
							.logError("Error while trying to execute clif command.\n"
									+ e1.getMessages().stream().filter(m -> m.getMessageType() == MessageType.error)
											.map(m -> m.getMessage()).collect(Collectors.joining("\n")),
									e1);
					MessageDialog.openError(getShell(), "Could not create new component wizard",
							"The wizard could not be created. See log for more details.");
					getContainer().getShell().close();
				}

			}

			private boolean initializeProjectCombo() 
			{
				projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
				// remove closed projects from list
				projects = Arrays.stream(projects).filter(p ->
				{
					try
					{
						return p != null && p.isOpen() && p.hasNature(PlcProjectNature.NATURE_ID)
								&& !p.hasNature(PlcnextConsumableLibraryNature.NATURE_ID);
					} catch (CoreException e2)
					{
						return false;
					}
				}
				).toArray(IProject[]::new);

				if (projects.length < 1)
				{
					setErrorMessage("No open projects for which a component can be added found in the workspace.");
					return false;
				}

				// find project belonging to selected element
				int selectionIndex = 0;
				IProject project = null;
				Object selectedElement = selection.getFirstElement();
				if (selection.size() == 1
						&& (selectedElement instanceof IResource | selectedElement instanceof ICElement))
				{
					IResource resource;
					if (selectedElement instanceof IResource)
					{
						resource = (IResource) selectedElement;
					} else
					{
						resource = ((ICElement) selectedElement).getResource();
					}
					project = resource.getProject();
				}

				String[] projectNames = new String[projects.length];
				for (int i = 0; i < projects.length; i++)
				{
					projectNames[i] = projects[i].getName();
					if (project != null && projects[i].equals(project))
					{
						selectionIndex = i;
					}
				}
				
				projectCombo.setItems(projectNames);
				projectCombo.select(selectionIndex);

				projectCombo.addModifyListener(new ModifyListener()
				{

					public void modifyText(ModifyEvent e)
					{
						BusyIndicator.showWhile(null, new Runnable()
						{

							@Override
							public void run()
							{
								try
								{
									setComponentsAndPrograms();
									checkName();
								} catch (CliNotExistingException e1)
								{
									Activator.getDefault().logError("Error while trying to execute clif command.", e1);
									MessageDialog.openError(getShell(), "Error while trying to execute clif command.",
											"See log for more details.");
									getContainer().getShell().close();
								} catch (ProcessExitedWithErrorException e1)
								{
									Activator.getDefault()
											.logError("Error while trying to execute clif command.\n" + e1.getMessages()
													.stream().filter(m -> m.getMessageType() == MessageType.error)
													.map(m -> m.getMessage()).collect(Collectors.joining("\n")), e1);
									MessageDialog.openError(getShell(), "Error while trying to execute clif command.",
											e1.getMessages().stream()
													.filter(m -> m.getMessageType() == MessageType.error)
													.map(m -> m.getMessage()).collect(Collectors.joining("\n"))
													+ "\nSee log for more details.");
									getContainer().getShell().close();
								}

							}
						});
					}
				});
				return true;
				
			}
		});

	}

	protected String getComponentName()
	{
		return nameText.getText();
	}

	protected String getComponentNamespace()
	{
		return namespaceText.getText();
	}

	protected IProject getProject()
	{
		return projects[projectCombo.getSelectionIndex()];
	}

	private void setComponentsAndPrograms() throws CliNotExistingException, ProcessExitedWithErrorException
	{
		programAndComponentNames = new ArrayList<String>();
		IProject project = getProject();
		if (project != null)
		{
			Map<String, String> options = new HashMap<String, String>();
			options.put(GetProjectInformationCommand.OPTION_PATH, project.getLocation().toOSString());
			options.put(GetProjectInformationCommand.OPTION_NO_INCLUDE_DETECTION, null);
			List<String> sourceEntries = FindSourcesUtil.findSourceEntries(project);
			if (sourceEntries != null)
			{
				String sourceFolder = sourceEntries.stream().collect(Collectors.joining(","));
				if (sourceFolder != null && !sourceFolder.isEmpty())
					options.put(GetProjectInformationCommand.OPTION_SOURCES, sourceFolder);
			}
			Command command = commandManager.createCommand(options, GetProjectInformationCommand.class);

			CommandResult commandResult = commandManager.executeCommand(command, false, null);
			GetProjectInformationCommandResult getProjectInformationResult = commandResult
					.convertToTypedCommandResult(GetProjectInformationCommandResult.class);
			List<String> results = getProjectInformationResult.getComponents().stream().map(c -> c.getName())
					.collect(Collectors.toList());

			for (String result : results)
			{

				programAndComponentNames.add(result);
			}

			// TODO namespaces are not considered when checking for existing component,
			// this way no two components with same name in different namespaces can be
			// created

			results = getProjectInformationResult.getPrograms().stream().map(p -> p.getName())
					.collect(Collectors.toList());
			for (String result : results)
			{
				programAndComponentNames.add(result);
			}

			if (namespaceText != null)
			{
				String namespace = getProjectInformationResult.getNamespace();
				if (namespace != null && !namespace.isEmpty())
				{
					if (namespaceText.getText().isEmpty() || namespaceText.getText().equals(temp))
					{
						temp = namespace;
						namespaceText.setText(namespace);
					} else
					{
						temp = namespace;
					}
				}

			}

		}
	}

	private void checkName()
	{
		String text = nameText.getText();
		if (programAndComponentNames != null && programAndComponentNames.contains(text))
		{
			setExistsError();
		} else
		{
			String errorMessage = null;
			boolean isValid = false;
			if (!text.isBlank() && text.trim().length() > 1 && text.trim().length() <= 128)
			{
				Pattern pattern = Pattern.compile("^[A-Z](?!.*__)[a-zA-Z0-9_]*$");
				if (pattern.matcher(text).matches())
				{

					Pattern namespacePattern = Pattern
							.compile("^(?:[a-zA-Z][a-zA-Z0-9_]*\\.)*[a-zA-Z](?!.*__)[a-zA-Z0-9_]*$");
					Matcher namespaceMatch = namespacePattern.matcher(namespaceText.getText());
					if (namespaceMatch.matches() || namespaceText.getText().isEmpty())
					{
						if(!namespaceText.getText().equalsIgnoreCase(text))
						{
							isValid = true;	
						}else
						{
							errorMessage = "Component name and namespace should not be the same."; 
						}
					} else
					{
						errorMessage = 
								"Component namespace does not match pattern ^(?:[a-zA-Z][a-zA-Z0-9_]*\\.)*[a-zA-Z](?!.*__)[a-zA-Z0-9_]*$";
					}

				} else
				{
					errorMessage = "Component name does not match pattern ^[A-Z](?!.*__)[a-zA-Z0-9_]*$";
				}
			} else
			{
				errorMessage = "Component name must have a length between 2 and 128 characters.";
			}
			setErrorMessage(errorMessage);
			setPageComplete(isValid);
		}
	}

	protected void setExistsError()
	{
		setErrorMessage("An entity with this name exists already");
		setPageComplete(false);
	}

	private void namespaceModified()
	{
		if (!temp.equals(namespaceText.getText()))
		{
			namespaceText.setForeground(black);
		} else
		{
			namespaceText.setForeground(grey);
		}
		checkName();
	}
}
