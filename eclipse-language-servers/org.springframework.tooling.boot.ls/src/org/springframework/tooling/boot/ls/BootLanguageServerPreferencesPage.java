/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.boot.ls;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page for Boot-Java LS extension
 * 
 * @author Alex Boyko
 *
 */
public class BootLanguageServerPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public BootLanguageServerPreferencesPage() {
	}

	@Override
	public void init(IWorkbench workbench) {
		setDescription("Settings for Boot-Java language server extension");
		setPreferenceStore(BootLanguageServerPlugin.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		BooleanFieldEditor liveHintsPrefEditor = new BooleanFieldEditor(Constants.PREF_BOOT_HINTS, "Live Boot Hint Decorators", getFieldEditorParent());
		addField(liveHintsPrefEditor);
		
		BooleanFieldEditor liveChangeDetectionPrefEditor = new BooleanFieldEditor(Constants.PREF_CHANGE_DETECTION, "Live Boot Change Detection", getFieldEditorParent());
		addField(liveChangeDetectionPrefEditor);
	}

}
