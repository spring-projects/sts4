/*******************************************************************************
 * Copyright (c) 2015, 2017 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test.mocks;

import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.DEFAULT_PATH;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.INSTANCES;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.NAME;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.PROJECT;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.RUN_STATE_ICN;
import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.TAGS;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.boot.dash.cf.runtarget.CloudFoundryTargetProperties;
import org.springframework.ide.eclipse.boot.dash.model.AbstractRunTarget;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RunTargetWithProperties;
import org.springframework.ide.eclipse.boot.dash.api.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.TargetProperties;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;

public class MockRunTarget extends AbstractRunTarget<CloudFoundryTargetProperties> implements RunTargetWithProperties<CloudFoundryTargetProperties> {

	private CloudFoundryTargetProperties properties;
	private boolean requiresCredentials;

	public MockRunTarget(RunTargetType<CloudFoundryTargetProperties> type, CloudFoundryTargetProperties properties) {
		this(type, properties, false);
	}

	public MockRunTarget(RunTargetType<CloudFoundryTargetProperties> type, CloudFoundryTargetProperties properties, boolean requiresCredentials) {
		super(type, properties.getRunTargetId());
		this.properties = properties;
		this.requiresCredentials = requiresCredentials;
	}

	private final BootDashColumn[] defaultColumns = { RUN_STATE_ICN, NAME, PROJECT, INSTANCES, DEFAULT_PATH, TAGS };

	@Override
	public BootDashColumn[] getDefaultColumns() {
		return defaultColumns;
	}

	@Override
	public MockBootDashModel createSectionModel(BootDashViewModel viewModel) {
		return new MockBootDashModel(this, viewModel.getContext(), viewModel);
	}

	@Override
	public boolean canRemove() {
		return true;
	}

	@Override
	public boolean canDeployAppsFrom() {
		return false;
	}

	public String get(String prop) {
		return properties.get(prop);
	}

	@Override
	public TargetProperties getTargetProperties() {
		return properties;
	}

	@Override
	public boolean requiresCredentials() {
		return requiresCredentials;
	}

	@Override
	public CloudFoundryTargetProperties getParams() {
		return properties;
	}

}
