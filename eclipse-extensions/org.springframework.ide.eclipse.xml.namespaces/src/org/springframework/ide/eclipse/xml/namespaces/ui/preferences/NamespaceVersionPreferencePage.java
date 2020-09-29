/*******************************************************************************
 * Copyright (c) 2009, 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.xml.namespaces.ui.preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.springframework.ide.eclipse.xml.namespaces.NamespaceManagerProvider;
import org.springframework.ide.eclipse.xml.namespaces.SpringXmlNamespacesPlugin;
import org.springframework.ide.eclipse.xml.namespaces.ui.INamespaceDefinition;
import org.springframework.ide.eclipse.xml.namespaces.ui.XmlNamespacesUIImages;
import org.springframework.ide.eclipse.xml.namespaces.ui.XmlUiNamespaceUtils;
import org.springsource.ide.eclipse.commons.core.SpringCorePreferences;
import org.springsource.ide.eclipse.commons.frameworks.core.util.StringUtils;
import org.springsource.ide.eclipse.commons.ui.ProjectAndPreferencePage;

/**
 * {@link ProjectAndPreferencePage} that allows to configure default namespace versions.
 * @author Christian Dupuis
 * @since 2.2.5
 */
@SuppressWarnings("deprecation")
public class NamespaceVersionPreferencePage extends ProjectAndPreferencePage {

	public static final String PREF_ID = "org.springframework.ide.eclipse.beans.ui.namespaces.preferencePage"; //$NON-NLS-1$

	public static final String PROP_ID = "org.springframework.ide.eclipse.beans.ui.namespaces.projectPropertyPage"; //$NON-NLS-1$

	public class XsdLabelProvider extends LabelProvider {

		public Image getImage(Object element) {
			String txt = getText(element);
			if (element instanceof INamespaceDefinition) {
				INamespaceDefinition xsdDef = (INamespaceDefinition) element;
				return xsdDef.getNamespaceImage();
			}
			return XmlNamespacesUIImages.getImage(XmlNamespacesUIImages.IMG_OBJS_XSD);
		}

		public String getText(Object element) {
			if (element instanceof INamespaceDefinition) {
				INamespaceDefinition xsdDef = (INamespaceDefinition) element;
				return prefixes.get(xsdDef) + " - " + xsdDef.getNamespaceURI();
			}
			return "";
		}
	}

	public class VersionLabelProvider extends LabelProvider {

		public Image getImage(Object element) {
			return XmlNamespacesUIImages.getImage(XmlNamespacesUIImages.IMG_OBJS_XSD);
		}

		public String getText(Object element) {
			if (element instanceof String) {
				String label = (String) element;
				return label;
			}
			return super.getText(element);
		}
	}

