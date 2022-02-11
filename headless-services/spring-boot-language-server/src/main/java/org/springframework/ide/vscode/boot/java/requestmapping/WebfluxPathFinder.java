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
import java.util.List;

import org.eclipse.lsp4j.Range;
import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.Literal;
import org.openrewrite.java.tree.J.MethodInvocation;
import org.openrewrite.java.tree.JavaType.Method;
import org.springframework.ide.vscode.boot.java.utils.ORAstUtils;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class WebfluxPathFinder extends JavaIsoVisitor<ExecutionContext> {

	private List<WebfluxRouteElement> path;
	private J root;
	private TextDocument doc;

	public WebfluxPathFinder(J root, TextDocument doc) {
		this.root = root;
		this.doc = doc;
		this.path = new ArrayList<>();
	}

	public List<WebfluxRouteElement> getPath() {
		return path;
	}
	
	@Override
	public MethodInvocation visitMethodInvocation(MethodInvocation methodInvocation, ExecutionContext p) {
		boolean visitChildren = true;

		if (methodInvocation != this.root) {
			Method method = methodInvocation.getMethodType();

			try {
				if (method != null && method.getDeclaringType() != null
						&& WebfluxUtils.REQUEST_PREDICATES_TYPE.equals(method.getDeclaringType().getFullyQualifiedName())) {

					String name = method.getName();
					if (name != null && WebfluxUtils.REQUEST_PREDICATE_ALL_PATH_METHODS.contains(name)) {
						Literal stringLiteral = WebfluxUtils.extractArgument(methodInvocation, Literal.class);
						if (stringLiteral != null) {
							org.openrewrite.marker.Range r = ORAstUtils.getRange(stringLiteral);
							Range range = doc.toRange(r.getStart().getOffset(), r.length());
							path.add(new WebfluxRouteElement(ORAstUtils.getLiteralValue(stringLiteral), range));
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
