/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.commons.preferences;

import static org.springframework.tooling.ls.eclipse.commons.preferences.LanguageServerConsolePreferenceConstants.ALL_SERVERS;

import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.springframework.tooling.ls.eclipse.commons.LanguageServerCommonsActivator;
import org.springframework.tooling.ls.eclipse.commons.preferences.LanguageServerConsolePreferenceConstants.ServerInfo;

@SuppressWarnings("restriction")
public class LanguageServerConsolesPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	static IPreferenceStore getPrefsStoreFromPlugin() {
		return LanguageServerCommonsActivator.getInstance().getPreferenceStore();
	}

	@Override
	public void init(IWorkbench workbench) {
		setDescription("Enablement of STS Language Server Debug Consoles. "
				+ "Changes only take effect the next time a Language Server is started.");
		setPreferenceStore(getPrefsStoreFromPlugin());
	}

	@Override
	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		for (ServerInfo s : ALL_SERVERS) {
			addField(new BooleanFieldEditor(s.preferenceKey, s.label, parent));
		}

		SWTFactory.createHorizontalSpacer(parent, 2);

		SWTFactory.createLabel(parent, "Settings for Spring Languare Server extensions:", 2);

		addField(new BooleanFieldEditor(PreferenceConstants.HIGHLIGHT_CODELENS_PREFS, "Highlights CodeLens (Experimental)", parent));

	}
}
