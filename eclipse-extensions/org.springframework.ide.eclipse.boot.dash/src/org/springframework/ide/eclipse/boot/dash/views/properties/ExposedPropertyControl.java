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
package org.springframework.ide.eclipse.boot.dash.views.properties;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.springframework.ide.eclipse.boot.dash.model.AbstractLaunchConfigurationsDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.ngrok.NGROKClient;
import org.springframework.ide.eclipse.boot.dash.ngrok.NGROKLaunchTracker;
import org.springsource.ide.eclipse.commons.ui.UiUtil;

import com.google.common.collect.ImmutableSet;

/**
 * @author Martin Lippert
 */
public class ExposedPropertyControl extends AbstractBdePropertyControl {

	private Link exposedURL;

	@Override
	public void createControl(Composite composite, TabbedPropertySheetPage page) {
		super.createControl(composite, page);
		page.getWidgetFactory().createLabel(composite, "Exposed via:").setLayoutData(GridDataFactory.fillDefaults().create()); //$NON-NLS-1$

		exposedURL = new Link(composite, SWT.NONE);
		exposedURL.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		exposedURL.setBackground(composite.getBackground());

		exposedURL.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				AbstractLaunchConfigurationsDashElement<?> bde = getLocalBootDashElement();
				if (bde != null) {
					ImmutableSet<ILaunchConfiguration> launchConfigs = bde.getLaunchConfigs();
					if (launchConfigs.size() == 1) {
						String tunnelName = launchConfigs.iterator().next().getName();
						NGROKClient ngrokClient = NGROKLaunchTracker.get(tunnelName);
						if (ngrokClient != null) {
							String addr = ngrokClient.getURL();
							UiUtil.openUrl(addr);
						}
					}
				}
			}
		});
	}

	@Override
	public void refreshControl() {
		AbstractLaunchConfigurationsDashElement<?> bde = getLocalBootDashElement();

		if (bde != null) {
			StringBuilder labelText = new StringBuilder();

			ImmutableSet<ILaunchConfiguration> configs = bde.getLaunchConfigs();
			if (configs.size() == 1) {
				String tunnelName = configs.iterator().next().getName();
				NGROKClient ngrokClient = NGROKLaunchTracker.get(tunnelName);
				if (ngrokClient != null) {
					labelText.append(ngrokClient.getTunnel().getPublic_url() + "   --- (local ngrok instance at: <a href=\"\">" + ngrokClient.getURL() + "</a>)");
				}
			}

			exposedURL.setText(labelText.toString());
		}
	}

	private AbstractLaunchConfigurationsDashElement<?> getLocalBootDashElement() {
		if (exposedURL != null && !exposedURL.isDisposed()) {
			BootDashElement bde = getBootDashElement();
			if (bde instanceof AbstractLaunchConfigurationsDashElement<?>) {
				return (AbstractLaunchConfigurationsDashElement<?>) bde;
			}
		}

		return null;
	}

}
