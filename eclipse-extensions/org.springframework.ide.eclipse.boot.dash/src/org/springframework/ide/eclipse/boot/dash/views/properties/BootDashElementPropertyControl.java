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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;

/**
 * Control for Boot Dash Element features for properties view
 *
 * @author Alex Boyko
 *
 */
public interface BootDashElementPropertyControl {

	void createControl(Composite composite, TabbedPropertySheetPage page);

	void refreshControl();

	void setInput(BootDashElement bde);

	void dispose();

}
