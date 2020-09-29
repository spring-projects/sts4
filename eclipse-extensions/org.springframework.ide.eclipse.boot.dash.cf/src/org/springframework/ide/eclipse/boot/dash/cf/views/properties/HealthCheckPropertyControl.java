/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.views.properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.springframework.ide.eclipse.boot.dash.cf.client.HealthChecks;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.cf.ops.SetHealthCheckOperation;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.views.properties.AbstractBdePropertyControl;
import org.springsource.ide.eclipse.commons.core.util.StringUtil;

/**
 * @author Kris De Volder
 */
public class HealthCheckPropertyControl extends AbstractBdePropertyControl {

	private CloudAppDashElement app;
	private Combo selection;

	@Override
	public void createControl(Composite composite, TabbedPropertySheetPage page) {
		super.createControl(composite, page);
		page.getWidgetFactory().createLabel(composite, "Healthcheck:");
		selection = new Combo(composite, SWT.READ_ONLY);
//		Don't use the 'CCombo' it looks really bad on linux. Doesn't properly size itself and so contents is 'chopped'.
//		selection = page.getWidgetFactory().createCCombo(composite, SWT.READ_ONLY);
		selection.setItems(HealthChecks.HC_ALL);
		refreshControl();
		selection.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handle();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				handle();
			}

			private void handle() {
				String selected = selection.getText();
				if (StringUtil.hasText(selected)) {
					SetHealthCheckOperation op = new SetHealthCheckOperation(app, selected, app.createCancelationToken());
					app.getBootDashModel().runAsynch(op, null /* no user interactions */);
				}
			}
		});
	}

	@Override
	public void refreshControl() {
		if (app!=null && selection!=null && !selection.isDisposed()) {
			String hc = app.getHealthCheck();
			if (StringUtil.hasText(hc)) {
				selection.setText(hc);
			} else {
				selection.setText("");
			}
		}
	}

	@Override
	public void setInput(BootDashElement bde) {
		if (bde instanceof CloudAppDashElement) {
			super.setInput(bde);
			app = (CloudAppDashElement)bde;
		}
	}

}
