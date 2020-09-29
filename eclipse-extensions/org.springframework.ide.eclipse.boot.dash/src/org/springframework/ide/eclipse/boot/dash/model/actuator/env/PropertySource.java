/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model.actuator.env;

import java.util.Collections;
import java.util.List;

import org.springframework.ide.eclipse.beans.ui.live.model.DisplayName;

import com.google.common.collect.ImmutableList;

public class PropertySource implements DisplayName {

	private final String name;
	private List<Property> properties = Collections.emptyList();

	public PropertySource(String name) {
		this.name = name;
	}

	@Override
	public String getDisplayName() {
		return name;
	}

	public List<Property> getProperties() {
		return ImmutableList.copyOf(properties);
	}

	public void add(List<Property> properties) {
		this.properties = properties != null ? properties : Collections.emptyList();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PropertySource other = (PropertySource) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		return true;
	}

}
