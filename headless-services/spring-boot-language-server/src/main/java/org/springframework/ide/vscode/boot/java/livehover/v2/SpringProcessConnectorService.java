/*******************************************************************************
 * Copyright (c) 2019, 2020 Pivotal, Inc.
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
import org.springframework.ide.vscode.commons.languageserver.ProgressTask;
import org.springframework.ide.vscode.boot.app.BootJavaConfig;
import org.springframework.ide.vscode.commons.languageserver.ProgressService;

/**
 * @author Martin Lippert
 */
public class SpringProcessConnectorService {

	private static final Logger log = LoggerFactory.getLogger(SpringProcessConnectorService.class);

	private final SpringProcessLiveDataProvider liveDataProvider;

	private final ScheduledThreadPoolExecutor scheduler;
	private final ConcurrentMap<String, SpringProcessConnector> connectors;
	private final ConcurrentMap<String, Boolean> connectedSuccess;

	private final SpringProcessConnectionChangeListener connectorListener;

	private final ProgressService progressService;
	
	private int progressIdKey = 0;
	private int maxRetryCount;
	private int retryDelayInSeconds;
	
	public SpringProcessConnectorService(ProgressService progressService, SpringProcessLiveDataProvider liveDataProvider) {
		this.liveDataProvider = liveDataProvider;
		this.scheduler = new ScheduledThreadPoolExecutor(10);
		this.connectors = new ConcurrentHashMap<>();
		this.connectedSuccess = new ConcurrentHashMap<>();
		this.progressService = progressService == null ? ProgressService.NO_PROGRESS : progressService;
		
		this.maxRetryCount = BootJavaConfig.LIVE_INFORMATION_FETCH_DATA_RETRY_MAX_NO_DEFAULT;
		this.retryDelayInSeconds = BootJavaConfig.LIVE_INFORMATION_FETCH_DATA_RETRY_DELAY_IN_SECONDS_DEFAULT;
		
		this.connectorListener = new SpringProcessConnectionChangeListener() {
			@Override
			public void connectionClosed(String processKey) {
				disconnectProcess(processKey);
			}
		};
	}
	
	public void setMaxRetryCount(int maxRetryCount) {
		this.maxRetryCount = maxRetryCount;
	}
	
	public void setRetryDelayInSeconds(int retryDelayInSeconds) {
		this.retryDelayInSeconds = retryDelayInSeconds;
	}
	
	public void connectProcess(String processKey, SpringProcessConnector connector) {
		log.info("connect to process: " + processKey);

		this.connectors.put(processKey, connector);
		this.connectedSuccess.put(processKey, false);
		
		connector.addConnectorChangeListener(connectorListener);

		try {
			scheduleConnect(/* start with no progress handler */null, processKey, connector, 0, TimeUnit.SECONDS, 0);
		}
		catch (Exception e) {
			log.error("error connecting to " + processKey, e);
		}
	}
	
	public void refreshProcess(String processKey) {
		log.info("refresh process: " + processKey);
		
		SpringProcessConnector connector = this.connectors.get(processKey);
		if (connector != null) {
			scheduleRefresh(/* start with no progress handler */null, processKey, connector, 0, TimeUnit.SECONDS, 0);
		}
	}

	public void disconnectProcess(String processKey) {
		log.info("disconnect from process: " + processKey);

		this.liveDataProvider.remove(processKey);

		SpringProcessConnector connector = this.connectors.remove(processKey);
		this.connectedSuccess.put(processKey, false);
		
		if (connector != null) {
			scheduleDisconnect(null, processKey, connector, 0, TimeUnit.SECONDS, 0);
		}
	}
	
	public SpringProcessConnector[] getConnectedProcesses() {
		return (SpringProcessConnector[]) this.connectors.values().stream()
				.filter((connector) -> connectedSuccess.get(connector.getProcessKey())).toArray(SpringProcessConnector[]::new);
	}
	
	public boolean isConnected(String processKey) {
		return this.connectors.containsKey(processKey);
	}

