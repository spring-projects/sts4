/*******************************************************************************
 * Copyright (c) 2021 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.ui;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.springsource.ide.eclipse.commons.livexp.core.CompositeValidator;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;

public class RowSection extends WizardPageSection {
	
	private IPageSection[] sections;
	private CompositeValidator validator;

	public RowSection(IPageWithSections owner, IPageSection... sections) {
		super(owner);
		this.sections = sections;
	}

	@Override
	public void createContents(Composite page) {
		Composite c = new Composite(page, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).applyTo(c);
		RowLayout layout = new RowLayout(SWT.HORIZONTAL);
		layout.pack = false;
		c.setLayout(layout);
		
		validator = new CompositeValidator();
		for (IPageSection s : sections) {
			s.createContents(c);
			validator.addChild(s.getValidator());
		}
	}

	@Override
	public void dispose() {
		for (IPageSection s : sections) {
			if (s instanceof Disposable) {
				((Disposable) s).dispose();
			}
		}
		super.dispose();
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return validator;
	}

}
