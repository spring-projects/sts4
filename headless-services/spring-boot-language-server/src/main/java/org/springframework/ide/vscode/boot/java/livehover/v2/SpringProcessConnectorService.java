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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Lippert
 */
public class SpringProcessConnectorService {

	private static final Logger log = LoggerFactory.getLogger(SpringProcessConnectorService.class);

	private static final int RETRY_MAX_NO = 10;
	private static final int RETRY_DELAY_IN_SECONDS = 3;

	private final ScheduledThreadPoolExecutor scheduler;
	private final ConcurrentMap<String, SpringProcessConnector> connectors;
	
	public SpringProcessConnectorService() {
		this.scheduler = new ScheduledThreadPoolExecutor(10);
		this.connectors = new ConcurrentHashMap<>();
	}
	
	public void connectProcess(String processKey, SpringProcessConnector connector) {
		log.info("connect to process: " + processKey);

		this.connectors.put(processKey, connector);
		try {
			scheduleConnect(processKey, connector, 0, TimeUnit.SECONDS, 0);
		}
		catch (Exception e) {
			log.error("error connecting to " + processKey, e);
		}
	}
	
	public void refreshProcess(String processKey) {
		log.info("refresh process: " + processKey);
		
		SpringProcessConnector connector = this.connectors.get(processKey);
		if (connector != null) {
			scheduleRefresh(processKey, connector, 0, TimeUnit.SECONDS, 0);
		}
	}

	public void disconnectProcess(String processKey) {
		log.info("disconnect from process: " + processKey);

		SpringProcessConnector connector = this.connectors.remove(processKey);
		
		if (connector != null) {
			scheduleDisconnect(processKey, connector, 0, TimeUnit.SECONDS, 0);
		}
	}
	
	public SpringProcessConnector[] getConnectedProcesses() {
		return (SpringProcessConnector[]) this.connectors.values().toArray(new SpringProcessConnector[this.connectors.values().size()]);
	}

	/**
	 * common method to generate process keys from process IDs and process names
	 */
	public static String getProcessKey(String processID, String processName) {
		return processID + " - " + processName;
	}
	
	private void scheduleConnect(String processKey, SpringProcessConnector connector, long delay, TimeUnit unit, int retryNo) {
		log.info("schedule task to connect to process: " + processKey + " - retry no: " + retryNo);
		
		this.scheduler.schedule(() -> {
			try {
				connector.connect();
			}
			catch (Exception e) {
				log.info("problem occured during process connect", e);

				if (retryNo < RETRY_MAX_NO) {
					scheduleConnect(processKey, connector, RETRY_DELAY_IN_SECONDS, TimeUnit.SECONDS, retryNo + 1);
				}
			}
			refreshProcess(processKey);
		}, delay, unit);
	}

	private void scheduleDisconnect(String processKey, SpringProcessConnector connector, long delay, TimeUnit unit, int retryNo) {
		log.info("schedule task to disconnect from process: " + processKey + " - retry no: " + retryNo);
		
		this.scheduler.schedule(() -> {
			try {
				connector.disconnect();
			}
			catch (Exception e) {
				log.info("problem occured during process disconnect", e);

				if (retryNo < RETRY_MAX_NO) {
					scheduleDisconnect(processKey, connector, RETRY_DELAY_IN_SECONDS, TimeUnit.SECONDS, retryNo + 1);
				}
			}
		}, delay, unit);
	}

	private void scheduleRefresh(String processKey, SpringProcessConnector connector, long delay, TimeUnit unit, int retryNo) {
		log.info("schedule task to refresh data from process: " + processKey + " - retry no: " + retryNo);
		
		this.scheduler.schedule(() -> {
			try {
				connector.refresh();
			}
			catch (Exception e) {
				log.info("problem occured during process live data refresh", e);
				
				if (retryNo < RETRY_MAX_NO) {
					scheduleRefresh(processKey, connector, 3, TimeUnit.SECONDS, retryNo + 1);
				}
				else {
					disconnectProcess(processKey);
				}
			}
		}, delay, unit);
	}

}
