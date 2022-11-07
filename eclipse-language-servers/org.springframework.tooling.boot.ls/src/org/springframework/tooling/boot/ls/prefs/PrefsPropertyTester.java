/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.boot.ls.prefs;

import org.eclipse.core.expressions.PropertyTester;
import org.springframework.tooling.boot.ls.BootLanguageServerPlugin;
import org.springframework.tooling.boot.ls.Constants;

public class PrefsPropertyTester extends PropertyTester {

	public PrefsPropertyTester() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		switch (property) {
		case "areRewriteProjectRefactoringsOn":
			return BootLanguageServerPlugin.getDefault().getPreferenceStore().getBoolean(Constants.PREF_REWRITE_PROJECT_REFACTORINGS);
		}
		return false;
	}

}
