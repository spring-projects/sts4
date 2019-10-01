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

import java.time.Duration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.app.BootJavaConfig;

/**
 * @author Martin Lippert
 */
public class SpringProcessTracker {

	private static final long DELAY_MINIMUM = 1000;
	
	private static final Logger log = LoggerFactory.getLogger(SpringProcessTracker.class);

	private final SpringProcessConnectorLocal localProcessConnector;
	private final boolean isConnectorAvailable;

	private boolean automaticTrackingEnabled;
	private Duration POLLING_INTERVAL;
	private ScheduledThreadPoolExecutor timer;
	
	private Set<String> processesAlreadySeen;

	public SpringProcessTracker(SpringProcessConnectorLocal localProcessConnector, Duration pollingInterval) {
		this.localProcessConnector = localProcessConnector;
		this.POLLING_INTERVAL = pollingInterval != null ? pollingInterval : Duration.ofMillis(BootJavaConfig.LIVE_INFORMATION_AUTOMATIC_TRACKING_DELAY_DEFAULT);
		this.automaticTrackingEnabled = false;
		this.processesAlreadySeen = new HashSet<>();
		
		this.isConnectorAvailable = SpringProcessConnectorLocal.isAvailable();
	}

	public synchronized void setTrackingEnabled(boolean trackingEnabled) {
		if (automaticTrackingEnabled != trackingEnabled) {
			automaticTrackingEnabled = trackingEnabled;

			if (automaticTrackingEnabled) {
				start();
			} else {
				stop();
			}
		}
	}

	public void setDelay(long delay) {
		Duration newDelay = Duration.ofMillis(Math.max(DELAY_MINIMUM, delay));
		
		if (!newDelay.equals(POLLING_INTERVAL)) {
			this.POLLING_INTERVAL = newDelay;

			if (automaticTrackingEnabled) {
				stop();
				start();
			}
		}
	}

	public synchronized void start() {
		if (!isConnectorAvailable) {
			log.error("virtual machine connector library not available, no automatic local process tracking possible");
			return;
		}
		
		if (automaticTrackingEnabled && timer == null) {
			log.info("Starting SpringProcessTracker");
			this.timer = new ScheduledThreadPoolExecutor(1);
			this.timer.scheduleWithFixedDelay(() -> this.update(), 0, POLLING_INTERVAL.toMillis(), TimeUnit.MILLISECONDS);
		}
	}

	public synchronized void stop() {
		if (timer != null) {
			log.info("Shutting down SpringProcessTracker");
			timer.shutdown();
			timer = null;
		}
	}

	private void update() {
		try {
			SpringProcessDescriptor[] autoConnectProcesses = this.localProcessConnector.getProcesses(true, SpringProcessStatus.AUTO_CONNECT);
			
			Set<String> autoConnectProcessKeys = new HashSet<>();
			
			for (SpringProcessDescriptor process : autoConnectProcesses) {
				autoConnectProcessKeys.add(process.getProcessKey());
				
				if (!processesAlreadySeen.contains(process.getProcessKey())) {
					processesAlreadySeen.add(process.getProcessKey());

					log.info("auto-connect to process: " + process.getProcessKey());
					this.localProcessConnector.connectProcess(process);
				}
			}
			
			// cleanup list of already seen processes
			Iterator<String> iter = this.processesAlreadySeen.iterator();
			while (iter.hasNext()) {
				String processKey = iter.next();
				if (!autoConnectProcessKeys.contains(processKey)) {
					iter.remove();
				}
			}
		}
		catch (Throwable e) {
			log.error("error searching for local processes", e);
		}
	}

}
