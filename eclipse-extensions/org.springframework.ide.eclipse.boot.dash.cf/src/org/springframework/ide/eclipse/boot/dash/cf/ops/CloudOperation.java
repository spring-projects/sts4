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
package org.springframework.ide.eclipse.boot.dash.cf.ops;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.springframework.ide.eclipse.boot.dash.cf.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cf.runtarget.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.cf.ui.CfUserInteractions;
import org.springframework.ide.eclipse.boot.dash.cf.util.CloudErrors;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public abstract class CloudOperation extends Operation<Void> {

	protected final CloudFoundryBootDashModel model;

	public CloudOperation(String opName, CloudFoundryBootDashModel model) {
		super(opName);
		this.model = model;
	}

	abstract protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException;

	@Override
	protected Void runOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		try {
			doCloudOp(monitor);
		} catch (Exception e) {
			CloudErrors.checkAndRethrowCloudException(e, getOpErrorPrefix());
		}
		return null;
	}

	String getOpErrorPrefix() {
		return "Error in target: " + model.getRunTarget().getName();
	}

	protected CloudFoundryRunTarget getRunTarget() {
		return model.getRunTarget();
	}

	protected ClientRequests getClientRequests() {
		CloudFoundryRunTarget target = getRunTarget();
		if (target.isConnected()) {
			return target.getClient();
		}
		return null;
	}

	protected SimpleDIContext getDIContext() {
		return model.getViewModel().getContext().injections;
	}

	protected CfUserInteractions cfUi() {
		return getDIContext().getBean(CfUserInteractions.class);
	}

	protected UserInteractions ui() {
		return getDIContext().getBean(UserInteractions.class);
	}


}
