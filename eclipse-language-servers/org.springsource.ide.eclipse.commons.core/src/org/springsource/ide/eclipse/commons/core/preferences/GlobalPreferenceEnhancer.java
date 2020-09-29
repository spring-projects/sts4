/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.core.preferences;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.springsource.ide.eclipse.commons.internal.core.CorePlugin;

public class GlobalPreferenceEnhancer {

	// JDT ui constants
	private static final String JDT_UI_BUNDLE_ID = "org.eclipse.jdt.ui";

	// org.eclipse.jdt.ui.PreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS_JAVA
	private static final String CODEASSIST_AUTOACTIVATION_TRIGGERS_JAVA = "content_assist_autoactivation_triggers_java";

	// org.eclipse.jdt.ui.PreferenceConstants.CODEASSIST_AUTOACTIVATION_DELAY
	private static final String CODEASSIST_AUTOACTIVATION_DELAY = "content_assist_autoactivation_delay";

	// org.eclipse.jdt.ui.PreferenceConstants.EDITOR_ESCAPE_STRINGS
	private static final String EDITOR_ESCAPE_STRINGS = "escape¥Strings";

	// org.eclipse.jdt.ui.PreferenceConstants.EDITOR_SMART_PASTE
	private static final String EDITOR_SMART_PASTE = "smartPaste";

	// org.eclipse.jdt.ui.PreferenceConstants.EDITOR_SMART_TAB
	private static final String EDITOR_SMART_TAB = "smart_tab";

	// org.eclipse.jdt.ui.PreferenceConstants.EDITOR_SMART_BACKSPACE
	private static final String EDITOR_SMART_BACKSPACE = "smart_backspace";

	// org.eclipse.jdt.ui.PreferenceConstants.EDITOR_SMART_OPENING_BRACE
	private static final String EDITOR_SMART_OPENING_BRACE = "smart_opening_brace";

	// org.eclipse.jdt.ui.PreferenceConstants.EDITOR_SMART_SEMICOLON
	private static final String EDITOR_SMART_SEMICOLON = "smart_semicolon";

	// M2E constants
	private static final String M2E_CORE_BUNDLE_ID = "org.eclipse.m2e.core";

	// org.eclipse.m2e.core.internal.preferences.MavenPreferenceConstants.P_HIDE_FOLDERS_OF_NESTED_PROJECTS
	private static final String P_HIDE_FOLDERS_OF_NESTED_PROJECTS = "eclipse.m2.hideFoldersOfNestedProjects";

	// org.eclipse.m2e.core.internal.preferences.MavenPreferenceConstants.P_DEFAULT_POM_EDITOR_PAGE
	private static final String P_DEFAULT_POM_EDITOR_PAGE = "eclipse.m2.defaultPomEditorPage";

	// org.eclipse.m2e.core.internal.preferences.MavenPreferenceConstants.P_UPDATE_INDEXES
	private static final String P_UPDATE_INDEXES = "eclipse.m2.updateIndexes";

	// jdt core constatnts
	private static final String JDT_CORE_BUNDLE_ID = "org.eclipse.jdt.core";

	// org.eclipse.jdt.core.JavaCore.COMPILER_PB_NON_NLS_STRING_LITERAL
	private static final String COMPILER_PB_NON_NLS_STRING_LITERAL = "org.eclipse.jdt.core.compiler.problem.nonExternalizedStringLiteral";

	public void enhanceAllPreferences() {
		enhanceM2EPreferences();
		enhanceJDTPreferences();
	}

	public void enhanceM2EPreferences() {
		IEclipsePreferences m2eCorePrefs = InstanceScope.INSTANCE.getNode(M2E_CORE_BUNDLE_ID);
		if (m2eCorePrefs != null) {
			m2eCorePrefs.putBoolean(P_HIDE_FOLDERS_OF_NESTED_PROJECTS, true);
			m2eCorePrefs.putBoolean(P_DEFAULT_POM_EDITOR_PAGE, true);
			m2eCorePrefs.putBoolean(P_UPDATE_INDEXES, false);
			try {
				m2eCorePrefs.flush();
			}
			catch (BackingStoreException e) {
				handleException(e);
			}
		}
	}

