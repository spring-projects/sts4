/*******************************************************************************
 * Copyright (c) 2017, 2022 Pivotal, Inc.
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
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.lsp4j.Location;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J.Annotation;
import org.openrewrite.java.tree.J.Assignment;
import org.openrewrite.java.tree.J.ClassDeclaration;
import org.openrewrite.java.tree.J.MethodDeclaration;
import org.openrewrite.java.tree.JavaType.FullyQualified;
import org.openrewrite.java.tree.TypeUtils;
import org.openrewrite.marker.Range;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.handlers.AbstractSymbolProvider;
import org.springframework.ide.vscode.boot.java.utils.CachedSymbol;
import org.springframework.ide.vscode.boot.java.utils.ORAstUtils;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexerJavaContext;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class RequestMappingSymbolProvider extends AbstractSymbolProvider {

	@Override
	protected void addSymbolsPass1(Annotation node, FullyQualified annotationType, Collection<FullyQualified> metaAnnotations, SpringIndexerJavaContext context, TextDocument doc) {

		if (ORAstUtils.getParent(node) instanceof MethodDeclaration) {
			try {
				Range r = ORAstUtils.getRange(node);
				Location location = new Location(doc.getUri(), doc.toRange(r.getStart().getOffset(), r.length()));
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
	
	private String[] getRequestMethodFromMultiArgs(Annotation annotation, SpringIndexerJavaContext context) {
		FullyQualified type = TypeUtils.asFullyQualified(annotation.getType());
		if (type != null && Annotations.SPRING_REQUEST_MAPPING.equals(type.getFullyQualifiedName())) {
			for (Expression arg : annotation.getArguments()) {
				if (arg instanceof Assignment) {
					Assignment assign = (Assignment) arg;
					if ("method".equals(assign.getVariable().printTrimmed())) {
						return ORAstUtils.getExpressionValueAsArray(assign.getAssignment(), context::addDependency);
					}
				}
			}
		}
		return null;
	}

	private String[] getMethod(Annotation node, SpringIndexerJavaContext context) {
		String[] methods = null;
		
		List<Expression> args = node.getArguments();
		
		// TODO: OR AST Annotation parameter
		if (args == null || (args.size() == 1 
				&& (!(args.get(0) instanceof Assignment)) || "value".equals(((Assignment)args.get(0)).getVariable().printTrimmed()) )
		) {
			methods = getRequestMethod(node);
		} else {
			methods = getRequestMethodFromMultiArgs(node, context);
		}

		if (methods == null && ORAstUtils.getParent(node) instanceof MethodDeclaration) {
			Annotation parentAnnotation = getParentAnnotation(node);
			if (parentAnnotation != null) {
				methods = getMethod(parentAnnotation, context);
			}
		}

		return methods;
	}

	private String[] getPath(Annotation node, SpringIndexerJavaContext context) {
		
		List<Expression> args = node.getArguments();
		
		if (args != null) {
			if (args.size() == 1 && !(args.get(0) instanceof Assignment)) {
				return ORAstUtils.getExpressionValueAsArray(args.get(0), context::addDependency);
			} else {
				for (Expression e : args) {
					if (e instanceof Assignment) {
						Assignment assign = (Assignment) e;
						String varName = assign.getVariable().printTrimmed();
						if ("value".equals(varName) || "path".equals(varName)) {
							return ORAstUtils.getExpressionValueAsArray(assign.getAssignment(), context::addDependency);
						}
					}
				}
			}
		}
		
		return new String[] { "" };
	}

	private String[] getParentPath(Annotation node, SpringIndexerJavaContext context) {
		Annotation parentAnnotation = getParentAnnotation(node);
		return parentAnnotation == null ? null : getPath(parentAnnotation, context);
	}

	private Annotation getParentAnnotation(Annotation node) {
		ClassDeclaration classDecl = ORAstUtils.findNode(node, ClassDeclaration.class);
		if (classDecl != null) {
			for (Annotation a : classDecl.getLeadingAnnotations()) {
				FullyQualified annotationType = TypeUtils.asFullyQualified(a.getType());
				if (annotationType != null && Annotations.SPRING_REQUEST_MAPPING.equals(annotationType.getFullyQualifiedName())) {
					return a;
				}
			}			
		}
		return null;
	}

	private String[] getRequestMethod(Annotation annotation) {
		FullyQualified type = TypeUtils.asFullyQualified(annotation.getType());
		if (type != null) {
			switch (type.getFullyQualifiedName()) {
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
			case Annotations.SPRING_REQUEST_MAPPING:
				return new String[] { "GET" };
			}
		}
		return null;
	}

	private String[] getAcceptTypes(Annotation node, SpringIndexerJavaContext context) {
		for (Expression e : node.getArguments()) {
			if (e instanceof Assignment) {
				Assignment assign = (Assignment) e;
				if ("consumes".equals(assign.getVariable().printTrimmed())) {
					return ORAstUtils.getExpressionValueAsArray(assign.getAssignment(), context::addDependency);
				}
			}
		}		
		return new String[0];
	}

	private String[] getContentTypes(Annotation node, SpringIndexerJavaContext context) {
		for (Expression e : node.getArguments()) {
			if (e instanceof Assignment) {
				Assignment assign = (Assignment) e;
				if ("produces".equals(assign.getVariable().printTrimmed())) {
					return ORAstUtils.getExpressionValueAsArray(assign.getAssignment(), context::addDependency);
				}
			}
		}		
		return new String[0];
	}

}
