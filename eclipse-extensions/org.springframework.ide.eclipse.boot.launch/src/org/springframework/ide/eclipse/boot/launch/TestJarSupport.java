/*******************************************************************************
 * Copyright (c) 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.launch;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.BootPreferences;

public class TestJarSupport {

	private static final TestJarLaunchListener LAUNCHES_LISTENER = new TestJarLaunchListener();

	private static final IPropertyChangeListener PREFERENCE_LISTENER = propertyChange -> {
		if (BootPreferences.PREF_BOOT_TESTJARS_LAUNCH_SUPPORT.equals(propertyChange.getProperty())) {
			ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
			if (BootActivator.getDefault().getPreferenceStore().getBoolean(BootPreferences.PREF_BOOT_TESTJARS_LAUNCH_SUPPORT)) {
				// TestJars support ON
				launchManager.addLaunchListener(LAUNCHES_LISTENER);
			} else {
				// TestJars support OFF
				launchManager.removeLaunchListener(LAUNCHES_LISTENER);
				LAUNCHES_LISTENER.clearTestJarArtifactEnvKeyFromLaunches(launchManager);
			}
		}
	};

	public static void start() {
		if (BootActivator.getDefault().getPreferenceStore().getBoolean(BootPreferences.PREF_BOOT_TESTJARS_LAUNCH_SUPPORT)) {
			BootActivator.getDefault().getPreferenceStore().addPropertyChangeListener(PREFERENCE_LISTENER);
			ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
			launchManager.addLaunchListener(LAUNCHES_LISTENER);
		}

	}

	public static void stop() {
		if (BootActivator.getDefault().getPreferenceStore().getBoolean(BootPreferences.PREF_BOOT_TESTJARS_LAUNCH_SUPPORT)) {
			BootActivator.getDefault().getPreferenceStore().removePropertyChangeListener(PREFERENCE_LISTENER);
			ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
			launchManager.removeLaunchListener(LAUNCHES_LISTENER);
			LAUNCHES_LISTENER.clearTestJarArtifactEnvKeyFromLaunches(launchManager);
		}
	}

}
