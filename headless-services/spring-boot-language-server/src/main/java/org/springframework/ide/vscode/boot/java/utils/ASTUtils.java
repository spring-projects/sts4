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

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.commons.util.CollectorUtil;
import org.springframework.ide.vscode.commons.util.text.DocumentRegion;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

public class ASTUtils {

	private static final Logger log = LoggerFactory.getLogger(ASTUtils.class);

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
			log.error("", e);
			return Optional.empty();
		}
	}

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


	public static DocumentRegion nodeRegion(TextDocument doc, ASTNode node) {
		int start = node.getStartPosition();
		int end = start + node.getLength();
		return new DocumentRegion(doc, start, end);
	}

	public static Optional<Expression> getAttribute(Annotation annotation, String name) {
		if (annotation != null) {
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
				log.error("", e);
			}
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

	public static TypeDeclaration findDeclaringType(ASTNode node) {
		while (node != null && !(node instanceof TypeDeclaration)) {
			node = node.getParent();
		}

		return node != null ? (TypeDeclaration) node : null;
	}

	public static boolean hasExactlyOneConstructor(TypeDeclaration typeDecl) {
		boolean oneFound = false;
		MethodDeclaration[] methods = typeDecl.getMethods();
		for (MethodDeclaration methodDeclaration : methods) {
			if (methodDeclaration.isConstructor()) {
				if (oneFound) {
					return false;
				} else {
					oneFound = true;
				}
			}
		}
		return oneFound;
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

	public static Optional<String> beanId(List<Object> modifiers) {
		return modifiers.stream()
				.filter(m -> m instanceof SingleMemberAnnotation)
				.map(m -> (SingleMemberAnnotation) m)
				.filter(m -> {
					ITypeBinding typeBinding = m.resolveTypeBinding();
					if (typeBinding != null) {
						return Annotations.QUALIFIER.equals(typeBinding.getQualifiedName());
					}
					return false;
				})
				.findFirst()
				.map(a -> a.getValue())
				.filter(e -> e != null)
				.map(e -> e.resolveConstantExpressionValue())
				.filter(o -> o instanceof String)
				.map(o -> (String) o);
	}

	public static Annotation getBeanAnnotation(MethodDeclaration method) {
		List<?> modifiers = method.modifiers();
		for (Object modifier : modifiers) {
			if (modifier instanceof Annotation) {
				Annotation annotation = (Annotation) modifier;
				ITypeBinding typeBinding = annotation.resolveTypeBinding();
				if (typeBinding != null) {
					String fqName = typeBinding.getQualifiedName();
					if (Annotations.BEAN.equals(fqName)) {
						return annotation;
					}
				}
			}
		}
		return null;
	}

}
