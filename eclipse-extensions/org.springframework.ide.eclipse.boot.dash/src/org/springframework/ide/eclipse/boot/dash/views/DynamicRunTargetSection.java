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
package org.springframework.ide.eclipse.boot.dash.views;

import java.util.Set;

import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.views.sections.DynamicCompositeSection;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;

public class DynamicRunTargetSection extends DynamicCompositeSection<BootDashModel> {

	public DynamicRunTargetSection(IPageWithSections owner, LiveExpression<Set<BootDashModel>> models,
			SectionFactory<BootDashModel> factory) {
		super(owner, models, factory, BootDashElement.class);
	}

}
