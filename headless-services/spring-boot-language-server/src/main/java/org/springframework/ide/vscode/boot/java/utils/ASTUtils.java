/*******************************************************************************
 * Copyright (c) 2017, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.openrewrite.java.tree.J.Modifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.util.CollectorUtil;
import org.springframework.ide.vscode.commons.util.text.DocumentRegion;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

public class ASTUtils {

	private static final Logger log = LoggerFactory.getLogger(ASTUtils.class);

	public static DocumentRegion stringRegion(TextDocument doc, StringLiteral node) {
		DocumentRegion nodeRegion = nodeRegion(doc, node);
		if (nodeRegion.startsWith("\"")) {
			nodeRegion = nodeRegion.subSequence(1);
		}
		if (nodeRegion.endsWith("\"")) {
			nodeRegion = nodeRegion.subSequence(0, nodeRegion.getLength()-1);
		}
		return nodeRegion;
	}


	public static String getExpressionValueAsString(Expression exp, Consumer<ITypeBinding> dependencies) {
		if (exp instanceof StringLiteral) {
			return getLiteralValue((StringLiteral) exp);
		} else if (exp instanceof Name) {
			IBinding binding = ((Name) exp).resolveBinding();
			if (binding != null && binding.getKind() == IBinding.VARIABLE) {
				IVariableBinding varBinding = (IVariableBinding) binding;
				ITypeBinding klass = varBinding.getDeclaringClass();
				if (klass!=null) {
					dependencies.accept(klass);
					
					
				}
				Object constValue = varBinding.getConstantValue();
				if (constValue != null) {
					return constValue.toString();
				}
			}
			if (exp instanceof QualifiedName) {
				return getExpressionValueAsString(((QualifiedName) exp).getName(), dependencies);
			}
			else if (exp instanceof SimpleName) {
				return ((SimpleName) exp).getIdentifier();
			}
			else {
				return null;
			}
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static String[] getExpressionValueAsArray(Expression exp, Consumer<ITypeBinding> dependencies) {
		if (exp instanceof ArrayInitializer) {
			ArrayInitializer array = (ArrayInitializer) exp;
			return ((List<Expression>) array.expressions()).stream().map(e -> getExpressionValueAsString(e, dependencies))
					.filter(Objects::nonNull).toArray(String[]::new);
		} else {
			String rm = getExpressionValueAsString(exp, dependencies);
			if (rm != null) {
				return new String[] { rm };
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static List<StringLiteral> getExpressionValueAsListOfLiterals(Expression exp) {
		if (exp instanceof ArrayInitializer) {
			ArrayInitializer array = (ArrayInitializer) exp;
			return ((List<Expression>) array.expressions()).stream()
					.flatMap(e -> e instanceof StringLiteral
							? Stream.of((StringLiteral)e)
							: Stream.empty()
					)
					.collect(CollectorUtil.toImmutableList());
		} else if (exp instanceof StringLiteral){
			return ImmutableList.of((StringLiteral)exp);
		}
		return ImmutableList.of();
	}



	public static String getAnnotationType(Annotation annotation) {
		ITypeBinding binding = annotation.resolveTypeBinding();
		if (binding!=null) {
			return binding.getQualifiedName();
		}
		return null;
	}
	
}
