/*******************************************************************************
 * Copyright (c) 2023, 2024 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.protocol.spring;

import java.util.Set;

import org.eclipse.lsp4j.Location;

import com.google.gson.Gson;

public class Bean {
	
	private final String name;
	private final String type;
	private final Location location;
	private final InjectionPoint[] injectionPoints;
	private final Set<String> supertypes;
	private final AnnotationMetadata[] annotations;
	private final boolean isConfiguration;

	public Bean(
			String name,
			String type,
			Location location,
			InjectionPoint[] injectionPoints,
			Set<String> supertypes,
			AnnotationMetadata[] annotations, boolean isConfiguration) {

		this.name = name;
		this.type = type;
		this.location = location;
		this.isConfiguration = isConfiguration;
		
		if (injectionPoints != null && injectionPoints.length == 0) {
			this.injectionPoints = DefaultValues.EMPTY_INJECTION_POINTS;
		}
		else {
			this.injectionPoints = injectionPoints;
		}
		
		if (supertypes != null && supertypes.size() == 0) {
			this.supertypes = DefaultValues.EMPTY_SUPERTYPES;
		}
		else if (supertypes != null && supertypes.size() == 1 && supertypes.contains("java.lang.Object")) {
			this.supertypes = DefaultValues.OBJECT_SUPERTYPE;
		}
		else {
			this.supertypes = supertypes;
		}

		if (annotations != null && annotations.length == 0) {
			this.annotations = DefaultValues.EMPTY_ANNOTATIONS;
		}
		else {
			this.annotations = annotations;
		}
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
		return type != null && ((this.type != null && this.type.equals(type)) || (supertypes.contains(type)));
	}
	
	public AnnotationMetadata[] getAnnotations() {
		return annotations;
	}
	
	public boolean isConfiguration() {
		return isConfiguration;
	}
	
	@Override
	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
	
}
