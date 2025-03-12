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
package org.springframework.ide.vscode.boot.java.utils;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class FunctionUtils {

	public static final String FUNCTION_FUNCTION_TYPE = Function.class.getName();
	public static final String FUNCTION_CONSUMER_TYPE = Consumer.class.getName();
	public static final String FUNCTION_SUPPLIER_TYPE = Supplier.class.getName();
	
	public static final Set<String> FUNCTION_TYPES = Set.of(FUNCTION_FUNCTION_TYPE, FUNCTION_CONSUMER_TYPE, FUNCTION_SUPPLIER_TYPE);

	public static ITypeBinding getFunctionBean(TypeDeclaration typeDeclaration, TextDocument doc) {
		ITypeBinding resolvedType = typeDeclaration.resolveBinding();

		if (resolvedType != null && !resolvedType.isInterface() && !ASTUtils.isAbstractClass(typeDeclaration)) {
			return ASTUtils.findInTypeHierarchy(resolvedType, FUNCTION_TYPES);
		}
		else {
			return null;
		}
	}

}
