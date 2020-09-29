/*******************************************************************************
 * Copyright (c) 2012, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.live.tree;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBean;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeanRelation;

/**
 * @author Leo Dos Santos
 * @author Alex Boyko
 */
public class InnerBeansViewerFilter extends ViewerFilter {

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof LiveBean) {
			LiveBean bean = (LiveBean) element;
			return !bean.isInnerBean();
		}
		else if (element instanceof LiveBeanRelation) {
			LiveBeanRelation bean = (LiveBeanRelation) element;
			return !bean.isInnerBean();
		}
		return true;
	}

}
