/*******************************************************************************
 * Copyright (c) 2017, 2019 Pivotal, Inc.
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
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Lippert
 */
public class SpringProcessTracker {

	private static final Duration DEFAULT_INTERVAL = Duration.ofMillis(5000);
	private static final Logger log = LoggerFactory.getLogger(SpringProcessTracker.class);

	private final long POLLING_INTERVAL_MILLISECONDS;
	private final SpringProcessConnectorLocal localProcessConnector;

	private boolean automaticTrackingEnabled;
	private ScheduledThreadPoolExecutor timer;

	public SpringProcessTracker(SpringProcessConnectorLocal localProcessConnector, Duration pollingInterval) {
		this.localProcessConnector = localProcessConnector;
		this.POLLING_INTERVAL_MILLISECONDS = pollingInterval == null ? DEFAULT_INTERVAL.toMillis() : pollingInterval.toMillis();
		this.automaticTrackingEnabled = true;
	}

	public synchronized void setTrackingEnabled(boolean trackingEnabled) {
		if (automaticTrackingEnabled != trackingEnabled) {
			automaticTrackingEnabled = trackingEnabled;
			refresh();
		}
	}

	public synchronized void start() {
		if (automaticTrackingEnabled && timer == null) {
			log.info("Starting SpringProcessTracker");
			this.timer = new ScheduledThreadPoolExecutor(1);
			this.timer.scheduleWithFixedDelay(() -> this.update(), 0, POLLING_INTERVAL_MILLISECONDS, TimeUnit.MILLISECONDS);
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
		log.info("scan for local processes cycle...");
		try {
			this.localProcessConnector.searchForNewProcesses();
		}
		catch (Throwable e) {
			log.error("error searching for local processes", e);
		}
	}

	private void refresh() {
		if (automaticTrackingEnabled) {
			start();
		} else {
			stop();
		}
	}

}
