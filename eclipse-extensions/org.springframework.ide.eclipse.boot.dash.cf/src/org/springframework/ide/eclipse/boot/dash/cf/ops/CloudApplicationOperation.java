/*******************************************************************************
 * Copyright (c) 2015, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.ops;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.console.LogType;
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens.CancelationToken;

/**
 * A cloud operation that is performed on a Cloud application (for example,
 * creating, starting, or stopping an application)
 */
public abstract class CloudApplicationOperation extends CloudOperation {

	protected String appName;
	private ISchedulingRule schedulingRule;
	private final CancelationToken cancelationToken;

	public CloudApplicationOperation(String opName, CloudFoundryBootDashModel model, String appName, CancelationToken cancelationToken) {
		super(opName, model);
		this.cancelationToken = cancelationToken;
		this.appName = appName;
		setSchedulingRule(new StartApplicationSchedulingRule(model.getRunTarget(), appName));
	}

	protected CloudAppDashElement getDashElement() {
		return model.getApplication(appName);
	}

	@Override
	public ISchedulingRule getSchedulingRule() {
		return this.schedulingRule;
	}

	public void setSchedulingRule(ISchedulingRule schedulingRule) {
		this.schedulingRule = schedulingRule;
	}

	protected void log(String message) {
		try {
			model.getElementConsoleManager().writeToConsole(getDashElement(), message, LogType.STDOUT);
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
	}

	protected void logAndUpdateMonitor(String message, IProgressMonitor monitor) {
		if (monitor != null) {
			monitor.setTaskName(message);
		}
		try {
			model.getElementConsoleManager().writeToConsole(getDashElement(), message, LogType.STDOUT);
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
	}

	@Override
	String getOpErrorPrefix() {
		return "Error: " + appName + " in '" + model.getRunTarget().getName() + "'";
	}

	public void checkTerminationRequested(IProgressMonitor mon) throws OperationCanceledException {
		if (
				mon!=null && mon.isCanceled() ||
				cancelationToken!=null && cancelationToken.isCanceled()
		) {
			throw new OperationCanceledException();
		}
	}

	protected CancelationToken getCancelationToken() {
		return cancelationToken;
	}

}
