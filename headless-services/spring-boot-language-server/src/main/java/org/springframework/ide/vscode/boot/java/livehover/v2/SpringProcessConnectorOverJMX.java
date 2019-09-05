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

	private final SpringProcessLiveDataProvider liveDataProvider;
	private final String processKey;
	private final String jmxURL;
	private final String urlScheme;
	private final String processID;
	private final String processName;
	private final String host;
	private final String port;
	
	public SpringProcessConnectorOverJMX(SpringProcessLiveDataProvider liveDataProvider, String processKey, String jmxURL,
			String urlScheme, String processID, String processName, String host, String port) {

		this.liveDataProvider = liveDataProvider;
		this.processKey = processKey;
		this.jmxURL = jmxURL;
		this.urlScheme = urlScheme;
		this.processID = processID;
		this.processName = processName;
		this.host = host;
		this.port = port;
	}
	
	@Override
	public String getProcessKey() {
		return processKey;
	}
	
	@Override
	public String getLabel() {
		return processID + " (" + processName + ") ";
	}
	
	@Override
	public void connect() throws Exception {
	}

	@Override
	public void refresh() throws Exception {
		log.info("try to open JMX connection to: " + jmxURL);
		JMXConnector jmxConnector = null;
		try {
			SpringProcessLiveDataExtractorOverJMX springJMXConnector = new SpringProcessLiveDataExtractorOverJMX();

			JMXServiceURL jmxServiceURL = new JMXServiceURL(jmxURL);
			jmxConnector = JMXConnectorFactory.connect(jmxServiceURL, null);

			String hostName = host != null ? host : jmxServiceURL.getHost(); 
			
			log.info("retrieve live data from: " + jmxURL);
			SpringProcessLiveData liveData = springJMXConnector.retrieveLiveData(jmxConnector, processID, processName, urlScheme, hostName, null, port);
			
			if (liveData != null && liveData.getBeans() != null && !liveData.getBeans().isEmpty()) {
				this.liveDataProvider.add(processKey, liveData);
				return;
			}
		}
		catch (Exception e) {
			log.error("exception while connecting to jmx: " + jmxURL, e);
		}
		finally {
			if (jmxConnector != null) {
				try {
					log.info("close JMX connection to: " + jmxURL);
					jmxConnector.close();
				}
				catch (Exception e) {
					log.error("error closing the JMX connection for: " + jmxURL, e);
				}
			}
		}
		
		throw new Exception("no live data received, lets try again");
	}

	@Override
	public void disconnect() throws Exception {
		this.liveDataProvider.remove(processKey);
	}

}
