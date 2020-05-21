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
import org.springframework.ide.vscode.boot.app.BootJavaConfig;
import org.springframework.ide.vscode.commons.languageserver.DiagnosticService;
import org.springframework.ide.vscode.commons.languageserver.ProgressService;
import org.springframework.ide.vscode.commons.languageserver.ProgressTask;
import org.springframework.ide.vscode.commons.languageserver.util.ShowMessageException;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;

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

	private ProgressService progressService;
	private DiagnosticService diagnosticService;
	
	private int progressIdKey = 0;
	private int maxRetryCount;
	private int retryDelayInSeconds;

		
	public SpringProcessConnectorService(SimpleLanguageServer server, SpringProcessLiveDataProvider liveDataProvider) {
		this.liveDataProvider = liveDataProvider;
		this.scheduler = new ScheduledThreadPoolExecutor(10);
		this.connectors = new ConcurrentHashMap<>();
		this.connectedSuccess = new ConcurrentHashMap<>();
		
		this.progressService = server.getProgressService();
		if (this.progressService == null) {
			this.progressService = ProgressService.NO_PROGRESS;
		}
		
		this.diagnosticService = server.getDiagnosticService();
		
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
			final ProgressTask progressTask = getProgressTask(
					"spring-process-connector-service-connect-" + processKey);
			
			scheduleConnect(progressTask, processKey, connector, 0, TimeUnit.SECONDS, 0);
		}
		catch (Exception e) {
			log.error("error connecting to " + processKey, e);
		}
	}
	
	public void refreshProcess(String processKey) {
		log.info("refresh process: " + processKey);
		
		SpringProcessConnector connector = this.connectors.get(processKey);
		if (connector != null) {
			final ProgressTask progressTask = getProgressTask(
					"spring-process-connector-service-refresh-data-" + processKey);
			
			scheduleRefresh(progressTask, processKey, connector, 0, TimeUnit.SECONDS, 0);
		}
	}

	public void disconnectProcess(String processKey) {
		log.info("disconnect from process: " + processKey);

		this.liveDataProvider.remove(processKey);

		SpringProcessConnector connector = this.connectors.remove(processKey);
		this.connectedSuccess.put(processKey, false);
		
		if (connector != null) {
			final ProgressTask progressTask = getProgressTask(
					"spring-process-connector-service-disconnect-" + processKey);
			
			scheduleDisconnect(progressTask, processKey, connector, 0, TimeUnit.SECONDS, 0);
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
		
	
		this.scheduler.schedule(() -> {
			try {
				progressTask.progressEvent(progressMessage);
				connector.connect();
				progressTask.progressDone();
				
				refreshProcess(processKey);
			}
			catch (Exception e) {
				log.info("problem occured during process connect", e);

				if (retryNo < maxRetryCount && isConnected(processKey)) {
					scheduleConnect(progressTask, processKey, connector, retryDelayInSeconds, TimeUnit.SECONDS, retryNo + 1);
				} else {
					progressTask.progressDone();
					
					// Send message to client if maximum retries reached on error
					if (isConnected(processKey)) {
						diagnosticService.diagnosticEvent(ShowMessageException
									.error("Failed to connect to process " + processKey + " after retries: " + retryNo, e));	
					}
				}
			}
		}, delay, unit);
	}

	private void scheduleDisconnect(ProgressTask progressTask, String processKey, SpringProcessConnector connector, long delay, TimeUnit unit, int retryNo) {
		String message = "Disconnect from process: " + processKey + " - retry no: " + retryNo;
		log.info(message);
	
		
		this.scheduler.schedule(() -> {
			try {
				progressTask.progressEvent(message);
				connector.disconnect();
				progressTask.progressDone();
			}
			catch (Exception e) {
				log.info("problem occured during process disconnect", e);

				if (retryNo < maxRetryCount) {
					scheduleDisconnect(progressTask, processKey, connector, retryDelayInSeconds, TimeUnit.SECONDS, retryNo + 1);
				} else {
					progressTask.progressDone();
					
					// Send message to client if maximum retries reached on error
					diagnosticService.diagnosticEvent(ShowMessageException
							.error("Failed to disconnect from process " + processKey + " after retries: " + retryNo, e));
				
				}
			}
		}, delay, unit);
	}

	private void scheduleRefresh(ProgressTask progressTask, String processKey, SpringProcessConnector connector, long delay, TimeUnit unit, int retryNo) {
		String progressMessage = "Refreshing data from Spring process: " + processKey + " - retry no: " + retryNo;
		log.info(progressMessage);
		
		
		this.scheduler.schedule(() -> {
			
			try {
				progressTask.progressEvent(progressMessage);
				SpringProcessLiveData newLiveData = connector.refresh();

				if (newLiveData != null) {
					if (!this.liveDataProvider.add(processKey, newLiveData)) {
						this.liveDataProvider.update(processKey, newLiveData);
					}
					
					this.connectedSuccess.put(processKey, true);
				}
				progressTask.progressDone();
			}
			catch (Exception e) {

				log.info("problem occured during process live data refresh", e);
				
				if (retryNo < maxRetryCount && isConnected(processKey)) {
					scheduleRefresh(progressTask, processKey, connector, retryDelayInSeconds, TimeUnit.SECONDS,
							retryNo + 1);
				}
				else {
					progressTask.progressDone();
					
					// Send message to client if maximum retries reached on error
					if (isConnected(processKey)) {
						diagnosticService.diagnosticEvent(ShowMessageException
								.error("Failed to refresh live data from process " + processKey + " after retries: " + retryNo, e));
	
						disconnectProcess(processKey);
					}
				}
			}
		}, delay, unit);
	}
	
	private ProgressTask getProgressTask(String prefixId) {
		return this.progressService.createProgressTask(prefixId + progressIdKey++);
	}
}
