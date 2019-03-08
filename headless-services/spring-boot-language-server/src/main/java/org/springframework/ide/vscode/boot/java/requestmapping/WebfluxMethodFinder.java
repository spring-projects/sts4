/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
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
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.lsp4j.Range;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class WebfluxMethodFinder extends ASTVisitor {
	
	private List<WebfluxRouteElement> methods;
	private ASTNode root;
	private TextDocument doc;
	
	public WebfluxMethodFinder(ASTNode root, TextDocument doc) {
		this.root = root;
		this.doc = doc;
		this.methods = new ArrayList<>();
	}
	
	public List<WebfluxRouteElement> getMethods() {
		return methods;
	}
	
	@Override
	public boolean visit(MethodInvocation node) {
		boolean visitChildren = true;

		if (node != this.root) {
			IMethodBinding methodBinding = node.resolveMethodBinding();
			
			try {
				if (WebfluxUtils.REQUEST_PREDICATES_TYPE.equals(methodBinding.getDeclaringClass().getBinaryName())) {
					String name = methodBinding.getName();
					if (name != null && WebfluxUtils.REQUEST_PREDICATE_HTTPMETHOD_METHODS.contains(name)) {
						Range range = doc.toRange(node.getStartPosition(), node.getLength());
						methods.add(new WebfluxRouteElement(name, range));
					}
					else if (name != null && WebfluxUtils.REQUEST_PREDICATE_METHOD_METHOD.equals(name)) {
						QualifiedName qualifiedName = WebfluxUtils.extractQualifiedNameArgument(node);
						if (qualifiedName.getName() != null) {
							Range range = doc.toRange(qualifiedName.getStartPosition(), qualifiedName.getLength());
							methods.add(new WebfluxRouteElement(qualifiedName.getName().toString(), range));
						}
					}
				}
			}
			catch (BadLocationException e) {
				// ignore
			}

			if (WebfluxUtils.isRouteMethodInvocation(methodBinding)) {
				visitChildren = false;
			}
		}
		return visitChildren;
	}

}
