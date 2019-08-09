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

import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.cdt.ui.templateengine.AbstractWizardDataPage;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import com.phoenixcontact.plcnext.common.CachedCliInformation;
import com.phoenixcontact.plcnext.common.CliInformationCacher;
import com.phoenixcontact.plcnext.common.EclipseContextHelper;
import com.phoenixcontact.plcnext.common.ICommandManager;
import com.phoenixcontact.plcnext.common.IDIHost;
import com.phoenixcontact.plcnext.common.ProcessExitedWithErrorException;
import com.phoenixcontact.plcnext.common.commands.GetTargetsCommand;
import com.phoenixcontact.plcnext.common.commands.results.GetTargetsCommandResult;
import com.phoenixcontact.plcnext.common.commands.results.Target;
import com.phoenixcontact.plcnext.cplusplus.project.Activator;

/**
 * Wizard page for selection of targets in new plcnext project wizard
 *
 */
public class SelectTargetsWizardDataPage extends AbstractWizardDataPage
{

	/**
	 * The page-id
	 */
	public static final String PAGE_ID = "com.phoenixcontact.plcnext.cplusplus.project.SelectTargetsWizardDataPage";
	/**
	 * The targets key
	 */
	public static final String KEY_TARGETS = "TARGETS";
	private TableViewer selectedViewer;
	private TableViewer availableViewer;
	private Label updateText;
	private boolean updated = false;
	private Shell shell = null;
	private Button defaultButton = null;

