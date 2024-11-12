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
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.springframework.ide.vscode.boot.index.SpringMetamodelIndex;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.handlers.ReferenceProvider;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.protocol.spring.Bean;

/**
 * @author Martin Lippert
 */
public class ProfileReferencesProvider implements ReferenceProvider {

	private final SpringMetamodelIndex springIndex;

	public ProfileReferencesProvider(SpringMetamodelIndex springIndex) {
		this.springIndex = springIndex;
	}

	@Override
	public List<? extends Location> provideReferences(CancelChecker cancelToken, IJavaProject project, ASTNode node, Annotation annotation, ITypeBinding type, int offset) {

		cancelToken.checkCanceled();

		try {
			// case: @Value("prefix<*>")
			if (node instanceof StringLiteral && node.getParent() instanceof Annotation) {
				if (node.toString().startsWith("\"") && node.toString().endsWith("\"")) {
					return provideReferences(project, ((StringLiteral) node).getLiteralValue());
				}
			}
			// case: @Value(value="prefix<*>")
			else if (node instanceof StringLiteral && node.getParent() instanceof MemberValuePair
					&& "value".equals(((MemberValuePair)node.getParent()).getName().toString())) {
				if (node.toString().startsWith("\"") && node.toString().endsWith("\"")) {
					return provideReferences(project, ((StringLiteral) node).getLiteralValue());
				}
			}
			// case: @Qualifier({"prefix<*>"})
			else if (node instanceof StringLiteral && node.getParent() instanceof ArrayInitializer) {
				if (node.toString().startsWith("\"") && node.toString().endsWith("\"")) {
					return provideReferences(project, ((StringLiteral) node).getLiteralValue());
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	private List<? extends Location> provideReferences(IJavaProject project, String value) {
		Bean[] beans = this.springIndex.getBeans();
		
		Stream<Location> profileLocationFromBeans = Arrays.stream(beans)
				// annotations from beans themselves
				.flatMap(bean -> Arrays.stream(bean.getAnnotations()))
				.filter(annotation -> Annotations.PROFILE.equals(annotation.getAnnotationType()))
				.filter(annotation -> annotation.getAttributes() != null && annotation.getAttributes().containsKey("value"))
				.flatMap(annotation -> Arrays.stream(annotation.getAttributes().get("value")))
				.filter(attribute -> attribute.getName().equals(value))
				.map(attribute -> attribute.getLocation());
		
		Stream<Location> profileLocationsFromInjectionPoints = Arrays.stream(beans)
				// annotations from injection points
				.filter(bean -> bean.getInjectionPoints() != null)
				.flatMap(bean -> Arrays.stream(bean.getInjectionPoints()))
				.filter(injectionPoint -> injectionPoint.getAnnotations() != null)
				.flatMap(injectionPoint -> Arrays.stream(injectionPoint.getAnnotations()))
				.filter(annotation -> Annotations.PROFILE.equals(annotation.getAnnotationType()))
				.filter(annotation -> annotation.getAttributes() != null && annotation.getAttributes().containsKey("value"))
				.flatMap(annotation -> Arrays.stream(annotation.getAttributes().get("value")))
				.filter(attribute -> attribute.getName().equals(value))
				.map(attribute -> attribute.getLocation());
		
		return Stream.concat(profileLocationFromBeans, profileLocationsFromInjectionPoints).toList();
	}

}
