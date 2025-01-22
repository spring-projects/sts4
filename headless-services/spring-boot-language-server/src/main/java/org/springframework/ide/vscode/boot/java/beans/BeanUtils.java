/*******************************************************************************
 * Copyright (c) 2018, 2025 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.beans;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.ide.vscode.commons.util.StringUtil;

import com.google.common.collect.ImmutableList;

public class BeanUtils {
	
	private static final String[] NAME_ATTRIBUTES = {"value", "name"};

	public static String getBeanNameFromComponentAnnotation(Annotation annotation, TypeDeclaration type) {
		Optional<Expression> attribute = ASTUtils.getAttribute(annotation, "value");
		if (attribute.isPresent()) {
			return ASTUtils.getExpressionValueAsString(attribute.get(), (a) -> {});
		}
		else {
			String beanName = type.getName().toString();
			return BeanUtils.getBeanNameFromType(beanName);
		}
	}
	
	public static Collection<String> getBeanNamesFromBeanAnnotation(Annotation node) {
		Collection<StringLiteral> beanNameNodes = getBeanNameLiterals(node);

		if (beanNameNodes != null && !beanNameNodes.isEmpty()) {
			return beanNameNodes.stream().map(nameNode -> ASTUtils.getLiteralValue(nameNode))
					.toList();
		}
		else {
			ASTNode parent = node.getParent();
			if (parent instanceof MethodDeclaration) {
				MethodDeclaration method = (MethodDeclaration) parent;
				return ImmutableList.of(method.getName().toString());
			}
			return ImmutableList.of();
		}
	}

	public static Collection<StringLiteral> getBeanNameLiterals(Annotation node) {
		ImmutableList.Builder<StringLiteral> literals = ImmutableList.builder();
		for (String attrib : NAME_ATTRIBUTES) {
			ASTUtils.getAttribute(node, attrib).ifPresent((valueExp) -> {
				literals.addAll(ASTUtils.getExpressionValueAsListOfLiterals(valueExp));
			});
		}
		return literals.build();
	}

	public static String getBeanNameFromType(String typeName) {
		if (StringUtil.hasText(typeName) && typeName.length() > 0 && Character.isUpperCase(typeName.charAt(0))) {
			// PT 162740382 - Special case: If more than one character is upper case, do not
			// convert bean name to starting lower case. only convert if name has one character
			// or the second character in the name is not upper case
			if (typeName.length() == 1 || !Character.isUpperCase(typeName.charAt(1))) {
				typeName = Character.toLowerCase(typeName.charAt(0)) + typeName.substring(1);
			}
		}
		return typeName;
	}

}
