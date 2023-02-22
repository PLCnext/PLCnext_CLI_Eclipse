/********************************************************************************
*  Copyright (c) Phoenix Contact GmbH & Co KG
*  This software is licensed under EPL-2.0
********************************************************************************/

package com.phoenixcontact.plcnext.cplusplus.project.comfortfunction;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.phoenixcontact.plcnext.common.CachedCliInformation;
import com.phoenixcontact.plcnext.common.EclipseContextHelper;
import com.phoenixcontact.plcnext.common.IDIHost;

/**
 * Wizard page for Port Comment Creation
 */
public class SelectPortAttributesWizardPage extends WizardPage
{

	private Text name;
	private CheckboxTableViewer attributesListViewer;
	private Group datatypeGroup;
	private Text commentPreview;
	private String text;
	private LinkedHashMap<String, String> attributes = new LinkedHashMap<String, String>();
	private LinkedHashMap<String, String> datatypes = new LinkedHashMap<String, String>();
	private Composite container;
	private ScrolledComposite scrolledComposite;
	
	private final String defaultButtonText = "Default";
	CachedCliInformation cache;

	protected SelectPortAttributesWizardPage(String text)
	{
		super("Generate Port Comments");
		setTitle("Generate Port Comments");
		setDescription("Select port name and attributes.");
		this.text = text;
		IEclipseContext context = EclipseContextHelper.getActiveContext();
		IDIHost host = ContextInjectionFactory.make(IDIHost.class, context);
		cache = host.getExport(CachedCliInformation.class);
		
		attributes.put("Input", "The variable is defined as IN port.");
		attributes.put("Output", "The variable is defined as OUT port.");
		attributes.put("Retain", "The variable value is retained in case of a warm and hot restart (only initialized in case of a cold restart).");
		attributes.put("Opc", "The variable is visible for OPC UA.");
		attributes.put("Ehmi", "The variable is visible for the PLCnext Engineer  HMI.( Note: This attribute is currently not implemented. Implementation is planned.)");
		attributes.put("ProfiCloud", "The variable is visible for Proficloud (for OUT ports only).");
		attributes.put("Redundant", "This attribute is relevant only for PLCnext Technology controllers with redundancy function.\r\n"
				+ "This variable is synchronized from PRIMARY controller to BACKUP controller.\r\n"
				+ "From FW 2022.0 LTS");
		
		datatypes.put(defaultButtonText, "Use default datatype mapping");
		datatypes.put("BYTE", "Use only for ports of type uint8");
		datatypes.put("WORD", "Use only for ports of type uint16");
		datatypes.put("DWORD", "Use only for ports of type uint32");
		datatypes.put("LWORD", "Use only for ports of type uint64");
	}

