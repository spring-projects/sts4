/*******************************************************************************
 * Copyright (c) 2032 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.index;

import org.eclipse.lsp4j.Location;

public class Bean {
	
	private final String name;
	private final String type;
	private final Location location;
	private final InjectionPoint[] injectionPoints;

	public Bean(String name, String type, Location location, InjectionPoint[] injectionPoints) {
		this.name = name;
		this.type = type;
		this.location = location;

		this.injectionPoints = injectionPoints;
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
	
}
