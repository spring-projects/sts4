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
package org.springframework.ide.eclipse.beans.ui.live.model;

import java.util.Map;

/**
 * A wrapper around a {@link LiveBean} used by the
 * {@link LiveBeansTreeContentProvider} for displaying bean relationships
 * without nesting.
 * 
 * @author Leo Dos Santos
 * @author Alex Boyko
 */
public class LiveBeanRelation extends AbstractLiveBeansModelElement {

	private final LiveBean bean;

	private final boolean isDependency;

	public LiveBeanRelation(LiveBean bean) {
		this(bean, false);
	}

	public LiveBeanRelation(LiveBean bean, boolean isDependency) {
		this.bean = bean;
		this.isDependency = isDependency;
	}

	@Override
	public void addAttribute(String key, String value) {
		// no-op
	}

	@Override
	public Map<String, String> getAttributes() {
		return bean.getAttributes();
	}

	public String getDisplayName() {
		return bean.getDisplayName();
	}

	public boolean isDependency() {
		return isDependency;
	}

	public boolean isInnerBean() {
		return bean.isInnerBean();
	}

	public LiveBean getBean() {
		return this.bean;
	}
}
