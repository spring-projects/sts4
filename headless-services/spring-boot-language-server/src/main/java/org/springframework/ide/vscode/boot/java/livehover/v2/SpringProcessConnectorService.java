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

	private static final String METRICS = "metrics";
	public static final String GC_PAUSES = "gcPauses";
	public static final String MEMORY = "memory";
	public static final String HEAP_MEMORY = "heapMemory";
	public static final String NON_HEAP_MEMORY = "nonHeapMemory";

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
			
			progressTask.progressBegin("Connect", null);
			
			scheduleConnect(progressTask, processKey, connector, 0, TimeUnit.SECONDS, 0);
		}
		catch (Exception e) {
			log.error("error connecting to " + processKey, e);
		}
	}
	
	public void refreshProcess(SpringProcessParams springProcessParams) {
		log.info("refresh process: " + springProcessParams.getProcessKey());
		
		SpringProcessConnector connector = this.connectors.get(springProcessParams.getProcessKey());
		if (connector != null) {
			final ProgressTask progressTask = getProgressTask(
					"spring-process-connector-service-refresh-data-" + springProcessParams.getProcessKey());

			progressTask.progressBegin("Refresh", null);

			scheduleRefresh(progressTask, springProcessParams, connector, 0, TimeUnit.SECONDS, 0);
		}
	}

	public SpringProcessLiveData getLiveData(String processKey) {
		return this.liveDataProvider.getCurrent(processKey);
	}
	
	public SpringProcessMemoryMetricsLiveData getMemoryMetricsLiveData(String processKey) {
		return this.liveDataProvider.getMemoryMetrics(processKey);
	}
	
	public SpringProcessGcPausesMetricsLiveData getGcPausesMetricsLiveData(String processKey) {
		return this.liveDataProvider.getGcPausesMetrics(processKey);
	}

	public void disconnectProcess(String processKey) {
		log.info("disconnect from process: " + processKey);

		this.liveDataProvider.remove(processKey);

		SpringProcessConnector connector = this.connectors.remove(processKey);
		this.connectedSuccess.put(processKey, false);
		
		if (connector != null) {
			final ProgressTask progressTask = getProgressTask(
					"spring-process-connector-service-disconnect-" + processKey);
			
			progressTask.progressBegin("Disconnect", null);

			scheduleDisconnect(progressTask, processKey, connector, 0, TimeUnit.SECONDS, 0);
		}
	}
	
	public SpringProcessConnector[] getConnectedProcesses() {
		return (SpringProcessConnector[]) this.connectors.values().stream()
				.filter((connector) -> connectedSuccess.get(connector.getProcessKey())).toArray(SpringProcessConnector[]::new);
	}
	
	public boolean isKnownProcessKey(String processKey) {
		return this.connectors.containsKey(processKey);
	}

	/**
	 * common method to generate process keys from process IDs and process names
	 */
	public static String getProcessKey(String processID, String processName) {
		return processID;
	}
	
	private void scheduleConnect(ProgressTask progressTask, String processKey, SpringProcessConnector connector, long delay, TimeUnit unit, int retryNo) {
		String progressMessage = "Connecting to process: " + processKey + " - retry no: " + retryNo;
		log.info(progressMessage);
		
	
		this.scheduler.schedule(() -> {
			try {
				progressTask.progressEvent(progressMessage);
				connector.connect();
				progressTask.progressDone();
				
				refreshProcess(new SpringProcessParams(processKey, "", "", ""));
				refreshProcess(new SpringProcessParams(processKey, METRICS, MEMORY, ""));
				refreshProcess(new SpringProcessParams(processKey, METRICS, GC_PAUSES, ""));
			}
			catch (Exception e) {
				log.info("problem occured during process connect", e);

				if (retryNo < maxRetryCount && isKnownProcessKey(processKey)) {
					scheduleConnect(progressTask, processKey, connector, retryDelayInSeconds, TimeUnit.SECONDS, retryNo + 1);
				} else {
					progressTask.progressDone();
					
					// Send message to client if maximum retries reached on error
					if (isKnownProcessKey(processKey)) {
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

	private void scheduleRefresh(ProgressTask progressTask, SpringProcessParams springProcessParams, SpringProcessConnector connector, long delay, TimeUnit unit, int retryNo) {
		String processKey = springProcessParams.getProcessKey();
		String endpoint = springProcessParams.getEndpoint();
		String metricName = springProcessParams.getMetricName();
	    String progressMessage = "Refreshing data from Spring process: " + processKey + " - retry no: " + retryNo;
		log.info(progressMessage);
		
		
		this.scheduler.schedule(() -> {
			
			try {
				progressTask.progressEvent(progressMessage);
				if(METRICS.equals(endpoint) && (MEMORY.equals(metricName))) {
					SpringProcessMemoryMetricsLiveData newMetricsLiveData = connector.refreshMemoryMetrics(this.liveDataProvider.getCurrent(processKey), metricName, springProcessParams.getTags());
					
					if (newMetricsLiveData != null) {
						if (!this.liveDataProvider.addMemoryMetrics(processKey, newMetricsLiveData)) {
							this.liveDataProvider.updateMemoryMetrics(processKey, newMetricsLiveData);
						}
						
						this.connectedSuccess.put(processKey, true);
					}
					
				} else if(METRICS.equals(endpoint) && GC_PAUSES.equals(metricName)) {
					SpringProcessGcPausesMetricsLiveData newMetricsLiveData = connector.refreshGcPausesMetrics(this.liveDataProvider.getCurrent(processKey), metricName, springProcessParams.getTags());
					
					if (newMetricsLiveData != null) {
						if (!this.liveDataProvider.addGcPausesMetrics(processKey, newMetricsLiveData)) {
							this.liveDataProvider.updateGcPausesMetrics(processKey, newMetricsLiveData);
						}
						
						this.connectedSuccess.put(processKey, true);
					}
					
				} else {
					SpringProcessLiveData newLiveData = connector.refresh(this.liveDataProvider.getCurrent(processKey));
	
					if (newLiveData != null) {
						if (!this.liveDataProvider.add(processKey, newLiveData)) {
							this.liveDataProvider.update(processKey, newLiveData);
						}
						
						this.connectedSuccess.put(processKey, true);
					}
				}
				progressTask.progressDone();
			}
			catch (Exception e) {

				log.info("problem occured during process live data refresh", e);
				
				if (retryNo < maxRetryCount && isKnownProcessKey(processKey)) {
					scheduleRefresh(progressTask, springProcessParams, connector, retryDelayInSeconds, TimeUnit.SECONDS,
							retryNo + 1);
				}
				else {
					progressTask.progressDone();
					
					// Send message to client if maximum retries reached on error
					if (isKnownProcessKey(processKey)) {
						diagnosticService.diagnosticEvent(ShowMessageException
								.error("Failed to refresh live data from process " + processKey + " after retries: " + retryNo, e));
	
						if (!connectedSuccess.containsKey(connector.getProcessKey())) {
							disconnectProcess(processKey);
						}
					}
				}
			}
		}, delay, unit);
	}
	
	private ProgressTask getProgressTask(String prefixId) {
		return this.progressService.createProgressTask(prefixId + progressIdKey++);
	}
}
