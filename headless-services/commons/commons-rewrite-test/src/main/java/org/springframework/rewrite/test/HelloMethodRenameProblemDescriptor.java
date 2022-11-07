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

import java.util.List;

import org.openrewrite.ExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J.MethodDeclaration;
import org.openrewrite.marker.Range;
import org.springframework.context.ApplicationContext;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemCategory;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemSeverity;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemTypes;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeCodeActionDescriptor;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeScope;
import org.springframework.ide.vscode.commons.rewrite.java.FixAssistMarker;
import org.springframework.ide.vscode.commons.rewrite.java.FixDescriptor;

public class HelloMethodRenameProblemDescriptor implements RecipeCodeActionDescriptor {

	private static final String LABEL = "Switch hello method into bye";
	private static final String RECIPE_ID = "org.springframework.rewrite.test.HelloMethodRenameRecipe";

	@Override
	public JavaVisitor<ExecutionContext> getMarkerVisitor(ApplicationContext applicationContext) {
		return new JavaIsoVisitor<>() {

			@Override
			public MethodDeclaration visitMethodDeclaration(MethodDeclaration method, ExecutionContext p) {
				MethodDeclaration m = super.visitMethodDeclaration(method, p);
				if ("hello".equals(method.getSimpleName())) {
					String uri = getCursor().firstEnclosing(SourceFile.class).getSourcePath().toUri().toString();
					FixAssistMarker marker = new FixAssistMarker(Tree.randomId(), getId())
							.withFixes(
									new FixDescriptor(RECIPE_ID, List.of(uri), RecipeCodeActionDescriptor.buildLabel(LABEL, RecipeScope.NODE))
										.withRecipeScope(RecipeScope.NODE)
										.withRangeScope(m.getMarkers().findFirst(Range.class).get()),
									new FixDescriptor(RECIPE_ID, List.of(uri), RecipeCodeActionDescriptor.buildLabel(LABEL, RecipeScope.FILE))
										.withRecipeScope(RecipeScope.FILE),
									new FixDescriptor(RECIPE_ID, List.of(uri), RecipeCodeActionDescriptor.buildLabel(LABEL, RecipeScope.PROJECT))
										.withRecipeScope(RecipeScope.PROJECT)
							);
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
