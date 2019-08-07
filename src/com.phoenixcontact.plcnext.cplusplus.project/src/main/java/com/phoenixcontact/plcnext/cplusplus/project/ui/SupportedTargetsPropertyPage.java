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
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

import com.phoenixcontact.plcnext.common.CachedCliInformation;
import com.phoenixcontact.plcnext.common.CliInformationCacher;
import com.phoenixcontact.plcnext.common.EclipseContextHelper;
import com.phoenixcontact.plcnext.common.ICommandManager;
import com.phoenixcontact.plcnext.common.IDIHost;
import com.phoenixcontact.plcnext.common.Messages;
import com.phoenixcontact.plcnext.common.MutexSchedulingRule;
import com.phoenixcontact.plcnext.common.ProcessExitedWithErrorException;
import com.phoenixcontact.plcnext.common.commands.GetProjectInformationCommand;
import com.phoenixcontact.plcnext.common.commands.GetTargetsCommand;
import com.phoenixcontact.plcnext.common.commands.results.CommandResult;
import com.phoenixcontact.plcnext.common.commands.results.GetProjectInformationCommandResult;
import com.phoenixcontact.plcnext.common.commands.results.GetTargetsCommandResult;
import com.phoenixcontact.plcnext.common.commands.results.GetProjectInformationCommandResult.ProjectTarget;
import com.phoenixcontact.plcnext.common.commands.results.Target;
import com.phoenixcontact.plcnext.cplusplus.project.Activator;

/**
 * Property page for selection of targets in project properties of plcnext
 * projects
 *
 */
public class SupportedTargetsPropertyPage extends PropertyPage implements IWorkbenchPropertyPage
{

	private IDIHost host;

