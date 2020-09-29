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

public class CloudSchedulingRule implements ISchedulingRule {

	protected final RunTarget runTarget;

	public CloudSchedulingRule(RunTarget runTarget) {
		this.runTarget = runTarget;
	}

	@Override
	public boolean contains(ISchedulingRule rule) {
		return rule == this;
	}

	@Override
	public boolean isConflicting(ISchedulingRule rule) {
		if (rule instanceof CloudSchedulingRule) {
			CloudSchedulingRule otherRefreshRule = (CloudSchedulingRule) rule;
			if (otherRefreshRule.getRunTarget().getId().equals(this.getRunTarget().getId())) {
				return true;
			}
		}
		return false;
	}

	public RunTarget getRunTarget() {
		return runTarget;
	}

}
