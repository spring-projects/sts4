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

import java.time.Duration;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFApplicationDetail;
import org.springframework.ide.eclipse.boot.dash.cf.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.RefreshState;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;

import reactor.core.publisher.Flux;

/**
 * Refreshes the application instances.
 * <p/>
 * This will indirectly refresh the application running state as the running
 * state of an app is resolved from the number of running instances
 *
 */
public class AppInstancesRefreshOperation extends CloudOperation {

	private List<CFApplication> appsToLookUp;

	public AppInstancesRefreshOperation(CloudFoundryBootDashModel model, List<CFApplication> appsToLookUp) {
		super("Refreshing running state of applications in: " + model.getRunTarget().getName(), model);
		this.appsToLookUp = appsToLookUp;
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception {
		this.model.refreshTracker.call("Fetching App Instances...", () -> {
			if (!appsToLookUp.isEmpty()) {
				Duration timeToWait = Duration.ofSeconds(30);
				ClientRequests client = model.getRunTarget().getClient();
				if (client!=null) {
					client.getApplicationDetails(appsToLookUp)
					.doOnNext(this.model::updateApplication)
					.then()
					.block(timeToWait);
				}
			}
			return null;
		});
	}

	@Override
	public ISchedulingRule getSchedulingRule() {
		return new RefreshSchedulingRule(model.getRunTarget());
	}
}
