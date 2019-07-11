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
import com.phoenixcontact.plcnext.common.plcncliclient.ServerMessageMessage.MessageType;
import com.phoenixcontact.plcnext.cplusplus.project.Activator;
import com.phoenixcontact.plcnext.cplusplus.project.PlcProjectNature;
import com.phoenixcontact.plcnext.cplusplus.toolchains.FindSourcesUtil;

/**
 * Wizard page for creation of new program with command line interface
 *
 */
public class NewProgramWizardPage extends WizardPage
{

	private Text nameText;
	private Text namespaceText;
	private Combo componentCombo;
	private Combo projectCombo;
	private String[] components;
	private List<String> programAndComponentNames;
	private IProject[] projects;
	private ICommandManager commandManager;
	private IStructuredSelection selection;
	private boolean componentsExist = true;

	private String temp = "";
	private Color grey = new Color(Display.getCurrent(), 190, 190, 190);
	private Color black = new Color(Display.getCurrent(), 0, 0, 0);

	protected NewProgramWizardPage(ICommandManager commandManager, IStructuredSelection selection)
	{
		super("new wizard");
		setTitle("Create New PLCnext C++ Program");
		setDescription("This wizard creates a new program.");
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
				setComplete();
				Composite container = new Composite(parent, SWT.NONE);
				setControl(container);

				GridLayout containerLayout = new GridLayout();
				container.setLayout(containerLayout);
				containerLayout.numColumns = 2;

				// ************Name*****************
				Label label = new Label(container, SWT.NONE);
				label.setText("Program &name:");
				label.setToolTipText("Name of the new program");

				nameText = new Text(container, SWT.BORDER | SWT.SINGLE);
				nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				nameText.setFocus();

				// ************Namespace***************
				Label nsLabel = new Label(container, SWT.NONE);
				nsLabel.setText("Program name&space:");
				nsLabel.setToolTipText("Namespace of the new program");

				namespaceText = new Text(container, SWT.BORDER | SWT.SINGLE);
				namespaceText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

				// ************Project*****************
				Label projectLabel = new Label(container, SWT.NONE);
				projectLabel.setText("Project:");

				projectCombo = new Combo(container, SWT.PUSH);

				// ************Component*****************
				Label componentLabel = new Label(container, SWT.NONE);
				componentLabel.setText("Parent Component:");

				componentCombo = new Combo(container, SWT.PUSH);

				// ************Initialization*****************
				if (initializeProjectCombo())
				{
					initializeComponentCombo();
				}
			}
		});
	}

	private boolean initializeProjectCombo()
	{
		projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		// remove closed projects from list
		projects = Arrays.stream(projects).filter(p ->
		{
			try
			{
				return p != null && p.isOpen() && p.hasNature(PlcProjectNature.NATURE_ID);
			} catch (CoreException e2)
			{
				return false;
			}
		}).toArray(IProject[]::new);

		if (projects.length < 1)
		{
			setErrorMessage("No open projects found in the workspace.");
			return false;
		}

		// find project belonging to selected element
		int selectionIndex = 0;
		IProject project = null;
		Object selectedElement = selection.getFirstElement();
		if (selection.size() == 1 && (selectedElement instanceof IResource | selectedElement instanceof ICElement))
		{
			IResource resource;
			if (selectedElement instanceof ICElement)
			{
				resource = ((ICElement) selectedElement).getResource();
			} else
			{
				resource = (IResource) selectedElement;
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
							if (componentsExist)
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
									e1.getMessages().stream().filter(m -> m.getMessageType() == MessageType.error)
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

	private void initializeComponentCombo()
	{
		try
		{
			nameText.addListener(SWT.Modify, event ->
			{
				if (componentsExist)
					checkName();
			});
			namespaceText.addListener(SWT.Modify, event -> namespaceModified());

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

	private void setComponentsAndPrograms() throws CliNotExistingException, ProcessExitedWithErrorException
	{
		programAndComponentNames = new ArrayList<String>();
		IProject project = getProject();

		if (project == null)
		{
			return;
		}

		Map<String, String> options = new HashMap<String, String>();
		options.put(GetProjectInformationCommand.OPTION_PATH, project.getLocation().toOSString());
		List<String> sourceEntries = FindSourcesUtil.findSourceEntries(getProject());
		if (sourceEntries != null)
		{
			String sourceFolder = sourceEntries.stream().collect(Collectors.joining(","));
			if (sourceFolder != null && !sourceFolder.isEmpty())
				options.put(GetProjectInformationCommand.OPTION_SOURCES, sourceFolder);
		}
		Command command = commandManager.createCommand(options, GetProjectInformationCommand.class);
		CommandResult commandResult = commandManager.executeCommand(command, false, null);
		GetProjectInformationCommandResult getProjectInformationResult = commandResult
				.convertToGetProjectInformationCommandResult();
		List<String> comps = new ArrayList<String>();
		getProjectInformationResult.getComponents().forEach(c ->
		{
			programAndComponentNames.add(c.getName());
			comps.add(c.getNamespace() + "::" + c.getName());
		});

		getProjectInformationResult.getPrograms().forEach(p ->
		{
			programAndComponentNames.add(p.getName());
		});

		components = comps.toArray(new String[0]);
		if (components.length == 0)
		{
			componentsExist = false;
			setErrorMessage("No components found for the selected project.");
		} else
		{
			componentsExist = true;
		}
		componentCombo.setItems(components);
		componentCombo.pack();
		componentCombo.select(0);

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

	private void setComplete()
	{
		setPageComplete(nameText != null && !nameText.getText().isEmpty());
	}

	private void checkName()
	{
		String text = nameText.getText();
		if (programAndComponentNames != null && programAndComponentNames.contains(text))
		{
			setExistsError();
		} else
		{
			Pattern pattern = Pattern.compile("^[A-Z](?!.*__)[a-zA-Z0-9_]*$");
			if (pattern.matcher(text).matches())
			{
				Pattern namespacePattern = Pattern
						.compile("^(?:[a-zA-Z][a-zA-Z0-9_]*\\.)*[a-zA-Z](?!.*__)[a-zA-Z0-9_]*$");
				Matcher namespaceMatch = namespacePattern.matcher(namespaceText.getText());
				if (namespaceMatch.matches() || namespaceText.getText().isEmpty())
				{
					setErrorMessage(null);
					setComplete();
					return;
				} else
				{
					setErrorMessage(
							"Component namespace does not match pattern ^(?:[a-zA-Z][a-zA-Z0-9_]*\\.)*[a-zA-Z](?!.*__)[a-zA-Z0-9_]*$");
				}
			} else
			{
				setErrorMessage("Program name does not match pattern ^[A-Z](?!.*__)[a-zA-Z0-9_]*$");
			}
			setPageComplete(false);
		}
	}

	protected void setExistsError()
	{
		setErrorMessage("An entity with this name exists already");
		setPageComplete(false);
	}

	protected String getProgramName()
	{
		return nameText.getText();
	}

	protected String getProgramNamespace()
	{
		return namespaceText.getText();
	}

	protected IProject getProject()
	{
		int index = projectCombo.getSelectionIndex();
		if (index >= 0 && index < projects.length)
			return projects[index];
		return null;
	}

	protected String getComponentName()
	{
		return components[componentCombo.getSelectionIndex()];
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
		if (componentsExist)
			checkName();
	}
}
