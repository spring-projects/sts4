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

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.SymbolInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class WebfluxRouterSymbolProvider implements SymbolProvider {
	
	private static final String ROUTER_FUNCTION_TYPE = "org.springframework.web.reactive.function.server.RouterFunction";
	private static final String ROUTER_FUNCTIONS_TYPE = "org.springframework.web.reactive.function.server.RouterFunctions";

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
				if (ROUTER_FUNCTION_TYPE.equals(resolvedBinding.getBinaryName())) {
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
				
				if (ROUTER_FUNCTIONS_TYPE.equals(methodBinding.getDeclaringClass().getBinaryName())
						&& "route".equals(node.getName().toString())) {
					extractMappingSymbol(node, doc, result);
				}
				else if (ROUTER_FUNCTION_TYPE.equals(methodBinding.getDeclaringClass().getBinaryName())
						&& "andRoute".equals(node.getName().toString())) {
					extractMappingSymbol(node, doc, result);
				}
				
				return super.visit(node);
			}
			
		});
		
		return result;
	}

	protected void extractMappingSymbol(MethodInvocation node, TextDocument doc, List<SymbolInformation> result) {
		List<?> arguments = node.arguments();
		if (arguments != null) {
			for (Object argument : arguments) {
				System.out.println(argument);
			}
		}
	}

}
