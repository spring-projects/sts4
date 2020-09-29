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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.docker.ui.SelectDockerDaemonDialog.Model;
import org.springsource.ide.eclipse.commons.livexp.ui.UIContext;

public class DefaultDockerUserInteractions implements DockerUserInteractions {

	private static final String MESSAGE_WIRING_ERROR = "This is embarassing... There was an error encountered in the OSGi setup as some bundles are incorrectly wired.\n" + 
			"\nPlease restart your IDE with the '-clean' parameter once to workaround the problem. It is shame of course that we could not automate the proper restart, apologies for the inconvenience.\n" + 
			"\nThe issue will be fixed in the next version of Eclipse (4.17)";
	private SimpleDIContext injections;

	public DefaultDockerUserInteractions(SimpleDIContext injections) {
		this.injections = injections;}

	@Override
	public void selectDockerDaemonDialog(Model model) {
		Display.getDefault().syncExec(() -> new SelectDockerDaemonDialog(model, getShell()).open());
	}

	private Shell getShell() {
		return injections.getBean(UIContext.class).getShell();
	}
	
	public static void openBundleWiringError(Throwable t) {
		List<Status> childStatuses = new ArrayList<>(t.getStackTrace().length);
		for (StackTraceElement stackTrace : t.getStackTrace()) {
			Status status = new Status(IStatus.ERROR, BootDashActivator.PLUGIN_ID, stackTrace.toString());
			childStatuses.add(status);
		}

		MultiStatus status = new MultiStatus(BootDashActivator.PLUGIN_ID, IStatus.ERROR,
				childStatuses.toArray(new Status[] {}), t.toString(), t);
		Display display = Display.getCurrent();
		if (display != null && !display.isDisposed()) {
			ErrorDialog.openError(display.getActiveShell(), "Error Wiring Bundles", MESSAGE_WIRING_ERROR, status);
		} else {
			display = PlatformUI.getWorkbench().getDisplay();
			if (display != null && !display.isDisposed()) {
				display.asyncExec(() -> ErrorDialog.openError(Display.getCurrent().getActiveShell(), "Error Wiring Bundles", MESSAGE_WIRING_ERROR, status));
			}
		}
	}
	
}
