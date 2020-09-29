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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchManager;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.devtools.DevtoolsUtil;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens.CancelationToken;

/**
 * Operation for (re)starting Remote DevTools Client
 *
 * @author Alex Boyko
 *
 */
public class RemoteDevClientStartOperation extends CloudApplicationOperation {

	private final RunState startMode;

	public RemoteDevClientStartOperation(CloudFoundryBootDashModel model, String appName, RunState startMode, CancelationToken cancelationToken) {
		super("Starting Remote DevTools Client for application '" + appName + "'", model, appName, cancelationToken);
		this.startMode = startMode;
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
		CloudAppDashElement cde = model.getApplication(appName);
		if (cde == null || cde.getProject() == null) {
			throw new CoreException(new Status(IStatus.ERROR, BootDashActivator.PLUGIN_ID, "Local project not associated to CF app '" + appName + "'"));
		}
		DevtoolsUtil.disconnectDevtoolsClientsFor(cde);
		//TODO: create a 'monitor' that is aware of cancelation token to apss to the launch operation.
		DevtoolsUtil.launchDevtools(cde, DevtoolsUtil.getSecret(cde.getProject()), startMode == RunState.DEBUGGING ? ILaunchManager.DEBUG_MODE : ILaunchManager.RUN_MODE , monitor);
	}

}
