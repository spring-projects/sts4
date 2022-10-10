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
package org.springframework.ide.vscode.boot.java.rewrite.reconcile;

import static org.springframework.ide.vscode.commons.java.SpringProjectUtil.springBootVersionGreaterOrEqual;

import java.util.List;
import java.util.stream.Collectors;

import org.openrewrite.ExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J.ClassDeclaration;
import org.openrewrite.java.tree.J.MethodDeclaration;
import org.openrewrite.marker.Range;
import org.springframework.context.ApplicationContext;
import org.springframework.ide.vscode.boot.java.Boot3JavaProblemType;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeCodeActionDescriptor;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeScope;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeSpringJavaProblemDescriptor;
import org.springframework.ide.vscode.commons.rewrite.java.BeanPostProcessingIgnoreInAot;
import org.springframework.ide.vscode.commons.rewrite.java.FixAssistMarker;
import org.springframework.ide.vscode.commons.rewrite.java.FixDescriptor;

public class BeanPostProcessingIgnoreInAotProblem implements RecipeSpringJavaProblemDescriptor {
	
	private static final String RECIPE_ID = "org.springframework.ide.vscode.commons.rewrite.java.BeanPostProcessingIgnoreInAot";
	private static final String LABEL = "Add method 'isBeanExcludedFromAotProcessing' that returns 'false'";

	@Override
	public JavaVisitor<ExecutionContext> getMarkerVisitor(ApplicationContext applicationContext) {
		return new JavaIsoVisitor<ExecutionContext>() {

			@Override
			public ClassDeclaration visitClassDeclaration(ClassDeclaration classDecl, ExecutionContext p) {
				ClassDeclaration c = super.visitClassDeclaration(classDecl, p);
				if (BeanPostProcessingIgnoreInAot.isApplicableClass(classDecl)) {
					List<MethodDeclaration> methods = classDecl.getBody().getStatements().stream()
							.filter(MethodDeclaration.class::isInstance).map(MethodDeclaration.class::cast)
							.filter(BeanPostProcessingIgnoreInAot::isApplicableMethod)
							.collect(Collectors.toList());
					String uri = getCursor().firstEnclosing(SourceFile.class).getSourcePath().toUri().toString();
					FixAssistMarker marker = new FixAssistMarker(Tree.randomId(), getId())
							.withFixes(
									new FixDescriptor(RECIPE_ID, List.of(uri), RecipeCodeActionDescriptor.buildLabel(LABEL, RecipeScope.NODE))
										.withRangeScope(classDecl.getMarkers().findFirst(Range.class).orElse(null))
										.withRecipeScope(RecipeScope.NODE),
									new FixDescriptor(RECIPE_ID, List.of(uri), RecipeCodeActionDescriptor.buildLabel(LABEL, RecipeScope.FILE))
										.withRecipeScope(RecipeScope.FILE),
									new FixDescriptor(RECIPE_ID, List.of(uri), RecipeCodeActionDescriptor.buildLabel(LABEL, RecipeScope.PROJECT))
										.withRecipeScope(RecipeScope.PROJECT)
							);
					if (methods.isEmpty()) {
						// Didn't find a method. Default implementation return true therefore mark it.
						c = c.withName(c.getName().withMarkers(c.getName().getMarkers().add(marker)));
					} else {
						MethodDeclaration m = methods.stream().filter(BeanPostProcessingIgnoreInAot::isReturnTrue).findFirst().orElse(null);
						// Found method that return true explicitly
						if (m != null) {
							c = c.withName(c.getName().withMarkers(c.getName().getMarkers().add(marker)));
						}
					}
				}
				return c;
			}
			
		};
	}

	@Override
	public boolean isApplicable(IJavaProject project) {
		return springBootVersionGreaterOrEqual(3, 0, 0).test(project);
	}

	@Override
	public ProblemType getProblemType() {
		return Boot3JavaProblemType.JAVA_BEAN_POST_PROCESSOR_IGNORED_IN_AOT;
	}

}
