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

public class InjectionPoint {
	
	private final String name;
	private final String type;
	private final Location location;
	
	public InjectionPoint(String name, String type, Location location) {
		super();
		this.name = name;
		this.type = type;
		this.location = location;
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

}
