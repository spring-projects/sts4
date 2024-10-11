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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
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
		
		boolean isOnType = isAnnotationOnType(node);
		boolean isOnBeanMethod = isOnBeanMethod(node);

		Stream<String> candidates = findAllQualifiers(beans);
		if (!isOnType && !isOnBeanMethod) {
			candidates = Stream.concat(candidates, Arrays.stream(beans).map(bean -> bean.getName()));
		}
		
		return candidates
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

	private boolean isAnnotationOnType(ASTNode node) {
		while (node != null) {
			if (node instanceof MethodDeclaration) {
				return false;
			}
			else if (node instanceof TypeDeclaration) {
				return true;
			}
			
			node = node.getParent();
		}
		
		return false;
	}
	
	private boolean isOnBeanMethod(ASTNode node) {
		Annotation annotation = findAnnotation(node);
		if (annotation != null) {
			if (annotation.getParent() instanceof MethodDeclaration) {
				MethodDeclaration method = (MethodDeclaration) annotation.getParent();
				return hasBeanAnnotation(method);
			}
		}
		return false;
	}

	private boolean hasBeanAnnotation(MethodDeclaration method) {
		List<?> modifiers = method.modifiers();
		for (Object modifier : modifiers) {
			if (modifier instanceof Annotation) {
				Annotation annotation = (Annotation) modifier;
				IAnnotationBinding annotationBinding = annotation.resolveAnnotationBinding();
				String type = annotationBinding.getAnnotationType().getBinaryName();

				if (type != null && Annotations.BEAN.equals(type)) {
					return true;
				}
			}
		}
		
		return false;
	}

	private Annotation findAnnotation(ASTNode node) {
		while (node != null) {
			if (node instanceof Annotation) {
				return (Annotation) node;
			}
			node = node.getParent();
		}

		return null;
	}

}
