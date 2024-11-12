/*******************************************************************************
 * Copyright (c) 2024 Broadcom
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.protocol.spring;

import java.util.Objects;

import org.eclipse.lsp4j.Location;

/**
 * @author Martin Lippert
 */
public class AnnotationAttributeValue {
	
	private final String name;
	private final Location location;
	
	public AnnotationAttributeValue(String name, Location location) {
		this.name = name;
		this.location = location;
	}
	
	public String getName() {
		return name;
	}
	
	public Location getLocation() {
		return location;
	}

	@Override
	public int hashCode() {
		return Objects.hash(location, name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AnnotationAttributeValue other = (AnnotationAttributeValue) obj;
		return Objects.equals(location, other.location) && Objects.equals(name, other.name);
	}

}
