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

import org.eclipse.lsp4j.Range;
import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.FieldAccess;
import org.openrewrite.java.tree.J.MethodInvocation;
import org.openrewrite.java.tree.JavaType.Method;
import org.springframework.ide.vscode.boot.java.utils.ORAstUtils;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class WebfluxMethodFinder extends JavaIsoVisitor<ExecutionContext> {
	
	private List<WebfluxRouteElement> methods;
	private J root;
	private TextDocument doc;
	
	public WebfluxMethodFinder(J root, TextDocument doc) {
		this.root = root;
		this.doc = doc;
		this.methods = new ArrayList<>();
	}
	
	public List<WebfluxRouteElement> getMethods() {
		return methods;
	}
	
	@Override
	public MethodInvocation visitMethodInvocation(MethodInvocation methodInvocation, ExecutionContext p) {
		boolean visitChildren = true;

		if (methodInvocation != this.root) {
			Method method = methodInvocation.getMethodType();
			try {
				if (method != null && method.getDeclaringType() != null && WebfluxUtils.REQUEST_PREDICATES_TYPE.equals(method.getDeclaringType().getFullyQualifiedName())) {
					String name = method.getName();
					if (name != null && WebfluxUtils.REQUEST_PREDICATE_HTTPMETHOD_METHODS.contains(name)) {
						org.openrewrite.marker.Range r = ORAstUtils.getRange(methodInvocation);
						Range range = doc.toRange(r.getStart().getOffset(), r.length());
						methods.add(new WebfluxRouteElement(name, range));
					}
					else if (name != null && WebfluxUtils.REQUEST_PREDICATE_METHOD_METHOD.equals(name)) {
						FieldAccess qualifiedName = WebfluxUtils.extractArgument(methodInvocation, FieldAccess.class);
						if (qualifiedName != null) {
							org.openrewrite.marker.Range r = ORAstUtils.getRange(qualifiedName);
							Range range = doc.toRange(r.getStart().getOffset(), r.length());
							methods.add(new WebfluxRouteElement(qualifiedName.getName().toString(), range));
						}
					}
				}
			}
			catch (BadLocationException e) {
				// ignore
			}

			if (WebfluxUtils.isRouteMethodInvocation(method)) {
				visitChildren = false;
			}
		}
		return visitChildren ? super.visitMethodInvocation(methodInvocation, p) : methodInvocation;
	}
	
}
