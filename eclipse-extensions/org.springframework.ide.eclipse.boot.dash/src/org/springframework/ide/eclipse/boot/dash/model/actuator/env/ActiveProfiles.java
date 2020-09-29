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

public class ActiveProfiles implements DisplayName {

	private final List<Profile> profiles;

	public ActiveProfiles(List<Profile> profiles) {
		this.profiles = profiles != null ? profiles : Collections.emptyList();
	}

	@Override
	public String getDisplayName() {
		return "Active Profiles";
	}

	public List<Profile> getProfiles() {
		return ImmutableList.copyOf(this.profiles);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((profiles == null) ? 0 : profiles.hashCode());
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
		ActiveProfiles other = (ActiveProfiles) obj;
		if (profiles == null) {
			if (other.profiles != null)
				return false;
		} else if (!profiles.equals(other.profiles))
			return false;
		return true;
	}

}
