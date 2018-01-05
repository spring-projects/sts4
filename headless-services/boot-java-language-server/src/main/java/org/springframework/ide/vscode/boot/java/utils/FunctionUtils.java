/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.springframework.ide.vscode.commons.languageserver.util.DocumentRegion;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

public class FunctionUtils {

	public static final String FUNCTION_FUNCTION_TYPE = Function.class.getName();
	public static final String FUNCTION_CONSUMER_TYPE = Consumer.class.getName();
	public static final String FUNCTION_SUPPLIER_TYPE = Supplier.class.getName();

	public static Tuple3<String, String, DocumentRegion> getFunctionBean(TypeDeclaration typeDeclaration, TextDocument doc) {
		ITypeBinding resolvedType = typeDeclaration.resolveBinding();
		if (resolvedType != null) {
			return getFunctionBean(typeDeclaration, doc, resolvedType);
		}
		else {
			return null;
		}
	}

	private static Tuple3<String, String, DocumentRegion> getFunctionBean(TypeDeclaration typeDeclaration, TextDocument doc,
			ITypeBinding resolvedType) {

		ITypeBinding[] interfaces = resolvedType.getInterfaces();
		for (ITypeBinding resolvedInterface : interfaces) {
			String simplifiedType = null;
			if (resolvedInterface.isParameterizedType()) {
				simplifiedType = resolvedInterface.getBinaryName();
			}
			else {
				simplifiedType = resolvedType.getQualifiedName();
			}

			if (FUNCTION_FUNCTION_TYPE.equals(simplifiedType) || FUNCTION_CONSUMER_TYPE.equals(simplifiedType)
					|| FUNCTION_SUPPLIER_TYPE.equals(simplifiedType)) {
				String beanName = getBeanName(typeDeclaration);
				String beanType = resolvedInterface.getName();
				DocumentRegion region = ASTUtils.nodeRegion(doc, typeDeclaration.getName());

				return Tuples.of(beanName, beanType, region);
			}
			else {
				Tuple3<String, String, DocumentRegion> result = getFunctionBean(typeDeclaration, doc, resolvedInterface);
				if (result != null) {
					return result;
				}
			}
		}

		ITypeBinding superclass = resolvedType.getSuperclass();
		if (superclass != null) {
			return getFunctionBean(typeDeclaration, doc, superclass);
		}
		else {
			return null;
		}
	}

	protected static String getBeanName(TypeDeclaration typeDeclaration) {
		String beanName = typeDeclaration.getName().toString();
		if (beanName.length() > 0 && Character.isUpperCase(beanName.charAt(0))) {
			beanName = Character.toLowerCase(beanName.charAt(0)) + beanName.substring(1);
		}
		return beanName;
	}

}
