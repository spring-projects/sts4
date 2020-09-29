/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.ops;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;

public class StopApplicationSchedulingRule extends CloudSchedulingRule {

	private final String appName;

	public StopApplicationSchedulingRule(RunTarget runTarget, String appName) {
		super(runTarget);
		this.appName = appName;
	}

	@Override
	public boolean isConflicting(ISchedulingRule otherRule) {
		if (otherRule instanceof CloudSchedulingRule) {
			CloudSchedulingRule otherCloudRule = (CloudSchedulingRule) otherRule;

			// Possibly in conflict only if the other operation is also on the
			// same run target
			if (otherCloudRule.getRunTarget().getId().equals(this.getRunTarget().getId())
					&& otherCloudRule instanceof StopApplicationSchedulingRule) {
				StopApplicationSchedulingRule otherApplicationSchedulingRule = (StopApplicationSchedulingRule) otherCloudRule;

				return otherApplicationSchedulingRule.getAppName().equals(this.getAppName());
			}
		}
		return false;
	}

	public String getAppName() {
		return this.appName;
	}

}