	/**
	 * Creates target selection property page
	 */
	public SupportedTargetsPropertyPage()
	{
		IEclipseContext context = EclipseContextHelper.getActiveContext();
		host = ContextInjectionFactory.make(IDIHost.class, context);
		commandManager = host.getExport(ICommandManager.class);
		cache = host.getExport(CachedCliInformation.class);

		Job cachingJob = new CliInformationCacher(false);
		cachingJob.schedule();
		cachingJob.addJobChangeListener(new JobChangeAdapter()
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
							if (availableViewer != null && selectedViewer != null)
								fillTargetViewer();
							updated = true;
						}
					});
				}
			}
		});
	}

	private ICommandManager commandManager;
	private CachedCliInformation cache;
	private TableViewer selectedViewer;
	private TableViewer availableViewer;

	private IProject project;
	private boolean updated = false;
	private boolean filled = false;

	private List<Target> initiallyAvailableTargets = new ArrayList<Target>();
	private List<Target> initiallySupportedTargets = new ArrayList<Target>();
	private List<Target> notAvailableProjectTargets = new ArrayList<Target>();

	

	@Override
	protected Control createContents(Composite parent)
	{
		noDefaultAndApplyButton();

		IAdaptable element = getElement();
		if (element instanceof IProject)
		{
			project = (IProject) element;
		}

		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(3, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// row1,column1+2
		Label availableLabel = new Label(container, SWT.NONE);
		availableLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		availableLabel.setText(Messages.SupportedTargetsPropertyPage_LabelAvailableViewer);

		// row1,column3
		Label selectedLabel = new Label(container, SWT.NONE);
		selectedLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		selectedLabel.setText(Messages.SupportedTargetsPropertyPage_LabelSelectedViewer);

		// row2+3,column1

		TargetsViewerComparator targetsComparator = new TargetsViewerComparator();
		TargetLabelProvider targetLabelProvider = new TargetLabelProvider();

		availableViewer = new TableViewer(container, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		availableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
		availableViewer.setLabelProvider(targetLabelProvider);
//		availableViewer.setContentProvider(new TargetContentProvider());
		availableViewer.setContentProvider(ArrayContentProvider.getInstance());
		availableViewer.setComparator(targetsComparator);

		// row2,column2
		Button addButton = new Button(container, SWT.PUSH);
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		addButton.setText(Messages.SupportedTargetsPropertyPage_ButtonAdd);
		addButton.setEnabled(false);

		// row2+3,column3
		selectedViewer = new TableViewer(container, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		selectedViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
		ColumnViewerToolTipSupport.enableFor(selectedViewer);
		selectedViewer.setLabelProvider(targetLabelProvider);
		selectedViewer.setContentProvider(ArrayContentProvider.getInstance());
		selectedViewer.setComparator(targetsComparator);

		// row3,column2
		Button removeButton = new Button(container, SWT.PUSH);
		removeButton.setLayoutData(new GridData(SWT.NONE, SWT.BEGINNING, false, false));
		removeButton.setText(Messages.SupportedTargetsPropertyPage_ButtonRemove);
		removeButton.setEnabled(false);

		if (!updated)
		{
			availableViewer.add("Fetching targets..."); //$NON-NLS-1$
		} else
		{
			fillTargetViewer();
		}

		// selection listeners
		availableViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{

			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				if (!updated && !event.getSelection().isEmpty())
				{
					availableViewer.setSelection(StructuredSelection.EMPTY);
					return;
				}
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection.size() > 0)
				{
					if (!selectedViewer.getStructuredSelection().isEmpty())
					{
						selectedViewer.setSelection(StructuredSelection.EMPTY);
					}
					addButton.setEnabled(true);
				} else
				{
					addButton.setEnabled(false);
				}
			}
		});

		selectedViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{

			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection.size() > 0)
				{
					if (!availableViewer.getStructuredSelection().isEmpty())
					{
						availableViewer.setSelection(StructuredSelection.EMPTY);
					}
					removeButton.setEnabled(true);
				} else
				{
					removeButton.setEnabled(false);
				}
			}
		});

		addButton.addSelectionListener(new SelectionAdapter()
		{

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				int[] indices = availableViewer.getTable().getSelectionIndices();
				for (int index : indices)
				{
					TableItem tableItem = availableViewer.getTable().getItem(index);
					Target item = (Target) tableItem.getData();
					selectedViewer.add(item);
				}
				availableViewer.getTable().remove(indices);
				availableViewer.setSelection(new StructuredSelection(availableViewer.getTable()));
				checkTargetsSelected();
			}
		});

		removeButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				int[] indices = selectedViewer.getTable().getSelectionIndices();
				for (int index : indices)
				{
					TableItem tableItem = selectedViewer.getTable().getItem(index);
					Target item = (Target) tableItem.getData();
					if (!notAvailableProjectTargets.contains(item))
						availableViewer.add(item);
				}
				selectedViewer.getTable().remove(indices);
				selectedViewer.setSelection(new StructuredSelection(selectedViewer.getTable()));
				checkTargetsSelected();
			}
		});

		return container;
	}

	private void fillTargetViewer()
	{
		if (!filled)
		{
			initiallyAvailableTargets = getPossibleTargets();
			initiallySupportedTargets = getSupportedTargets();
			initiallyAvailableTargets.removeAll(initiallySupportedTargets);
			availableViewer.setInput(initiallyAvailableTargets);
			selectedViewer.setInput(initiallySupportedTargets);
			checkTargetsSelected();
			filled = true;
		}
	}

	private List<Target> getPossibleTargets()
	{
		List<Target> results = cache.getAllTargets();
		if (results == null)
		{
			Map<String, String> options = new HashMap<String, String>();
			options.put(GetTargetsCommand.OPTION_SHORT, null);
			try
			{
				CommandResult commandResult = commandManager
						.executeCommand(commandManager.createCommand(options, GetTargetsCommand.class), false, null);

				Target[] targets = commandResult.convertToTypedCommandResult(GetTargetsCommandResult.class).getTargets();
				
				return Arrays.asList(targets);

			} catch (ProcessExitedWithErrorException e)
			{
				Activator.getDefault().logError("Error while trying to execute clif command.", e); //$NON-NLS-1$
			}
			return new ArrayList<Target>();
		}
		return new ArrayList<Target>(results);
	}

	private List<Target> getSupportedTargets()
	{
		if (project != null)
		{
			Map<String, String> options = new HashMap<String, String>();

			options.put(GetProjectInformationCommand.OPTION_PATH, project.getLocation().toOSString());

			try
			{
				CommandResult commandResult = commandManager.executeCommand(
						commandManager.createCommand(options, GetProjectInformationCommand.class), false, null);
				ProjectTarget[] targets = commandResult.convertToTypedCommandResult(GetProjectInformationCommandResult.class).getTargets();
				
				notAvailableProjectTargets.clear();
				for (ProjectTarget target : targets)
				{
					if (!target.isAvailable())
					{
						notAvailableProjectTargets.add(target);
					}
				}
				return Arrays.asList(targets);

			} catch (ProcessExitedWithErrorException e)
			{
				Activator.getDefault().logError("Error while trying to execute clif command.", e); //$NON-NLS-1$
			}
		}
		return new ArrayList<Target>();
	}

	private void checkTargetsSelected()
	{
		// show warning if no target is selected
		int selectedTargetsCount = selectedViewer.getTable().getItems().length;
		if (selectedTargetsCount > 0)
		{
			setMessage(null, WARNING);
		} else
		{
			setMessage(Messages.SupportedTargetsPropertyPage_WarningNoTargetSupported, WARNING);
		}
	}

	@Override
	public boolean performOk()
	{
		List<Target> targetsToAdd = new ArrayList<Target>();
		List<Target> targetsToRemove = new ArrayList<Target>();

		for (TableItem tableItem : availableViewer.getTable().getItems())
		{
			Target item = (Target) tableItem.getData();
			if (initiallySupportedTargets.contains(item))
				targetsToRemove.add(item);
		}
		for (TableItem tableItem : selectedViewer.getTable().getItems())
		{
			Target item = (Target) tableItem.getData();
			if (initiallyAvailableTargets.contains(item))
				targetsToAdd.add(item);
		}

		notAvailableProjectTargets.stream()
				.filter(s -> Arrays.stream(selectedViewer.getTable().getItems()).allMatch(i -> !i.getData().equals(s)))
				.forEach(s -> targetsToRemove.add(s));

		Job job = new SupportedTargetsPerformOKJob(
				Messages.SupportedTargetsPropertyPage_UpdateTargetJobName + project.getName(),
				targetsToAdd.stream().map(t -> t.getDisplayName()).collect(Collectors.toList()),
				targetsToRemove.stream().map(t -> t.getDisplayName()).collect(Collectors.toList()), project,
				commandManager);
		job.setRule(MultiRule.combine(ResourcesPlugin.getWorkspace().getRuleFactory().modifyRule(project),
				new MutexSchedulingRule()));
		job.schedule();

		return super.performOk();
	}
}
