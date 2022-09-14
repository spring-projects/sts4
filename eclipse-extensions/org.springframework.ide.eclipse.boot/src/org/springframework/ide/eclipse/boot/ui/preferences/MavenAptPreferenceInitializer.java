package org.springframework.ide.eclipse.boot.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.IStartup;
import org.osgi.service.prefs.BackingStoreException;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

public class MavenAptPreferenceInitializer implements IStartup {

	private static final String M2E_APT_PLUGIN_ID = "org.eclipse.m2e.apt";
	private static final String PREF_MODE= M2E_APT_PLUGIN_ID+".mode";

	private static final String PREF_STS_CUSTONISATIONS_APPLIED= BootActivator.PLUGIN_ID+".customised";
	// ^^^ used to ensure we do not apply our customisations more than once (allows users to change the preference
	// by themselves and not have us repeatedly return it back to our own default setting.

	@Override
	public void earlyStartup() {
		try {
			IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(M2E_APT_PLUGIN_ID);
			if (!prefs.getBoolean(PREF_STS_CUSTONISATIONS_APPLIED, false)) {
			    prefs.put(PREF_MODE, "jdt_apt");
				prefs.putBoolean(PREF_STS_CUSTONISATIONS_APPLIED, true);
			}
			prefs.flush();
		} catch (BackingStoreException e) {
			Log.log(e);
		}
	}
}
