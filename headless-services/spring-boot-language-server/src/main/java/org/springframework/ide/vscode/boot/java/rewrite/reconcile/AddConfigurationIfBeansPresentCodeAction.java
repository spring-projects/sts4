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
import org.openrewrite.java.spring.boot2.AddConfigurationAnnotationIfBeansPresent;
import org.openrewrite.java.tree.J.ClassDeclaration;
import org.openrewrite.java.tree.J.MethodDeclaration;
import org.openrewrite.java.tree.J.VariableDeclarations;
import org.springframework.ide.vscode.boot.java.Boot2JavaProblemType;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.java.Version;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.rewrite.config.MarkerVisitorContext;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeCodeActionDescriptor;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeScope;
import org.springframework.ide.vscode.commons.rewrite.java.FixAssistMarker;
import org.springframework.ide.vscode.commons.rewrite.java.FixDescriptor;
import org.springframework.ide.vscode.commons.rewrite.java.JavaMarkerVisitor;

public class AddConfigurationIfBeansPresentCodeAction implements RecipeCodeActionDescriptor {
	
	private static final String ID = "org.openrewrite.java.spring.boot2.AddConfigurationAnnotationIfBeansPresent";
	
	private static final String PROBLEM_LABEL = "'@Configuration' is missing on a class defining Spring Beans";
	
	private static final String FIX_LABEL = "Add missing '@Configuration' annotations over classes";
	
	@Override
	public JavaVisitor<ExecutionContext> getMarkerVisitor(MarkerVisitorContext context) {
		return new JavaMarkerVisitor<ExecutionContext>() {
			
			@Override
			public MethodDeclaration visitMethodDeclaration(MethodDeclaration method, ExecutionContext p) {
				return method;
			}
			
			@Override
			public VariableDeclarations visitVariableDeclarations(VariableDeclarations multiVariable,
					ExecutionContext p) {
				return multiVariable;
			}

			@Override
			public ClassDeclaration visitClassDeclaration(ClassDeclaration classDecl, ExecutionContext p) {
				ClassDeclaration c = super.visitClassDeclaration(classDecl, p);
				if (AddConfigurationAnnotationIfBeansPresent.isApplicableClass(classDecl, getCursor())) {
					String uri = getCursor().firstEnclosing(SourceFile.class).getSourcePath().toUri().toASCIIString();
					FixAssistMarker marker = new FixAssistMarker(Tree.randomId(), ID).withLabel(PROBLEM_LABEL)
							.withFixes(
									new FixDescriptor(ID, List.of(uri),
											RecipeCodeActionDescriptor.buildLabel(FIX_LABEL, RecipeScope.FILE))
											.withRecipeScope(RecipeScope.FILE),
									new FixDescriptor(ID, List.of(uri),
											RecipeCodeActionDescriptor.buildLabel(FIX_LABEL, RecipeScope.PROJECT))
											.withRecipeScope(RecipeScope.PROJECT));
					c = c.withName(c.getName().withMarkers(c.getName().getMarkers().add(marker)));
				}
				return c;
			}
			
		};
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public ProblemType getProblemType() {
		return Boot2JavaProblemType.MISSING_CONFIGURATION_ANNOTATION;
	}

	@Override
	public boolean isApplicable(IJavaProject project) {
		Version version = SpringProjectUtil.getDependencyVersion(project, "spring-context");
		return version != null && version.compareTo(new Version(3, 0, 0, null)) >= 0;
	}

}
