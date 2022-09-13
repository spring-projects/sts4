/*******************************************************************************
 *  Copyright (c) 2020, 2022 Pivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.prefs;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.api.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.views.DeleteElementsAction;

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
		for (RunTargetType rtt : BootDashActivator.getDefault().getModel().getRunTargetTypes()) {
			if (rtt.supportsDeletion()) {
				addField(new BooleanFieldEditor(DeleteElementsAction.PREF_SKIP_CONFIRM_DELETE(rtt), "Skip Delete Element Confirmation ("+rtt.getName()+")", parent));
			}
		}
	}

//	@Override
//	protected void adjustGridLayout() {
//		// Do nothing. Page offers one column grid layout. Group controls layout fields appropriately.
//	}

//	private void setTooltip(Composite parent, StringFieldEditor fe, String tooltip) {
//		fe.getLabelControl(parent).setToolTipText(tooltip);
//		fe.getTextControl(parent).setToolTipText(tooltip);
//	}
//
//	private void setTooltip(Composite parent, BooleanFieldEditor2 fe, String tooltip) {
//		fe.getChangeControl(parent).setToolTipText(tooltip);
//	}

}
