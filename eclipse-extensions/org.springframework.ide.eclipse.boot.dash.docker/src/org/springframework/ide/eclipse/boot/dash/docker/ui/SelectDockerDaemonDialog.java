/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.docker.ui;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;
import org.springsource.ide.eclipse.commons.core.util.OsUtils;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.StringFieldModel;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.ui.CheckboxSection;
import org.springsource.ide.eclipse.commons.livexp.ui.DialogWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.OkButtonHandler;
import org.springsource.ide.eclipse.commons.livexp.ui.StringFieldSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;

import com.google.common.collect.ImmutableList;

public class SelectDockerDaemonDialog extends DialogWithSections {
	
	private Model model;

	public static class Model implements OkButtonHandler {
		private static final String DEFAULT_UNIX_DOCKER_URL = "unix:///var/run/docker.sock";
		private static final String DEFAULT_WINDOWS_DOCKER_URL = "tcp://localhost:2375";
		
		public final LiveVariable<Boolean> useLocalDaemon = new LiveVariable<>(true);
		public final LiveExpression<Boolean> daemonUrlEnabled = useLocalDaemon.apply(local -> !local);
		public final LiveExpression<ValidationResult> urlValidator = new LiveExpression<ValidationResult>(ValidationResult.OK) {

			@Override
			protected ValidationResult compute() {
				if (daemonUrl != null && isWindowsDefaultUrl(daemonUrl.getValue())) {
					return ValidationResult.warning("May require exposing local Docker daemon in the Docker settings");
				}
				return ValidationResult.OK;
			}
		};
		
		public final StringFieldModel daemonUrl = new StringFieldModel("Url", getDefaultDaemonUrl());
		public final LiveVariable<Boolean> okPressed = new LiveVariable<>(false);
		
		{
			daemonUrl.validator(urlValidator);
			urlValidator.dependsOn(daemonUrl.getVariable());
			
			daemonUrlEnabled.onChange((e,v) -> {
				if (useLocalDaemon.getValue()) {
					daemonUrl.setValue(getDefaultDaemonUrl());
				}
			});
		}

		@Override
		public void performOk() throws Exception {
			okPressed.setValue(true);
		}

		private String getDefaultDaemonUrl() {
			if (OsUtils.isWindows()) {
				return DEFAULT_WINDOWS_DOCKER_URL;
			} else {
				return DEFAULT_UNIX_DOCKER_URL;
			}
		}
		
		private boolean isWindowsDefaultUrl(String url) {
			return OsUtils.isWindows() && DEFAULT_WINDOWS_DOCKER_URL.equals(url);
		}
	}

	public SelectDockerDaemonDialog(Model model, Shell shell) {
		super("Connect to Docker Daemon", model, shell);
		this.model = model;
	}
	
	@SuppressWarnings("resource") @Override
	protected List<WizardPageSection> createSections() throws CoreException {
		return ImmutableList.of(
				new CheckboxSection(this, model.useLocalDaemon, "Use Local Daemon"), 
				new StringFieldSection(this, model.daemonUrl).setEnabler(model.daemonUrlEnabled)
		);
	}
}
