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

import org.springframework.ide.eclipse.boot.dash.cf.model.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cf.ui.CfUserInteractions;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.views.AbstractBootDashModelAction;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

/**
 * Action for Cloud Foundry dash model element
 *
 * @author Alex Boyko
 *
 */
public class AbstractCloudDashModelAction extends AbstractBootDashModelAction {

	protected AbstractCloudDashModelAction(LiveExpression<BootDashModel> section, SimpleDIContext ui) {
		super(section, ui);
	}

	protected CfUserInteractions cfUi() {
		return context.getBean(CfUserInteractions.class);
	}

	@Override
	public void updateVisibility() {
		this.setVisible(sectionSelection.getValue() instanceof CloudFoundryBootDashModel);
	}

}
