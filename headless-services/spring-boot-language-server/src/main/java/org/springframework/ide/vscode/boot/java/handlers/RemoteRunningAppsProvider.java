/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.handlers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.boot.app.cli.RemoteSpringBootApp;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.languageserver.util.Settings;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.CollectorUtil;

public class RemoteRunningAppsProvider implements RunningAppProvider {

	private static Logger logger = LoggerFactory.getLogger(RemoteRunningAppsProvider.class);

	/**
	 * We keep the remote app instances in a Map indexed by url. This allows us to
	 * return the same instance(s) repeatedly as long as the urls do not change.
	 */
	private Map<String,SpringBootApp> remoteAppByUrl = new HashMap<>();

	public RemoteRunningAppsProvider(SimpleLanguageServer server) {
		server.getWorkspaceService().onDidChangeConfiguraton(this::handleSettings);
	}

	@Override
	public synchronized Collection<SpringBootApp> getAllRunningSpringApps() throws Exception {
		return remoteAppByUrl.values().stream().filter(SpringBootApp::isSpringBootApp).collect(CollectorUtil.toImmutableList());
	}

	synchronized void handleSettings(Settings settings) {
		Set<String> urls = settings.getStringSet("boot-java", "remote-apps");

		{ //Remove obsolete apps...
			Iterator<String> keys = remoteAppByUrl.keySet().iterator();
			while (keys.hasNext()) {
				String key = keys.next();
				if (!urls.contains(key)) {
					logger.debug("Removing RemoteSpringBootApp: "+key);
					keys.remove();
				}
			}
		}

		{ //Add new apps
			for (String url : urls) {
				remoteAppByUrl.computeIfAbsent(url, (_url) -> {
					logger.debug("Creating RemoteStringBootApp: "+_url);
					return RemoteSpringBootApp.create(_url);
				});
			}
		}
	}

}
