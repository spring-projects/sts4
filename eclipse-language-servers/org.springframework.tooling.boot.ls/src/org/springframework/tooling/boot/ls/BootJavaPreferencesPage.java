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
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
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
public class BootJavaPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

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
		final IPreferenceStore commonsLsPrefs = LanguageServerCommonsActivator.getInstance().getPreferenceStore();
		
		Composite fieldEditorParent = getFieldEditorParent();
		
		addField(new BooleanFieldEditor(Constants.PREF_SCAN_JAVA_TEST_SOURCES, "Scan Java test sources", fieldEditorParent));
		
		addField(new BooleanFieldEditor(Constants.PREF_LIVE_INFORMATION_AUTOMATIC_TRACKING_ENABLED, "Live Information - Automatic Process Tracking Enabled", fieldEditorParent));
		addField(new StringFieldEditor(Constants.PREF_LIVE_INFORMATION_AUTOMATIC_TRACKING_DELAY, "Live Information - Automatic Process Tracking Delay in ms", fieldEditorParent));
		
		addField(new StringFieldEditor(Constants.PREF_LIVE_INFORMATION_FETCH_DATA_RETRY_MAX_NO, "Live Information - Max number of retries (before giving up)", fieldEditorParent));
		addField(new StringFieldEditor(Constants.PREF_LIVE_INFORMATION_FETCH_DATA_RETRY_DELAY_IN_SECONDS, "Live Information - Delay between retries in seconds", fieldEditorParent));

		addField(new BooleanFieldEditor(PreferenceConstants.HIGHLIGHT_CODELENS_PREFS, "Highlights CodeLens", fieldEditorParent) {
			@Override
			public IPreferenceStore getPreferenceStore() {
				return commonsLsPrefs;
			}
		});
		addField(new BooleanFieldEditor(Constants.PREF_CHANGE_DETECTION, "Live Boot Change Detection", fieldEditorParent));
		
		addField(new BooleanFieldEditor(Constants.PREF_VALIDATION_SPEL_EXPRESSIONS, "SpEL Expression Syntax Validation", fieldEditorParent));
	}

}
