/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.xterm.views;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.springframework.ide.eclipse.xterm.XtermPlugin;

public class XtermPreferencesInitializer extends AbstractPreferenceInitializer {

	public XtermPreferencesInitializer() {
	}

	@Override
	public void initializeDefaultPreferences() {
		XtermPlugin.getDefault().getPreferenceStore().setDefault(XtermPlugin.PREFS_DEFAULT_SHELL_CMD, Platform.OS_WIN32.equals(Platform.getOS()) ? "cmd.exe" : "/bin/bash");
	}

}
