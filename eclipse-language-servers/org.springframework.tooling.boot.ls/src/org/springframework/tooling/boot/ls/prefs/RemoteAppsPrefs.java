/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.tooling.boot.ls.prefs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.tooling.boot.ls.BootLanguageServerPlugin;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class RemoteAppsPrefs {

	public static final String REMOTE_APPS_KEY = "remote-apps";

	private IEclipsePreferences prefs = BootLanguageServerPlugin.getPreferences();

	public void setRawJson(String json) {
		prefs.put(REMOTE_APPS_KEY, json);
	}

	public String getRawJson() {
		return prefs.get(REMOTE_APPS_KEY, "");
	}

	public List<List<String>> getRemoteAppData() {
		String json = getRawJson();
		try {
			return parse(json);
		} catch (Exception e) {
			Log.warn("Problem parsing manually configured boot remote apps data: "+ExceptionUtil.getMessage(e));
			return ImmutableList.of();
		}
	}

	public static List<List<String>> parse(String json) throws JSONException {
		Builder<List<String>> buider = ImmutableList.builder();
		if (!json.trim().equals("")) {
			JSONArray remoteApps = new JSONArray(json);
			for (int i = 0; i < remoteApps.length(); i++) {
				JSONObject app = remoteApps.getJSONObject(i);
				String host = app.getString("host");
				String jmxUrl = app.getString("jmxurl");
				String port = app.optString("port");
				String urlScheme = app.optString("urlScheme");
				if (host!=null && jmxUrl!=null) {
					//Not using ImmutableList because it doesn't allow null values in elements.
					ArrayList<String> remoteApp = new ArrayList<>(4);
					remoteApp.add(jmxUrl);
					remoteApp.add(host);
					if (urlScheme!=null) {
						remoteApp.add(port); //could be null!
						remoteApp.add(urlScheme);
					} else if (host!=null) {
						remoteApp.add(port);
						//don't need to add urlScheme because we know it is null
					}
					buider.add(remoteApp);
				}
			}
		}
		return buider.build();
	}

	public static Disposable addListener(Runnable runnable) {
		IPreferenceChangeListener l = event -> {
			if (event.getKey().equals(REMOTE_APPS_KEY)) {
				runnable.run();
			}
		};
		getPreferences().addPreferenceChangeListener(l);
		return () -> {
				getPreferences().removePreferenceChangeListener(l);
		};
	}

	private static IEclipsePreferences getPreferences() {
		return BootLanguageServerPlugin.getPreferences();
	}

}
