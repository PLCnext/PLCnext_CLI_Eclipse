/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.common.preferences;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import com.phoenixcontact.plcnext.common.Activator;
import com.phoenixcontact.plcnext.common.EclipseContextHelper;
import com.phoenixcontact.plcnext.common.ICommandManager;
import com.phoenixcontact.plcnext.common.IDIHost;
import com.phoenixcontact.plcnext.common.Messages;
import com.phoenixcontact.plcnext.common.ProcessExitedWithErrorException;
import com.phoenixcontact.plcnext.common.commands.GetSdksCommand;
import com.phoenixcontact.plcnext.common.commands.InstallSdkCommand;
import com.phoenixcontact.plcnext.common.commands.SetSettingCommand;
import com.phoenixcontact.plcnext.common.commands.results.CommandResult;
import com.phoenixcontact.plcnext.common.commands.results.GetSdksCommandResult;
import com.phoenixcontact.plcnext.common.commands.results.GetSdksCommandResult.Sdk;
import com.phoenixcontact.plcnext.common.preferences.InstallSdkDialog.InstallSdkDialogResult;
import com.phoenixcontact.plcnext.common.preferences.SdkPreferenceDataModel.InstallSdk;

/**
 * Preference page to list/add/remove/install sdks
 */
public class SdksPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
	private SdkPreferenceDataModel model = new SdkPreferenceDataModel();
	private ListViewer sdkViewer;
	private ICommandManager commandManager; 

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#Constructor
	 */
	public SdksPreferencePage()
	{
		super();
		IEclipseContext context = EclipseContextHelper.getActiveContext();
		IDIHost host = ContextInjectionFactory.make(IDIHost.class, context);
		commandManager = host.getExport(ICommandManager.class);
	}
	
	@Override
	public void init(IWorkbench workbench)
	{
	}

	@Override
	protected Control createContents(Composite parent)
	{
		Composite control = new Composite(parent, SWT.NONE);
		control.setLayout(new GridLayout(2, false));
		noDefaultAndApplyButton();
		
		Label description = new Label(control, SWT.NONE); 
		description.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false, 2, 1));
		description.setText(Messages.SdksPreferencePage_SDKsLabel);
		
		
		sdkViewer = new ListViewer(control, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		sdkViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3));
		
		sdkViewer.setLabelProvider(new LabelProvider());
		sdkViewer.setContentProvider(new IStructuredContentProvider()
		{
			private String[] elements;

			@Override
			public Object[] getElements(Object inputElement)
			{
				return elements;
			}

			@SuppressWarnings("unchecked")
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
			{
				if (newInput instanceof String[])
				{
					this.elements = (String[]) newInput;
				} else if (newInput instanceof Collection)
				{
					this.elements = (String[]) ((Collection<String>) newInput).toArray(new String[0]);
				}

				if (viewer instanceof ListViewer)
				{
					((ListViewer) viewer).refresh();
				}

			}
		});
		fillViewer();
		
		
		
		Button addButton = new Button(control, SWT.PUSH);
		GridData addButtonGD = new GridData();
		addButtonGD.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		addButton.setLayoutData(addButtonGD);
		addButton.setText(Messages.SdksPreferencePage_AddButton);
		addButton.addListener(SWT.Selection, event -> handleAddButtonSelected());
		addButton.setToolTipText(Messages.SdksPreferencePage_ToolTipAddButton);
		
		Button installButton = new Button(control, SWT.PUSH);
		GridData installButtonGD = new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false);
		installButtonGD.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		installButton.setLayoutData(installButtonGD);
		installButton.setText(Messages.SdksPreferencePage_InstallButton);
		installButton.addListener(SWT.Selection, event -> handleInstallButtonSelected());
		installButton.setToolTipText(Messages.SdksPreferencePage_ToolTipInstallButton);
		
		
		Button removeButton = new Button(control, SWT.PUSH);
		GridData removeButtonGD = new GridData(SWT.DEFAULT, SWT.DEFAULT, false, true);
		removeButtonGD.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		removeButton.setLayoutData(removeButtonGD);
		removeButton.setText(Messages.SdksPreferencePage_RemoveButton);
		removeButton.addListener(SWT.Selection, event -> handleRemoveButtonSelected());
		removeButton.setToolTipText(Messages.SdksPreferencePage_ToolTipRemoveButton);
		
		sdkViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
		
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				if(event.getSelection() instanceof IStructuredSelection) 
				{
				
					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		
					removeButton.setEnabled(selection.size() > 0);
				}
			}
		});
		sdkViewer.setSelection(StructuredSelection.EMPTY);
		
		setVisible(true);
		
		return control;
	}

	private void handleAddButtonSelected()
	{
		AddSdkDialog dialog = new AddSdkDialog(getShell());
		String result = dialog.openWithResult();
		
		if(result != null)
		{
			model.addSdkForSet(result);
			
			sdkViewer.add(result);
		}
	}
	
	private void handleInstallButtonSelected()
	{
		InstallSdkDialog dialog = new InstallSdkDialog(getShell());
		InstallSdkDialogResult result = dialog.openWithResult();
		
		if(result != null)
		{
			model.addSdkForInstall(result.getArchive(), result.getDestination(), result.getForce());
			sdkViewer.add(result.getDestination());
		}
	}
	
	private void handleRemoveButtonSelected()
	{
		
		
		int index = sdkViewer.getList().getSelectionIndex();
		if(index >= 0)
		{
			String element = sdkViewer.getList().getItem(index);
			
			sdkViewer.getList().remove(element);
			model.removeSdk(element);
		sdkViewer.setSelection(StructuredSelection.EMPTY);
	}
	
	}
	
	private void fillViewer()
	{
		try {
			CommandResult commandResult = commandManager.executeCommand(commandManager.createCommand(null, GetSdksCommand.class), false, null);
			GetSdksCommandResult sdksCommandResult = commandResult.convertToGetSdksCommandResult();
			
			Sdk[] sdks = sdksCommandResult.getSdks();
			
			for (Sdk sdk : sdks)
			{
				sdkViewer.add(sdk.getPath());
			}
		}catch(ProcessExitedWithErrorException e)
		{
			ErrorDialog.openError(getShell(), null, null, new Status(Status.ERROR, Activator.PLUGIN_ID, Messages.SdksPreferencePage_GetSdksError, e));
		}
	}
	
	@Override
	public boolean performOk()
	{
		List<String> removeSdks = model.getRemoveSdks();
		List<String> setSdks = model.getSetSdks();
		List<InstallSdk> installSdks = model.getInstallSdks();
		
		if(removeSdks.size() > 0) 
		{
			RemoveSdkDialog dialog = new RemoveSdkDialog(null, removeSdks);
			int result = dialog.open();
			if(result != Window.OK) {
				return false;
			}
			if(dialog.getButtonSelection())
			{
				for(String sdk : removeSdks)
				{
		
					IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(sdk));
					if (fileStore.fetchInfo().isDirectory() && fileStore.fetchInfo().exists())
					{
						try
						{
							fileStore.delete(EFS.NONE, null);
						} catch (CoreException e)
						{
							Activator.getDefault().logError("Could not delete sdk directory", e);
						}
					}
				}
			}
		}
		
		Job okJob = new Job(Messages.SdksPreferencePage_SdksJobName)
		{
			
			@Override
			public IStatus run(IProgressMonitor monitor)
			{
				int sum = 1 + removeSdks.size() + setSdks.size() + installSdks.size() * 5;
				SubMonitor subMonitor = SubMonitor.convert(monitor, sum);
				subMonitor.split(1);
				
				for (String sdk : removeSdks)
				{
					subMonitor.split(1);
					Map<String, String> options = new HashMap<>();
					options.put(SetSettingCommand.OPTION_SdkPaths, sdk);
					options.put(SetSettingCommand.OPTION_REMOVE, null);
					try
					{
						commandManager.executeCommand(commandManager.createCommand(options, SetSettingCommand.class), false, monitor);
					} catch (ProcessExitedWithErrorException e)
					{
						return new Status(Status.ERROR, Activator.PLUGIN_ID, "Could not remove sdk "+sdk, e); //$NON-NLS-1$
					}
				}
				
				for(String sdk : setSdks)
				{
					subMonitor.split(1);
					Map<String, String> options = new HashMap<>();
					options.put(SetSettingCommand.OPTION_ADD, null);
					options.put(SetSettingCommand.OPTION_SdkPaths, sdk);
					try
					{
						commandManager.executeCommand(commandManager.createCommand(options, SetSettingCommand.class), false, monitor);
					} catch (ProcessExitedWithErrorException e)
					{
						return new Status(Status.ERROR, Activator.PLUGIN_ID, "Could not set sdk "+sdk, e); //$NON-NLS-1$
					}
				}
				
				
				for(InstallSdk sdk : installSdks)
				{
					subMonitor.split(5);
					Map<String, String> options = new HashMap<>();
					options.put(InstallSdkCommand.OPTION_PATH, sdk.getArchive());
					options.put(InstallSdkCommand.OPTION_DESTINATION, sdk.getDestination());
					if(sdk.getForce())
						options.put(InstallSdkCommand.OPTION_FORCE, null);
					
					try
					{
						commandManager.executeCommand(commandManager.createCommand(options, InstallSdkCommand.class), monitor);
					} catch (ProcessExitedWithErrorException e)
					{
						return new Status(Status.ERROR, Activator.PLUGIN_ID, "Could not install sdk "+sdk.getArchive(), e); //$NON-NLS-1$
					}
					
				}
				return Status.OK_STATUS;
			}
		};
		
		okJob.schedule();
		
		return super.performOk();
	}

}
