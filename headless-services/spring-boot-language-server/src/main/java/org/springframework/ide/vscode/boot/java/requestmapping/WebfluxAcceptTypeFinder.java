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

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * @author Martin Lippert
 */
public class WebfluxAcceptTypeFinder extends ASTVisitor {
	
	private Set<String> acceptTypes;
	
	public WebfluxAcceptTypeFinder() {
		this.acceptTypes = new LinkedHashSet<>();
	}
	
	public Set<String> getAcceptTypes() {
		return acceptTypes;
	}
	
	@Override
	public boolean visit(MethodInvocation node) {
		IMethodBinding methodBinding = node.resolveMethodBinding();

		if (WebfluxUtils.REQUEST_PREDICATES_TYPE.equals(methodBinding.getDeclaringClass().getBinaryName())) {
			String name = methodBinding.getName();
			if (name != null && WebfluxUtils.REQUEST_PREDICATE_ACCEPT_TYPE_METHOD.equals(name)) {
				String acceptType = WebfluxUtils.extractSimpleNameArgument(node);
				if (acceptType != null) {
					acceptTypes.add(acceptType);
				}
			}
		}

		return !WebfluxUtils.isRouteMethodInvocation(methodBinding);
	}

}
