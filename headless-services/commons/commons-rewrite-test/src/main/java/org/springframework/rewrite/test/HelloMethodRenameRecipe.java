/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.rewrite.test;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J.MethodDeclaration;
import org.openrewrite.java.tree.J.MethodInvocation;

public class HelloMethodRenameRecipe extends Recipe {
	
	@Override
	public String getDisplayName() {
		return "Rename hello method into bye";
	}

	@Override
	protected TreeVisitor<?, ExecutionContext> getVisitor() {
		return new JavaIsoVisitor<>() {

			@Override
			public MethodDeclaration visitMethodDeclaration(MethodDeclaration method, ExecutionContext p) {
				MethodDeclaration m = super.visitMethodDeclaration(method, p);
				if ("hello".equals(m.getSimpleName())) {
					m = m.withName(m.getName().withSimpleName("bye"));
				}
				return m;
			}

			@Override
			public MethodInvocation visitMethodInvocation(MethodInvocation method, ExecutionContext p) {
				MethodInvocation m = super.visitMethodInvocation(method, p);
				if ("hello".equals(m.getSimpleName())) {
					m = m.withName(m.getName().withSimpleName("bye"));
				}
				return m;
			}
			
		};
	}
	
	

}
