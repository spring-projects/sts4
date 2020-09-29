/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.springframework.ide.eclipse.boot.core.BootPreferences;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

/**
 * Action opens Preferences dialog at Spring -> Boot page to allow Spring Boot
 * projects filtering
 *
 * @author Alex Boyko
 *
 */
public class OpenFilterPreferencesAction extends AbstractBootDashAction {

	protected OpenFilterPreferencesAction(SimpleDIContext context) {
		super(context, IAction.AS_PUSH_BUTTON);
		setText("Boot Projects Filters Preferences...");
		setToolTipText("Open Preferences for Spring Boot projects filters");
	}

	@Override
	public void run() {
		PreferencesUtil.createPreferenceDialogOn(null, BootPreferences.BOOT_PREFERENCE_PAGE_ID, null, null).open();
	}

}
