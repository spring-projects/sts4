/*******************************************************************************
 * Copyright (c) 2015, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views;

import java.util.Collection;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.dash.model.AbstractLaunchConfigurationsDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel.ElementStateListener;
import org.springframework.ide.eclipse.boot.dash.model.LocalCloudServiceDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetTypes;
import org.springframework.ide.eclipse.boot.dash.ngrok.NGROKClient;
import org.springframework.ide.eclipse.boot.dash.ngrok.NGROKInstallManager;

/**
 * @author Martin Lippert
 */
public class ExposeAppAction extends RunStateAction {

	private NGROKInstallManager ngrokManager;

	public ExposeAppAction(Params params, final RunState goalState, final NGROKInstallManager ngrokManager) {
		super(params, goalState);

		Assert.isLegal(goalState == RunState.RUNNING || goalState == RunState.DEBUGGING);

		this.ngrokManager = ngrokManager;

		model.addElementStateListener(new ElementStateListener() {
			@Override
			public void stateChanged(BootDashElement e) {
				if (e instanceof AbstractLaunchConfigurationsDashElement<?> && RunState.INACTIVE.equals(e.getRunState())) {
					try {
						AbstractLaunchConfigurationsDashElement<?> localDashProject = (AbstractLaunchConfigurationsDashElement<?>) e;
						localDashProject.shutdownExpose();
					} catch (Exception ex) {
						ui().errorPopup("error shutting down tunnel", "error shutting down tunnel");
					}
				}
			}
		});
	}

	@Override
	protected boolean appliesToElement(BootDashElement bootDashElement) {
		return bootDashElement.getTarget().getType().equals(RunTargetTypes.LOCAL) && !(bootDashElement instanceof LocalCloudServiceDashElement);
	}

	@Override
	protected Job createJob() {
		final Collection<BootDashElement> selecteds = getSelectedElements();
		if (!selecteds.isEmpty()) {
			boolean riskAccepted = ui().confirmWithToggle("ngrok.tunnel.warning.state", "Really Expose local service on public internet?",
					"The ngrok tunnel uses a third-party server to pass all data between your local app and its clients over a public internet connection.\n\n" +
					"Do you really want to do this?",
					null);
			if (riskAccepted) {
				String ngrokInstall = this.ngrokManager.getDefaultInstall();
				if (ngrokInstall == null) {
					ngrokInstall = ui().chooseFile("ngrok installation", null);
					if (ngrokInstall != null) {
						this.ngrokManager.addInstall(ngrokInstall);
						this.ngrokManager.setDefaultInstall(ngrokInstall);
						this.ngrokManager.save();
					}
				}

				if (ngrokInstall != null) {
					final NGROKClient ngrokClient = new NGROKClient(ngrokInstall);

					final String eurekaInstance = getEurekaInstance();
					if (eurekaInstance != null) {
						return new Job("Restarting and Exposing " + selecteds.size() + " Dash Elements") {
							@Override
							public IStatus run(IProgressMonitor monitor) {
								monitor.beginTask("Restart and Expose Boot Dash Elements", selecteds.size());
								try {
									for (BootDashElement el : selecteds) {
										if (el instanceof AbstractLaunchConfigurationsDashElement<?>) {
											monitor.subTask("Restarting: " + el.getName());
											try {
												AbstractLaunchConfigurationsDashElement<?> localDashProject = (AbstractLaunchConfigurationsDashElement<?>) el;
												localDashProject.restartAndExpose(getGoalState(), ngrokClient, eurekaInstance, ui());
											} catch (Exception e) {
												return BootActivator.createErrorStatus(e);
											}
										}
										monitor.worked(1);
									}
									return Status.OK_STATUS;
								} finally {
									monitor.done();
								}
							}
						};
					}
				}
			}
		}
		return null;
	}

	private String getEurekaInstance() {
		String eurekaInstance = ui().selectRemoteEureka(model, "Eureka URL", "please enter the full URL of the Eureka instance you would like to use", "", null);

		if (eurekaInstance != null) {
			if (eurekaInstance.endsWith("/eureka") || eurekaInstance.endsWith("/eureka/")) {
				return eurekaInstance;
			}
			else if (eurekaInstance.endsWith("/")) {
				eurekaInstance = eurekaInstance + "eureka/";
			}
			else {
				eurekaInstance = eurekaInstance + "/eureka/";
			}
		}

		return eurekaInstance;
	}



	@Override
	public void updateVisibility() {
		boolean visible = !getSelectedElements().isEmpty();
		for (BootDashElement e : getSelectedElements()) {
			if (!RunTargetTypes.LOCAL.equals(e.getTarget().getType())) {
				visible = false;
				break;
			}
		}
		setVisible(visible);
	}

	@Override
	public void dispose() {
		super.dispose();
	}

}
