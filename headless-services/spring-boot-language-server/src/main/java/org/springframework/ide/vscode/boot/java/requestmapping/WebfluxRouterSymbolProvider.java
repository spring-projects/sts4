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
package org.springframework.ide.vscode.boot.java.requestmapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author Martin Lippert
 */
public class WebfluxRouterSymbolProvider implements SymbolProvider {
	
	@Override
	public Collection<EnhancedSymbolInformation> getSymbols(Annotation node, ITypeBinding typeBinding,
			Collection<ITypeBinding> metaAnnotations, TextDocument doc) {
		return null;
	}

	@Override
	public Collection<EnhancedSymbolInformation> getSymbols(TypeDeclaration typeDeclaration, TextDocument doc) {
		return null;
	}

	@Override
	public Collection<EnhancedSymbolInformation> getSymbols(MethodDeclaration methodDeclaration, TextDocument doc) {
		Type returnType = methodDeclaration.getReturnType2();
		if (returnType != null) {
			ITypeBinding resolvedBinding = returnType.resolveBinding();
			if (resolvedBinding != null) {
				if (WebfluxUtils.ROUTER_FUNCTION_TYPE.equals(resolvedBinding.getBinaryName())) {
					return getSymbolsForRouterFunction(methodDeclaration, doc);
				}
			}
		}
		return null;
	}

	private Collection<EnhancedSymbolInformation> getSymbolsForRouterFunction(MethodDeclaration methodDeclaration,
			TextDocument doc) {
		List<EnhancedSymbolInformation> result = new ArrayList<>();

		Block body = methodDeclaration.getBody();
		body.accept(new ASTVisitor() {
			
			@Override
			public boolean visit(MethodInvocation node) {
				IMethodBinding methodBinding = node.resolveMethodBinding();
				
				if (WebfluxUtils.isRouteMethodInvocation(methodBinding)) {
					extractMappingSymbol(node, doc, result);
				}
				
				return super.visit(node);
			}
			
		});
		
		return result;
	}

