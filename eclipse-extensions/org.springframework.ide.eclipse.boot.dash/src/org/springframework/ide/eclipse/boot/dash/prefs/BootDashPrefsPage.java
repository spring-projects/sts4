/*******************************************************************************
 *  Copyright (c) 2020, 2023 Pivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.prefs;

import static org.springframework.ide.eclipse.boot.core.BootPreferences.PREF_BOOT_PROJECT_EXCLUDE;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.api.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.views.DeleteElementsAction;
import org.springframework.ide.eclipse.boot.ui.preferences.RegExpFieldEditor;

/**
 * @author Kris De Volder
 */
public class BootDashPrefsPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public BootDashPrefsPage() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(BootDashActivator.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();

		StringFieldEditor projectExclude = new RegExpFieldEditor(PREF_BOOT_PROJECT_EXCLUDE, "Exclude Projects", parent);
		setTooltip(parent, projectExclude, "Any project who's name matches this regexp will NOT be treated as a Spring Boot App");
		projectExclude.setPreferenceStore(BootActivator.getDefault().getPreferenceStore());
		addField(projectExclude);


		for (RunTargetType<?> rtt : BootDashActivator.getDefault().getModel().getRunTargetTypes()) {
			if (rtt.supportsDeletion()) {
				addField(new BooleanFieldEditor(DeleteElementsAction.PREF_SKIP_CONFIRM_DELETE(rtt), "Skip Delete Element Confirmation ("+rtt.getName()+")", parent));
			}
		}
	}

//	@Override
//	protected void adjustGridLayout() {
//		// Do nothing. Page offers one column grid layout. Group controls layout fields appropriately.
//	}

	private void setTooltip(Composite parent, StringFieldEditor fe, String tooltip) {
		fe.getLabelControl(parent).setToolTipText(tooltip);
		fe.getTextControl(parent).setToolTipText(tooltip);
	}
//
//	private void setTooltip(Composite parent, BooleanFieldEditor2 fe, String tooltip) {
//		fe.getChangeControl(parent).setToolTipText(tooltip);
//	}

}