	public void enhanceJDTPreferences() {
		IEclipsePreferences jdtCorePrefs = InstanceScope.INSTANCE.getNode(JDT_CORE_BUNDLE_ID);
		if (jdtCorePrefs != null) {
			jdtCorePrefs.put(COMPILER_PB_NON_NLS_STRING_LITERAL, "ignore");

			try {
				jdtCorePrefs.flush();
			}
			catch (BackingStoreException e) {
				handleException(e);
			}
		}

		IEclipsePreferences jdtUiPrefs = InstanceScope.INSTANCE.getNode(JDT_UI_BUNDLE_ID);
		if (jdtUiPrefs != null) {
			jdtUiPrefs.putBoolean(EDITOR_SMART_PASTE, true);
			jdtUiPrefs.putBoolean(EDITOR_ESCAPE_STRINGS, true);
			jdtUiPrefs.putInt(CODEASSIST_AUTOACTIVATION_DELAY, 40);
			//jdtUiPrefs.put(CODEASSIST_AUTOACTIVATION_TRIGGERS_JAVA, "_.abcdefghijklmnopqrstuvwxyz");
			jdtUiPrefs.putBoolean(EDITOR_SMART_SEMICOLON, true);
			jdtUiPrefs.putBoolean(EDITOR_SMART_OPENING_BRACE, true);
			jdtUiPrefs.putBoolean(EDITOR_SMART_BACKSPACE, true);
			jdtUiPrefs.putBoolean(EDITOR_SMART_TAB, true);

			// org.eclipse.jdt.ui.PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS
			// disable for now until we figure out the best things to include
			// here;
			// it is a semi-colon separateted list where each element looks like
			// a static import statement
			// jdtUiPrefs.put("content_assist_favorite_static_members",
			// "org.eclipse.jdt.internal.ui.javaeditor.IJavaEditorActionConstants.*");

			try {
				jdtUiPrefs.flush();
			}
			catch (BackingStoreException e) {
				handleException(e);
			}
		}
	}

	public void undoAll() {
		undoJDTEnhance();
		undoM2EEnhance();
	}

	public void undoJDTEnhance() {
		IEclipsePreferences jdtCorePrefs = InstanceScope.INSTANCE.getNode(JDT_CORE_BUNDLE_ID);
		if (jdtCorePrefs != null) {
			jdtCorePrefs.remove(COMPILER_PB_NON_NLS_STRING_LITERAL);

			try {
				jdtCorePrefs.flush();
			}
			catch (BackingStoreException e) {
				handleException(e);
			}
		}

		IEclipsePreferences jdtUiPrefs = InstanceScope.INSTANCE.getNode(JDT_UI_BUNDLE_ID);
		if (jdtUiPrefs != null) {
			jdtUiPrefs.remove(EDITOR_SMART_PASTE);
			jdtUiPrefs.remove(EDITOR_ESCAPE_STRINGS);
			jdtUiPrefs.remove(CODEASSIST_AUTOACTIVATION_DELAY);
			jdtUiPrefs.remove(CODEASSIST_AUTOACTIVATION_TRIGGERS_JAVA);
			jdtUiPrefs.remove(EDITOR_SMART_SEMICOLON);
			jdtUiPrefs.remove(EDITOR_SMART_OPENING_BRACE);
			jdtUiPrefs.remove(EDITOR_SMART_BACKSPACE);
			jdtUiPrefs.remove(EDITOR_SMART_TAB);

			try {
				jdtUiPrefs.flush();
			}
			catch (BackingStoreException e) {
				handleException(e);
			}
		}
	}

	public void undoM2EEnhance() {
		IEclipsePreferences m2eCorePrefs = InstanceScope.INSTANCE.getNode(M2E_CORE_BUNDLE_ID);
		if (m2eCorePrefs != null) {
			m2eCorePrefs.remove(P_HIDE_FOLDERS_OF_NESTED_PROJECTS);
			m2eCorePrefs.remove(P_DEFAULT_POM_EDITOR_PAGE);
			m2eCorePrefs.remove(P_UPDATE_INDEXES);
			try {
				m2eCorePrefs.flush();
			}
			catch (BackingStoreException e) {
				handleException(e);
			}
		}
	}

	private void handleException(BackingStoreException e) {
		CorePlugin.getDefault().getLog()
				.log(new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, e.getLocalizedMessage(), e));
	}

}
