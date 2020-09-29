/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.actions;

import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cf.runtarget.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

/**
 * @author Martin Lippert
 */
public class OpenCloudAdminConsoleAction extends AbstractCloudDashModelAction {

	public OpenCloudAdminConsoleAction(LiveExpression<BootDashModel> sectionSelection, SimpleDIContext ui) {
		super(sectionSelection, ui);
		this.setText("Open Cloud Admin Console");
		this.setToolTipText("Opens the Cloud Administration Console for this Cloud Foundry Space and Organisation");
		this.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/cloud_obj.png"));
	}

	@Override
	public void run() {
		final BootDashModel targetModel = sectionSelection.getValue();
		if (targetModel != null) {
			RunTarget target = targetModel.getRunTarget();
			if (target instanceof CloudFoundryRunTarget) {
				((CloudFoundryRunTarget) target).openCloudAdminConsole(ui());
			}
		}
	}

	@Override
	public void updateEnablement() {
		final BootDashModel targetModel = sectionSelection.getValue();
		if (targetModel != null) {
			RunTarget runTarget = targetModel.getRunTarget();
			this.setEnabled(runTarget instanceof CloudFoundryRunTarget);
		} else {
			this.setEnabled(false);
		}
	}

}
