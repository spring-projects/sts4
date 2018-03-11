/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.requestmapping;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.Location;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class RequestMappingSymbolProvider implements SymbolProvider {

	@Override
	public Collection<EnhancedSymbolInformation> getSymbols(Annotation node, ITypeBinding annotationType, Collection<ITypeBinding> metaAnnotations, TextDocument doc) {
		if (node.getParent() instanceof MethodDeclaration) {
			try {
				Location location = new Location(doc.getUri(), doc.toRange(node.getStartPosition(), node.getLength()));
				String[] path = getPath(node);
				String[] parentPath = getParentPath(node);
				String[] methods = getMethod(node);
				String[] contentTypes = new String[0];
				String[] acceptTypes = new String[0];

//				String methodStr = method == null || method.length == 0 ? "" : String.join(",", method);

				return (parentPath == null ? Stream.of("") : Arrays.stream(parentPath)).filter(Objects::nonNull)
						.flatMap(parent -> (path == null ? Stream.<String>empty() : Arrays.stream(path))
								.filter(Objects::nonNull).map(p -> {
									String separator = !parent.endsWith("/") && !p.startsWith("/") ? "/" : "";
									String resultPath = parent + separator + p;
									if (resultPath.endsWith("/")) {
										resultPath = resultPath.substring(0, resultPath.length() - 1);
									}
									return resultPath.startsWith("/") ? resultPath : "/" + resultPath;
								}))
						.map(p -> RouteUtils.createRouteSymbol(location, p, methods, contentTypes, acceptTypes, null))
						.collect(Collectors.toList());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private String[] getMethod(Annotation node) {
		String[] methods = null;

		if (node.isNormalAnnotation()) {
			NormalAnnotation normNode = (NormalAnnotation) node;
			List<?> values = normNode.values();
			for (Iterator<?> iterator = values.iterator(); iterator.hasNext();) {
				Object object = iterator.next();
				if (object instanceof MemberValuePair) {
					MemberValuePair pair = (MemberValuePair) object;
					String valueName = pair.getName().getIdentifier();
					if (valueName != null && valueName.equals("method")) {
						Expression expression = pair.getValue();
						methods = ASTUtils.getExpressionValueAsArray(expression);
						break;
					}
				}
			}
		} else if (node instanceof SingleMemberAnnotation) {
			methods = getRequestMethod((SingleMemberAnnotation)node);
		}

		if (methods == null && node.getParent() instanceof MethodDeclaration) {
			Annotation parentAnnotation = getParentAnnotation(node);
			if (parentAnnotation != null) {
				methods = getMethod(parentAnnotation);
			}
		}

		return methods;
	}

	private String[] getPath(Annotation node) {
		if (node.isNormalAnnotation()) {
			NormalAnnotation normNode = (NormalAnnotation) node;
			List<?> values = normNode.values();
			for (Iterator<?> iterator = values.iterator(); iterator.hasNext();) {
				Object object = iterator.next();
				if (object instanceof MemberValuePair) {
					MemberValuePair pair = (MemberValuePair) object;
					String valueName = pair.getName().getIdentifier();
					if (valueName != null && (valueName.equals("value") || valueName.equals("path"))) {
						Expression expression = pair.getValue();
						return ASTUtils.getExpressionValueAsArray(expression);
					}
				}
			}
		} else if (node.isSingleMemberAnnotation()) {
			SingleMemberAnnotation singleNode = (SingleMemberAnnotation) node;
			Expression expression = singleNode.getValue();
			return ASTUtils.getExpressionValueAsArray(expression);
		}

		return new String[] { "" };
	}

	private String[] getParentPath(Annotation node) {
		Annotation parentAnnotation = getParentAnnotation(node);
		return parentAnnotation == null ? null : getPath(parentAnnotation);
	}

	private Annotation getParentAnnotation(Annotation node) {
		ASTNode parent = node.getParent() != null ? node.getParent().getParent() : null;
		while (parent != null && !(parent instanceof TypeDeclaration)) {
			parent = parent.getParent();
		}

		if (parent != null) {
			TypeDeclaration type = (TypeDeclaration) parent;
			List<?> modifiers = type.modifiers();
			Iterator<?> iterator = modifiers.iterator();
			while (iterator.hasNext()) {
				Object modifier = iterator.next();
				if (modifier instanceof Annotation) {
					Annotation annotation = (Annotation) modifier;
					ITypeBinding resolvedType = annotation.resolveTypeBinding();
					String annotationType = resolvedType.getQualifiedName();
					if (annotationType != null && Annotations.SPRING_REQUEST_MAPPING.equals(annotationType)) {
						return annotation;
					}
				}
			}
		}
		return null;
	}

	private String[] getRequestMethod(SingleMemberAnnotation annotation) {
		ITypeBinding type = annotation.resolveTypeBinding();
		if (type != null) {
			switch (type.getQualifiedName()) {
			case Annotations.SPRING_GET_MAPPING:
				return new String[] { "GET" };
			case Annotations.SPRING_POST_MAPPING:
				return new String[] { "POST" };
			case Annotations.SPRING_DELETE_MAPPING:
				return new String[] { "DELETE" };
			case Annotations.SPRING_PUT_MAPPING:
				return new String[] { "PUT" };
			case Annotations.SPRING_PATCH_MAPPING:
				return new String[] { "PATCH" };
			}
		}
		return null;
	}

	@Override
	public Collection<EnhancedSymbolInformation> getSymbols(TypeDeclaration typeDeclaration, TextDocument doc) {
		return null;
	}

	@Override
	public Collection<EnhancedSymbolInformation> getSymbols(MethodDeclaration methodDeclaration, TextDocument doc) {
		return null;
	}

}
