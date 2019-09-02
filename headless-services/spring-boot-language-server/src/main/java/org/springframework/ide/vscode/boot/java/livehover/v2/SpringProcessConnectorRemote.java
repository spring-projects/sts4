/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.livehover.v2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.languageserver.util.Settings;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;

/**
 * @author Martin Lippert
 */
public class SpringProcessConnectorRemote {

	public static class RemoteBootAppData {

		private String jmxurl;
		private String host;
		private String urlScheme = "https";
		private String port = "443";
		private boolean keepChecking = true;
			//keepChecking defaults to true. Boot dash automatic remote apps should override this explicitly.
			//Reason. All other 'sources' of remote apps are 'manual' and we want them to default to
			//'keepChecking' even if the user doesn't set this to true manually.

		public String getJmxurl() {
			return jmxurl;
		}

		public void setJmxurl(String jmxurl) {
			this.jmxurl = jmxurl;
		}

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public String getUrlScheme() {
			return urlScheme;
		}

		public void setUrlScheme(String urlScheme) {
			this.urlScheme = urlScheme;
		}

		public String getPort() {
			return port;
		}

		public void setPort(String port) {
			this.port = port;
		}

		public boolean isKeepChecking() {
			return keepChecking;
		}

		public void setKeepChecking(boolean keepChecking) {
			this.keepChecking = keepChecking;
		}

		@Override
		public String toString() {
			return "RemoteBootAppData [jmxurl=" + jmxurl + ", host=" + host + ", urlScheme=" + urlScheme + ", port="
					+ port + ", keepChecking=" + keepChecking + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((host == null) ? 0 : host.hashCode());
			result = prime * result + ((jmxurl == null) ? 0 : jmxurl.hashCode());
			result = prime * result + (keepChecking ? 1231 : 1237);
			result = prime * result + ((port == null) ? 0 : port.hashCode());
			result = prime * result + ((urlScheme == null) ? 0 : urlScheme.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RemoteBootAppData other = (RemoteBootAppData) obj;
			if (host == null) {
				if (other.host != null)
					return false;
			} else if (!host.equals(other.host))
				return false;
			if (jmxurl == null) {
				if (other.jmxurl != null)
					return false;
			} else if (!jmxurl.equals(other.jmxurl))
				return false;
			if (keepChecking != other.keepChecking)
				return false;
			if (port == null) {
				if (other.port != null)
					return false;
			} else if (!port.equals(other.port))
				return false;
			if (urlScheme == null) {
				if (other.urlScheme != null)
					return false;
			} else if (!urlScheme.equals(other.urlScheme))
				return false;
			return true;
		}

	}

	private static Logger logger = LoggerFactory.getLogger(SpringProcessConnectorRemote.class);

	/**
	 * We keep the remote app instances in a Map indexed by the json daya. This allows us to
	 * return the same instance(s) repeatedly as long as the data does not change.
	 */
	private final Map<RemoteBootAppData, String> remoteAppInstances;
	private final SpringProcessConnectorService processConnectorService;
	private final SpringProcessLiveDataProvider liveDataProvider;

	public SpringProcessConnectorRemote(SimpleLanguageServer server, SpringProcessConnectorService processConnector,
			SpringProcessLiveDataProvider liveDataProvider) {
		this.processConnectorService = processConnector;
		this.liveDataProvider = liveDataProvider;
		this.remoteAppInstances = new HashMap<>();

		server.getWorkspaceService().onDidChangeConfiguraton(this::handleSettings);
	}

	private synchronized void handleSettings(Settings settings) {
		RemoteBootAppData[] appData = settings.getAs(RemoteBootAppData[].class, "boot-java", "remote-apps");
		if (appData == null) {
			//Avoid NPE
			appData = new RemoteBootAppData[0];
		}

		// remove outdated remote apps
		Set<RemoteBootAppData> newAppData = new HashSet<>(Arrays.asList(appData));

		Iterator<Entry<RemoteBootAppData, String>> entries = remoteAppInstances.entrySet().iterator();
		while (entries.hasNext()) {
			Entry<RemoteBootAppData, String> entry = entries.next();
			RemoteBootAppData key = entry.getKey();

			if (!newAppData.contains(key)) {
				logger.info("Removing RemoteSpringBootApp: "+key);
				entries.remove();
				
				// create meaningful process key here
				String processKey = entry.getValue();
				processConnectorService.disconnectProcess(processKey);
			}
		}

		//Add new apps
		for (RemoteBootAppData data : newAppData) {
			remoteAppInstances.computeIfAbsent(data, (_appData) -> {
				logger.info("Creating RemoteStringBootApp: " + _appData);
				
				String processKey = getProcessKey(_appData);
				String processID = null;
				String processName = null;
				String jmxURL = _appData.getJmxurl();
				String host = _appData.getHost();
				String port = _appData.getPort();
				String urlScheme = _appData.getUrlScheme();
				boolean keepChecking = _appData.isKeepChecking();
				
				SpringProcessConnectorOverJMX connector = new SpringProcessConnectorOverJMX(liveDataProvider, processKey, jmxURL, urlScheme, processID, processName, host, port, keepChecking);
				processConnectorService.connectProcess(processKey, connector);
				
				return processKey;
			});
		}
	}
	
	private static String getProcessKey(RemoteBootAppData appData) {
		return "remote";
	}

}
