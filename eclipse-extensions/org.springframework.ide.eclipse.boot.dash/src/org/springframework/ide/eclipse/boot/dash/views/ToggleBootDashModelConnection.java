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
package org.springframework.ide.eclipse.boot.dash.views;

import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.RemoteBootDashModel;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RemoteRunTarget.ConnectMode;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

/**
 * Action for toggling Boot Dash model connection
 *
 * @author Alex Boyko
 */
public class ToggleBootDashModelConnection extends AbstractBootDashModelAction {

	protected ToggleBootDashModelConnection(LiveExpression<BootDashModel> section, SimpleDIContext context) {
		super(section, context);
	}

	@Override
	public void update() {
		super.update();
		BootDashModel model = sectionSelection.getValue();
		if (model instanceof RemoteBootDashModel) {
			RemoteBootDashModel connectable = (RemoteBootDashModel) model;
			if (connectable.getRunTarget().isConnected()) {
				setText("Disconnect");
				setDescription("Disconnect Run Target");
				setImageDescriptor(BootDashActivator.getImageDescriptor("icons/cloud-inactive.png"));
			} else {
				setText("Connect");
				setDescription("Connect Run Target");
				setImageDescriptor(BootDashActivator.getImageDescriptor("icons/cloud-ready.png"));
			}
		}
	}

	@Override
	public void updateEnablement() {
		setEnabled(isVisible());
	}

	@Override
	public void updateVisibility() {
		setVisible(sectionSelection.getValue() instanceof RemoteBootDashModel);
	}

	@Override
	public void run() {
		BootDashModel model = sectionSelection.getValue();
		if (model  instanceof RemoteBootDashModel) {
			RemoteBootDashModel connectable = (RemoteBootDashModel) model;
			if (connectable.getRunTarget().isConnected()) {
				connectable.disconnect();
			} else {
				try {
					connectable.connect(ConnectMode.INTERACTIVE);
				} catch (Exception e) {
					//TODO: show error on the view somehow? E.g. error marker on the target?
					ui().errorPopup("Failed to connect to remote target", ExceptionUtil.getMessage(e));
					Log.warn(e);
				}
			}
		}
	}

}
