/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.debug;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.cf.ops.CloudApplicationOperation;
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens.CancelationToken;

/**
 * @author Kris De Volder
 */
public class SshDebugStartOperation extends CloudApplicationOperation {

	private CloudAppDashElement app;

	public SshDebugStartOperation(CloudAppDashElement app, DebugSupport debugSupport, CancelationToken cancelationToken) {
		super("Starting SSH debugging for app '"+app.getName()+"'", app.getBootDashModel(), app.getName(), cancelationToken);
		this.app = app;
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
		//TODO: wireup a progress monitor to pass to 'doLaunch' so its aware of cancelation token
		SshDebugLaunchConfigurationDelegate.doLaunch(app, monitor);
	}
}
