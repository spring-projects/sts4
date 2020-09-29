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

import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cf.ops.SelectManifestOp;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class SelectManifestAction extends AbstractCloudAppDashElementsAction {

	public SelectManifestAction(Params params) {
		super(params);
		params.getContext().assertDefinitionFor(UserInteractions.class);
		this.setText("Select Manifest");
		this.setToolTipText("Selects a manifest YAML file to use during application restart.");
		this.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/selectmanifest.gif"));
	}

	@Override
	public void run() {
		CloudAppDashElement element = getSelectedCloudElementWithProject();
		if (element != null) {
			CloudFoundryBootDashModel model = element.getBootDashModel();
			model.runAsynch(new SelectManifestOp(element), ui());
		}
	}

	@Override
	public void updateEnablement() {
		this.setEnabled(getSelectedCloudElementWithProject() != null);
	}

	protected CloudAppDashElement getSelectedCloudElementWithProject() {
		if (getSelectedElements().size() == 1) {

			for (BootDashElement e : getSelectedElements()) {
				if (e instanceof CloudAppDashElement) {
					CloudAppDashElement cde = (CloudAppDashElement) e;
					if (cde.getProject() != null && cde.getProject().isAccessible()) {
						return cde;
					}
				}
			}
		}
		return null;
	}
}
