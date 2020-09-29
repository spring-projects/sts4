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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBean;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeanRelation;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeanType;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeansContext;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeansGroup;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeansResource;

/**
 * A content provider for the Live Beans tree display
 * 
 * @author Leo Dos Santos
 * @author Alex Boyko
 */
public abstract class AbstractLiveBeansTreeContentProvider implements ITreeContentProvider {

	@SuppressWarnings("unchecked")
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof LiveBeansGroup) {
			LiveBeansGroup<?> group = (LiveBeansGroup<?>) parentElement;
			if (group instanceof LiveBeansContext || group instanceof LiveBeansResource) {
				List<LiveBean> sortedBeans = new ArrayList<>(((LiveBeansGroup<LiveBean>)group).getElements());
				sortedBeans.sort((a, b) -> a.getDisplayName().compareToIgnoreCase(b.getDisplayName()));
				return sortedBeans.toArray(new LiveBean[sortedBeans.size()]);
			} else {
				return group.getElements().toArray();
			}
		}
		else if (parentElement instanceof LiveBean) {
			LiveBean bean = (LiveBean) parentElement;			
			return getBeanChildren(bean).toArray();
		}
		return null;
	}
	
	protected List<Object> getBeanChildren(LiveBean bean) {
		List<Object> children = new ArrayList<>();
		
		children.add(new LiveBeanType(bean));
				
		children.add(new LiveBeansGroup<>("Dependencies", bean.getDependencies()
			.stream()
			.map(b -> new LiveBeanRelation(b, true))
			.collect(Collectors.toList())));
					
		children.add(new LiveBeansGroup<>("Injected Into", bean.getInjectedInto()
			.stream()
			.map(b -> new LiveBeanRelation(b))
			.collect(Collectors.toList())));
		
		return children;
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof LiveBeansGroup<?>) {
			return !((LiveBeansGroup<?>)element).getElements().isEmpty();
		} else if (element instanceof LiveBean) {
			return true;
		}
		return false;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}

}
