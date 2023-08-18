/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.boot.ls.prefs;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.springframework.tooling.boot.ls.BootLanguageServerPlugin;
import org.springframework.tooling.boot.ls.Constants;

public class ValidationPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(BootLanguageServerPlugin.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		Composite fieldEditorParent = getFieldEditorParent();
		
		addField(new BooleanFieldEditor(Constants.PREF_JAVA_RECONCILE,
				"Reconciling of Java Sources", fieldEditorParent));
				
		addField(new BooleanFieldEditor(Constants.PREF_JAVA_RECONCILE_PROMPT,
				"Prompt for Reconciling of Java Sources", fieldEditorParent));
	}

}
