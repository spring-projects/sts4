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
package org.springframework.ide.eclipse.beans.ui.live.model;

/**
 * @author Leo Dos Santos
 * @author Alex Boyko
 */
public class LiveBeansContext extends LiveBeansGroup<LiveBean> {
	
	public static final String ATTR_CONTEXT_ID = "contextId";

	public static final String ATTR_CONTEXT = "context";

	public static final String ATTR_PARENT = "parent";

	public static final String ATTR_BEANS = "beans";

	private LiveBeansContext parent;

	public LiveBeansContext(String label) {
		super(label);
		attributes.put(ATTR_CONTEXT, label);
	}

	@Override
	public String getDisplayName() {
		return getLabel();
	}

	public LiveBeansContext getParent() {
		return parent;
	}

	public void setParent(LiveBeansContext parent) {
		this.parent = parent;
		attributes.put(ATTR_PARENT, parent.getLabel());
	}

}
