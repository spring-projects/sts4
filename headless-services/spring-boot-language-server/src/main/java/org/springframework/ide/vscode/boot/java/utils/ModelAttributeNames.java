/*******************************************************************************
 * Copyright (c) 2026 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * Determines whether the explicit name of a {@code @ModelAttribute} method parameter is the
 * default name Spring derives from the parameter type anyway. Shared by the reconciler and the
 * quickfix, which re-checks the name before removing it.
 *
 * @author Nihal Kumar Sharma
 */
public class ModelAttributeNames {

	// Spring pluralizes the default name for collections and appends a suffix for reactive types
	private static final Set<String> COLLECTION_AND_REACTIVE_TYPES = Set.of(
			"java.util.Collection",
			"java.util.concurrent.CompletionStage",
			"java.util.concurrent.Flow.Publisher",
			"org.reactivestreams.Publisher");

	/**
	 * Returns the explicit name of the given annotation if it is Spring's {@code @ModelAttribute} on a
	 * method parameter and the name is the default Spring derives from the parameter type anyway, or
	 * {@code null} otherwise, including when the annotation or the parameter type can't be resolved.
	 */
	public static StringLiteral findRedundantName(Annotation annotation) {
		ITypeBinding annotationType = annotation.resolveTypeBinding();
		if (annotationType == null || annotationType.isRecovered() || !Annotations.MODEL_ATTRIBUTE.equals(annotationType.getQualifiedName())) {
			return null;
		}
		if (!(annotation.getParent() instanceof SingleVariableDeclaration parameter)
				|| !(parameter.getParent() instanceof MethodDeclaration method) || method.isConstructor()) {
			return null;
		}
		return findName(annotation) instanceof StringLiteral literal && literal.getLiteralValue().equals(getDefaultName(parameter.resolveBinding()))
				? literal
				: null;
	}

	/**
	 * Returns the explicit name of the given annotation, i.e. its single member value or its
	 * {@code value} or {@code name} attribute, or {@code null} if there is none or both are set.
	 */
	private static Expression findName(Annotation annotation) {
		Optional<Expression> value = ASTUtils.getAttribute(annotation, "value");
		Optional<Expression> name = ASTUtils.getAttribute(annotation, "name");
		return value.isPresent() && name.isPresent() ? null : value.or(() -> name).orElse(null);
	}

	/**
	 * Returns the name Spring uses for a {@code @ModelAttribute} parameter that doesn't declare one
	 * (see {@code ModelFactory.getNameForParameter}), or {@code null} if it can't be derived reliably.
	 * Only plain class types are supported: arrays, collections and reactive types get different
	 * names, and generic types as well as types with unresolved parts in their hierarchy are skipped.
	 */
	private static String getDefaultName(IVariableBinding parameter) {
		ITypeBinding type = parameter != null ? parameter.getType() : null;
		if (type == null || !(type.isClass() || type.isInterface() || type.isEnum() || type.isRecord())
				|| type.isParameterizedType() || type.isRawType() || type.getBinaryName() == null) {
			return null;
		}

		for (Iterator<ITypeBinding> itr = ASTUtils.getHierarchyTypesBreadthFirstIterator(type); itr.hasNext();) {
			ITypeBinding hierarchyType = itr.next();
			if (hierarchyType.isRecovered() || COLLECTION_AND_REACTIVE_TYPES.contains(hierarchyType.getErasure().getQualifiedName())) {
				return null;
			}
		}

		// same as ClassUtils.getShortNameAsProperty(Class), which Spring uses for the default name
		String shortName = ClassUtils.getShortName(type.getBinaryName());
		return StringUtils.uncapitalizeAsProperty(shortName.substring(shortName.lastIndexOf('.') + 1));
	}

}
