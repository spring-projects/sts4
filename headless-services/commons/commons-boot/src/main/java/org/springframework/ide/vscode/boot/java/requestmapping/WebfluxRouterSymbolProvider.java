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

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class WebfluxRouterSymbolProvider implements SymbolProvider {
	
	@Override
	public Collection<SymbolInformation> getSymbols(Annotation node, ITypeBinding typeBinding,
			Collection<ITypeBinding> metaAnnotations, TextDocument doc) {
		return null;
	}

	@Override
	public Collection<SymbolInformation> getSymbols(TypeDeclaration typeDeclaration, TextDocument doc) {
		return null;
	}

	@Override
	public Collection<SymbolInformation> getSymbols(MethodDeclaration methodDeclaration, TextDocument doc) {
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

	private Collection<SymbolInformation> getSymbolsForRouterFunction(MethodDeclaration methodDeclaration,
			TextDocument doc) {
		List<SymbolInformation> result = new ArrayList<>();

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

	protected void extractMappingSymbol(MethodInvocation node, TextDocument doc, List<SymbolInformation> result) {
		String foundPath = extractPathFromRouterFunction(node);
		String path = extractPath(node, foundPath);
		String httpMethod = extractMethod(node);
		
		int methodNameStart = node.getName().getStartPosition();
		int invocationStart = node.getStartPosition();
		
		if (path != null && path.length() > 0) {
			try {
				Location location = new Location(doc.getUri(), doc.toRange(methodNameStart, node.getLength() - (methodNameStart - invocationStart)));
				String label = "@" + (path.startsWith("/") ? path : ("/" + path)) + (httpMethod == null || httpMethod.isEmpty() ? "" : " -- " + httpMethod);
				result.add(new SymbolInformation(label, SymbolKind.Interface, location));
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

	private String extractPathFromRouterFunction(MethodInvocation routerInvocation) {
		WebfluxPathFinder pathFinder = new WebfluxPathFinder(routerInvocation);
		routerInvocation.accept(pathFinder);
		
		String path = pathFinder.getPath();
		if (path == null) path = "";
		
		return path;
	}
	
	private String extractPath(ASTNode node, String path) {
		if (node == null || node instanceof TypeDeclaration) {
			return path;
		}

		if (node instanceof MethodInvocation) {
			MethodInvocation methodInvocation = (MethodInvocation) node;
			IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
			
			if (WebfluxUtils.ROUTER_FUNCTIONS_TYPE.equals(methodBinding.getDeclaringClass().getBinaryName())) {
				String name = methodBinding.getName();
				if ("nest".equals(name)) {
					List<?> arguments = methodInvocation.arguments();
					for (Object argument : arguments) {
						if (argument instanceof MethodInvocation) {
							MethodInvocation nestedMethod = (MethodInvocation) argument;
							IMethodBinding nestedMethodBinding = nestedMethod.resolveMethodBinding();
							
							String nestedMethodName = nestedMethodBinding.getName();
							if ("path".equals(nestedMethodName)) {
								String additionalPath = WebfluxUtils.extractPath(nestedMethod);
								if (additionalPath != null && additionalPath.length() > 0) {
									path = additionalPath + path;
								}
							}
						}
						
					}
				}
			}
		}
		
		return extractPath(node.getParent(), path);
	}

	private String extractMethod(MethodInvocation routerInvocation) {
		WebfluxMethodFinder methodFinder = new WebfluxMethodFinder(routerInvocation);
		routerInvocation.accept(methodFinder);
		
		String method = methodFinder.getMethod();
		return method;
	}

}
