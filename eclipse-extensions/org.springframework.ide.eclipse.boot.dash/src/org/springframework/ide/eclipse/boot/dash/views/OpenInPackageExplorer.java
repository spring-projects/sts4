/*******************************************************************************
 * Copyright (c) 2015, 2017 Pivotal, Inc.
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

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;

@SuppressWarnings("restriction")
public class OpenInPackageExplorer extends AbstractBootDashElementsAction {

	public OpenInPackageExplorer(Params params) {
		super(params);
		this.setText("Open In Package Explorer");
		this.setToolTipText("Open In Package Explorer");
		this.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/package.png"));
		this.setDisabledImageDescriptor(BootDashActivator.getImageDescriptor("icons/package.png"));
	}

	@Override
	public void updateEnablement() {
		Collection<BootDashElement> selectedElements = getSelectedElements();
		if (selectedElements.size() != 1) {
			this.setEnabled(false);
		}
		else {
			BootDashElement selectedElement = selectedElements.iterator().next();
			IProject project = selectedElement.getProject();
			this.setEnabled(project != null);
		}
	}

	@Override
	public void run() {
		final Collection<BootDashElement> selected = getSelectedElements();
		BootDashElement selectedProject = selected.iterator().next();

		IProject project = selectedProject.getProject();
		if (project != null) {
			PackageExplorerPart view= PackageExplorerPart.openInActivePerspective();
			view.tryToReveal(project);
		}
	}
}
