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
package org.springframework.ide.eclipse.boot.dash.cf.debug;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.springframework.ide.eclipse.boot.dash.cf.runtarget.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.launch.LaunchTabSelectionModel;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;

import com.google.common.collect.ImmutableSet;

public class SelectRunTargetLaunchTabModel extends LaunchTabSelectionModel<CloudFoundryRunTarget> {

//	public static SelectProjectLaunchTabModel create() {
//		LiveVariable<IProject> project = new LiveVariable<IProject>();
//		ExistingBootProjectSelectionValidator validator = new ExistingBootProjectSelectionValidator(project);
//		return new SelectProjectLaunchTabModel(project, validator);
//	}

	private BootDashViewModel context;

	public SelectRunTargetLaunchTabModel(LiveVariable<CloudFoundryRunTarget> variable, LiveExpression<ValidationResult> validator, BootDashViewModel context) {
		super(variable, validator);
		this.context = context;
	}

	@Override
	public void initializeFrom(ILaunchConfiguration conf) {
		selection.setValue(SshDebugLaunchConfigurationDelegate.getRunTarget(conf, context));
		getDirtyState().setValue(false);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy conf) {
		SshDebugLaunchConfigurationDelegate.setRunTarget(conf, selection.getValue());
		getDirtyState().setValue(false);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy conf) {
//		if (choices.length>0) {
//			SshDebugLaunchConfigurationDelegate.setRunTarget(conf, choices[0]);
//		}
	}

	public CloudFoundryRunTarget[] getChoices() {
		ImmutableSet<RunTarget> targets = context.getRunTargets().getValues();
		ArrayList<CloudFoundryRunTarget> interesting = new ArrayList<>(targets.size());
		for (RunTarget t : targets) {
			if (t instanceof CloudFoundryRunTarget) {
				interesting.add((CloudFoundryRunTarget) t);
			}
		}
		return interesting.toArray(new CloudFoundryRunTarget[interesting.size()]);
	}

}