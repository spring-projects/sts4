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

import org.openrewrite.ExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.ClassDeclaration;
import org.openrewrite.marker.Range;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.Boot2JavaProblemType;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeCodeActionDescriptor;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeScope;
import org.springframework.ide.vscode.commons.rewrite.java.FixAssistMarker;
import org.springframework.ide.vscode.commons.rewrite.java.FixDescriptor;

public class NoRepoAnnotationProblem implements RecipeCodeActionDescriptor {

	private static final String ID = "org.openrewrite.java.spring.NoRepoAnnotationOnRepoInterface";
	private static final String LABEL = "Remove Unnecessary @Repository";
	private static final String INTERFACE_REPOSITORY = "org.springframework.data.repository.Repository";
	private static final String ANNOTATION_REPOSITORY = Annotations.REPOSITORY;

	@Override
	public JavaVisitor<ExecutionContext> getMarkerVisitor(ApplicationContext applicationContext) {
		return new JavaIsoVisitor<ExecutionContext>() {

			@Override
			public ClassDeclaration visitClassDeclaration(ClassDeclaration classDecl,
					ExecutionContext executionContext) {
				J.ClassDeclaration c = super.visitClassDeclaration(classDecl, executionContext);
				if (c.getKind() == ClassDeclaration.Kind.Type.Interface) {
					final J.Annotation repoAnnotation = c.getLeadingAnnotations().stream().filter(annotation -> {
						if (annotation.getArguments() == null || annotation.getArguments().isEmpty()
								|| annotation.getArguments().get(0) instanceof J.Empty) {
							JavaType.FullyQualified type = TypeUtils.asFullyQualified(annotation.getType());
							return type != null && ANNOTATION_REPOSITORY.equals(type.getFullyQualifiedName());
						}
						return false;
					}).findFirst().orElse(null);
					if (repoAnnotation != null && TypeUtils.isAssignableTo(INTERFACE_REPOSITORY, c.getType())) {
						c = c.withLeadingAnnotations(ListUtils.map(c.getLeadingAnnotations(), a -> {
							if (a == repoAnnotation) {
								String uri = getCursor().firstEnclosing(SourceFile.class).getSourcePath().toUri()
										.toASCIIString();
								FixAssistMarker fixAssistMarker = new FixAssistMarker(Tree.randomId(), getId()).withFixes(
										new FixDescriptor(ID, List.of(uri), LABEL)
												.withRangeScope(classDecl.getMarkers().findFirst(Range.class).get())
												.withRecipeScope(RecipeScope.NODE),
										new FixDescriptor(ID, List.of(uri),
												RecipeCodeActionDescriptor.buildLabel(LABEL, RecipeScope.FILE))
												.withRecipeScope(RecipeScope.FILE),
										new FixDescriptor(ID, List.of(uri),
												RecipeCodeActionDescriptor.buildLabel(LABEL, RecipeScope.PROJECT))
												.withRecipeScope(RecipeScope.PROJECT)

								);
								return a.withMarkers(a.getMarkers().add(fixAssistMarker));
							}
							return a;
						}));
					}
				}
				return c;
			}

		};
	}

	@Override
	public boolean isApplicable(IJavaProject project) {
		return springBootVersionGreaterOrEqual(2, 0, 0).test(project);
	}

	@Override
	public Boot2JavaProblemType getProblemType() {
		return Boot2JavaProblemType.JAVA_REPOSITORY;
	}

}
