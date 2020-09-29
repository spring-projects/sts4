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
package org.springframework.ide.eclipse.boot.dash.model;

import org.springsource.ide.eclipse.commons.core.util.StringUtil;
import org.springsource.ide.eclipse.commons.livexp.core.FilterBoxModel;
import org.springsource.ide.eclipse.commons.livexp.util.Filter;
import org.springsource.ide.eclipse.commons.livexp.util.Filters;

/**
 * @author Kris De Volder
 */
public class BootDashElementsFilterBoxModel extends FilterBoxModel<BootDashElement> {

	@Override
	protected Filter<BootDashElement> createFilterForInput(String text) {
		if (StringUtil.hasText(text)) {
			return new BootDashElementSearchFilter(text);
		} else {
			return Filters.acceptAll();
		}
	}

}
