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

import java.util.List;

public class Profiles1x extends ActiveProfiles {

	public Profiles1x(List<Profile> profiles) {
		super(profiles);
	}

	@Override
	public String getDisplayName() {
		return "Profiles";
	}
}
