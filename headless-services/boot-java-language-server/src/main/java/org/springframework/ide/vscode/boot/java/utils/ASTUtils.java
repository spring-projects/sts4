/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.Range;
import org.springframework.ide.vscode.commons.languageserver.util.DocumentRegion;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

public class ASTUtils {

	public static DocumentRegion nameRegion(TextDocument doc, Annotation annotation) {
		int start = annotation.getTypeName().getStartPosition();
		int end = start + annotation.getTypeName().getLength();
		if (doc.getSafeChar(start - 1) == '@') {
			start--;
		}
		return new DocumentRegion(doc, start, end);
	}


	public static Optional<Range> nameRange(TextDocument doc, Annotation annotation) {
		try {
			return Optional.of(nameRegion(doc, annotation).asRange());
		} catch (Exception e) {
			Log.log(e);
			return Optional.empty();
		}
	}

	public static Optional<Expression> getAttribute(Annotation annotation, String name) {
		try {
			if (annotation.isSingleMemberAnnotation() && name.equals("value")) {
				SingleMemberAnnotation sma = (SingleMemberAnnotation) annotation;
				return Optional.ofNullable(sma.getValue());
			} else if (annotation.isNormalAnnotation()) {
				NormalAnnotation na = (NormalAnnotation) annotation;
				Object attributeObjs = na.getStructuralProperty(NormalAnnotation.VALUES_PROPERTY);
				if (attributeObjs instanceof List) {
					for (Object atrObj : (List<?>)attributeObjs) {
						if (atrObj instanceof MemberValuePair) {
							MemberValuePair mvPair = (MemberValuePair) atrObj;
							if (name.equals(mvPair.getName().getIdentifier())) {
								return Optional.ofNullable(mvPair.getValue());
							}
						}
					}
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return Optional.empty();
	}

	/**
	 * For case where a expression can be either a String or a array of Strings and
	 * we are interested in the first element of the array. (I.e. typical case
	 * when annotation attribute is of type String[] (because Java allows using a single
	 * value as a convenient syntax for writing an array of length 1 in that case.
	 */
	public static Optional<String> getFirstString(Expression exp) {
		if (exp instanceof StringLiteral) {
			return Optional.ofNullable(getLiteralValue((StringLiteral) exp));
		} else if (exp instanceof ArrayInitializer) {
			ArrayInitializer array = (ArrayInitializer) exp;
			Object objs = array.getStructuralProperty(ArrayInitializer.EXPRESSIONS_PROPERTY);
			if (objs instanceof List) {
				List<?> list = (List<?>) objs;
				if (!list.isEmpty()) {
					Object firstObj = list.get(0);
					if (firstObj instanceof Expression) {
						return getFirstString((Expression) firstObj);
					}
				}
			}
		}
		return Optional.empty();
	}

	public static TypeDeclaration findDeclaringType(Annotation annotation) {
		ASTNode node = annotation;
		while (node != null && !(node instanceof TypeDeclaration)) {
			node = node.getParent();
		}

		return node != null ? (TypeDeclaration) node : null;
	}

	public static MethodDeclaration[] findConstructors(TypeDeclaration typeDecl) {
		List<MethodDeclaration> constructors = new ArrayList<>();

		MethodDeclaration[] methods = typeDecl.getMethods();
		for (MethodDeclaration methodDeclaration : methods) {
			if (methodDeclaration.isConstructor()) {
				constructors.add(methodDeclaration);
			}
		}

		return constructors.toArray(new MethodDeclaration[constructors.size()]);
	}


	public static MethodDeclaration getAnnotatedMethod(Annotation annotation) {
		ASTNode parent = annotation.getParent();
		if (parent instanceof MethodDeclaration) {
			return (MethodDeclaration)parent;
		}
		return null;
	}

	public static TypeDeclaration getAnnotatedType(Annotation annotation) {
		ASTNode parent = annotation.getParent();
		if (parent instanceof TypeDeclaration) {
			return (TypeDeclaration)parent;
		}
		return null;
	}

	public static String getLiteralValue(StringLiteral node) {
		synchronized (node.getAST()) {
			return node.getLiteralValue();
		}
	}

	public static String getExpressionValueAsString(Expression exp) {
		if (exp instanceof StringLiteral) {
			return getLiteralValue((StringLiteral) exp);
		} else if (exp instanceof QualifiedName) {
			return getExpressionValueAsString(((QualifiedName) exp).getName());
		} else if (exp instanceof SimpleName) {
			return ((SimpleName) exp).getIdentifier();
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static String[] getExpressionValueAsArray(Expression exp) {
		if (exp instanceof ArrayInitializer) {
			ArrayInitializer array = (ArrayInitializer) exp;
			return ((List<Expression>) array.expressions()).stream().map(e -> getExpressionValueAsString(e))
					.filter(Objects::nonNull).toArray(String[]::new);
		} else {
			String rm = getExpressionValueAsString(exp);
			if (rm != null) {
				return new String[] { rm };
			}
		}
		return null;
	}


	public static Collection<Annotation> getAnnotations(TypeDeclaration declaringType) {
		Object modifiersObj = declaringType.getStructuralProperty(TypeDeclaration.MODIFIERS2_PROPERTY);
		if (modifiersObj instanceof List) {
			ImmutableList.Builder<Annotation> annotations = ImmutableList.builder();
			for (Object node : (List<?>)modifiersObj) {
				if (node instanceof Annotation) {
					annotations.add((Annotation) node);
				}
			}
			return annotations.build();
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
