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

public class PropertySources implements DisplayName {

	private final List<PropertySource> propertySources;

	public PropertySources(List<PropertySource> propertySources) {
		this.propertySources = propertySources != null ? propertySources : Collections.emptyList();
	}

	public List<PropertySource> getPropertySources() {
		return ImmutableList.copyOf(propertySources);
	}

	@Override
	public String getDisplayName() {
		return "Property Sources";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((propertySources == null) ? 0 : propertySources.hashCode());
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
		PropertySources other = (PropertySources) obj;
		if (propertySources == null) {
			if (other.propertySources != null)
				return false;
		} else if (!propertySources.equals(other.propertySources))
			return false;
		return true;
	}

}
