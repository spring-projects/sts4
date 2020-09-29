/*******************************************************************************
 * Copyright (c) 2017 Pivotal Software, Inc.
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
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.util.ReflowUtil;

public abstract class ReflowableSection extends WizardPageSection {
	public ReflowableSection(IPageWithSections owner) {
		super(owner);
	}

	protected final LiveVariable<Boolean> visibleState = new LiveVariable<>(true);

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return OK_VALIDATOR;
	}

	/**
	 * Called after a expandable section was expanded or collapsed. It should
	 * cause the surrounding parent widgets to 'reflow' to adapt to new size.
	 */
	protected void reflow(IPageWithSections owner, Composite area) {
		ReflowUtil.reflowParents(owner, area);
	}

	public void setVisible(boolean reveal) {
		this.visibleState.setValue(reveal);
	}
}
