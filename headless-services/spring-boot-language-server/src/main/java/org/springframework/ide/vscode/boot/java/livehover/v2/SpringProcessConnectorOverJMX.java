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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.remote.JMXConnectionNotification;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Lippert
 */
public class SpringProcessConnectorOverJMX implements SpringProcessConnector {

	private static final Logger log = LoggerFactory.getLogger(SpringProcessConnectorOverJMX.class);

	private static final String JMX_CLIENT_CONNECTION_CHECK_PERIOD_PROPERTY_KEY = "jmx.remote.x.client.connection.check.period";
	private static final long JMX_HEARTBEAT_INTERVAL = 1000;

	private final String processKey;
	private final String jmxURL;
	private final String urlScheme;
	private final String port;
	private final String projectName;
	
	// not final, might be updated with data from JMX process, if not initially set
	private String processID;
	private String processName;
	private String host;
	
	private final List<SpringProcessConnectionChangeListener> listeners;
	
	private JMXConnector jmxConnection;
	private JMXServiceURL jmxServiceURL;

	private final NotificationListener notificationListener;

	public SpringProcessConnectorOverJMX(String processKey, String jmxURL,
			String urlScheme, String processID, String processName, String projectName, String host, String port) {

		this.processKey = processKey;

		this.jmxURL = jmxURL;
		this.urlScheme = urlScheme;
		this.processID = processID;
		this.processName = processName;
		this.projectName = projectName;
		this.host = host;
		this.port = port;

		this.jmxConnection = null;
		this.jmxServiceURL = null;
		
		this.listeners = new CopyOnWriteArrayList<>();
		
		this.notificationListener = new NotificationListener() {
			@Override
			public void handleNotification(Notification notification, Object handback) {
				String notificationType = notification.getType();
				
				if (JMXConnectionNotification.CLOSED.equals(notificationType)) {
					try {
						jmxConnection.removeConnectionNotificationListener(notificationListener);
						jmxConnection = null;
					}
					catch (Exception e) {
						log.error("exception while reacting to connection close of: " + jmxURL, e);
					}
					announceConnectionClosed();
				}
			}
		};
	}
	
	@Override
	public String getProcessKey() {
		return processKey;
	}
	
	@Override
	public void connect() throws Exception {
		jmxServiceURL = new JMXServiceURL(jmxURL);
		
		Map<String, Object> environment = new HashMap<>();
		environment.put(JMX_CLIENT_CONNECTION_CHECK_PERIOD_PROPERTY_KEY, new Long(JMX_HEARTBEAT_INTERVAL));
		jmxConnection = JMXConnectorFactory.connect(jmxServiceURL, environment);
		
		jmxConnection.addConnectionNotificationListener(notificationListener, null, null);
	}

	@Override
	public SpringProcessLiveData refresh() throws Exception {
		log.info("try to open JMX connection to: " + jmxURL);
		
		if (jmxConnection != null) {
			try {
				SpringProcessLiveDataExtractorOverJMX springJMXConnector = new SpringProcessLiveDataExtractorOverJMX();
	
				if (this.host == null) {
					this.host = jmxServiceURL.getHost();
				}
				
				log.info("retrieve live data from: " + jmxURL);
				SpringProcessLiveData liveData = springJMXConnector.retrieveLiveData(jmxConnection, processID, processName, urlScheme, host, null, port);
				
				if (this.processID == null) {
					this.processID = liveData.getProcessID();
				}
				
				if (this.processName == null) {
					this.processName = liveData.getProcessName();
				}
				
				if (liveData != null && liveData.getBeans() != null && !liveData.getBeans().isEmpty()) {
					return liveData;
				}
			}
			catch (Exception e) {
				log.error("exception while connecting to jmx: " + jmxURL, e);
			}
		}
		
		throw new Exception("no live data received, lets try again");
	}

	@Override
	public void disconnect() throws Exception {
		try {
			if (jmxConnection != null) {

				log.info("close JMX connection to: " + jmxURL);
				jmxConnection.removeConnectionNotificationListener(notificationListener);
				jmxConnection.close();
				jmxConnection = null;
			}
		}
		catch (Exception e) {
			log.error("error closing the JMX connection for: " + jmxURL, e);
		}
	}

	@Override
	public void addConnectorChangeListener(SpringProcessConnectionChangeListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void removeConnectorChangeListener(SpringProcessConnectionChangeListener listener) {
		this.listeners.remove(listener);
	}
	
	private void announceConnectionClosed() {
		for (SpringProcessConnectionChangeListener listener : this.listeners) {
			listener.connectionClosed(processKey);
		}
	}

	@Override
	public String getProjectName() {
		return projectName;
	}

	@Override
	public String getProcessId() {
		return processID;
	}

	@Override
	public String getProcessName() {
		return processName;
	}

	@Override
	public String toString() {
		return "SpringProcessConnectorOverJMX [jmxURL=" + jmxURL + ", processID=" + processID + ", processName="
				+ processName + ", listeners=" + listeners + ", jmxConnection=" + jmxConnection + ", jmxServiceURL="
				+ jmxServiceURL + ", notificationListener=" + notificationListener + "]";
	}
	
}
