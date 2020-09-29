/*******************************************************************************
 * Copyright (c) 2015 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.ui;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * A horizontal like that spans the width of its parent composite.
 * <p>
 * Can be used as a spacer to seperate groups of stuff from one another.
 *
 * @author Kris De Volder
 */
public class HLineSection extends WizardPageSection {

	public HLineSection(IPageWithSections owner) {
		super(owner);
	}

	@Override
	public void createContents(Composite page) {
		Label line = new Label(page, SWT.SEPARATOR|SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(line);
	}

}
