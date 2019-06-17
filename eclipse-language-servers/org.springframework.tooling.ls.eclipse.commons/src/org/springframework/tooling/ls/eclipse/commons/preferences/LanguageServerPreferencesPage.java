/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.commons.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.springframework.tooling.ls.eclipse.commons.LanguageServerCommonsActivator;
import org.springframework.tooling.ls.eclipse.commons.preferences.LanguageServerConsolePreferenceConstants.ServerInfo;

public class LanguageServerPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

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
		ServerInfo[] installedServers = LsPreferencesUtil.getInstalledLs();
		for (ServerInfo s : installedServers) {
			addField(new BooleanFieldEditor(s.preferenceKey, s.label, parent));
		}
	}
}
