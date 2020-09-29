/*******************************************************************************
 * Copyright (c) 2015, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.*;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetTypes;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;

public class LocalRunTarget extends AbstractRunTarget<Void> {

	public static final RunTarget INSTANCE = new LocalRunTarget();
	public static final BootDashColumn[] DEFAULT_COLUMNS = {
			RUN_STATE_ICN,
			NAME,
			DEVTOOLS,
			LIVE_PORT,
			INSTANCES,
			DEFAULT_PATH,
			TAGS,
			EXPOSED_URL
	};

	private LocalRunTarget() {
		super(RunTargetTypes.LOCAL, "local");
	}

	/**
	 * Create a launch config for a given dash element and initialize it with
	 * some suitable defaults.
	 *
	 * @param mainType,
	 *            may be null if the main type can not be 'guessed'
	 *            unambiguosly.
	 */
	public ILaunchConfiguration createLaunchConfig(IJavaProject jp, IType mainType) throws Exception {
		if (mainType != null) {
			return BootLaunchConfigurationDelegate.createConf(mainType);
		} else {
			return BootLaunchConfigurationDelegate.createConf(jp);
		}
	}

	@Override
	public BootDashColumn[] getDefaultColumns() {
		return DEFAULT_COLUMNS;
	}

	@Override
	public BootDashModel createSectionModel(BootDashViewModel viewModel) {
		return new LocalBootDashModel(viewModel.getContext(), viewModel);
	}

	@Override
	public boolean canRemove() {
		return false;
	}

	@Override
	public boolean canDeployAppsFrom() {
		return true;
	}

	@Override
	public Void getParams() {
		return null;
	}
}