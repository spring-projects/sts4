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
package org.springframework.ide.vscode.boot.java.beans;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ide.vscode.boot.index.SpringMetamodelIndex;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationAttributeCompletionProvider;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.protocol.spring.Bean;

/**
 * @author Martin Lippert
 */
public class ResourceCompletionProvider implements AnnotationAttributeCompletionProvider {
	
	private final SpringMetamodelIndex springIndex;

	public ResourceCompletionProvider(SpringMetamodelIndex springIndex) {
		this.springIndex = springIndex;
	}
	
	@Override
	public Map<String, String> getCompletionCandidates(IJavaProject project) {

		Bean[] beans = this.springIndex.getBeansOfProject(project.getElementName());

		return Arrays.stream(beans).map(bean -> bean.getName())
				.distinct()
				.collect(Collectors.toMap(key -> key, value -> value, (u, v) -> u, LinkedHashMap::new));
	}

}
