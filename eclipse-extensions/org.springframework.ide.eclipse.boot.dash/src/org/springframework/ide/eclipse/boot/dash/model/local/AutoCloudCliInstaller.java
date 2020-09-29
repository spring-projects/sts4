/*******************************************************************************
 * Copyright (c) 2017 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model.local;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.boot.core.cli.install.AutoInstallDescription;
import org.springframework.ide.eclipse.boot.core.cli.install.CloudCliInstall;
import org.springframework.ide.eclipse.boot.core.cli.install.IBootInstall;
import org.springframework.ide.eclipse.boot.core.cli.install.IBootInstallExtension;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

/**
 * Helper class to perform an automatic installation of SprincCloud CLI into
 * SpringBoot CLI.
 */
public class AutoCloudCliInstaller {

	private final IBootInstall bootInstall;

	public AutoCloudCliInstaller(IBootInstall bootInstall) {
		super();
		this.bootInstall = bootInstall;
	}

	public void performInstall(UserInteractions ui) {
		AutoInstallDescription autoInstallation = bootInstall.checkAutoInstallable(CloudCliInstall.class);
		if (!autoInstallation.isPossible) {
			ui.errorPopup("Auto installation of Spring Cloud CLI not possible", autoInstallation.message);
		} else if (ui.confirmOperation("Confirm Installation of Spring Cloud CLI?", autoInstallation.message)) {
			InstallBootCliExtensionJob installCloudCliJob = new InstallBootCliExtensionJob("Auto install Spring Cloud CLI", CloudCliInstall.class);
			installCloudCliJob.schedule();
			long waitTime = 200L;
			for (long waited = 0L; installCloudCliJob.getState() != Job.NONE && waited < 30000L; waited+=waitTime) {
				try {
					Thread.sleep(waitTime);
				} catch (InterruptedException e) {
					// ignore
				}
			}
			if (installCloudCliJob.getState() != Job.NONE) {
				Log.error("Timed out waiting for Spring Cloud CLI to be installed");
			}
		}
	}

	private class InstallBootCliExtensionJob extends Job {

		private Class<? extends IBootInstallExtension> extensionType;

		InstallBootCliExtensionJob(String name, Class<? extends IBootInstallExtension> extensionType) {
			super(name);
			this.extensionType = extensionType;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				bootInstall.installExtension(extensionType);
				return Status.OK_STATUS;
			} catch (Exception e) {
				return ExceptionUtil.status(e);
			}
		}

	}
}
