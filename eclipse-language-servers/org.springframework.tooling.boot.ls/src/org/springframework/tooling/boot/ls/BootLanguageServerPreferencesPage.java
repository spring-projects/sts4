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
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
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
	protected void createFieldEditors() {
		
		Composite parent = getFieldEditorParent();
		
		Label l = new Label(parent, SWT.NONE);
		l.setFont(parent.getFont());
		l.setText("Settings for Spring Boot Live Beans data:");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.grabExcessHorizontalSpace = false;
		l.setLayoutData(gd);

		final IPreferenceStore commonsLsPrefs = LanguageServerCommonsActivator.getInstance().getPreferenceStore();

		addField(new BooleanFieldEditor(Constants.PREF_BOOT_HINTS, "Live Boot Hint Decorators", parent));
		addField(new BooleanFieldEditor(PreferenceConstants.HIGHLIGHT_CODELENS_PREFS, "Highlights CodeLens (experimental)", parent) {
			@Override
			public IPreferenceStore getPreferenceStore() {
				return commonsLsPrefs;
			}
		});
		
		addField(new BooleanFieldEditor(Constants.PREF_SUPPORT_SPRING_XML_CONFIGS, "Support Spring XML Config files (experimental)", parent));
		addField(new BooleanFieldEditor(Constants.PREF_CHANGE_DETECTION, "Live Boot Change Detection", parent));
		
		l = new Label(parent, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		l.setLayoutData(gd);

	}

}
