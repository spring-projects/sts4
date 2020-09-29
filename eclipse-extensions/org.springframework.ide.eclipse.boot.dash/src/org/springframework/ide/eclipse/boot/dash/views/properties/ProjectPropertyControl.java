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
package org.springframework.ide.eclipse.boot.dash.views.properties;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;

/**
 * Controls for Project for the properties section
 *
 * @author Alex Boyko
 *
 */
public class ProjectPropertyControl extends AbstractBdePropertyControl {

	private CLabel project;

	@Override
	public void createControl(Composite composite, TabbedPropertySheetPage page) {
		super.createControl(composite, page);
		page.getWidgetFactory().createLabel(composite, "Project:").setLayoutData(GridDataFactory.fillDefaults().create()); //$NON-NLS-1$
		project = page.getWidgetFactory().createCLabel(composite, ""); //$NON-NLS-1$
		project.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		project.setBottomMargin(0);
		project.setLeftMargin(0);
		project.setRightMargin(0);
		project.setTopMargin(0);
	}

	@Override
	public void refreshControl() {
		if (project != null && !project.isDisposed()) {
			BootDashElement bde = getBootDashElement();
			project.setText(getLabels().getStyledText(bde, BootDashColumn.PROJECT).getString());
			project.setImage(getLabels().getImage(bde, BootDashColumn.PROJECT));
		}
	}

}
