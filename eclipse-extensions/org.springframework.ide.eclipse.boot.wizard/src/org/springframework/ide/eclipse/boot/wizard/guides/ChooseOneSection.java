/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.guides;

import org.eclipse.jface.viewers.ILabelProvider;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.SimpleLabelProvider;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;

public abstract class ChooseOneSection extends WizardPageSection {

	protected ILabelProvider labelProvider = new SimpleLabelProvider();

	public ChooseOneSection(IPageWithSections owner) {
		super(owner);
	}

	public ChooseOneSection setLabelProvider(ILabelProvider p) {
		this.labelProvider = p;
		return this;
	}

}
