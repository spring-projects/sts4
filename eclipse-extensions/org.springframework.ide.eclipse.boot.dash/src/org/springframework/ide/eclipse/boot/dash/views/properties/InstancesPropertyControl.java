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
 * Control for Instances of the app for properties section
 *
 * @author Alex Boyko
 *
 */
public class InstancesPropertyControl extends AbstractBdePropertyControl {

	private Label instances;
	private Label nameLabel;
	private Composite composite;

	@Override
	public void createControl(Composite composite, TabbedPropertySheetPage page) {
		super.createControl(composite, page);
		this.composite = composite;
		nameLabel = page.getWidgetFactory().createLabel(composite, "Instances:");
		nameLabel.setLayoutData(GridDataFactory.fillDefaults().create()); //$NON-NLS-1$
		instances = page.getWidgetFactory().createLabel(composite, ""); //$NON-NLS-1$
		instances.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
	}

	@Override
	public void refreshControl() {
		if (instances != null && !instances.isDisposed()) {
			BootDashElement element = getBootDashElement();
			instances.setText(getLabels().getStyledText(element, BootDashColumn.INSTANCES).getString());
		}
	}

	private void relayout() {
		Composite c = composite;
		while (c != null) {
			c.layout(true, true);
			c = c.getParent();
		}
	}

	// For future possible dynamic filtering of controls
	private void hide() {
		nameLabel.setLayoutData(GridDataFactory.swtDefaults().exclude(true).create()); //$NON-NLS-1$
		nameLabel.setVisible(false);
		instances.setLayoutData(GridDataFactory.swtDefaults().exclude(true).create());
		instances.setVisible(false);
		relayout();
	}

	private void show() {
		nameLabel.setLayoutData(GridDataFactory.fillDefaults().create()); //$NON-NLS-1$
		nameLabel.setVisible(true);
		instances.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		instances.setVisible(true);
		relayout();
	}

}
