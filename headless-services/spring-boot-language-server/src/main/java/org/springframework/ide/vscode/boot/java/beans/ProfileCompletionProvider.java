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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.springframework.ide.vscode.boot.index.SpringMetamodelIndex;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationAttributeCompletionProvider;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationAttributeProposal;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.protocol.spring.Bean;

/**
 * @author Martin Lippert
 */
public class ProfileCompletionProvider implements AnnotationAttributeCompletionProvider {
	
	private final SpringMetamodelIndex springIndex;

	public ProfileCompletionProvider(SpringMetamodelIndex springIndex) {
		this.springIndex = springIndex;
	}
	
	@Override
	public List<AnnotationAttributeProposal> getCompletionCandidates(IJavaProject project, ASTNode node) {

		Bean[] beans = this.springIndex.getBeansOfProject(project.getElementName());

		return findAllProfiles(beans)
				.distinct()
				.map(profile -> new AnnotationAttributeProposal(profile))
				.collect(Collectors.toList());
	}

	private Stream<String> findAllProfiles(Bean[] beans) {

		Stream<String> profilesFromBeans = Arrays.stream(beans)
				// annotations from beans themselves
				.flatMap(bean -> Arrays.stream(bean.getAnnotations()))
				.filter(annotation -> Annotations.PROFILE.equals(annotation.getAnnotationType()))
				.filter(annotation -> annotation.getAttributes() != null && annotation.getAttributes().containsKey("value"))
				.flatMap(annotation -> Arrays.stream(annotation.getAttributes().get("value")))
				.map(attributeValue -> attributeValue.getName());
		
		Stream<String> profilesFromInjectionPoints = Arrays.stream(beans)
				// annotations from beans themselves
				.filter(bean -> bean.getInjectionPoints() != null)
				.flatMap(bean -> Arrays.stream(bean.getInjectionPoints()))
				.filter(injectionPoint -> injectionPoint.getAnnotations() != null)
				.flatMap(injectionPoint -> Arrays.stream(injectionPoint.getAnnotations()))
				.filter(annotation -> Annotations.PROFILE.equals(annotation.getAnnotationType()))
				.filter(annotation -> annotation.getAttributes() != null && annotation.getAttributes().containsKey("value"))
				.flatMap(annotation -> Arrays.stream(annotation.getAttributes().get("value")))
				.map(attributeValue -> attributeValue.getName());
		
		return Stream.concat(profilesFromBeans, profilesFromInjectionPoints);
	}

}
