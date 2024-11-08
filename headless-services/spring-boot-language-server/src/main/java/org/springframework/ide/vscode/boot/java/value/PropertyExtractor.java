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
package org.springframework.ide.vscode.boot.java.value;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.lsp4j.LocationLink;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.commons.java.IJavaProject;

public class PropertyExtractor {
	
	private static final String PARAM_VALUE = "value";
	private static final String PARAM_NAME = "name";
	private static final String PARAM_PREFIX = "prefix";

	private static interface PropertyKeyExtractor {
		String extract(Annotation annotation, MemberValuePair memberValuePair, StringLiteral stringLiteral);
	}

	private final Map<String, PropertyKeyExtractor> propertyKeyExtractors;
	
	public PropertyExtractor() {
		propertyKeyExtractors = Map.of(

				Annotations.VALUE, (annotation, memberValuePair, stringLiteral) -> {
					if (annotation.isSingleMemberAnnotation()) {
						return extractPropertyKey(stringLiteral.getLiteralValue());
					} else if (annotation.isNormalAnnotation() && PARAM_VALUE.equals(memberValuePair.getName().getIdentifier())) {
						return extractPropertyKey(stringLiteral.getLiteralValue());
					}
					return null;
				},

				Annotations.CONDITIONAL_ON_PROPERTY, (annotation, memberValuePair, stringLiteral) -> {
					if (annotation.isSingleMemberAnnotation()) {
						return stringLiteral.getLiteralValue();
					} else if (annotation.isNormalAnnotation()) {
						switch (memberValuePair.getName().getIdentifier()) {
						case PARAM_VALUE:
							return stringLiteral.getLiteralValue();
						case PARAM_NAME:
							String prefix = extractAnnotationParameter(annotation, PARAM_PREFIX);
							String name = stringLiteral.getLiteralValue();
							return prefix != null && !prefix.isBlank() ? prefix + "." + name : name;
						case PARAM_PREFIX:
							name = extractAnnotationParameter(annotation, PARAM_NAME);
							prefix = stringLiteral.getLiteralValue();
							return prefix != null && !prefix.isBlank() ? prefix + "." + name : name;
						}
					}
					return null;
				}
		);
	}
	
	public String extractPropertyKey(StringLiteral valueNode) {
		
		ASTNode parent = valueNode.getParent();

		if (parent instanceof Annotation) {
			
			Annotation a = (Annotation) parent;
			IAnnotationBinding binding = a.resolveAnnotationBinding();
			if (binding != null && binding.getAnnotationType() != null) {
				PropertyKeyExtractor propertyExtractor = propertyKeyExtractors.get(binding.getAnnotationType().getQualifiedName());
				if (propertyExtractor != null) {
					return propertyExtractor.extract(a, null, valueNode);
				}
			}

		} else if (parent instanceof MemberValuePair && parent.getParent() instanceof Annotation) {

			MemberValuePair pair = (MemberValuePair) parent;
			Annotation a = (Annotation) parent.getParent();
			IAnnotationBinding binding = a.resolveAnnotationBinding();
			if (binding != null && binding.getAnnotationType() != null) {
				PropertyKeyExtractor propertyExtractor = propertyKeyExtractors.get(binding.getAnnotationType().getQualifiedName());
				if (propertyExtractor != null) {
					return propertyExtractor.extract(a, pair, valueNode);
				}
			}
		}
		
		return null;
	}
	
	private String extractPropertyKey(String s) {
		if (s.length() > 3 && (s.startsWith("${") || s.startsWith("#{")) && s.endsWith("}")) {
			return s.substring(2, s.length() - 1);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static String extractAnnotationParameter(Annotation a, String param) {
		Expression value = null;
		if (a.isSingleMemberAnnotation() && PARAM_VALUE.equals(param)) {
			value = ((SingleMemberAnnotation) a).getValue();
		} else if (a.isNormalAnnotation()) {
			for (MemberValuePair pair : (List<MemberValuePair>) ((NormalAnnotation) a).values()) {
				if (param.equals(pair.getName().getIdentifier())) {
					value = pair.getValue();
					break;
				}
			}
		}
		if (value instanceof StringLiteral) {
			return ((StringLiteral) value).getLiteralValue();
		}
		return null;
	}

}
