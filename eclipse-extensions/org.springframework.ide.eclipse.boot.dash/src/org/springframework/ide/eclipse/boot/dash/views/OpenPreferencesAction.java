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
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;

/**
 * Action opens Preferences dialog on some specific page.
 *
 * @author Alex Boyko
 */
public class OpenPreferencesAction extends AbstractBootDashAction {

	private final String PREFERENCE_PAGE_ID;

	protected OpenPreferencesAction(SimpleDIContext context, String pageId, String text, String tooltip) {
		super(context, IAction.AS_PUSH_BUTTON);
		this.PREFERENCE_PAGE_ID = pageId;
		setText(text);
		setToolTipText(tooltip);
	}

	@Override
	public void run() {
		PreferencesUtil.createPreferenceDialogOn(null, PREFERENCE_PAGE_ID, null, null).open();
	}

}
