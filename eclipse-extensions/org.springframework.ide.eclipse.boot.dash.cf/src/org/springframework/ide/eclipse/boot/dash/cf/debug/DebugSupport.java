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

import java.util.Map;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.cf.ops.Operation;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens.CancelationToken;
import org.springframework.ide.eclipse.boot.util.ProcessListenerAdapter;
import org.springframework.ide.eclipse.boot.util.ProcessTracker;

/**
 * Abstract class that must be implemented to add debug support to a CF application.
 *
 * @author Kris De Volder
 */
public abstract class DebugSupport {

	/**
	 * Determine whether debugging can be supported (using the strategy impemented by this DebugSupport instance)
	 */
	public abstract boolean isSupported(CloudAppDashElement app);
	/**
	 * If isSupported returns false than the support strategy may also return an explanation why the strategy is not
	 * supported (e.g. PCF version too old, SSH support disabled etc.)
	 */
	public abstract String getNotSupportedMessage(CloudAppDashElement app);

	/**
	 * Creates operation that does whatever is needed to get debugger connected to the targetted app.
	 */
	public abstract Operation<?> createOperation(CloudAppDashElement app, String opName, UserInteractions ui, CancelationToken cancelToken);

	/**
	 * Called to allow debug support to muck around with environment variables so that it can
	 * do things like add debugging options to 'JAVA_OPTS'.
	 */
	public abstract void setupEnvVars(Map<String, String> environmentVariables);

	/**
	 * Like setupEnvVars, but called when debugging is disabled. The debug strategy should try to
	 * undo any changes it made to the env vars to enable debugging.
	 */
	public abstract void clearEnvVars(Map<String, String> environmentVariables);

	/**
	 * Determines whether debugger is currently attached to the targetted app.
	 */
	public abstract boolean isDebuggerAttached(CloudAppDashElement app);

	/**
	 * A debug strategy typically involves creating some type of launch that establishes
	 * a debug connection. To be able to update the debug state in response to changes in
	 * launches, its necessary to be able to determine what dashboard element corresponds to
	 * a given launch. Therefore a debug strategy must implement this method.
	 *
	 * @return The corresponding CDE for a given launch, or null
	 */
	public abstract CloudAppDashElement getElementFor(ILaunch l, BootDashViewModel viewModel);

	/**
	 * Provides a process tracker. Subclasses may override if the default implementation is not suitable.
	 * <p>
	 * The process tracker is responsible to listen for changes in the Eclipse debug ui so it can make
	 * bootdash elements 'debug' state update when processes and/or debug targets are created or terminated.
	 */
	public ProcessTracker createProcessTracker(final BootDashViewModel viewModel) {
		return new ProcessTracker(new ProcessListenerAdapter() {
			@Override
			public void debugTargetCreated(ProcessTracker tracker, IDebugTarget target) {
				handleStateChange(target.getLaunch(), "debugTargetCreated");
			}
			@Override
			public void debugTargetTerminated(ProcessTracker tracker, IDebugTarget target) {
				handleStateChange(target.getLaunch(), "debugTargetTerminated");
			}

			@Override
			public void processTerminated(ProcessTracker tracker, IProcess process) {
				//Typically a debug strategy only needs to care about debugtargets, not IProcesses.
				// So nothing to do here.
			}
			@Override
			public void processCreated(ProcessTracker tracker, IProcess process) {
				//Typically a debug strategy only needs to care about debugtargets, not IProcesses.
				// So nothing to do here.
			}
			private void handleStateChange(ILaunch l, Object info) {
				CloudAppDashElement e = getElementFor(l, viewModel);
				if (e!=null) {
					BootDashModel model = e.getBootDashModel();
					model.notifyElementChanged(e, info);
				}
			}
		});
	}
}
