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
import org.openrewrite.Tree;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J.MethodDeclaration;
import org.openrewrite.marker.Range;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemCategory;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemSeverity;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemTypes;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeCodeActionDescriptor;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeScope;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeSpringJavaProblemDescriptor;
import org.springframework.ide.vscode.commons.rewrite.java.FixAssistMarker;

public class HelloMethodRenameProblemDescriptor implements RecipeSpringJavaProblemDescriptor {

	@Override
	public String getRecipeId() {
		return "org.springframework.rewrite.test.HelloMethodRenameRecipe";
	}

	@Override
	public String getLabel(RecipeScope s) {
		return RecipeCodeActionDescriptor.buildLabel("Switch hello method into bye", s);
	}

	@Override
	public RecipeScope[] getScopes() {
		return RecipeScope.values();
	}

	@Override
	public JavaVisitor<ExecutionContext> getMarkerVisitor() {
		return new JavaIsoVisitor<>() {

			@Override
			public MethodDeclaration visitMethodDeclaration(MethodDeclaration method, ExecutionContext p) {
				MethodDeclaration m = super.visitMethodDeclaration(method, p);
				if ("hello".equals(method.getSimpleName())) {
					FixAssistMarker marker = new FixAssistMarker(Tree.randomId()).withRecipeId(getRecipeId()).withScope(m.getMarkers().findFirst(Range.class).get());
					m = m.withName(m.getName().withMarkers(m.getName().getMarkers().add(marker)));
				}
				return m;
			}
			
		};
	}

	@Override
	public boolean isApplicable(IJavaProject project) {
		return true;
	}

	@Override
	public ProblemType getProblemType() {
		return ProblemTypes.create("Hello Method!", ProblemSeverity.ERROR, ProblemCategory.NO_CATEGORY);
	}

}
