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
import org.openrewrite.java.AnnotationMatcher;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.marker.Range;
import org.springframework.context.ApplicationContext;
import org.springframework.ide.vscode.boot.java.Boot2JavaProblemType;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeCodeActionDescriptor;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeScope;
import org.springframework.ide.vscode.commons.rewrite.java.FixAssistMarker;
import org.springframework.ide.vscode.commons.rewrite.java.FixDescriptor;

public class NoRequestMappingAnnotationCodeAction implements RecipeCodeActionDescriptor {
	
    private static final String LABEL = "Replace @RequestMapping with specific @GetMapping, @PostMapping etc.";
	private static final String ID = "org.openrewrite.java.spring.NoRequestMappingAnnotation";
	private static final AnnotationMatcher REQUEST_MAPPING_ANNOTATION_MATCHER = new AnnotationMatcher("@org.springframework.web.bind.annotation.RequestMapping");

	@Override
	public JavaVisitor<ExecutionContext> getMarkerVisitor(ApplicationContext applicationContext) {
		return new JavaIsoVisitor<ExecutionContext>() {
	        @Override
	        public J.Annotation visitAnnotation(J.Annotation annotation, ExecutionContext ctx) {
	            J.Annotation a = super.visitAnnotation(annotation, ctx);
	            if (REQUEST_MAPPING_ANNOTATION_MATCHER.matches(a) && getCursor().getParentOrThrow().getValue() instanceof J.MethodDeclaration) {
					String uri = getCursor().firstEnclosing(SourceFile.class).getSourcePath().toUri().toASCIIString();
            		FixAssistMarker fixAssistMarker = new FixAssistMarker(Tree.randomId(), getId())
            				.withFixes(
            					new FixDescriptor(ID, List.of(uri), RecipeCodeActionDescriptor.buildLabel(LABEL, RecipeScope.NODE))
            						.withRangeScope(a.getMarkers().findFirst(Range.class).get())
            						.withRecipeScope(RecipeScope.NODE),
                				new FixDescriptor(ID, List.of(uri), RecipeCodeActionDescriptor.buildLabel(LABEL, RecipeScope.FILE))
            						.withRecipeScope(RecipeScope.FILE),
                    				new FixDescriptor(ID, List.of(uri), RecipeCodeActionDescriptor.buildLabel(LABEL, RecipeScope.PROJECT))
                						.withRecipeScope(RecipeScope.PROJECT)
            				);
	            	a = a.withMarkers(a.getMarkers().add(fixAssistMarker));
	            }
	            return a;
	        }

		};
	}

	@Override
	public boolean isApplicable(IJavaProject project) {
		return springBootVersionGreaterOrEqual(2, 0, 0).test(project);
	}

	@Override
	public ProblemType getProblemType() {
		return Boot2JavaProblemType.JAVA_PRECISE_REQUEST_MAPPING;
	}

}