	/**
	 * @param pageName
	 * @see org.eclipse.cdt.ui.templateengine.AbstractWizardDataPage#Constructor
	 */
	public SelectTargetsWizardDataPage(String pageName)
	{
		super(pageName);
		setTitle("Supported Targets");
		setDescription("Select targets to be supported by the project.");
		MBSCustomPageManager.addPageProperty(PAGE_ID, KEY_TARGETS, "");
		setPageComplete(false);
		Job cachingJob = new CliInformationCacher();
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
							updateTargetViewer();
							updated = true;
							if (updateText != null)
							{
								updateText.setText("");
								setPageComplete(true);
							}
						}
					});
				}
			}
		});
	}

	@Override
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		if (visible)
		{
			getControl().setFocus();
		}
	}

	@Override
	public void createControl(Composite parent)
	{
		shell = getShell();
		defaultButton = shell.getDefaultButton();
		
		Composite container = new Composite(parent, SWT.NONE);
		setControl(container);

		GridLayout controlLayout = new GridLayout();
		container.setLayout(controlLayout);
		controlLayout.numColumns = 3;

		// row1,column1
		Label availableLabel = new Label(container, SWT.NONE);
		availableLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		availableLabel.setText("Available Targets:");

		// row1,column3
		Label selectedLabel = new Label(container, SWT.NONE);
		selectedLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		selectedLabel.setText("Selected Targets:");

		TargetLabelProvider targetLabelProvider = new TargetLabelProvider();
		TargetsViewerComparator targetsComparator = new TargetsViewerComparator();
		
		// row2+3,column1
		availableViewer = new TableViewer(container, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		GridData layout = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2);
		layout.minimumWidth = 220;
		availableViewer.getTable().setLayoutData(layout);
		availableViewer.setLabelProvider(targetLabelProvider);
		availableViewer.setContentProvider(ArrayContentProvider.getInstance());
		availableViewer.setComparator(targetsComparator);
		
		List<Target> availableTargets = getPossibleTargets();
		availableViewer.setInput(availableTargets);

		// row2,column2
		Button addButton = new Button(container, SWT.PUSH);
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		addButton.setText("&Add");
		addButton.setEnabled(false);

		// row2+3,column3
		selectedViewer = new TableViewer(container, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		layout = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2);
		layout.minimumWidth = 220;
		selectedViewer.getTable().setLayoutData(layout);
		selectedViewer.setLabelProvider(targetLabelProvider);
		selectedViewer.setContentProvider(ArrayContentProvider.getInstance());
		selectedViewer.setComparator(targetsComparator);
		checkTargetsSelected();

		// row3,column2
		Button removeButton = new Button(container, SWT.PUSH);
		removeButton.setLayoutData(new GridData(SWT.NONE, SWT.BEGINNING, false, false));
		removeButton.setText("&Remove");
		removeButton.setEnabled(false);

		// ***********row 4**********************
		updateText = new Label(container, SWT.READ_ONLY);
		updateText.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		if (!updated)
		{
			updateText.setText("Updating...");
			setPageComplete(false);
		} else
		{
			updateText.setText("");
			setPageComplete(true);
		}

		// selection listeners
		availableViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{

			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection.size() > 0)
				{
					addButton.setEnabled(true);
					Button b = shell.getDefaultButton();
					if(!b.equals(addButton) && !b.equals(removeButton))
					{
						defaultButton = b;
					}
					shell.setDefaultButton(addButton);
				} else
				{
					addButton.setEnabled(false);
					shell.setDefaultButton(defaultButton);
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
					removeButton.setEnabled(true);
					Button b = shell.getDefaultButton();
					if(!b.equals(addButton) && !b.equals(removeButton))
					{
						defaultButton = b;
					}
					shell.setDefaultButton(removeButton);
				} else
				{
					removeButton.setEnabled(false);
					shell.setDefaultButton(defaultButton);
				}
			}
		});

		addButton.addSelectionListener(new SelectionListener()
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
				updateMBSProperties();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				widgetSelected(e);
			}
		});

		removeButton.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				int[] indices = selectedViewer.getTable().getSelectionIndices();
				for (int index : indices)
				{
					TableItem tableItem = selectedViewer.getTable().getItem(index);
					Target item = (Target) tableItem.getData();
					availableViewer.add(item);
				}
				selectedViewer.getTable().remove(indices);
				selectedViewer.setSelection(new StructuredSelection(selectedViewer.getTable()));
				updateMBSProperties();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				widgetSelected(e);
			}
		});
	}

	private void updateMBSProperties()
	{
		Target[] targets = Arrays.stream(selectedViewer.getTable().getItems())
				.map(i -> ((Target) i.getData())).toArray(Target[]::new);

		MBSCustomPageManager.addPageProperty(PAGE_ID, KEY_TARGETS, targets);
		checkTargetsSelected();
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
			setMessage("Project will support no target! Select at least one target.", WARNING);
		}
	}

	private List<Target> getPossibleTargets()
	{

		IEclipseContext context = EclipseContextHelper.getActiveContext();
		IDIHost host = ContextInjectionFactory.make(IDIHost.class, context);
		CachedCliInformation cache = host.getExport(CachedCliInformation.class);
		List<Target> targets = cache.getAllTargets();
		if (targets == null)
		{
			ICommandManager commandManager = host.getExport(ICommandManager.class);
			Map<String, String> options = new HashMap<String, String>();
			options.put(GetTargetsCommand.OPTION_SHORT, null);
			try
			{
				Target[] targetsResult = commandManager
						.executeCommand(commandManager.createCommand(options, GetTargetsCommand.class), false, null)
						.convertToTypedCommandResult(GetTargetsCommandResult.class).getTargets();
				targets = Arrays.asList(targetsResult);
				cache.setAllTargets(targets);
			} catch (ProcessExitedWithErrorException e)
			{
				Activator.getDefault().logError("Error while trying to execute clif command.", e);
				getContainer().getShell().close();
			}
		}

		return new ArrayList<Target>(targets);
	}

	@Override
	public Map<String, String> getPageData()
	{
		// TODO Auto-generated method stub
		return new HashMap<String, String>();
	}

	@Override
	public boolean canFlipToNextPage()
	{
		// hack to hide 'Select Configurations' page in new project wizard
		return false;
	}

	private void updateTargetViewer()
	{
		if (availableViewer != null && selectedViewer != null)
		{
			List<Target> newlyCachedTargets = getPossibleTargets();

			List<Target> availableViewerItemsList = Arrays.stream(availableViewer.getTable().getItems())
					.map(i -> ((Target) i.getData())).collect(Collectors.toList());
			List<Target> selectedViewerItemsList = Arrays.stream(selectedViewer.getTable().getItems())
					.map(i -> ((Target) i.getData())).collect(Collectors.toList());

			List<Target> currentlyVisibleTargets = new ArrayList<Target>();
			currentlyVisibleTargets.addAll(availableViewerItemsList);
			currentlyVisibleTargets.addAll(selectedViewerItemsList);

			List<Target> newTargets = newlyCachedTargets.stream().filter(x -> !currentlyVisibleTargets.contains(x))
					.collect(Collectors.toList());
			List<Target> oldTargets = currentlyVisibleTargets.stream().filter(x -> !newlyCachedTargets.contains(x))
					.collect(Collectors.toList());

			for (Target target : newTargets)
			{
				if (!currentlyVisibleTargets.contains(target))
				{
					availableViewer.add(target);
				}
			}
			for (Target target : oldTargets)
			{
				if (availableViewerItemsList.contains(target))
				{
					availableViewer.remove(target);
				}
				if (selectedViewerItemsList.contains(target))
				{
					selectedViewer.remove(target);
				}
			}

			checkTargetsSelected();
		}
	}
}
