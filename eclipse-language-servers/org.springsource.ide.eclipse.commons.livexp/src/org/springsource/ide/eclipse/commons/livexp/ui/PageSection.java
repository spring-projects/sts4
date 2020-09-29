/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.ui;

import org.eclipse.swt.widgets.Composite;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;


/**
 * @author Kris De Volder
 */
public abstract class PageSection implements IPageSection {
	
	protected final IPageWithSections owner;
	
	protected PageSection(IPageWithSections owner) {
		this.owner = owner;
	}

	public static final LiveExpression<ValidationResult> OK_VALIDATOR = LiveExpression.constant(ValidationResult.OK);
	public abstract LiveExpression<ValidationResult> getValidator();
	public abstract void createContents(Composite page);
	
}
