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

import org.eclipse.core.runtime.Assert;

public class LiveEnvModel implements Comparable<LiveEnvModel> {

	private final PropertySources propertySources;
	private ActiveProfiles profiles;

	public LiveEnvModel(ActiveProfiles profiles, PropertySources propertySources) {
		Assert.isNotNull(profiles);
		Assert.isNotNull(propertySources);
		this.profiles = profiles;
		this.propertySources = propertySources;
	}

	@Override
	public int compareTo(LiveEnvModel o) {
		return 0;
	}

	public ActiveProfiles getActiveProfiles() {
		return this.profiles;
	}

	public PropertySources getPropertySources() {
		return this.propertySources;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LiveEnvModel) {
			LiveEnvModel other = (LiveEnvModel) obj;
			// Should be enough to compare contexts only since this is close to raw JSON data
			return profiles.equals(other.profiles) && propertySources.equals(other.propertySources);
		}
		return super.equals(obj);
	}

}
