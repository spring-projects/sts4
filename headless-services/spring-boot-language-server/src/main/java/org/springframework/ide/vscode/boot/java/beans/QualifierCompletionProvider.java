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
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.springframework.ide.vscode.boot.index.SpringMetamodelIndex;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationAttributeCompletionProvider;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.protocol.spring.Bean;

/**
 * @author Martin Lippert
 */
public class QualifierCompletionProvider implements AnnotationAttributeCompletionProvider {
	
	private final SpringMetamodelIndex springIndex;

	public QualifierCompletionProvider(SpringMetamodelIndex springIndex) {
		this.springIndex = springIndex;
	}
	
	@Override
	public Map<String, String> getCompletionCandidates(IJavaProject project, ASTNode node) {

		Bean[] beans = this.springIndex.getBeansOfProject(project.getElementName());

		return Stream.concat(
				findAllQualifiers(beans),
				Arrays.stream(beans).map(bean -> bean.getName()))
				.distinct()
				.collect(Collectors.toMap(key -> key, value -> value, (u, v) -> u, LinkedHashMap::new));
	}

	private Stream<String> findAllQualifiers(Bean[] beans) {

		Stream<String> qualifiersFromBeans = Arrays.stream(beans)
				// annotations from beans themselves
				.flatMap(bean -> Arrays.stream(bean.getAnnotations()))
				.filter(annotation -> Annotations.QUALIFIER.equals(annotation.getAnnotationType()))
				.filter(annotation -> annotation.getAttributes() != null && annotation.getAttributes().containsKey("value") && annotation.getAttributes().get("value").length == 1)
				.map(annotation -> annotation.getAttributes().get("value")[0]);
		
		Stream<String> qualifiersFromInjectionPoints = Arrays.stream(beans)
				// annotations from beans themselves
				.filter(bean -> bean.getInjectionPoints() != null)
				.flatMap(bean -> Arrays.stream(bean.getInjectionPoints()))
				.filter(injectionPoint -> injectionPoint.getAnnotations() != null)
				.flatMap(injectionPoint -> Arrays.stream(injectionPoint.getAnnotations()))
				.filter(annotation -> Annotations.QUALIFIER.equals(annotation.getAnnotationType()))
				.filter(annotation -> annotation.getAttributes() != null && annotation.getAttributes().containsKey("value") && annotation.getAttributes().get("value").length == 1)
				.map(annotation -> annotation.getAttributes().get("value")[0]);
		
		return Stream.concat(qualifiersFromBeans, qualifiersFromInjectionPoints);
	}

}