	protected void extractMappingSymbol(MethodInvocation node, TextDocument doc, List<EnhancedSymbolInformation> result) {
		String path = extractPath(node);
		String httpMethod = extractMethod(node);
		
		String contentType = extractContentType(node);
		String acceptType = extractAcceptType(node);
		
		int methodNameStart = node.getName().getStartPosition();
		int invocationStart = node.getStartPosition();
		
		if (path != null && path.length() > 0) {
			try {
				Location location = new Location(doc.getUri(), doc.toRange(methodNameStart, node.getLength() - (methodNameStart - invocationStart)));
				String label = "@" + (path.startsWith("/") ? path : ("/" + path)) + (httpMethod == null || httpMethod.isEmpty() ? "" : " -- " + httpMethod);

				WebfluxHandlerInformation handler = extractHandlerInformation(node, path, httpMethod, contentType, acceptType);
				
				result.add(new EnhancedSymbolInformation(new SymbolInformation(label, SymbolKind.Interface, location), handler));
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

	private String extractPath(MethodInvocation routerInvocation) {
		WebfluxPathFinder pathFinder = new WebfluxPathFinder(routerInvocation);
		routerInvocation.accept(pathFinder);
		
		String path = pathFinder.getPath();
		if (path == null) path = "";
		
		return extractNestedValue(routerInvocation, path, (methodInvocationPathPrefix) -> {
			IMethodBinding methodBinding = methodInvocationPathPrefix.getT1().resolveMethodBinding();
			String methodName = methodBinding.getName();
			
			if (WebfluxUtils.REQUEST_PREDICATE_PATH_METHOD.equals(methodName)) {
				String additionalPath = WebfluxUtils.extractStringLiteralArgument(methodInvocationPathPrefix.getT1());
				if (additionalPath != null && additionalPath.length() > 0) {
					return additionalPath + methodInvocationPathPrefix.getT2();
				}
			}
			
			return methodInvocationPathPrefix.getT2();
		});
	}
	
	private String extractMethod(MethodInvocation routerInvocation) {
		WebfluxMethodFinder methodFinder = new WebfluxMethodFinder(routerInvocation);
		routerInvocation.accept(methodFinder);
		
		String method = methodFinder.getMethod();
		
		return extractNestedValue(routerInvocation, method, (methodInvocationPathPrefix) -> {
			IMethodBinding methodBinding = methodInvocationPathPrefix.getT1().resolveMethodBinding();
			String methodName = methodBinding.getName();
			
			if (WebfluxUtils.REQUEST_PREDICATE_METHOD_METHOD.equals(methodName)) {
				String newMethod = WebfluxUtils.extractStringLiteralArgument(methodInvocationPathPrefix.getT1());
				if (method == null) {
					return newMethod;
				}
			}
			
			return methodInvocationPathPrefix.getT2();
		});
	}
	
	private String extractAcceptType(MethodInvocation routerInvocation) {
		String acceptType = null;
		
		WebfluxAcceptTypeFinder acceptTypeFinder = new WebfluxAcceptTypeFinder();
		List<?> arguments = routerInvocation.arguments();
		for (Object argument : arguments) {
			if (argument != null && argument instanceof ASTNode) {
				((ASTNode)argument).accept(acceptTypeFinder);
				if (acceptTypeFinder.getAcceptType() != null) {
					acceptType = acceptTypeFinder.getAcceptType();
				}
			}
		}
		
		return extractNestedValue(routerInvocation, acceptType, (methodInvocationPathPrefix) -> {
			IMethodBinding methodBinding = methodInvocationPathPrefix.getT1().resolveMethodBinding();
			String methodName = methodBinding.getName();
			
			if (WebfluxUtils.REQUEST_PREDICATE_ACCEPT_TYPE_METHOD.equals(methodName)) {
				String newAcceptType = WebfluxUtils.extractSimpleNameArgument(methodInvocationPathPrefix.getT1());
				if (newAcceptType != null) {
					return newAcceptType;
				}
			}
			
			return methodInvocationPathPrefix.getT2();
		});
	}
	
	private String extractContentType(MethodInvocation routerInvocation) {
		WebfluxContentTypeFinder contentTypeFinder = new WebfluxContentTypeFinder(routerInvocation);
		routerInvocation.accept(contentTypeFinder);
		
		String contentType = contentTypeFinder.getContentType();
		
		return extractNestedValue(routerInvocation, contentType, (methodInvocationPathPrefix) -> {
			IMethodBinding methodBinding = methodInvocationPathPrefix.getT1().resolveMethodBinding();
			String methodName = methodBinding.getName();
			
			if (WebfluxUtils.REQUEST_PREDICATE_CONTENT_TYPE_METHOD.equals(methodName)) {
				String newContentType = WebfluxUtils.extractSimpleNameArgument(methodInvocationPathPrefix.getT1());
				if (contentType == null) {
					return newContentType;
				}
			}
			
			return methodInvocationPathPrefix.getT2();
		});
	}
	
	private String extractNestedValue(ASTNode node, String value, Function<Tuple2<MethodInvocation, String>, String> extractor) {
		if (node == null || node instanceof TypeDeclaration) {
			return value;
		}

		if (node instanceof MethodInvocation) {
			MethodInvocation methodInvocation = (MethodInvocation) node;
			IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
			
			if (WebfluxUtils.ROUTER_FUNCTIONS_TYPE.equals(methodBinding.getDeclaringClass().getBinaryName())) {
				String name = methodBinding.getName();
				if (WebfluxUtils.REQUEST_PREDICATE_NEST_METHOD.equals(name)) {
					List<?> arguments = methodInvocation.arguments();
					for (Object argument : arguments) {
						if (argument instanceof MethodInvocation) {
							MethodInvocation nestedMethod = (MethodInvocation) argument;
							value = extractor.apply(Tuples.of(nestedMethod, value));
						}
					}
				}
			}
		}
		
		return extractNestedValue(node.getParent(), value, extractor);
	}

	private WebfluxHandlerInformation extractHandlerInformation(MethodInvocation node, String path, String httpMethod, String contentType, String acceptType) {
		List<?> arguments = node.arguments();

		if (arguments != null) {
			for (Object argument : arguments) {
				if (argument instanceof ExpressionMethodReference) {
					ExpressionMethodReference methodReference = (ExpressionMethodReference) argument;
					IMethodBinding methodBinding = methodReference.resolveMethodBinding();

					if (methodBinding != null && methodBinding.getDeclaringClass() != null && methodBinding.getMethodDeclaration() != null) {
						String handlerClass = methodBinding.getDeclaringClass().getBinaryName();
						if (handlerClass != null) handlerClass = handlerClass.trim();
						
						String handlerMethod = methodBinding.getMethodDeclaration().toString();
						if (handlerMethod != null) handlerMethod = handlerMethod.trim();
						
						return new WebfluxHandlerInformation(handlerClass, handlerMethod, path, httpMethod, contentType, acceptType);
					}
				}
			}
		}
		
		return null;
	}

}
