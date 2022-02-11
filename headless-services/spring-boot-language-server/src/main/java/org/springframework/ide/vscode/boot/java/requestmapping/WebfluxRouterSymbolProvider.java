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
package org.springframework.ide.vscode.boot.java.requestmapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Range;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.Block;
import org.openrewrite.java.tree.J.ClassDeclaration;
import org.openrewrite.java.tree.J.FieldAccess;
import org.openrewrite.java.tree.J.Literal;
import org.openrewrite.java.tree.J.MethodDeclaration;
import org.openrewrite.java.tree.J.MethodInvocation;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.JavaType.FullyQualified;
import org.openrewrite.java.tree.JavaType.Method;
import org.openrewrite.java.tree.TypeUtils;
import org.springframework.ide.vscode.boot.java.handlers.AbstractSymbolProvider;
import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolAddOnInformation;
import org.springframework.ide.vscode.boot.java.utils.CachedSymbol;
import org.springframework.ide.vscode.boot.java.utils.ORAstUtils;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexerJava.SCAN_PASS;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexerJavaContext;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class WebfluxRouterSymbolProvider extends AbstractSymbolProvider {

	@Override
	public void addSymbols(MethodDeclaration methodDeclaration, SpringIndexerJavaContext context, TextDocument doc) {
		JavaType returnType = methodDeclaration.getMethodType().getReturnType();
		if (returnType != null) {

			FullyQualified fqType = TypeUtils.asFullyQualified(returnType);

			if (fqType != null && WebfluxUtils.ROUTER_FUNCTION_TYPE.equals(fqType.getFullyQualifiedName())) {

				Block methodBody = methodDeclaration.getBody();
				if (methodBody != null && methodBody.getStatements() != null && methodBody.getStatements().size() > 0) {
					addSymbolsForRouterFunction(methodBody, context, doc);
				}
				else if (SCAN_PASS.ONE.equals(context.getPass())) {
					context.getNextPassFiles().add(context.getFile());
				}

			}
		}
	}

	private void addSymbolsForRouterFunction(Block methodBody, SpringIndexerJavaContext context, TextDocument doc) {
		new JavaIsoVisitor<ExecutionContext>() {
			public MethodInvocation visitMethodInvocation(MethodInvocation method, ExecutionContext p) {
				Method m = method.getMethodType();
				if (m != null && WebfluxUtils.isRouteMethodInvocation(m)) {
					extractMappingSymbol(method, doc, context);
				}
				return super.visitMethodInvocation(method, p);
			}
		}.visitNonNull(methodBody, new InMemoryExecutionContext());		
	}

	protected void extractMappingSymbol(MethodInvocation node, TextDocument doc, SpringIndexerJavaContext context) {
		WebfluxRouteElement[] pathElements = extractPath(node, doc);
		WebfluxRouteElement[] httpMethods = extractMethods(node, doc);
		WebfluxRouteElement[] contentTypes = extractContentTypes(node, doc);
		WebfluxRouteElement[] acceptTypes = extractAcceptTypes(node, doc);

		org.openrewrite.marker.Range methodInvocationRange = ORAstUtils.getRange(node);
		org.openrewrite.marker.Range methodNameRange = ORAstUtils.getRange(node.getName());
		int methodNameStart = methodInvocationRange.getStart().getOffset();
		int invocationStart = methodNameRange.getStart().getOffset();

		StringBuilder pathBuilder = new StringBuilder();
		for (WebfluxRouteElement pathElement : pathElements) {
			pathBuilder.insert(0, pathElement.getElement());
		}

		String path = pathBuilder.toString();

		if (path.length() > 0) {
			try {

				Location location = new Location(doc.getUri(), doc.toRange(methodNameStart, methodInvocationRange.length() - (methodNameStart - invocationStart)));
				WebfluxHandlerInformation handler = extractHandlerInformation(node, path, httpMethods, contentTypes, acceptTypes);
				WebfluxElementsInformation elements = extractElementsInformation(pathElements, httpMethods, contentTypes, acceptTypes);
				
				SymbolAddOnInformation[] addon = handler != null ?
						new SymbolAddOnInformation[] {handler, elements} :
						new SymbolAddOnInformation[] {elements};

				EnhancedSymbolInformation enhancedSymbol = RouteUtils.createRouteSymbol(location, path, getElementStrings(httpMethods),
						getElementStrings(contentTypes), getElementStrings(acceptTypes), addon);

				context.getGeneratedSymbols().add(new CachedSymbol(context.getDocURI(), context.getLastModified(), enhancedSymbol));

			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

	private WebfluxElementsInformation extractElementsInformation(WebfluxRouteElement[] path, WebfluxRouteElement[] methods,
			WebfluxRouteElement[] contentTypes, WebfluxRouteElement[] acceptTypes) {
		List<Range> allRanges = new ArrayList<>();

		WebfluxRouteElement[][] allElements = new WebfluxRouteElement[][] {path, methods, contentTypes, acceptTypes};
		for (int i = 0; i < allElements.length; i++) {
			for (int j = 0; j < allElements[i].length; j++) {
				allRanges.add(allElements[i][j].getElementRange());
			}
		}

		return new WebfluxElementsInformation((Range[]) allRanges.toArray(new Range[allRanges.size()]));
	}

	private WebfluxRouteElement[] extractPath(MethodInvocation routerInvocation, TextDocument doc) {
		WebfluxPathFinder pathFinder = new WebfluxPathFinder(routerInvocation, doc);
		InMemoryExecutionContext ctx = new InMemoryExecutionContext();
		for (Expression argument : routerInvocation.getArguments()) {
			if (argument != null) {
				pathFinder.visitNonNull(argument, ctx);
			}
		}

		List<WebfluxRouteElement> path = pathFinder.getPath();

		extractNestedValue(routerInvocation, path, (methodInvocation) -> {
			Method method = methodInvocation.getMethodType();

			try {
				if (method != null && WebfluxUtils.REQUEST_PREDICATE_PATH_METHOD.equals(method.getName())) {
					Literal stringLiteral = WebfluxUtils.extractArgument(methodInvocation, Literal.class);
					if (stringLiteral != null) {
						org.openrewrite.marker.Range r = ORAstUtils.getRange(stringLiteral);
						Range range = doc.toRange(r.getStart().getOffset(), r.length());
						return new WebfluxRouteElement(ORAstUtils.getLiteralValue(stringLiteral), range);
					}
				}
			}
			catch (BadLocationException e) {
				// ignore
			}
			return null;
		});

		return (WebfluxRouteElement[]) path.toArray(new WebfluxRouteElement[path.size()]);
	}

	private WebfluxRouteElement[] extractMethods(MethodInvocation routerInvocation, TextDocument doc) {
		WebfluxMethodFinder methodFinder = new WebfluxMethodFinder(routerInvocation, doc);
		InMemoryExecutionContext ctx = new InMemoryExecutionContext();
		for (Expression argument : routerInvocation.getArguments()) {
			if (argument != null) {
				methodFinder.visitNonNull(argument, ctx);
			}
		}

		final List<WebfluxRouteElement> methods = methodFinder.getMethods();

		extractNestedValue(routerInvocation, methods, (methodInvocation) -> {
			Method method = methodInvocation.getMethodType();

			try {
				if (method != null && WebfluxUtils.REQUEST_PREDICATE_METHOD_METHOD.equals(method.getName())) {
					FieldAccess qualifiedName = WebfluxUtils.extractArgument(methodInvocation, FieldAccess.class);
					if (qualifiedName != null) {
						org.openrewrite.marker.Range r = ORAstUtils.getRange(qualifiedName);
						Range range = doc.toRange(r.getStart().getOffset(), r.length());
						return new WebfluxRouteElement(qualifiedName.getName().toString(), range);
					}
				}
			}
			catch (BadLocationException e) {
				// ignore
			}

			return null;
		});

		return (WebfluxRouteElement[]) methods.toArray(new WebfluxRouteElement[methods.size()]);
	}

	private WebfluxRouteElement[] extractAcceptTypes(MethodInvocation routerInvocation, TextDocument doc) {
		WebfluxAcceptTypeFinder typeFinder = new WebfluxAcceptTypeFinder(doc);
		InMemoryExecutionContext ctx = new InMemoryExecutionContext();
		for (Expression argument : routerInvocation.getArguments()) {
			if (argument != null) {
				typeFinder.visitNonNull(argument, ctx);
			}
		}

		final List<WebfluxRouteElement> acceptTypes = typeFinder.getAcceptTypes();

		extractNestedValue(routerInvocation, acceptTypes, (methodInvocation) -> {
			Method methodBinding = methodInvocation.getMethodType();

			try {
				if (methodBinding != null && WebfluxUtils.REQUEST_PREDICATE_ACCEPT_TYPE_METHOD.equals(methodBinding.getName())) {
					FieldAccess nameArgument = WebfluxUtils.extractArgument(methodInvocation, FieldAccess.class);
					if (nameArgument != null) {
						org.openrewrite.marker.Range r = ORAstUtils.getRange(nameArgument);
						Range range = doc.toRange(r.getStart().getOffset(), r.length());
						return new WebfluxRouteElement(nameArgument.getSimpleName(), range);
					}
				}
			}
			catch (BadLocationException e) {
				// ignore
			}

			return null;
		});

		return (WebfluxRouteElement[]) acceptTypes.toArray(new WebfluxRouteElement[acceptTypes.size()]);
	}

	private WebfluxRouteElement[] extractContentTypes(MethodInvocation routerInvocation, TextDocument doc) {
		WebfluxContentTypeFinder contentTypeFinder = new WebfluxContentTypeFinder(doc);
		InMemoryExecutionContext ctx = new InMemoryExecutionContext();
		for (Expression argument : routerInvocation.getArguments()) {
			if (argument != null) {
				contentTypeFinder.visitNonNull(argument, ctx);
			}
		}

		final List<WebfluxRouteElement> contentTypes = contentTypeFinder.getContentTypes();

		extractNestedValue(routerInvocation, contentTypes, (methodInvocation) -> {
			Method methodBinding = methodInvocation.getMethodType();

			try {
				if (methodBinding != null && WebfluxUtils.REQUEST_PREDICATE_CONTENT_TYPE_METHOD.equals(methodBinding.getName())) {
					// TODO: OR AST FieldAcess? So we need FQ name of something? See above a lot of similar places

					FieldAccess nameArgument = WebfluxUtils.extractArgument(methodInvocation, FieldAccess.class);
					if (nameArgument != null) {
						org.openrewrite.marker.Range r = ORAstUtils.getRange(nameArgument);
						Range range = doc.toRange(r.getStart().getOffset(), r.length());
						return new WebfluxRouteElement(nameArgument.getSimpleName(), range);
					}
				}
			}
			catch (BadLocationException e) {
				// ignore
			}

			return null;
		});

		return (WebfluxRouteElement[]) contentTypes.toArray(new WebfluxRouteElement[contentTypes.size()]);
	}

	private void extractNestedValue(J node, Collection<WebfluxRouteElement> values, Function<MethodInvocation, WebfluxRouteElement> extractor) {
		if (node == null || node instanceof ClassDeclaration) {
			return;
		}

		if (node instanceof MethodInvocation) {
			MethodInvocation methodInvocation = (MethodInvocation) node;
			Method method = methodInvocation.getMethodType();

			if (method != null && method.getDeclaringType() != null
					&& WebfluxUtils.ROUTER_FUNCTIONS_TYPE.equals(method.getDeclaringType().getFullyQualifiedName())) {

				String name = method.getName();
				if (WebfluxUtils.REQUEST_PREDICATE_NEST_METHOD.equals(name)) {
					for (Expression argument : methodInvocation.getArguments()) {
						if (argument instanceof MethodInvocation) {
							MethodInvocation nestedMethod = (MethodInvocation) argument;
							WebfluxRouteElement value = extractor.apply(nestedMethod);
							if (value != null) {
								values.add(value);
							}
						}
					}
				}
			}
		}

		extractNestedValue(ORAstUtils.getParent(node), values, extractor);
	}

	private WebfluxHandlerInformation extractHandlerInformation(MethodInvocation node, String path, WebfluxRouteElement[] httpMethods,
			WebfluxRouteElement[] contentTypes, WebfluxRouteElement[] acceptTypes) {

			for (Expression e : node.getArguments()) {
				// TODO: OR AST Method references!!!
				
//				if (argument instanceof ExpressionMethodReference) {
//					ExpressionMethodReference methodReference = (ExpressionMethodReference) argument;
//					IMethodBinding methodBinding = methodReference.resolveMethodBinding();
//
//					if (methodBinding != null && methodBinding.getDeclaringClass() != null && methodBinding.getMethodDeclaration() != null) {
//						String handlerClass = methodBinding.getDeclaringClass().getBinaryName();
//						if (handlerClass != null) handlerClass = handlerClass.trim();
//
//						String handlerMethod = methodBinding.getMethodDeclaration().toString();
//						if (handlerMethod != null) handlerMethod = handlerMethod.trim();
//
//						return new WebfluxHandlerInformation(handlerClass, handlerMethod, path, getElementStrings(httpMethods), getElementStrings(contentTypes), getElementStrings(acceptTypes));
//					}
//				}
			}

		return null;
	}

	private String[] getElementStrings(WebfluxRouteElement[] routeElements) {
		List<String> result = new ArrayList<>();

		for (int i = 0; i < routeElements.length; i++) {
			result.add(routeElements[i].getElement());
		}

		return (String[]) result.toArray(new String[result.size()]);
	}

}
