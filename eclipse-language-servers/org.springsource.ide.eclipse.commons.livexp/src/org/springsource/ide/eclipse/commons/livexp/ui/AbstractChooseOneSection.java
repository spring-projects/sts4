/*******************************************************************************
 * Copyright (c) 2013, 2015 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.ui;

import org.eclipse.jface.viewers.ILabelProvider;

public abstract class AbstractChooseOneSection<T> extends WizardPageSection {

	protected ILabelProvider labelProvider = new SimpleLabelProvider();

	public AbstractChooseOneSection(IPageWithSections owner) {
		super(owner);
	}

	public AbstractChooseOneSection<T> setLabelProvider(ILabelProvider p) {
		this.labelProvider = p;
		return this;
	}

}
