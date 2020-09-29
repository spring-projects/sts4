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

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;

/**
 * Abstract common functionality for general properties view section for the
 * {@link BootDashElement}
 *
 * @author Alex Boyko
 *
 */
public abstract class AbstractBdeGeneralPropertiesSection extends AbstractBdePropertiesSection {

	private BootDashElementPropertyControl[] propertyControls = new BootDashElementPropertyControl[0];

	abstract protected BootDashElementPropertyControl[] createPropertyControls();

	@Override
	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);

		BootDashElementPropertyControl[] createdControls = createPropertyControls();
		propertyControls = createdControls == null ? new BootDashElementPropertyControl[0] : createdControls;

		final Composite composite = getWidgetFactory().createFlatFormComposite(parent);
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).margins(ITabbedPropertyConstants.HSPACE, ITabbedPropertyConstants.VSPACE).create());
		for (BootDashElementPropertyControl control : propertyControls) {
			control.createControl(composite, aTabbedPropertySheetPage);
		}
	}

	@Override
	public void setInput(IWorkbenchPart part, ISelection selection) {
		super.setInput(part, selection);
		BootDashElement bootDashElement = getBootDashElement();
		for (BootDashElementPropertyControl control : propertyControls) {
			control.setInput(bootDashElement);
		}
	}

	@Override
	public void dispose() {
		for (BootDashElementPropertyControl control : propertyControls) {
			control.dispose();
		}
		super.dispose();
	}

	@Override
	public void refresh() {
		super.refresh();
		for (BootDashElementPropertyControl control : propertyControls) {
			control.refreshControl();
		}
	}

}
