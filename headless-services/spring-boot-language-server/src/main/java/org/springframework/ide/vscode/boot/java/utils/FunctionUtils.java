/*******************************************************************************
 * Copyright (c) 2018, 2022 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.openrewrite.java.tree.J.ClassDeclaration;
import org.openrewrite.java.tree.J.Modifier;
import org.openrewrite.java.tree.JavaType.FullyQualified;
import org.openrewrite.java.tree.JavaType.FullyQualified.Kind;
import org.springframework.ide.vscode.boot.java.beans.BeanUtils;
import org.springframework.ide.vscode.commons.util.text.DocumentRegion;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * @author Martin Lippert
 */
public class FunctionUtils {

	public static final String FUNCTION_FUNCTION_TYPE = Function.class.getName();
	public static final String FUNCTION_CONSUMER_TYPE = Consumer.class.getName();
	public static final String FUNCTION_SUPPLIER_TYPE = Supplier.class.getName();

	public static Tuple3<String, String, DocumentRegion> getFunctionBean(ClassDeclaration typeDeclaration, TextDocument doc) {
		FullyQualified resolvedType = typeDeclaration.getType();

		if (resolvedType != null && resolvedType.getKind() == Kind.Class  && !isAbstractClass(typeDeclaration)) {
			return getFunctionBean(typeDeclaration, doc, resolvedType);
		}
		else {
			return null;
		}
	}

	private static Tuple3<String, String, DocumentRegion> getFunctionBean(ClassDeclaration typeDeclaration, TextDocument doc,
			FullyQualified resolvedType) {
		for (FullyQualified resolvedInterface : resolvedType.getInterfaces()) {
			String simplifiedType = resolvedInterface.getFullyQualifiedName();

			if (FUNCTION_FUNCTION_TYPE.equals(simplifiedType) || FUNCTION_CONSUMER_TYPE.equals(simplifiedType)
					|| FUNCTION_SUPPLIER_TYPE.equals(simplifiedType)) {
				String beanName = getBeanName(typeDeclaration);
				String beanType = resolvedInterface.toString();
				DocumentRegion region = ORAstUtils.nodeRegion(doc, typeDeclaration.getName());

				return Tuples.of(beanName, beanType, region);
			}
			else {
				Tuple3<String, String, DocumentRegion> result = getFunctionBean(typeDeclaration, doc, resolvedInterface);
				if (result != null) {
					return result;
				}
			}
		}

		FullyQualified superclass = resolvedType.getSupertype();
		if (superclass != null) {
			return getFunctionBean(typeDeclaration, doc, superclass);
		}
		else {
			return null;
		}
	}

	protected static String getBeanName(ClassDeclaration typeDeclaration) {
		String beanName = typeDeclaration.getSimpleName();
		return BeanUtils.getBeanNameFromType(beanName);
	}

	protected static boolean isAbstractClass(ClassDeclaration typeDeclaration) {
		return Modifier.hasModifier(typeDeclaration.getModifiers(), Modifier.Type.Abstract);
	}

}
