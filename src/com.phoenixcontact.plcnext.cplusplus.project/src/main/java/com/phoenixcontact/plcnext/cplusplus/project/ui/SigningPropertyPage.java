/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.project.ui;

import java.io.IOException;
import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

import com.phoenixcontact.plcnext.common.Messages;
import com.phoenixcontact.plcnext.common.PasswordPersistFileType;
import com.phoenixcontact.plcnext.common.SetPasswordDialog;
import com.phoenixcontact.plcnext.common.ConfigFile.Certificates;
import com.phoenixcontact.plcnext.common.ConfigFile.ConfigFileProvider;
import com.phoenixcontact.plcnext.common.ConfigFile.ProjectConfiguration;
import com.phoenixcontact.plcnext.cplusplus.project.Activator;

/**
 *
 */
public class SigningPropertyPage extends PropertyPage implements IWorkbenchPropertyPage
{
	private IProject project;
	private Button signingCheckBox;
	private Button radioButtonPKCS12;
	private Text pkcs12Text;
	private Button pkcs12browseButton;
	private Button passwordButton;
	private Button radioButtonPEM;
	private Label privateKeyLabel;
	private Text privateKeyText;
	private Button browseButton2;
	private Button passwordButton2;
	private Label publicKeyLabel;
	private Text publicKeyText;
	private Button browseButton3;
	private Label certificatesLabel;
	private TableViewer certificatesViewer;
	private Button browseButton4;
	private Button deleteButton;
	private Button timestampCheckBox;
	private Label timestampConfigLabel;
	private Text timestampConfigText;
	private Button configBrowseButton;

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

