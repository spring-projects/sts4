/*******************************************************************************
 * Copyright (c) 2018 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Empty container page just to have a category of Spring project setting pages
 *
 * @author Alex Boyko
 *
 */
public class CommonSpringProjectPropertiesPage extends PropertyPage implements IWorkbenchPropertyPage {

	public CommonSpringProjectPropertiesPage() {
		super();
		noDefaultAndApplyButton();
	}

	@Override
	protected Control createContents(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText("Container for various Spring project settings");
        return label;
	}

}
