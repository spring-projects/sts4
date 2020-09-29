/*******************************************************************************
 * Copyright (c) 2012, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.live.tree;

import java.util.List;

import org.springframework.ide.eclipse.beans.ui.live.model.LiveBean;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeansModel;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeansResource;

/**
 * Beans grouped by contexts
 * 
 * @author Alex Boyko
 *
 */
public final class ContextGroupedBeansContentProvider extends AbstractLiveBeansTreeContentProvider {
	
	public static final ContextGroupedBeansContentProvider INSTANCE = new ContextGroupedBeansContentProvider();
	
	private ContextGroupedBeansContentProvider() {}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof LiveBeansModel) {
			LiveBeansModel model = (LiveBeansModel) inputElement;
			return model.getBeansByContext().toArray();
		}
		return new Object[0];
	}

	@Override
	protected List<Object> getBeanChildren(LiveBean bean) {
		List<Object> children = super.getBeanChildren(bean);
		String resource = bean.getResource();
		if (resource != null && !resource.isEmpty()) {
			// Do not add the bean as a element to the resource, otherwise
			// it will appear as a child and the tree will have an infinite
			// deep livebean. However, we still need the type lookup in the bean
			// to resolve the types in the resource
			children.add(1, new LiveBeansResource(resource, bean.getTypeLookup()));
		}
		return children;
	}

}
