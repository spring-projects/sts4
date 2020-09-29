/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.model;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFAppState;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFApplicationDetail;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFInstanceState;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFInstanceStats;
import org.springframework.ide.eclipse.boot.dash.cf.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.DefaultClientRequestsV2;
import org.springframework.ide.eclipse.boot.dash.console.LogType;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens.CancelationToken;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

public class ApplicationRunningStateTracker {
	// Give time for Diego-enabled apps with health check that may take a while to start
	// Users can always manually stop the element if it is taking too long to check the run state of the element
	public static final long APP_START_TIMEOUT = DefaultClientRequestsV2.APP_START_TIMEOUT.toMillis();

	public static final long WAIT_TIME = 1000;

	private final ClientRequests requests;

	private final String appName;

	private final CloudFoundryBootDashModel model;

	private final long timeout;

	private final CancelationToken cancelationToken;

	private final CloudAppDashElement element;


	public ApplicationRunningStateTracker(CancelationToken cancelationToken, CloudAppDashElement app) {
		this.model = app.getBootDashModel();
		this.requests = model.getClient();
		this.appName = app.getName();
		this.timeout = APP_START_TIMEOUT;
		this.cancelationToken = cancelationToken;
		this.element = app;
	}

	protected void checkTerminate(IProgressMonitor monitor)
			throws OperationCanceledException {
		this.element.checkTerminationRequested(cancelationToken, monitor);
	}

	/**
	 * Polls cloudfoundry until element has succeeded or failed to start. Sending updates to console
	 * and return the final run state.
	 */
	public RunState startTracking(IProgressMonitor monitor) throws Exception, OperationCanceledException {

		// fetch an updated Cloud Application that reflects changes that
		// were
		// performed on it. Make sure the element element reference is updated
		// as
		// run state of the element depends on the element being up to date.
		// Wait for application to be started
		RunState runState = RunState.UNKNOWN;

		long currentTime = System.currentTimeMillis();
		long roughEstimateFetchStatsms = 5000;

		long totalTime = currentTime + timeout;
		String checkingMessage = "Checking if the application is running";

		int estimatedAttempts = (int) (timeout / (WAIT_TIME + roughEstimateFetchStatsms));

		monitor.beginTask(checkingMessage, estimatedAttempts);

		model.getElementConsoleManager().writeToConsole(element, checkingMessage + ". Please wait...",
				LogType.STDOUT);

		CFApplicationDetail app = requests.getApplication(appName);

		if (app == null) {
			throw new OperationCanceledException();
		}

		// Get the guid, as it is more efficient for lookup
		//UUID appGuid = element.getGuid();

		while (runState != RunState.RUNNING && runState != RunState.FLAPPING && runState != RunState.CRASHED
				&& currentTime < totalTime) {
			int timeLeft = (int) ((totalTime - currentTime) / 1000);

			// Don't log this. Only update the monitor
			monitor.setTaskName(checkingMessage + ". Time left before timeout: " + timeLeft + 's');

			checkTerminate(monitor);

			monitor.worked(1);

			runState = getRunState(app.getInstanceDetails());
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {

			}

			app = requests.getApplication(app.getName());
			// App no longer exists
			if (app == null) {
				throw new OperationCanceledException();
			}

			currentTime = System.currentTimeMillis();
		}

		if (runState != RunState.RUNNING) {
			String warning = "Timed out waiting for application - " + appName
					+ " to start. Please wait and manually refresh the target, or check if the application logs show any errors.";
			model.getElementConsoleManager().writeToConsole(this.element, warning, LogType.STDERROR);
			throw ExceptionUtil.coreException(warning);

		} else {
			model.getElementConsoleManager().writeToConsole(this.element, "Application appears to have started - " + appName,
					LogType.STDOUT);
		}
		return runState;
	}

	public static RunState getRunState(CFInstanceState instanceState) {
		RunState runState = null;
		if (instanceState != null) {
			switch (instanceState) {
			case RUNNING:
				runState = RunState.RUNNING;
				break;
			case CRASHED:
				runState = RunState.CRASHED;
				break;
			case FLAPPING:
				runState = RunState.FLAPPING;
				break;
			case STARTING:
				runState = RunState.STARTING;
				break;
			case DOWN:
				runState = RunState.INACTIVE;
				break;
			default:
				runState = RunState.UNKNOWN;
				break;
			}
		}

		return runState;
	}

	public static RunState getRunState(CFApplication app, List<CFInstanceStats> instances) {

		RunState runState = RunState.UNKNOWN;
		// if element desired state is "Stopped", return inactive
		if ((instances == null || instances.isEmpty())
				&& app.getState() == CFAppState.STOPPED) {
			runState = RunState.INACTIVE;
		} else {
			runState = getRunState(instances);
		}
		return runState;
	}

	private static RunState getRunState(List<CFInstanceStats> stats) {
		RunState runState = RunState.UNKNOWN;
		if (stats!=null && !stats.isEmpty()) {
			for (CFInstanceStats stat : stats) {
				RunState instanceState = getRunState(stat.getState());
				runState = instanceState != null ? runState.merge(instanceState) : null;
			}
		}
		return runState;
	}
}
