/*******************************************************************************
 * Copyright (c) 2016, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.LocalRunTarget;
import org.springframework.ide.eclipse.boot.dash.ngrok.NGROKClient;
import org.springframework.ide.eclipse.boot.dash.ngrok.NGROKLaunchTracker;

import com.google.common.collect.ImmutableSet;

public class OpenNgrokAdminUi extends AbstractBootDashElementsAction {

	public OpenNgrokAdminUi(Params params) {
		super(params);
		this.setText("Open Ngrok Admin UI");
		this.setToolTipText("Opens Ngrok Admin UI for this app in a Web Browser");
		this.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/open_browser.png")); //TODO: different icon?
		this.setDisabledImageDescriptor(BootDashActivator.getImageDescriptor("icons/open_browser_disabled.png"));
	}

	@Override
	public void updateVisibility() {
		// Visible if at least one local app is selected
		this.setVisible(getSelectedElements().stream().anyMatch(this::isLocalApp));
	}

	@Override
	public void updateEnablement() {
		BootDashElement selected = getSingleSelectedElement();
		this.setEnabled(selected != null && getClient(selected) != null);
	}

	private NGROKClient getClient(BootDashElement bde) {
		if (isLocalApp(bde)) {
			ImmutableSet<ILaunchConfiguration> launchConfigs = bde.getLaunchConfigs();
			if (launchConfigs.size() == 1) {
				return NGROKLaunchTracker.get(launchConfigs.iterator().next().getName());
			}
		}
		return null;
	}

	boolean isLocalApp(BootDashElement bde) {
		return bde != null && bde.getTarget() instanceof LocalRunTarget;
	}

	@Override
	public void run() {
		NGROKClient client = getClient(getSingleSelectedElement());
		if (client != null) {
			String url = client.getURL();
			if (url != null) {
				ui().openUrl(url);
			}
		}
	}

}