	/**
	 * common method to generate process keys from process IDs and process names
	 */
	public static String getProcessKey(String processID, String processName) {
		return processID + " - " + processName;
	}
	
	private void scheduleConnect(ProgressTask progressTask, String processKey, SpringProcessConnector connector, long delay, TimeUnit unit, int retryNo) {
		String progressMessage = "Connecting to process: " + processKey + " - retry no: " + retryNo;
		log.info(progressMessage);
		
		// Use the same progress task for all retries so that progress messages appear in the same progress task
		final ProgressTask connectProgressTask = getProgressTask(progressTask,
				"spring-process-connector-service-connect-" + processKey);
	
		this.scheduler.schedule(() -> {
			try {
				connectProgressTask.progressEvent(progressMessage);
				connector.connect();
				connectProgressTask.progressDone();
				
				refreshProcess(processKey);
			}
			catch (Exception e) {
				log.info("problem occured during process connect", e);

				if (retryNo < maxRetryCount) {
					scheduleConnect(connectProgressTask, processKey, connector, retryDelayInSeconds, TimeUnit.SECONDS, retryNo + 1);
				} else {
					connectProgressTask.progressDone();
				}
			}
		}, delay, unit);
	}

	private void scheduleDisconnect(ProgressTask progressTask, String processKey, SpringProcessConnector connector, long delay, TimeUnit unit, int retryNo) {
		String message = "Disconnect from process: " + processKey + " - retry no: " + retryNo;
		log.info(message);
		
		// Use the same progress task for all retries so that progress messages appear in the same progress task
		final ProgressTask disconnectProgressTask = getProgressTask(progressTask,
				"spring-process-connector-service-disconnect-" + processKey);
		
		this.scheduler.schedule(() -> {
			try {
				disconnectProgressTask.progressEvent(message);
				connector.disconnect();
				disconnectProgressTask.progressDone();
			}
			catch (Exception e) {
				log.info("problem occured during process disconnect", e);

				if (retryNo < maxRetryCount) {
					scheduleDisconnect(disconnectProgressTask, processKey, connector, retryDelayInSeconds, TimeUnit.SECONDS, retryNo + 1);
				} else {
					disconnectProgressTask.progressDone();
				}
			}
		}, delay, unit);
	}

	private void scheduleRefresh(ProgressTask progressTask, String processKey, SpringProcessConnector connector, long delay, TimeUnit unit, int retryNo) {
		String progressMessage = "Refreshing data from Spring process: " + processKey + " - retry no: " + retryNo;
		log.info(progressMessage);
		
		// Use the same progress task for all retries so that progress messages appear in the same progress task
		final ProgressTask refreshProgressTask = getProgressTask(progressTask,
				"spring-process-connector-service-refresh-data-" + processKey);
		
		this.scheduler.schedule(() -> {
			
			try {
				refreshProgressTask.progressEvent(progressMessage);
				SpringProcessLiveData newLiveData = connector.refresh();

				if (newLiveData != null) {
					if (!this.liveDataProvider.add(processKey, newLiveData)) {
						this.liveDataProvider.update(processKey, newLiveData);
					}
					
					this.connectedSuccess.put(processKey, true);
				}
				refreshProgressTask.progressDone();
			}
			catch (Exception e) {

				log.info("problem occured during process live data refresh", e);
				
				if (retryNo < maxRetryCount) {
					scheduleRefresh(refreshProgressTask, processKey, connector, retryDelayInSeconds, TimeUnit.SECONDS,
							retryNo + 1);
				}
				else {
					refreshProgressTask.progressDone();

					disconnectProcess(processKey);
				}
			}
		}, delay, unit);
	}
	
	private ProgressTask getProgressTask(ProgressTask progressTask, String prefixId) {
		ProgressTask task = progressTask == null
				? this.progressService.createProgressTask(prefixId + progressIdKey++)
				: progressTask;
		return task;
	}
}
