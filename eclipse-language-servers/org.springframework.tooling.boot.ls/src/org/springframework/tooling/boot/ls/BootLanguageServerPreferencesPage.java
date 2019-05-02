/*******************************************************************************
 * Copyright (c) 2017, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.boot.ls;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.springframework.tooling.ls.eclipse.commons.LanguageServerCommonsActivator;
import org.springframework.tooling.ls.eclipse.commons.preferences.PreferenceConstants;

/**
 * Preference page for Boot-Java LS extension
 * 
 * @author Alex Boyko
 *
 */
public class BootLanguageServerPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public BootLanguageServerPreferencesPage() {
		super(GRID);
	}

	/**
	 * Starts a preference change listener that keeps code mining preferences in sync with
	 * whether or not STS4 codelenses are enabled.
	 */
	public static void manageCodeMiningPreferences() {
		IPreferenceStore ourPrefs = LanguageServerCommonsActivator.getInstance().getPreferenceStore();
		synchronizeCodeMiningPrefs(ourPrefs);
		ourPrefs.addPropertyChangeListener(new IPropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getProperty().equals(PreferenceConstants.HIGHLIGHT_CODELENS_PREFS)) {
					synchronizeCodeMiningPrefs(ourPrefs);
				}
			}
		});
	}

	private static void synchronizeCodeMiningPrefs(IPreferenceStore ourPrefs) {
		boolean codeLensEnabled = ourPrefs.getBoolean(PreferenceConstants.HIGHLIGHT_CODELENS_PREFS);
		if (codeLensEnabled) {
			//Make sure jdt code mining is enabled. Codelenses do not work without it.
			IEclipsePreferences jdtPrefs = InstanceScope.INSTANCE.getNode("org.eclipse.jdt.ui");
			boolean codeMiningIsEnabled = jdtPrefs.getBoolean("editor_codemining_enabled", false);
			if (!codeMiningIsEnabled) {
				jdtPrefs.putBoolean("editor_codemining_enabled", true);
				//Disable all individual code minings. Since code mining wasn't enabled before...
				//This merely serves to ensure they don't start showing up all of a sudden.
				jdtPrefs.putBoolean("java.codemining.references", false);
				jdtPrefs.putBoolean("java.codemining.references.onMethods", false);
				jdtPrefs.putBoolean("java.codemining.references.onFields", false);
				jdtPrefs.putBoolean("java.codemining.references.onTypes", false);
				jdtPrefs.putBoolean("java.codemining.implementations", false);
				jdtPrefs.putBoolean("java.codemining.atLeastOne", false);
			}
		}
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(BootLanguageServerPlugin.getDefault().getPreferenceStore());
	}
	
	@Override
	protected void adjustGridLayout() {
		// Keep empty
	}

	@Override
	protected void createFieldEditors() {
		final IPreferenceStore commonsLsPrefs = LanguageServerCommonsActivator.getInstance().getPreferenceStore();
		
		Composite contents = new Composite(getFieldEditorParent(), SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		contents.setLayoutData(gd);
		contents.setLayout(new GridLayout());
		
		Group liveBeansGroup = new Group(contents, SWT.NONE);
		liveBeansGroup.setText("Spring Boot Live Beans");
		liveBeansGroup.setLayout(new GridLayout(1, false));
		liveBeansGroup.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());

		addField(new BooleanFieldEditor(Constants.PREF_BOOT_HINTS, "Live Boot Hint Decorators", liveBeansGroup));
		addField(new BooleanFieldEditor(PreferenceConstants.HIGHLIGHT_CODELENS_PREFS, "Highlights CodeLens", liveBeansGroup) {
			@Override
			public IPreferenceStore getPreferenceStore() {
				return commonsLsPrefs;
			}
		});
		addField(new BooleanFieldEditor(Constants.PREF_CHANGE_DETECTION, "Live Boot Change Detection", liveBeansGroup));
		
		Group symbolGroup = new Group(contents, SWT.NONE);
		symbolGroup.setText("Spring Symbols");
		symbolGroup.setLayout(new GridLayout(1, false));
		symbolGroup.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		
		addField(new BooleanFieldEditor(Constants.PREF_SCAN_JAVA_TEST_SOURCES, "Scan Java test sources", symbolGroup));
		addField(new BooleanFieldEditor(Constants.PREF_SUPPORT_SPRING_XML_CONFIGS, "Scan Spring XML Config files (experimental)", symbolGroup));
		
		Composite scanFoldersComposite = new Composite(symbolGroup, SWT.NONE);
		scanFoldersComposite.setFont(symbolGroup.getFont());
		scanFoldersComposite.setLayoutData(GridDataFactory.swtDefaults().span(2, 1).align(GridData.FILL, GridData.BEGINNING).grab(true, false).create());
		scanFoldersComposite.setLayout(new GridLayout(2, false));
		addField(new StringFieldEditor(Constants.PREF_XML_CONFIGS_SCAN_FOLDERS, "Scan Spring XML in folders:", scanFoldersComposite));

	}

}
