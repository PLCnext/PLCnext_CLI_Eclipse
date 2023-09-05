/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.toolchains.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.ui.properties.ICustomBuildOptionEditor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import com.phoenixcontact.plcnext.common.CachedCliInformation;
import com.phoenixcontact.plcnext.common.CliInformationCacher;
import com.phoenixcontact.plcnext.common.EclipseContextHelper;
import com.phoenixcontact.plcnext.common.ICommandManager;
import com.phoenixcontact.plcnext.common.IDIHost;
import com.phoenixcontact.plcnext.common.ProcessExitedWithErrorException;
import com.phoenixcontact.plcnext.common.commands.GetTargetsCommand;
import com.phoenixcontact.plcnext.common.commands.results.GetTargetsCommandResult;
import com.phoenixcontact.plcnext.common.commands.results.Target;
import com.phoenixcontact.plcnext.cplusplus.toolchains.Activator;

/**
 * FieldEditor describing a checkbox tree viewer with available targets inside
 *
 */
public class SelectMultiTargetOptionEditor extends StringFieldEditor implements ICustomBuildOptionEditor
{

	private CheckboxTableViewer viewer;
	private String value;
	private static final String ALLTARGETS = "obtain from project settings";

	private IDIHost host;
	private ICommandManager commandManager;

	/**
	 * creates a checkbox tree viewer field editor which gets filled with targets
	 * from command line interface
	 */
	public SelectMultiTargetOptionEditor()
	{
		// get commandManager to create and execute commands
		IEclipseContext context = EclipseContextHelper.getActiveContext();
		host = ContextInjectionFactory.make(IDIHost.class, context);
		commandManager = host.getExport(ICommandManager.class);
	}

	@Override
	public boolean init(IOption option, String extraArgument, String preferenceName, Composite parent)
	{
		init(option.getId(), "Build Target");
		createControl(parent);
		return true;
	}

	@Override
	public Control[] getToolTipSources()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void adjustForNumColumns(int numColumns)
	{

		((GridData) viewer.getControl().getLayoutData()).horizontalSpan = numColumns;
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns)
	{

		Control control = getLabelControl(parent);
		GridData layout = new GridData();
		layout.horizontalSpan = numColumns;
		control.setLayoutData(layout);

		layout = new GridData();
		layout.horizontalSpan = numColumns;
		getViewer(parent).getControl().setLayoutData(layout);

	}

	@Override
	public String getStringValue()
	{
		if (value != null)
			return value;
		return super.getStringValue();
	}

	private CheckboxTableViewer getViewer(Composite parent)
	{
		if (viewer == null)
		{
			viewer = CheckboxTableViewer.newCheckList(parent, SWT.PUSH);
			viewer.setLabelProvider(new LabelProvider());
			viewer.setContentProvider(new IStructuredContentProvider()
			{
				private String[] elements;

				@Override
				public Object[] getElements(Object inputElement)
				{
					return elements;
				}

				@Override
				public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
				{
					this.elements = (String[]) newInput;
				}
			});

			List<String> elements = new ArrayList<String>();
			elements.add("Updating targets...");
			viewer.setInput(elements.toArray(new String[0]));
			viewer.getTable().setEnabled(false);

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
								updateViewer();
							}
						});
					}
				}
			});
		}
		return viewer;
	}

	private void updateViewer()
	{
		CachedCliInformation cache = host.getExport(CachedCliInformation.class);
		List<String> elements = new ArrayList<String>();
		elements.add(ALLTARGETS);
		List<Target> targets = cache.getAllTargets();
		if (targets == null)
		{
			try
			{
				// use command line tool to get list of all available targets
				Map<String, String> options = new HashMap<String, String>();
				options.put(GetTargetsCommand.OPTION_SHORT, null);

				targets = Arrays.asList(commandManager
						.executeCommand(commandManager.createCommand(options, GetTargetsCommand.class), false, null)
						.convertToTypedCommandResult(GetTargetsCommandResult.class).getTargets());
				cache.setAllTargets(targets);

			} catch (ProcessExitedWithErrorException e)
			{
				Activator.getDefault().logError("Error while trying to execute clif command.", e);
				targets = new ArrayList<Target>();
			}
		}
		elements.addAll(targets.stream().map(t -> t.getDisplayName()).collect(Collectors.toList()));
		viewer.setInput(elements.toArray(new String[0]));
		viewer.getTable().setEnabled(true);
		doLoad();

		value = transformViewerValue(viewer.getCheckedElements());

		viewer.addCheckStateListener(new ICheckStateListener()
		{

			@Override
			public void checkStateChanged(CheckStateChangedEvent event)
			{
				if (event.getElement().equals(ALLTARGETS))
				{
					for (Object o : viewer.getCheckedElements())
					{
						if (!o.equals(ALLTARGETS))
						{
							viewer.setChecked(o, false);
						}
					}
				} else
				{
					viewer.setChecked(ALLTARGETS, false);
				}
				String newValue = transformViewerValue(viewer.getCheckedElements());
				if (!value.equals(newValue))
				{
					oldValue = value;
					value = newValue;

					fireValueChanged(VALUE, value, value);
				}
			}
		});
	}

	private String transformViewerValue(Object[] checkedElements)
	{
		String result = "";
		if (checkedElements != null && checkedElements.length > 0)
		{
			for (Object o : checkedElements)
			{
				if (o.equals(ALLTARGETS) || !(o instanceof String))
				{
					return "";
				}
				result += "\"" + o + "\"" + " ";
			}
		}
		return result;
	}

	private String[] transformValueForViewer(String value)
	{

		List<String> result = new ArrayList<String>();

		if (value == null || value.isBlank())
		{
			return new String[] { ALLTARGETS };
		} else
		{
			String[] parts = value.split("\"");

			// these lines are needed to transform 19.0 project configurations (short target
			// form) to new project configurations
			if (!value.contains("\""))
			{
				parts = value.split(" ");
				List<String> items = Arrays.stream(viewer.getTable().getItems()).map(x -> ((String) x.getData()))
						.collect(Collectors.toList());

				 return Arrays.stream(parts).map(s ->
				{
					String[] moreParts = s.split(",");
					if (moreParts.length == 2)
					{
						return items.stream().filter(x -> x.contains(moreParts[0]) && x.contains(moreParts[1])).findAny()
								.orElse(null);
					}
					return null;
				}).filter(x -> x != null).toArray(String[]::new);
			}

			for (String part : parts)
			{
				if (!part.isBlank())
				{
					result.add(part);
				}
			}
			return result.toArray(new String[0]);
		}
	}

	@Override
	protected void doLoad()
	{
		value = getPreferenceStore().getString(getPreferenceName());
		viewer.setCheckedElements(transformValueForViewer(value));
		viewer.getTable().requestLayout();
	}

	@Override
	protected void doLoadDefault()
	{
		doLoad();
	}

	@Override
	protected void doStore()
	{
		getPreferenceStore().setValue(getPreferenceName(), value);
	}

	@Override
	public int getNumberOfControls()
	{
		return 2;
	}
}
