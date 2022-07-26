/*******************************************************************************
 * Copyright (c) 2019, 2022 Pivotal, Inc.
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
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.util.StringUtils;

/**
 * @author Martin Lippert
 */
public class SpringProcessConnectorRemote {

	public static class RemoteBootAppData {

		private String jmxurl;
		private String host;
		private String urlScheme = "https";
		private String port = "443";
		private boolean manualConnect = false;
		private boolean keepChecking = true;
			//keepChecking defaults to true. Boot dash automatic remote apps should override this explicitly.
			//Reason. All other 'sources' of remote apps are 'manual' and we want them to default to
			//'keepChecking' even if the user doesn't set this to true manually.
		
		private String processId;
		private String processName;

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

		public String getProcessID() {
			return processId;
		}

		public void setProcessId(String processId) {
			this.processId = processId;
		}
		
		public String getProcessName() {
			return processName;
		}
		
		public void setProcessName(String processName) {
			this.processName = processName;
		}
		
		@Override
		public String toString() {
			return "RemoteBootAppData [jmxurl=" + jmxurl + ", host=" + host + ", urlScheme=" + urlScheme + ", port="
					+ port + ", manualConnect=" + manualConnect + ", keepChecking=" + keepChecking + ", processId="
					+ processId + ", processName=" + processName + "]";
		}

		public void setManualConnection(boolean manualConnect) {
			this.manualConnect = manualConnect;
		}
		
		public boolean isManualConnect() {
			return manualConnect;
		}

		@Override
		public int hashCode() {
			return Objects.hash(host, jmxurl, keepChecking, manualConnect, port, processId, processName, urlScheme);
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
			return Objects.equals(host, other.host) && Objects.equals(jmxurl, other.jmxurl)
					&& keepChecking == other.keepChecking && manualConnect == other.manualConnect
					&& Objects.equals(port, other.port) && Objects.equals(processId, other.processId)
					&& Objects.equals(processName, other.processName) && Objects.equals(urlScheme, other.urlScheme);
		}
		
	}

	private static Logger logger = LoggerFactory.getLogger(SpringProcessConnectorRemote.class);

	/**
	 * We keep the remote app instances in a Map indexed by the json data. This allows us to
	 * return the same instance(s) repeatedly as long as the data does not change.
	 */
	private final Map<RemoteBootAppData, String> remoteAppInstances;
	
	private final SpringProcessConnectorService processConnectorService;

	public SpringProcessConnectorRemote(SimpleLanguageServer server, SpringProcessConnectorService processConnector) {
		this.processConnectorService = processConnector;
		this.remoteAppInstances = new HashMap<>();
	}
	
	/**
	 * Replaces existing remote app data map with the new remote app data. Empty array of appData will result in no remote apps. 
	 * @param appData new remote app data
	 */
	final public synchronized void updateApps(RemoteBootAppData[] appData) {
		logger.info("updating settings for remote processses to track - start");
		
		
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
				if (!_appData.isManualConnect()) {
					connectProcess(_appData);
				}
				return processKey;
			});
		}
		
		logger.info("updating settings for remote processses to track - done");
	}
	
	public static String getProcessName(RemoteBootAppData appData) {
		if (StringUtils.hasText(appData.getProcessName())) {
			return appData.getProcessName();
		}
		else if (StringUtils.hasText(appData.getHost())) {
			return "remote process - "+ appData.getHost();
		} else {
			return "remote process - " + appData.getJmxurl();
		}
	}

	public static String getProcessKey(RemoteBootAppData appData) {
		return "remote process - " + appData.getJmxurl();
	}

	public void connectProcess(RemoteBootAppData remoteProcess) {
		String processKey = getProcessKey(remoteProcess);
		String processID = remoteProcess.getProcessID();
		String processName = getProcessName(remoteProcess);
		String jmxURL = remoteProcess.getJmxurl();
		String host = remoteProcess.getHost();
		String port = remoteProcess.getPort();
		String urlScheme = remoteProcess.getUrlScheme();
//		boolean keepChecking = _appData.isKeepChecking();
		
		if (jmxURL.startsWith("http")) {
			SpringProcessConnectorOverHttp connector = new SpringProcessConnectorOverHttp(ProcessType.REMOTE, processKey, jmxURL, urlScheme, processID, processName, urlScheme, host, port);
			processConnectorService.connectProcess(processKey, connector);
		} else {
			SpringProcessConnectorOverJMX connector = new SpringProcessConnectorOverJMX(ProcessType.REMOTE, processKey, jmxURL, urlScheme, processID, processName, null, host, port);
			processConnectorService.connectProcess(processKey, connector);
		}
	}
	
	public RemoteBootAppData[] getProcesses() {
		Set<RemoteBootAppData> remoteApps = this.remoteAppInstances.keySet();
		return (RemoteBootAppData[]) remoteApps.toArray(new RemoteBootAppData[remoteApps.size()]);
	}

}