		Label description = new Label(container, SWT.NONE);
		description.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 3, 1));
		description.setText("Signing properties for PLCnext C++ Projects.\r\n"
				+ "Rebuild the project after saving your changes to transfer the configuration to the library.");
		
		Label subDescription = new Label(container, SWT.NONE);
		container.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 3, 1));
		subDescription.setText("Choose a PKCS#12 container or select the certificates, private and public key \r\n"
				+ "as separate PEM files, set a password and decide whether a timestamp shall be added");
		
		signingCheckBox = new Button(container, SWT.CHECK);
		signingCheckBox.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 1));
		signingCheckBox.setText("Sign Library");
		signingCheckBox.addListener(SWT.Selection, event -> handleSigningButtonSelected(signingCheckBox));		
		
		radioButtonPKCS12 = new Button(container, SWT.RADIO);
		GridData data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 1);
		data.horizontalIndent = 15;
		radioButtonPKCS12.setLayoutData(data);
		radioButtonPKCS12.setText("PKCS#12 Container");
		radioButtonPKCS12.addListener(SWT.Selection, event -> handlePKCS12ButtonSelected(radioButtonPKCS12));
		
		pkcs12Text = new Text(container, SWT.SINGLE | SWT.BORDER);
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.horizontalIndent = 30;
		pkcs12Text.setLayoutData(data);
		
		pkcs12browseButton = new Button(container, SWT.PUSH);
		setButtonLayoutData(pkcs12browseButton);
		pkcs12browseButton.setText("Browse...");
		pkcs12browseButton.addListener(SWT.Selection, event -> handleBrowseButtonSelected(pkcs12browseButton, pkcs12Text));
		
		passwordButton = new Button(container, SWT.PUSH);
		setButtonLayoutData(passwordButton);
		passwordButton.setText("Password...");
		passwordButton.addListener(SWT.Selection, event -> handleSetPasswordButtonSelected(PasswordPersistFileType.PKCS12));
		
		radioButtonPEM = new Button(container, SWT.RADIO);
		data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 1);
		data.horizontalIndent = 15;
		radioButtonPEM.setLayoutData(data);
		radioButtonPEM.setText("Separate PEM Files");
		
		privateKeyLabel = new Label(container, SWT.NONE);
		data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 1);
		data.horizontalIndent = 30;
		privateKeyLabel.setLayoutData(data);
		privateKeyLabel.setText("Private Key File (PEM)");
		privateKeyText = new Text(container, SWT.SINGLE | SWT.BORDER);
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.horizontalIndent = 30;
		privateKeyText.setLayoutData(data);
		
		browseButton2 = new Button(container, SWT.PUSH);
		setButtonLayoutData(browseButton2);
		browseButton2.setText("Browse...");
		browseButton2.addListener(SWT.Selection, event -> handleBrowseButtonSelected(browseButton2, privateKeyText));
		
		passwordButton2 = new Button(container, SWT.PUSH);
		setButtonLayoutData(passwordButton2);
		passwordButton2.setText("Password...");
		passwordButton2.addListener(SWT.Selection, event -> handleSetPasswordButtonSelected(PasswordPersistFileType.PEMKeyFile));
				
		publicKeyLabel = new Label(container, SWT.NONE);
		data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 1);
		data.horizontalIndent = 30;
		publicKeyLabel.setLayoutData(data);
		publicKeyLabel.setText("Public Key File (PEM)");
		publicKeyText = new Text(container, SWT.SINGLE | SWT.BORDER);
		data = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
		data.horizontalIndent = 30;
		publicKeyText.setLayoutData(data);
		
		browseButton3 = new Button(container, SWT.PUSH);
		setButtonLayoutData(browseButton3);
		browseButton3.setText("Browse...");
		browseButton3.addListener(SWT.Selection, event -> handleBrowseButtonSelected(browseButton3, publicKeyText));
		
		certificatesLabel = new Label(container, SWT.NONE);
		data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 1);
		data.horizontalIndent = 30;
		certificatesLabel.setLayoutData(data);
		certificatesLabel.setText("Certificate Files (PEM)");
		certificatesViewer = new TableViewer(container, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		data = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2);
		data.horizontalIndent = 30;
		certificatesViewer.getTable().setLayoutData(data);
		certificatesViewer.setContentProvider(ArrayContentProvider.getInstance());
		
		browseButton4 = new Button(container, SWT.PUSH);
		setButtonLayoutData(browseButton4);
		browseButton4.setText("Browse...");
		browseButton4.addListener(SWT.Selection, event -> handleViewerBrowseButtonSelected(browseButton4, certificatesViewer));
		
		deleteButton = new Button(container, SWT.PUSH);
		data = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
		deleteButton.setLayoutData(data);
		deleteButton.setText("Delete");
		deleteButton.addListener(SWT.Selection, event -> handleDeleteButtonSelected(certificatesViewer));
		
		timestampCheckBox = new Button(container, SWT.CHECK);
		data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 1);
		data.horizontalIndent = 15;
		timestampCheckBox.setLayoutData(data);
		timestampCheckBox.setText("Timestamp");
		timestampCheckBox.addListener(SWT.Selection, event -> handleTimestampCheckBoxSelected());
		
		timestampConfigLabel = new Label(container, SWT.NONE);
		data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 1);
		data.horizontalIndent = 15;
		timestampConfigLabel.setLayoutData(data);
		timestampConfigLabel.setText("Timestamp Configuration File");
		timestampConfigText = new Text(container, SWT.SINGLE | SWT.BORDER);
		data = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
		data.horizontalIndent = 15;
		timestampConfigText.setLayoutData(data);
		timestampConfigText.addModifyListener(arg0 -> handleTimestampCheckBoxSelected());
		
		configBrowseButton = new Button(container, SWT.PUSH);
		setButtonLayoutData(configBrowseButton);
		configBrowseButton.setText("Browse...");
		configBrowseButton.addListener(SWT.Selection, event -> handleBrowseButtonSelected(configBrowseButton, timestampConfigText));
		
		LoadConfigFile();
		handleSigningButtonSelected(signingCheckBox);
		handleTimestampCheckBoxSelected();
		
		return container;
	}
	
	private void handleTimestampCheckBoxSelected()
	{
		if(timestampCheckBox.getSelection())
		{
			if(timestampConfigText.getText() == null 
				|| timestampConfigText.getText().length() < 1)
			{
				setMessage("TimestampConfiguration must be provided if timestamp is requested", ERROR);
				setValid(false);
				return;
			}
				
		}
		setMessage(null);
		setValid(true);
	}
	
	private void handleSigningButtonSelected(Button button) 
	{
		boolean isSelected = button.getSelection();
		
		radioButtonPKCS12.setEnabled(isSelected);
		radioButtonPEM.setEnabled(isSelected);
		
		if(!radioButtonPKCS12.getSelection() && !radioButtonPEM.getSelection())
		{
			if((privateKeyText.getText() != null && !privateKeyText.getText().isEmpty())
		    	|| (publicKeyText.getText() != null && !publicKeyText.getText().isEmpty())
		    	|| (certificatesViewer.getTable() != null && certificatesViewer.getTable().getItemCount() > 0))
			{
				radioButtonPEM.setSelection(true);
			}else {
				radioButtonPKCS12.setSelection(true);
			}
		}
		
		
		if(isSelected)
		{
			
			handlePKCS12ButtonSelected(radioButtonPKCS12);
		}
		else
		{
			pkcs12Text.setEnabled(isSelected);
			pkcs12browseButton.setEnabled(isSelected);
			passwordButton.setEnabled(isSelected);
			privateKeyLabel.setEnabled(isSelected);
			privateKeyText.setEnabled(isSelected);
			browseButton2.setEnabled(isSelected);
			passwordButton2.setEnabled(isSelected);
			publicKeyLabel.setEnabled(isSelected);
			publicKeyText.setEnabled(isSelected);
			browseButton3.setEnabled(isSelected);
			certificatesLabel.setEnabled(isSelected);
			certificatesViewer.getTable().setEnabled(isSelected);
			browseButton4.setEnabled(isSelected);
			deleteButton.setEnabled(isSelected);
		}
		
		timestampCheckBox.setEnabled(isSelected);
		timestampConfigLabel.setEnabled(isSelected);
		timestampConfigText.setEnabled(isSelected);
		configBrowseButton.setEnabled(isSelected);
		
	}
	
	private void handlePKCS12ButtonSelected(Button button)
	{
		boolean isSelected = button.getSelection();
		
		pkcs12Text.setEnabled(isSelected);
		pkcs12browseButton.setEnabled(isSelected);
		passwordButton.setEnabled(isSelected);
		
		privateKeyLabel.setEnabled(!isSelected);
		privateKeyText.setEnabled(!isSelected);
		browseButton2.setEnabled(!isSelected);
		passwordButton2.setEnabled(!isSelected);
		publicKeyLabel.setEnabled(!isSelected);
		publicKeyText.setEnabled(!isSelected);
		browseButton3.setEnabled(!isSelected);
		certificatesLabel.setEnabled(!isSelected);
		certificatesViewer.getTable().setEnabled(!isSelected);
		browseButton4.setEnabled(!isSelected);
		deleteButton.setEnabled(!isSelected);
	}
	
	private void handleBrowseButtonSelected(Button button, Text text)
	{
		FileDialog dialog = new FileDialog(getShell());
		
		if(button == pkcs12browseButton)
		{
			dialog.setFilterExtensions(new String[] {"*.p12;*.pfx", "*.*"});
			dialog.setFilterNames(new String[] {"PKCS#12 container", "All files"});
		}
		else if(button == configBrowseButton)
		{
			dialog.setFilterExtensions(new String[] {"*.json"});
			dialog.setFilterNames(new String[] {"JSON"});
		}
		else 
		{
			dialog.setFilterExtensions(new String[] {"*.pem;*.cer;*.crt;*.key", "*.*"});
			dialog.setFilterNames(new String[] {"Privacy-Enhanced Mail (PEM)", "All files"});
		}
		
		String result = dialog.open();
		if (result != null && !result.isEmpty())
		{
			text.setText(result);
		}
	}
	
	private void handleViewerBrowseButtonSelected(Button button, TableViewer viewer)
	{
		FileDialog dialog = new FileDialog(getShell());
		
		dialog.setFilterExtensions(new String[] {"*.pem;*.cer;*.crt;*.key", "*.*"});
		dialog.setFilterNames(new String[] {"Privacy-Enhanced Mail (PEM)", "All files"});
		
		String result = dialog.open();
		if (result != null && !result.isEmpty())
		{
			viewer.add(result);
		}
	}
	
	private void handleDeleteButtonSelected(TableViewer viewer)
	{
		int[] indices = viewer.getTable().getSelectionIndices();
		viewer.getTable().remove(indices);
		viewer.setSelection(new StructuredSelection(viewer.getTable()));
	}
		
	private void handleSetPasswordButtonSelected(PasswordPersistFileType type)
	{
		SetPasswordDialog passwordDialog = new SetPasswordDialog(getShell(), "Save", Messages.SetPasswordDialog_DialogTitle, "");
		passwordDialog.setPassword(getPassword(type));
		String password = passwordDialog.openWithResult();
		if(password != null)
		{
			setPassword(password, type);
		}
	}
	
	private String getPassword(PasswordPersistFileType type)
	{
		String wspLocation = getWorkspaceLocation();
		
		try
		{
			ISecurePreferences securePreferences = SecurePreferencesFactory.getDefault();
			ISecurePreferences rootNode = securePreferences.node(Messages.SecureStorageNodeName);
			ISecurePreferences wspNode = rootNode.node(Messages.SecureStorageWorkspacesKey).node(wspLocation);
			if(wspNode.nodeExists(project.getName()))
			{
				return wspNode.node(project.getName()).get(type.toString(), "");
			}
			
			if(rootNode.nodeExists(project.getName())) 
			{
				return rootNode.node(project.getName()).get(type.toString(), "");
			}
			return "";
		} 
		catch (StorageException e1)
		{
			Activator.getDefault().logError("Error while trying to fetch password from secure storage", e1);
			return null;
		}
	}
	
	private String getWorkspaceLocation() {
		return project.getWorkspace().getRoot().getLocation().toOSString();
	}
	
	private void setPassword(String password, PasswordPersistFileType type)
	{
		try
		{
			String workspaceLocation = getWorkspaceLocation();
			ISecurePreferences securePreferences = SecurePreferencesFactory.getDefault();
			ISecurePreferences node = securePreferences.node(Messages.SecureStorageNodeName);
			ISecurePreferences wspNode = node.node(Messages.SecureStorageWorkspacesKey).node(workspaceLocation);
			
			if(node.nodeExists(project.getName())) {
				movePasswordsFromNameToWSPLocation(node, wspNode);
			}
			
			ISecurePreferences projectNode = wspNode.node(project.getName());
			projectNode.put(type.toString(), password, true);
			securePreferences.flush();
		}
		catch (StorageException | IOException e)
		{
			Activator.getDefault().logError("Error while trying to set password in secure storage", e);
		}
	}
	
	//version 25.0 saved projectName without workspace loc therefore this conversion is necessary
	private void movePasswordsFromNameToWSPLocation(ISecurePreferences namesNode, ISecurePreferences wspNode)
	{
			ISecurePreferences nodeToDelete = namesNode.node(project.getName());
			ISecurePreferences newNode = wspNode.node(project.getName());
			for (String key : nodeToDelete.keys())
			{
				String value;
				try
				{
					value = nodeToDelete.get(key, null);

					if (value != null && !value.isBlank())
					{
						newNode.put(key, value, true);
					}
				} catch (StorageException e)
				{
					Activator.getDefault().logError("Error while trying to move password in secure storage", e);
				}
			}
			nodeToDelete.removeNode();
	}
	
	private void LoadConfigFile()
	{
		ProjectConfiguration configuration = ConfigFileProvider.LoadFromConfig(project.getLocation());
		if(configuration != null )
		{
			signingCheckBox.setSelection(configuration.getSign());
			pkcs12Text.setText(configuration.getPkcs12());
			if((configuration.getPrivateKey() != null && !configuration.getPrivateKey().isBlank())
			    || (configuration.getPublicKey() != null && !configuration.getPublicKey().isBlank())
			    || (configuration.getCertificates() != null 
			    	&& configuration.getCertificates().getFiles() != null 
			    	&& configuration.getCertificates().getFiles().length > 0))
			{
				radioButtonPEM.setSelection(true);
			}
			else
			{
				radioButtonPKCS12.setSelection(true);
			}
			privateKeyText.setText(configuration.getPrivateKey());
			publicKeyText.setText(configuration.getPublicKey());
			if(configuration.getCertificates() != null) {
				certificatesViewer.setInput(configuration.getCertificates().getFiles());
			}
			timestampConfigText.setText(configuration.getTimestampConfiguration());
			
			if(configuration.getTimestamp() && configuration.getNoTimestamp())
			{
				MessageDialog.openError(getShell(), "Invalid configuration", "Timestamp and NoTimestamp cannot be combined together.");
			}
			
			timestampCheckBox.setSelection(configuration.getTimestamp());
		}		
	}
	
	@Override
	public boolean performOk()
	{	
		if(!pkcs12Text.getText().isBlank() && (!privateKeyText.getText().isBlank() ||
											   !publicKeyText.getText().isBlank() || 
											   certificatesViewer.getTable().getItemCount() > 0))
		{
			String unpersistedValue = radioButtonPKCS12.getSelection() ? "PEM" : "PKCS#12";
			boolean confirmed = MessageDialog.openConfirm(getShell(), "Value will not be saved", 
							"The entered "+ unpersistedValue + " file(s) will not be persisted.");
			if(!confirmed)
			{
				return false;
			}
		}
		
		ProjectConfiguration config = ConfigFileProvider.LoadFromConfig(project.getLocation());
		if(config == null) 
		{
			config = new ProjectConfiguration();
		}
		config.setSign(signingCheckBox.getSelection()); 
		
		config.setPkcs12(radioButtonPKCS12.getSelection() ? pkcs12Text.getText() : null);
		
		config.setPrivateKey(radioButtonPEM.getSelection() ? privateKeyText.getText() : null);
		config.setPublicKey(radioButtonPEM.getSelection() ? publicKeyText.getText() : null);
		config.setCertificates(radioButtonPEM.getSelection() ? new Certificates(Arrays.stream(certificatesViewer.getTable().getItems()).map(item -> item.getText()).toArray(String[]::new)) : null);
		config.setTimestampConfiguration(timestampConfigText.getText());
		
		if(timestampCheckBox.getSelection())
		{
			config.setTimestamp(true);
			config.setNoTimestamp(false);
		}
		else
		{
			config.setTimestamp(false);
			config.setNoTimestamp(true);
		}
		
		ConfigFileProvider.WriteConfigFile(config, project.getLocation());	
		
		return super.performOk();
	}
}