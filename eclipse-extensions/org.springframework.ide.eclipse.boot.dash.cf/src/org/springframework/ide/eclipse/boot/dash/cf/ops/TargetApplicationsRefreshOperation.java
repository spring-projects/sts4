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

import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.RefreshState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

/**
 * This performs a "two-tier" refresh as fetching list of
 * {@link CloudApplication} can be slow, especially if also fetching each app's
 * instances.
 * <p/>
 * This refresh operation only fetches a "basic" shallow list of
 * {@link CloudApplication}, which may be quicker to resolve, and notifies the
 * model when element changes occur.
 *
 * <p/>
 * It also launches a separate refresh job that may take longer to complete
 * which is fetching instances and app running state.
 *
 * @see AppInstancesRefreshOperation
 */
public final class TargetApplicationsRefreshOperation extends CloudOperation {

	private UserInteractions ui;

	public TargetApplicationsRefreshOperation(CloudFoundryBootDashModel model, UserInteractions ui) {
		super("Refreshing list of Cloud applications for: " + model.getRunTarget().getName(), model);
		this.ui = ui;
	}

	@Override
	synchronized protected void doCloudOp(IProgressMonitor monitor) throws Exception {
		if (model.getRunTarget().isConnected()) {
			model.refreshTracker.call("Fetching Apps...", () -> {
				for (CloudAppDashElement app : model.getApplicationValues()) {
					app.setError(null); // clear old error states.
				}
				try {

					// 1. Fetch basic list of applications. Should be the "faster" of
					// the
					// two refresh operations

					List<CFApplication> apps = model.getRunTarget().getClient().getApplicationsWithBasicInfo();
					this.model.updateAppNames(getNames(apps));

					// 2. Launch the slower app stats/instances refresh operation.
					this.model.runAsynch(new AppInstancesRefreshOperation(this.model, apps), ui);
					return null;
				} catch (Exception e) {
					/*
					 * Failed to obtain applications list from CF
					 */
					model.updateElements(null);
					throw e;
				}
			});
		} else {
			model.updateElements(null);
		}
	}

	private Collection<String> getNames(List<CFApplication> apps) {
		Builder<String> builder = ImmutableList.builder();
		for (CFApplication app : apps) {
			builder.add(app.getName());
		}
		return builder.build();
	}

	@Override
	public ISchedulingRule getSchedulingRule() {
		return new RefreshSchedulingRule(model.getRunTarget());
	}
}