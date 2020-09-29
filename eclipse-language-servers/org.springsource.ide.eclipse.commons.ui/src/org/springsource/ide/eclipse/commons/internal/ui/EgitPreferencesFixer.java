/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.internal.ui;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.IStartup;
import org.osgi.service.prefs.BackingStoreException;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;

/**
 * Override default setting(s) from egit which tend(s) to annoy our users.
 */
public class EgitPreferencesFixer implements IStartup {

	private final IEclipsePreferences ourPrefs = InstanceScope.INSTANCE.getNode(UiPlugin.PLUGIN_ID);
	private final IEclipsePreferences egitPrefs = InstanceScope.INSTANCE.getNode("org.eclipse.egit.core");

	boolean dirty = false;

	/**
	 * Decides is a given key should be fixed by us. This is true if and only if we have not fixed the
	 * that key before. Thus we only try to fix a key once in each workspace. This is to avoid
	 * overwriting the key again the next time we start eclipse (if a user manually changed its value
	 * this will be annoying, and if the user did not change the value setting it again is unnecssary).
	 */
	private boolean shouldFixKey(String key) {
		String wasFixedKey = "egit.pref.fixed."+key;
		boolean wasFixed = ourPrefs.getBoolean(wasFixedKey, false);
		if (!wasFixed) {
			ourPrefs.putBoolean(wasFixedKey, true);
			dirty = true;
		}
		return !wasFixed;
	}

	@Override
	public void earlyStartup() {
		set("core_autoIgnoreDerivedResources", "false");
		flush();
	}

	private void flush() {
		if (dirty) {
			flush(ourPrefs);
			flush(egitPrefs);
		}
	}

	private void flush(IEclipsePreferences prefs) {
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			UiPlugin.getDefault().getLog().log(ExceptionUtil.status(e));
		}
	}

	private void set(String key, String value) {
		if (shouldFixKey(key)) {
			egitPrefs.put(key, value);
		}
	}

}