	private class XsdConfigContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object obj) {
			return getNamespaceDefinitionList().toArray();
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}
	}

	private class VersionContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object obj) {
			if (obj instanceof INamespaceDefinition) {
				return ((INamespaceDefinition) obj).getSchemaLocations().toArray();
			}
			else {
				return new Object[0];
			}
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}
	}

	private static final int XSD_LIST_VIEWER_HEIGHT = 150;

	private static final int LIST_VIEWER_WIDTH = 340;

	private TableViewer xsdViewer;

	private CheckboxTableViewer versionViewer;

	private Button useHttpsCheckbox;
	private Button versionCheckbox;
	private Button classpathCheckbox;
	private Button disableNamespaceCachingCheckbox;

	private Map<INamespaceDefinition, String> versions = new ConcurrentHashMap<INamespaceDefinition, String>();

	private Map<INamespaceDefinition, String> prefixes = new ConcurrentHashMap<INamespaceDefinition, String>();

	private INamespaceDefinition selectedNamespaceDefinition;

	private Text prefixText;

	private volatile List<INamespaceDefinition> namespaceDefinitionList = new CopyOnWriteArrayList<INamespaceDefinition>();

	private volatile boolean loading = false;


	private synchronized List<INamespaceDefinition> getNamespaceDefinitionList() {
		if ((namespaceDefinitionList == null || namespaceDefinitionList.size() == 0) && !loading) {
			loading = true;
			XmlUiNamespaceUtils.getNamespaceDefinitions(getProject(), new XmlUiNamespaceUtils.INamespaceDefinitionTemplate() {

				public void doWithNamespaceDefinitions(INamespaceDefinition[] namespaceDefinitions, IProject project) {

					namespaceDefinitionList = new ArrayList<INamespaceDefinition>(Arrays.asList(namespaceDefinitions));

					versions.clear();
					prefixes.clear();
					
					if (isProjectPreferencePage()) {
						SpringCorePreferences prefs = SpringCorePreferences.getProjectPreferences(getProject(),
								SpringXmlNamespacesPlugin.PLUGIN_ID);
						for (INamespaceDefinition namespace : namespaceDefinitions) {
							String version = prefs.getString(SpringXmlNamespacesPlugin.NAMESPACE_DEFAULT_VERSION_PREFERENCE_ID
									+ namespace.getNamespaceURI(), "");
							versions.put(namespace, version);
							String prefix = prefs.getString(SpringXmlNamespacesPlugin.NAMESPACE_PREFIX_PREFERENCE_ID
									+ namespace.getNamespaceURI(), namespace.getDefaultNamespacePrefix());
							prefixes.put(namespace, prefix);
						}
					}
					else {
						Preferences prefs = SpringXmlNamespacesPlugin.getDefault().getPluginPreferences();
						for (INamespaceDefinition namespace : namespaceDefinitions) {
							String version = prefs.getString(SpringXmlNamespacesPlugin.NAMESPACE_DEFAULT_VERSION_PREFERENCE_ID
									+ namespace.getNamespaceURI());
							versions.put(namespace, version);
							String prefix = prefs.getString(SpringXmlNamespacesPlugin.NAMESPACE_PREFIX_PREFERENCE_ID
									+ namespace.getNamespaceURI());
							prefixes.put(namespace, (StringUtils.hasLength(prefix) ? prefix : namespace
									.getDefaultNamespacePrefix()));

						}
					}

					Display.getDefault().asyncExec(new Runnable() {

						public void run() {
							refresh();
						}
					});

					loading = false;
				}
			});
		}
		return namespaceDefinitionList;
	}

	public Composite createPreferenceContent(Composite parent) {

		boolean versionClasspath = true;
		boolean useClasspath = true;
		boolean disableCachingNamespaces = false;
		boolean useHttps = false;
		
		if (isProjectPreferencePage()) {
			SpringCorePreferences prefs = SpringCorePreferences.getProjectPreferences(getProject(),
					SpringXmlNamespacesPlugin.PLUGIN_ID);
			versionClasspath = prefs.getBoolean(SpringXmlNamespacesPlugin.NAMESPACE_DEFAULT_FROM_CLASSPATH_ID, true);
			useClasspath = prefs.getBoolean(SpringXmlNamespacesPlugin.LOAD_NAMESPACEHANDLER_FROM_CLASSPATH_ID, true);
			disableCachingNamespaces = prefs.getBoolean(SpringXmlNamespacesPlugin.DISABLE_CACHING_FOR_NAMESPACE_LOADING_ID, false);
			useHttps = prefs.getBoolean(SpringXmlNamespacesPlugin.USE_HTTPS_FOR_SCHEMA_LOCATIONS, false);
		}
		else {
			Preferences prefs = SpringXmlNamespacesPlugin.getDefault().getPluginPreferences();
			versionClasspath = prefs.getBoolean(SpringXmlNamespacesPlugin.NAMESPACE_DEFAULT_FROM_CLASSPATH_ID);
			useClasspath = prefs.getBoolean(SpringXmlNamespacesPlugin.LOAD_NAMESPACEHANDLER_FROM_CLASSPATH_ID);
			disableCachingNamespaces = prefs.getBoolean(SpringXmlNamespacesPlugin.DISABLE_CACHING_FOR_NAMESPACE_LOADING_ID);
			useHttps = prefs.getBoolean(SpringXmlNamespacesPlugin.USE_HTTPS_FOR_SCHEMA_LOCATIONS);
		}

		initializeDialogUnits(parent);
		// top level group
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		composite.setFont(parent.getFont());
		
		useHttpsCheckbox = new Button(composite, SWT.CHECK);
		useHttpsCheckbox.setText("Use 'https' by default for namespace schema locations");
		useHttpsCheckbox.setSelection(useHttps);

		versionCheckbox = new Button(composite, SWT.CHECK);
		versionCheckbox.setText("Use highest XSD version that is available on the project's classpath");
		versionCheckbox.setSelection(versionClasspath);

		classpathCheckbox = new Button(composite, SWT.CHECK);
		classpathCheckbox.setText("Load NamespaceHandlers and XSDs from project's classpath");
		classpathCheckbox.setSelection(useClasspath);
		classpathCheckbox.addSelectionListener(new SelectionAdapter() {
			
			public void widgetSelected(SelectionEvent e) {
				if (isProjectPreferencePage()) {
					if (useProjectSettings()) {
						SpringCorePreferences.getProjectPreferences(getProject(), SpringXmlNamespacesPlugin.PLUGIN_ID).putBoolean(
								SpringXmlNamespacesPlugin.PROJECT_PROPERTY_ID, true);
					}
					else {
						SpringCorePreferences.getProjectPreferences(getProject(), SpringXmlNamespacesPlugin.PLUGIN_ID).putBoolean(
								SpringXmlNamespacesPlugin.PROJECT_PROPERTY_ID, false);
					}
					SpringCorePreferences.getProjectPreferences(getProject(), SpringXmlNamespacesPlugin.PLUGIN_ID).putBoolean(
							SpringXmlNamespacesPlugin.LOAD_NAMESPACEHANDLER_FROM_CLASSPATH_ID, classpathCheckbox.getSelection());
				}
				else {
					SpringXmlNamespacesPlugin.getDefault().getPluginPreferences().setValue(
							SpringXmlNamespacesPlugin.LOAD_NAMESPACEHANDLER_FROM_CLASSPATH_ID, classpathCheckbox.getSelection());
					SpringXmlNamespacesPlugin.getDefault().savePluginPreferences();
				}
				namespaceDefinitionList.clear();
				refresh();
				
				disableNamespaceCachingCheckbox.setEnabled(classpathCheckbox.getSelection());
			}
			
		});
		
		disableNamespaceCachingCheckbox = new Button(composite, SWT.CHECK);
		disableNamespaceCachingCheckbox.setText("Disable caching for namespace resolving and loading");
		disableNamespaceCachingCheckbox.setSelection(disableCachingNamespaces);
		disableNamespaceCachingCheckbox.setEnabled(useClasspath);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
		data.horizontalIndent = 15;
		disableNamespaceCachingCheckbox.setLayoutData(data);
		
		disableNamespaceCachingCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (isProjectPreferencePage()) {
					SpringCorePreferences.getProjectPreferences(getProject(), SpringXmlNamespacesPlugin.PLUGIN_ID).putBoolean(
							SpringXmlNamespacesPlugin.DISABLE_CACHING_FOR_NAMESPACE_LOADING_ID, disableNamespaceCachingCheckbox.getSelection());
				}
				else {
					SpringXmlNamespacesPlugin.getDefault().getPluginPreferences().setValue(
							SpringXmlNamespacesPlugin.DISABLE_CACHING_FOR_NAMESPACE_LOADING_ID, disableNamespaceCachingCheckbox.getSelection());
					SpringXmlNamespacesPlugin.getDefault().savePluginPreferences();
				}
			}
		});

		Label namespaceLabel = new Label(composite, SWT.NONE);
		namespaceLabel.setText("Select XSD namespace to configure prefix and default version:");

		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = LIST_VIEWER_WIDTH;
		gd.heightHint = XSD_LIST_VIEWER_HEIGHT;

		// config set list viewer
		xsdViewer = new TableViewer(composite, SWT.BORDER);
		xsdViewer.getTable().setLayoutData(gd);
		xsdViewer.setContentProvider(new XsdConfigContentProvider());
		xsdViewer.setLabelProvider(new XsdLabelProvider());
		xsdViewer.setInput(this); // activate content provider

		xsdViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					Object obj = ((IStructuredSelection) event.getSelection()).getFirstElement();
					if (obj != null) {
						selectedNamespaceDefinition = (INamespaceDefinition) obj;
						versionViewer.setInput(obj);
						prefixText.setText(prefixes.get(selectedNamespaceDefinition));
						if (versions.get(selectedNamespaceDefinition) != null) {
							versionViewer.setCheckedElements(new Object[] { versions.get(selectedNamespaceDefinition) });
						}
						if (selectedNamespaceDefinition.getSchemaLocations().size() > 0) {
							versionViewer.getControl().setEnabled(true);
						}
						else {
							versionViewer.getControl().setEnabled(false);
						}
					}
				}
			}
		});

		new Label(composite, SWT.DELIMITER_SELECTION);

		Composite prefixComposite = new Composite(composite, SWT.NONE);
		GridLayout prefixLayout = new GridLayout();
		prefixLayout.numColumns = 2;
		prefixLayout.marginWidth = 0;
		prefixLayout.marginHeight = 0;
		prefixComposite.setLayout(prefixLayout);
		prefixComposite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		prefixComposite.setFont(parent.getFont());

		Label prefixLabel = new Label(prefixComposite, SWT.NONE);
		prefixLabel.setText("Namespace prefix:");

		prefixText = new Text(prefixComposite, SWT.BORDER);
		prefixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		prefixText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				boolean valid = true;
				for (Map.Entry<INamespaceDefinition, String> entry : prefixes.entrySet()) {
					if (!entry.getKey().equals(selectedNamespaceDefinition)
							&& entry.getValue().equals(prefixText.getText())) {
						setErrorMessage(String.format("Prefix '%s' not unique", entry.getValue()));
						valid = false;
					}
				}
				setValid(valid);
				if (valid) {
					setErrorMessage(null);
				}
				if (StringUtils.hasLength(prefixText.getText())) {
					prefixes.put(selectedNamespaceDefinition, prefixText.getText());
				}
				else {
					prefixes.put(selectedNamespaceDefinition, selectedNamespaceDefinition.getDefaultNamespacePrefix());
				}
				xsdViewer.setInput(this);
			}
		});

		Label versionLabel = new Label(composite, SWT.NONE);
		versionLabel
				.setText("Select default schema version (if none is selected the versionless schema will be used):");

		versionViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
		versionViewer.getTable().setLayoutData(gd);
		versionViewer.setContentProvider(new VersionContentProvider());
		versionViewer.setLabelProvider(new VersionLabelProvider());
		versionViewer.setSorter(new ViewerSorter());

		versionViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(final CheckStateChangedEvent event) {
				if (event.getChecked()) {
					versionViewer.setCheckedElements(new Object[] { event.getElement() });
					if (selectedNamespaceDefinition != null) {
						versions.put(selectedNamespaceDefinition, (String) event.getElement());
					}
				}
				else {
					versionViewer.setCheckedElements(new Object[0]);
					versions.put(selectedNamespaceDefinition, "");
				}
			}
		});

		return composite;
	}

	@Override
	protected String getPreferencePageID() {
		return PREF_ID;
	}

	@Override
	protected String getPropertyPageID() {
		return PROP_ID;
	}

	@Override
	protected boolean hasProjectSpecificOptions(IProject project) {
		return SpringCorePreferences.getProjectPreferences(project, SpringXmlNamespacesPlugin.PLUGIN_ID).getBoolean(
				SpringXmlNamespacesPlugin.PROJECT_PROPERTY_ID, false);
	}

	public boolean performOk() {
		if (isProjectPreferencePage()) {
			if (useProjectSettings()) {
				SpringCorePreferences.getProjectPreferences(getProject(), SpringXmlNamespacesPlugin.PLUGIN_ID).putBoolean(
						SpringXmlNamespacesPlugin.PROJECT_PROPERTY_ID, true);
			}
			else {
				SpringCorePreferences.getProjectPreferences(getProject(), SpringXmlNamespacesPlugin.PLUGIN_ID).putBoolean(
						SpringXmlNamespacesPlugin.PROJECT_PROPERTY_ID, false);
			}
			for (Map.Entry<INamespaceDefinition, String> entry : versions.entrySet()) {
				SpringCorePreferences.getProjectPreferences(getProject(), SpringXmlNamespacesPlugin.PLUGIN_ID).putString(
						SpringXmlNamespacesPlugin.NAMESPACE_DEFAULT_VERSION_PREFERENCE_ID + entry.getKey().getNamespaceURI(),
						entry.getValue());
			}
			for (Map.Entry<INamespaceDefinition, String> entry : prefixes.entrySet()) {
				SpringCorePreferences.getProjectPreferences(getProject(), SpringXmlNamespacesPlugin.PLUGIN_ID).putString(
						SpringXmlNamespacesPlugin.NAMESPACE_PREFIX_PREFERENCE_ID + entry.getKey().getNamespaceURI(),
						entry.getValue());
			}
			SpringCorePreferences.getProjectPreferences(getProject(), SpringXmlNamespacesPlugin.PLUGIN_ID).putBoolean(
					SpringXmlNamespacesPlugin.NAMESPACE_DEFAULT_FROM_CLASSPATH_ID, versionCheckbox.getSelection());
			SpringCorePreferences.getProjectPreferences(getProject(), SpringXmlNamespacesPlugin.PLUGIN_ID).putBoolean(
					SpringXmlNamespacesPlugin.LOAD_NAMESPACEHANDLER_FROM_CLASSPATH_ID, classpathCheckbox.getSelection());
			SpringCorePreferences.getProjectPreferences(getProject(), SpringXmlNamespacesPlugin.PLUGIN_ID).putBoolean(
					SpringXmlNamespacesPlugin.USE_HTTPS_FOR_SCHEMA_LOCATIONS, useHttpsCheckbox.getSelection());
		}
		else {
			for (Map.Entry<INamespaceDefinition, String> entry : versions.entrySet()) {
				SpringXmlNamespacesPlugin.getDefault().getPluginPreferences().setValue(
						SpringXmlNamespacesPlugin.NAMESPACE_DEFAULT_VERSION_PREFERENCE_ID + entry.getKey().getNamespaceURI(),
						entry.getValue());
			}
			for (Map.Entry<INamespaceDefinition, String> entry : prefixes.entrySet()) {
				SpringXmlNamespacesPlugin.getDefault().getPluginPreferences().setValue(
						SpringXmlNamespacesPlugin.NAMESPACE_PREFIX_PREFERENCE_ID + entry.getKey().getNamespaceURI(),
						entry.getValue());
			}
			SpringXmlNamespacesPlugin.getDefault().getPluginPreferences().setValue(
					SpringXmlNamespacesPlugin.NAMESPACE_DEFAULT_FROM_CLASSPATH_ID, versionCheckbox.getSelection());
			SpringXmlNamespacesPlugin.getDefault().getPluginPreferences().setValue(
					SpringXmlNamespacesPlugin.LOAD_NAMESPACEHANDLER_FROM_CLASSPATH_ID, classpathCheckbox.getSelection());
			SpringXmlNamespacesPlugin.getDefault().getPluginPreferences().setValue(
					SpringXmlNamespacesPlugin.USE_HTTPS_FOR_SCHEMA_LOCATIONS, useHttpsCheckbox.getSelection());

			SpringXmlNamespacesPlugin.getDefault().savePluginPreferences();
		}
		// Set notifications to update UI
		NamespaceManagerProvider.get().notifyNamespaceDefinitionListeners(null);
		return true;
	};

	@Override
	protected void performDefaults() {
		super.performDefaults();
		for (Map.Entry<INamespaceDefinition, String> entry : versions.entrySet()) {
			entry.setValue("");
		}
		for (Map.Entry<INamespaceDefinition, String> entry : prefixes.entrySet()) {
			prefixes.put(entry.getKey(), entry.getKey().getDefaultNamespacePrefix());
		}
		xsdViewer.setInput(this);
	}

	private void refresh() {
		if (xsdViewer != null && xsdViewer.getControl() != null && !xsdViewer.getControl().isDisposed()) {
			xsdViewer.setInput(this);
		}
	}
}
