/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.util;

import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springsource.ide.eclipse.commons.livexp.util.Filter;

/**
 * A wrapper around a Filter, adapting the filter to become 'tree aware'.
 * <p>
 * The resulting filter uses these two rules:
 * <p>
 * a) If the base-filter accepts a child, then the resulting filter must also
 * accept the child's parent (and grandparent etc.)
 * <p>
 * b) If the base-filter wrapped filter accepts a (parent) node, then the resulting filter
 * must also accept all the children (and grandchildren etc.) of that node.
 *
 * TODO: it feels like this class could be made more abstract and reused for other
 * things that are 'tree-like', not just BootDashElements.
 *
 * @author Kris De Volder
 */
public class TreeAwareFilter implements Filter<BootDashElement> {

	private Filter<BootDashElement> baseFilter;

	public TreeAwareFilter(Filter<BootDashElement> baseFilter) {
		this.baseFilter = baseFilter;
	}

	@Override
	public boolean accept(BootDashElement e) {
		return baseAccepts(e) || baseAcceptsChild(e) || baseAcceptsParent(e);
	}

	private boolean baseAcceptsParent(BootDashElement e) {
		Object _p = e.getParent();
		if (_p instanceof BootDashElement) {
			BootDashElement p = (BootDashElement) _p;
			return baseAccepts(p) || baseAcceptsParent(p);
		}
		return false;
	}

	private boolean baseAcceptsChild(BootDashElement e) {
		for (BootDashElement c : e.getCurrentChildren()) {
			if (baseAccepts(c) || baseAcceptsChild(c)) {
				return true;
			}
		}
		return false;
	}

	private boolean baseAccepts(BootDashElement e) {
		return baseFilter.accept(e);
	}
}
