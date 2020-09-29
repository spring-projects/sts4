/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.ngrok;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springsource.ide.eclipse.commons.frameworks.core.util.ArrayEncoder;

/**
 * Manages the ngrok installations that are configured in this workspace.
 *
 * @author Martin Lippert
 * @author Kris De Volder
 */
public class NGROKInstallManager {

	private static final String NGROK_INSTALLS = "ngroks"; //Key used to store info in the prefs.
	private static final String DEFAULT_NGROK_INSTALL = "default.ngrok";

	private static NGROKInstallManager instance;

	public static NGROKInstallManager getInstance() {
		if (instance==null) {
			instance = new NGROKInstallManager();
		}
		return instance;
	}

	private List<String> installs = null;
	private String defaultInstall;

	private NGROKInstallManager() {
		installs = new CopyOnWriteArrayList<String>();
		read();
	}

	private void read() {
		IEclipsePreferences prefs = getPrefs();
		String defaultName = prefs.get(DEFAULT_NGROK_INSTALL, "");
		String encoded = prefs.get(NGROK_INSTALLS, null);

		if (encoded!=null) {
			String[] encodedInstalls = ArrayEncoder.decode(encoded);
			for (String encodedInstall : encodedInstalls) {
				try {
					String[] parts = ArrayEncoder.decode(encodedInstall);
					String install = parts[0];
					installs.add(install);
					if (defaultName.equals(install)) {
						setDefaultInstall(install);
					}
				} catch (Exception e) {
					BootActivator.log(e);
				}
			}
		}
	}

	protected IEclipsePreferences getPrefs() {
		return InstanceScope.INSTANCE.getNode(BootDashActivator.PLUGIN_ID);
	}

	public void save() {
		List<String> local = installs;
		String[] encodedInstalls = new String[local.size()];
		for (int i = 0; i < encodedInstalls.length; i++) {
			encodedInstalls[i] = encodeInstall(local.get(i));
		}

		String encoded = ArrayEncoder.encode(encodedInstalls);
		IEclipsePreferences prefs = getPrefs();
		prefs.put(NGROK_INSTALLS, encoded);

		String di = null;
		try {
			di = getDefaultInstall();
		} catch (Exception e) {
			BootActivator.log(e);
		}

		if (di==null) {
			prefs.remove(DEFAULT_NGROK_INSTALL);
		} else {
			prefs.put(DEFAULT_NGROK_INSTALL, di);
		}
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			BootActivator.log(e);
		}
	}

	private String encodeInstall(String install) {
		return ArrayEncoder.encode(new String[] { install});
	}

	public synchronized String getDefaultInstall() {
		return defaultInstall;
	}

	public void setDefaultInstall(String defaultInstall) {
		if (!installs.contains(defaultInstall)) {
			installs.add(defaultInstall);
		}
		this.defaultInstall = defaultInstall;
	}

	public void setInstalls(Collection<String> newInstalls) {
		this.installs = new CopyOnWriteArrayList<String>(newInstalls);
	}

	public Collection<String> getInstalls() {
		return new ArrayList<String>(installs);
	}

	public void addInstall(String ngrokInstall) {
		this.installs.add(ngrokInstall);
	}

}
