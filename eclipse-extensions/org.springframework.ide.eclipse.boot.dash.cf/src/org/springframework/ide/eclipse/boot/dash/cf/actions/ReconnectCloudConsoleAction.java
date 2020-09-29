/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.actions;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.views.AbstractBootDashElementsAction;
import org.springframework.ide.eclipse.boot.dash.views.AbstractBootDashElementsAction.Params;

public class ReconnectCloudConsoleAction extends AbstractCloudAppDashElementsAction {

	public ReconnectCloudConsoleAction(Params params) {
		super(params);
		this.setText("Reconnect Console");
		this.setToolTipText("Reconnect Console");
		this.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/open_console.png"));
		this.setDisabledImageDescriptor(BootDashActivator.getImageDescriptor("icons/open_console_disabled.png"));
	}

	@Override
	public void run() {
		final Collection<BootDashElement> selecteds = getSelectedElements();

		Job job = new Job("Reconnecting console") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				doShowConsoles(selecteds);
				return Status.OK_STATUS;
			}

		};
		job.schedule();
	}

	protected void doShowConsoles(Collection<BootDashElement> selectedElements) {

		if (selectedElements != null) {

			Iterator<BootDashElement> it = selectedElements.iterator();

			// Show first element only for now
			if (it.hasNext()) {
				BootDashElement element = selectedElements.iterator().next();
				BootDashModel model = element.getBootDashModel();
				try {
					if (model.getElementConsoleManager() != null) {
						model.getElementConsoleManager().reconnect(element);
					}
				} catch (Exception e) {
					ui().errorPopup("Reconnect Console Failure", e.getMessage());
				}
			}
		}
	}

	@Override
	public void updateEnablement() {
		boolean enable = false;
		if (!getSelectedElements().isEmpty()) {
			enable = true;
			for (BootDashElement e : getSelectedElements()) {
				if (!(e instanceof CloudAppDashElement)) {
					enable = false;
					break;
				}
			}
		}

		this.setEnabled(enable);
	}
}
