/*******************************************************************************
 * Copyright (c) 2015, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.properties;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;

/**
 * App control for properties view section
 *
 * @author Alex Boyko
 *
 */
public class AppPropertyControl extends AbstractBdePropertyControl {

	private Label app;

	@Override
	public void createControl(Composite composite, TabbedPropertySheetPage page) {
		super.createControl(composite, page);
		page.getWidgetFactory().createLabel(composite, "Name:").setLayoutData(GridDataFactory.fillDefaults().create()); //$NON-NLS-1$
		app = page.getWidgetFactory().createLabel(composite, ""); //$NON-NLS-1$
		app.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
	}

	@Override
	public void refreshControl() {
		if (app != null && !app.isDisposed()) {
			BootDashElement element = getBootDashElement();
			app.setText(getLabels().getStyledText(element, BootDashColumn.NAME).getString());
		}
	}

}
