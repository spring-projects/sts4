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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Leo Dos Santos
 * @author Alex Boyko
 */
public class LiveBeansGroup<T extends AbstractLiveBeansModelElement> extends AbstractLiveBeansModelElement {

	private final String label;

	private final List<T> elements;

	public LiveBeansGroup(String label) {
		this(label, new ArrayList<T>());
	}
	
	public LiveBeansGroup(String label, List<T> elements) {
		this.label = label;
		this.elements = elements;
	}

	public void addElement(T bean) {
		elements.add(bean);
	}

	public List<T> getElements() {
		return elements;
	}

	public String getDisplayName() {
		return getLabel();
	}

	public String getLabel() {
		return label;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LiveBeansGroup) {
			LiveBeansGroup<?> other = (LiveBeansGroup<?>) obj;
			return Objects.equals(label, other.label)
					&& Objects.equals(attributes, other.attributes)
					&& Objects.equals(elements, other.elements);
		}
		return super.equals(obj);
	}
	
}
