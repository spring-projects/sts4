/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.protocol.spring;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.lsp4j.Location;

import com.google.gson.Gson;

public class Bean {
	
	private final String name;
	private final String type;
	private final Location location;
	private final InjectionPoint[] injectionPoints;
	private final Set<String> supertypes;

	public Bean(String name, String type, Location location, InjectionPoint[] injectionPoints, String[] supertypes) {
		this.name = name;
		this.type = type;
		this.location = location;

		if (injectionPoints != null && injectionPoints.length == 0) {
			this.injectionPoints = DefaultValues.EMPTY_INJECTION_POINTS;
		}
		else {
			this.injectionPoints = injectionPoints;
		}

		this.supertypes = new HashSet<>(Arrays.asList(supertypes));
	}
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public InjectionPoint[] getInjectionPoints() {
		return injectionPoints;
	}

	public boolean isTypeCompatibleWith(String type) {
		return supertypes.contains(type);
	}
	
	@Override
	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
	
}
