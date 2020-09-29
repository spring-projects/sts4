/*******************************************************************************
 * Copyright (c) 2015, 2019 Pivotal, Inc.
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
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.remote.GenericRemoteAppElement;

public class OpenConsoleAction extends AbstractBootDashElementsAction {

	public OpenConsoleAction(Params params) {
		super(params);
		this.setText("Open Console");
		this.setToolTipText("Open Console");
		this.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/open_console.png"));
		this.setDisabledImageDescriptor(BootDashActivator.getImageDescriptor("icons/open_console_disabled.png"));
	}

	@Override
	public void run() {
		final Collection<BootDashElement> selecteds = getSelectedElements();
		showSelected(ui(), selecteds);
	}

	protected void showSelected(UserInteractions ui, Collection<BootDashElement> selected) {

		Job job = new Job("Opening console") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				doShowConsoles(selected);
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
					BootDashModelConsoleManager consoleManager = model.getElementConsoleManager();
					if (consoleManager != null) {
						consoleManager.showConsole(element);
					}

				} catch (Exception e) {
					ui().errorPopup("Open Console Failure", e.getMessage());
				}
			}
		}
	}

	@Override
	public void updateVisibility() {
		this.setVisible(supportsConsole());
	}

	@Override
	public void updateEnablement() {
		this.setEnabled(supportsConsole());
	}

	protected boolean supportsConsole() {
    	BootDashElement element = getSingleSelectedElement();
		boolean supports = false;
		if (element != null) {
			if (element instanceof GenericRemoteAppElement) {
				supports = ((GenericRemoteAppElement) element).canWriteToConsole();
			} else {
				// Backward compatibility with CF. Console action is enabled when a single CF element
				// is selected
				supports = true;
			}
		}
		return supports;
	}
}