	@Override
	public void createControl(Composite parent)
	{
		scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		setControl(scrolledComposite);

		container = new Composite(scrolledComposite, SWT.NONE);
		scrolledComposite.setContent(container);

		GridLayout containerLayout = new GridLayout();
		container.setLayout(containerLayout);
		containerLayout.numColumns = 5;
		containerLayout.verticalSpacing = 15;
		containerLayout.horizontalSpacing = 15;

		// *******************NAME************************
		Label nameLabel = new Label(container, SWT.NONE);
		nameLabel.setText("Port Name:");
		name = new Text(container, SWT.SINGLE | SWT.BORDER);
		name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		name.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				updateCommentPreview();
			}
		});

		// ********************separator******************
		Label separator = new Label(container, SWT.SEPARATOR | SWT.VERTICAL | SWT.SHADOW_OUT);
		separator.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, true, 1, 7));

		// ******************comment preview*************
		Label previewLabel = new Label(container, SWT.NONE);
		previewLabel.setText("Preview:");
		previewLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

		// ********************separator******************
		Label separatorH1 = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.SHADOW_OUT);
		separatorH1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 3, 1));
				
		// ******************comment preview*************
		commentPreview = new Text(container, SWT.MULTI);
		commentPreview.setEnabled(false);
		commentPreview.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, true, false, 1, 3));
		
		// *****************ATTRIBUTES*******************
		Label attributesLabel = new Label(container, SWT.NONE);
		attributesLabel.setText("Select port attributes from list below");
		attributesLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 1));

		attributesListViewer = CheckboxTableViewer.newCheckList(container, SWT.PUSH | SWT.BORDER);
		attributesListViewer.getTable().setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 1));

		ColumnViewerToolTipSupport.enableFor(attributesListViewer);
		attributesListViewer.setLabelProvider(new ColumnLabelProvider() 
		{
			@Override
			public String getToolTipText(Object element) {
				if(element instanceof String) 
				{
					return attributes.get(element);
				}
				return super.getToolTipText(element);
			}
		});
		attributesListViewer.setContentProvider(new ObjectArrayContentProvider());
		attributesListViewer.setInput(attributes.keySet().toArray(new String[0]));
		
		attributesListViewer.addCheckStateListener(new ICheckStateListener()
		{
			
			@Override
			public void checkStateChanged(CheckStateChangedEvent event)
			{
				updateCommentPreview();
			}
		});

		// ********************separator******************
		Label separatorH2 = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.SHADOW_OUT);
		separatorH2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 3, 1));
		// *****************IEC datatype*******************

		datatypeGroup = new Group(container, SWT.NONE);
		datatypeGroup.setText("IEC datatype mapping");
		datatypeGroup.setLayout(new GridLayout(2, false));
		datatypeGroup.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 1));
				
		for(String key : datatypes.keySet())
		{
			Button button = new Button(datatypeGroup, SWT.RADIO);
			
			button.setText(key);
			if(key.equals(defaultButtonText)) 
			{
				button.setSelection(true);
			}
			button.setToolTipText(datatypes.get(key));
			button.addSelectionListener(new SelectionListener()
			{
				
				@Override
				public void widgetSelected(SelectionEvent e)
				{
					if(e.getSource() instanceof Button)
					{
						Button button = (Button) e.getSource();
						if(button.getSelection() == true)
						{
							updateCommentPreview();	
						}
					}
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e)
				{
				}
			});
		}
		
		
		// ******************info label*************
		Label infoLabel = new Label(container, SWT.NONE);
		infoLabel.setText("For more information visit www.plcnext.help");
		infoLabel.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, true, true, 5,1));
				
				
		updateCommentPreview();
	}
	
	private class ObjectArrayContentProvider implements IStructuredContentProvider
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
			if (newInput instanceof String[])
			{
				this.elements = (String[]) newInput;
			}
		}
	}

	/**
	 * @return the attributes which shall be added to the port comment
	 */
	private List<String> getCheckedAttributes()
	{
		Object[] checkedElements = attributesListViewer.getCheckedElements();
		return Arrays.asList(checkedElements).stream().filter(w -> w instanceof String).map(w -> (String) w)
				.collect(Collectors.toList());
	}

	/**
	 * @return the name which shall be set for the port
	 */
	private String getPortName()
	{
		return name.getText();
	}
	
	private String getPortDatatype() 
	{
		 Button selectedButton = Arrays.stream(datatypeGroup.getChildren())
								  		.filter(element -> element instanceof Button)
								  		.map(element -> (Button) element)
								  		.filter(button -> button.getSelection() == true)
								  		.findFirst().orElse(null);
		
		if(selectedButton != null && !selectedButton.getText().equals(defaultButtonText))
		{
			return selectedButton.getText();
		}
		return null;
	}

	/**
	 * @return the prefix for the comments
	 */
	public String getPrefix()
	{
		if (cache != null)
			return cache.getPortCommentPrefix();
		return "#";
	}

	protected String getPortComment()
	{
		Pattern pattern = Pattern.compile("^\\p{Blank}*");
		Matcher matcher = pattern.matcher(text);
		String tabs = ""; 
		if(matcher.find()) {
			tabs = matcher.group();
		}
		
		List<String> attributes = getCheckedAttributes();
		String name = getPortName();
		String datatype = getPortDatatype();
		String prefix = getPrefix();

		String commonLinePrefix = tabs + "//" + prefix; //$NON-NLS-1$
		String lineSuffix = "\n"; //$NON-NLS-1$

		String result = commonLinePrefix + "port" + lineSuffix; //$NON-NLS-1$

		if (name != null && !name.isEmpty())
		{
			result += commonLinePrefix + "name(" + name + ")" + lineSuffix; //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		if (attributes != null && !attributes.isEmpty())
		{
			String joinedAttributes = attributes.stream().collect(Collectors.joining("|", "(", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			result += commonLinePrefix + "attributes" + joinedAttributes + lineSuffix; //$NON-NLS-1$
		}
		
		if(datatype != null && !datatype.isEmpty())
		{
			result += commonLinePrefix + "iecdatatype(" + datatype + ")" +lineSuffix; //$NON-NLS-1$ //$NON-NLS-2$
		}

		return result;
	}
	
	private void updateCommentPreview()
	{
		commentPreview.setText(getPortComment().replaceAll("(\\t| )", "") + text.trim());
		commentPreview.pack();
		scrolledComposite.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		container.layout();
	}
}
