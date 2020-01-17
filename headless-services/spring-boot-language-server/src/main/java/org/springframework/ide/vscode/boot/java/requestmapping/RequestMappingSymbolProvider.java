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
package org.springframework.ide.vscode.boot.java.requestmapping;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
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
import org.springframework.ide.vscode.boot.java.handlers.AbstractSymbolProvider;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.ide.vscode.boot.java.utils.CachedSymbol;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexerJavaContext;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class RequestMappingSymbolProvider extends AbstractSymbolProvider {

	@Override
	protected void addSymbolsPass1(Annotation node, ITypeBinding annotationType, Collection<ITypeBinding> metaAnnotations, SpringIndexerJavaContext context, TextDocument doc) {

		if (node.getParent() instanceof MethodDeclaration) {
			try {
				Location location = new Location(doc.getUri(), doc.toRange(node.getStartPosition(), node.getLength()));
				String[] path = getPath(node, context);
				String[] parentPath = getParentPath(node, context);
				String[] methods = getMethod(node, context);
				String[] contentTypes = getContentTypes(node, context);
				String[] acceptTypes = getAcceptTypes(node, context);

				Stream<String> stream = parentPath == null ? Stream.of("") : Arrays.stream(parentPath);
				stream.filter(Objects::nonNull)
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
						.forEach((enhancedSymbol) -> context.getGeneratedSymbols().add(new CachedSymbol(context.getDocURI(), context.getLastModified(), enhancedSymbol)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private String[] getMethod(Annotation node, SpringIndexerJavaContext context) {
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
						methods = ASTUtils.getExpressionValueAsArray(expression, context::addDependency);
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
				methods = getMethod(parentAnnotation, context);
			}
		}

		return methods;
	}

	private String[] getPath(Annotation node, SpringIndexerJavaContext context) {
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
						return ASTUtils.getExpressionValueAsArray(expression, context::addDependency);
					}
				}
			}
		} else if (node.isSingleMemberAnnotation()) {
			SingleMemberAnnotation singleNode = (SingleMemberAnnotation) node;
			Expression expression = singleNode.getValue();
			return ASTUtils.getExpressionValueAsArray(expression, context::addDependency);
		}

		return new String[] { "" };
	}

	private String[] getParentPath(Annotation node, SpringIndexerJavaContext context) {
		Annotation parentAnnotation = getParentAnnotation(node);
		return parentAnnotation == null ? null : getPath(parentAnnotation, context);
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

	private String[] getAcceptTypes(Annotation node, SpringIndexerJavaContext context) {
		if (node.isNormalAnnotation()) {
			NormalAnnotation normNode = (NormalAnnotation) node;
			List<?> values = normNode.values();
			for (Iterator<?> iterator = values.iterator(); iterator.hasNext();) {
				Object object = iterator.next();
				if (object instanceof MemberValuePair) {
					MemberValuePair pair = (MemberValuePair) object;
					String valueName = pair.getName().getIdentifier();
					if (valueName != null && valueName.equals("consumes")) {
						Expression expression = pair.getValue();
						return ASTUtils.getExpressionValueAsArray(expression, context::addDependency);
					}
				}
			}
		}
		return new String[0];
	}

	private String[] getContentTypes(Annotation node, SpringIndexerJavaContext context) {
		if (node.isNormalAnnotation()) {
			NormalAnnotation normNode = (NormalAnnotation) node;
			List<?> values = normNode.values();
			for (Iterator<?> iterator = values.iterator(); iterator.hasNext();) {
				Object object = iterator.next();
				if (object instanceof MemberValuePair) {
					MemberValuePair pair = (MemberValuePair) object;
					String valueName = pair.getName().getIdentifier();
					if (valueName != null && valueName.equals("produces")) {
						Expression expression = pair.getValue();
						return ASTUtils.getExpressionValueAsArray(expression, context::addDependency);
					}
				}
			}
		}
		return new String[0];
	}

}
