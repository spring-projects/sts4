/*******************************************************************************
 * Copyright (c) 2012, 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.ui;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.springsource.ide.eclipse.commons.internal.ui.UiPlugin;

/**
 * A header page for all preference pages contributed to the
 * org.springsource.ide.eclipse.commons.preferencePage category
 *
 * @author Leo Dos Santos
 * @author Andrew Eisenberg
 */
public class CommonsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public CommonsPreferencePage() {
		super(GRID);
		setPreferenceStore(UiPlugin.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
	}

	public void init(IWorkbench workbench) {
		noDefaultAndApplyButton();
	}

}
