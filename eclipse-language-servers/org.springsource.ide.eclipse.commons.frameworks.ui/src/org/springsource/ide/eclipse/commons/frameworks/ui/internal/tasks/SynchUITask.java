/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.frameworks.ui.internal.tasks;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.springsource.ide.eclipse.commons.frameworks.ui.FrameworkUIActivator;


/**
 * @author Nieraj Singh
 */
public class SynchUITask {

	private IUIRunnable userRunnable;
	private String taskName;

	public SynchUITask(IUIRunnable runnable, String taskName) {
		this.userRunnable = runnable;
		this.taskName = taskName;
	}

	protected Shell getShell() {

		Shell shell = null;
		if (userRunnable != null) {
			shell = userRunnable.getShell();
		}

		if (shell == null) {
			shell = Display.getDefault().getActiveShell();
			if (shell == null) {
				IWorkbenchWindow[] windows = PlatformUI.getWorkbench()
						.getWorkbenchWindows();
				for (IWorkbenchWindow window : windows) {
					shell = window.getShell();
					if (shell != null) {
						break;
					}
				}
			}
		}

		return shell;
	}

	public void execute() {
		if (userRunnable == null) {
			return;
		}
		Shell shell = getShell();
		if (shell != null) {
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
			IProgressMonitor monitor = dialog.getProgressMonitor();
			if (monitor != null) {
				monitor.setTaskName(taskName);
			}
			try {
				dialog.run(true, true, new IRunnableWithProgress() {

					public void run(IProgressMonitor monitor)
							throws InvocationTargetException,
							InterruptedException {
						userRunnable.run(monitor);

					}
				});
			} catch (InvocationTargetException e) {
				FrameworkUIActivator.log(new Status(IStatus.ERROR, FrameworkUIActivator.PLUGIN_ID, "Unexpected error", e));
			} catch (InterruptedException e) {
				FrameworkUIActivator.log(new Status(IStatus.ERROR, FrameworkUIActivator.PLUGIN_ID, "Operation cancelled", e));
			}
		}

	}

}
