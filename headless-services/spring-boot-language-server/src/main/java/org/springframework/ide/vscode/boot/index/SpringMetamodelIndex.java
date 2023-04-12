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
package org.springframework.ide.vscode.boot.index;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.Location;
import org.springframework.ide.vscode.commons.protocol.spring.Bean;
import org.springframework.ide.vscode.commons.protocol.spring.InjectionPoint;
import org.springframework.ide.vscode.commons.protocol.spring.SpringModelService;

public class SpringMetamodelIndex implements SpringModelService {

	private List<Bean> beans;
	
	public SpringMetamodelIndex() {
		this.beans = new ArrayList<>();
	}
	
	@Override
	public CompletableFuture<List<Bean>> beans(String project) {
		return CompletableFuture.completedFuture(beans);
	}

	public Bean[] getBeans(String name) {
		return this.beans.stream().filter(bean -> bean.getName().equals(name)).collect(Collectors.toList()).toArray(new Bean[0]);
	}

	public void registerBean(String name, String type, Location location, InjectionPoint[] injectionPoints, String[] supertypes) {
		Bean bean = new Bean(name, type, location, injectionPoints, supertypes);
		this.beans.add(bean);
	}

}
