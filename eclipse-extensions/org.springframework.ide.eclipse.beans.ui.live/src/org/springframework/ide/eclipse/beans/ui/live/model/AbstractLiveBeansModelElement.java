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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * @author Leo Dos Santos
 * @author Alex Boyko
 */
public abstract class AbstractLiveBeansModelElement implements IAdaptable, DisplayName {

	protected final Map<String, String> attributes;

	public AbstractLiveBeansModelElement() {
		this.attributes = new HashMap<String, String>();
	}

	public void addAttribute(String key, String value) {
		attributes.put(key, value);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			return new LiveBeanPropertySource(this);
		}
		return null;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

}
