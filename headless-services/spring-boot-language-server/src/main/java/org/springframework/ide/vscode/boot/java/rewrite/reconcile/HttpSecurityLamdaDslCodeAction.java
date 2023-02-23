/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.rewrite.reconcile;

import java.util.List;

import org.openrewrite.ExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.spring.boot2.HttpSecurityLambdaDsl;
import org.openrewrite.java.tree.J.MethodInvocation;
import org.openrewrite.marker.Range;
import org.springframework.context.ApplicationContext;
import org.springframework.ide.vscode.boot.java.Boot2JavaProblemType;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.java.Version;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeCodeActionDescriptor;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeScope;
import org.springframework.ide.vscode.commons.rewrite.java.FixAssistMarker;
import org.springframework.ide.vscode.commons.rewrite.java.FixDescriptor;
import org.springframework.ide.vscode.commons.rewrite.java.JavaMarkerVisitor;

public class HttpSecurityLamdaDslCodeAction implements RecipeCodeActionDescriptor {

	private static final String PROBLEM_LABEL = "Consider switching to 'HttpSecurity' Lambda DSL syntax";

	private static final String FIX_LABEL = "Switch to 'HttpSecurity` Lambda DSL syntax";

	private HttpSecurityLambdaDsl recipe = new HttpSecurityLambdaDsl();

	@Override
	public JavaVisitor<ExecutionContext> getMarkerVisitor(ApplicationContext applicationContext) {
		return new JavaMarkerVisitor<ExecutionContext>() {

			@Override
			public MethodInvocation visitMethodInvocation(MethodInvocation method, ExecutionContext p) {
				if (recipe.getVisitor().isApplicableTopLevelMethodInvocation(method)) {
					// Don't step into the method any further
					String uri = getCursor().firstEnclosing(SourceFile.class).getSourcePath().toUri().toASCIIString();
					FixAssistMarker marker = new FixAssistMarker(Tree.randomId(), getId()).withLabel(PROBLEM_LABEL)
							.withFixes(
									new FixDescriptor(recipe.getName(), List.of(uri),
											RecipeCodeActionDescriptor.buildLabel(FIX_LABEL, RecipeScope.NODE))
											.withRangeScope(method.getMarkers().findFirst(Range.class).get())
											.withRecipeScope(RecipeScope.NODE),
									new FixDescriptor(recipe.getName(), List.of(uri),
											RecipeCodeActionDescriptor.buildLabel(FIX_LABEL, RecipeScope.FILE))
											.withRecipeScope(RecipeScope.FILE),
									new FixDescriptor(recipe.getName(), List.of(uri),
											RecipeCodeActionDescriptor.buildLabel(FIX_LABEL, RecipeScope.PROJECT))
											.withRecipeScope(RecipeScope.PROJECT));

					return method.withMarkers(method.getMarkers().add(marker));
				}
				return super.visitMethodInvocation(method, p);
			}

		};
	}

	@Override
	public boolean isApplicable(IJavaProject project) {
		Version version = SpringProjectUtil.getDependencyVersion(project, "spring-security-config");
		return version != null && version.compareTo(new Version(5, 2, 0, null)) >= 0;
	}

	@Override
	public ProblemType getProblemType() {
		return Boot2JavaProblemType.JAVA_LAMBDA_DSL;
	}

}
