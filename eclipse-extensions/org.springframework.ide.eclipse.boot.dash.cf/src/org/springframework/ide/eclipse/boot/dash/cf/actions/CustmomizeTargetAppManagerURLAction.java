/*******************************************************************************
 * Copyright (c) 2016 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.actions;

import java.util.EnumSet;

import org.springframework.ide.eclipse.boot.dash.cf.dialogs.CustomizeAppsManagerURLDialogModel;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springsource.ide.eclipse.commons.core.pstore.PropertyStoreApi;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

public class CustmomizeTargetAppManagerURLAction extends AbstractCloudDashModelAction{

	protected CustmomizeTargetAppManagerURLAction(LiveExpression<BootDashModel> section, SimpleDIContext context) {
		super(section, context);
		setText("Customize Cloud Admin Console URL...");
	}

	@Override
	public void updateEnablement() {
		this.setEnabled(isApplicable(sectionSelection.getValue()));
	}

	@Override
	public void updateVisibility() {
		this.setVisible(isApplicable(sectionSelection.getValue()));
	}

	private boolean isApplicable(BootDashModel section) {
		if (section != null && section instanceof CloudFoundryBootDashModel) {
			PropertyStoreApi props = section.getRunTarget().getType().getPersistentProperties();
			return props != null;
		}
		return false;
	}

	@Override
	public void run() {
		final BootDashModel section = sectionSelection.getValue();
		if (isApplicable(section)) {
			CustomizeAppsManagerURLDialogModel model = new CustomizeAppsManagerURLDialogModel((CloudFoundryBootDashModel)section);
			cfUi().openEditAppsManagerURLDialog(model);
		}
	}

	@Override
	public EnumSet<Location> showIn() {
		return EnumSet.of(Location.CUSTOMIZE_MENU);
	}

}
