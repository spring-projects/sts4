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
package org.springframework.ide.vscode.boot.java.rewrite.codeaction;

import static org.springframework.ide.vscode.commons.java.SpringProjectUtil.springBootVersionGreaterOrEqual;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Tree;
import org.openrewrite.java.AnnotationMatcher;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J;
import org.springframework.ide.vscode.boot.java.rewrite.RecipeCodeActionDescriptor;
import org.springframework.ide.vscode.boot.java.rewrite.RecipeScope;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.rewrite.java.FixAssistMarker;

public class NoRequestMappingAnnotationCodeAction implements RecipeCodeActionDescriptor {
	
    private static final String LABEL = "Replace @RequestMapping with specific @GetMapping, @PostMapping etc.";
	private static final String ID = "org.openrewrite.java.spring.NoRequestMappingAnnotation";
	private static final AnnotationMatcher REQUEST_MAPPING_ANNOTATION_MATCHER = new AnnotationMatcher("@org.springframework.web.bind.annotation.RequestMapping");

	@Override
	public String getRecipeId() {
		return ID;
	}

	@Override
	public String getLabel(RecipeScope s) {
		return RecipeCodeActionDescriptor.buildLabel(LABEL, s);
	}

	@Override
	public RecipeScope[] getScopes() {
		return RecipeScope.values();
	}

	@Override
	public JavaVisitor<ExecutionContext> getMarkerVisitor() {
		return new JavaIsoVisitor<ExecutionContext>() {
	        @Override
	        public J.Annotation visitAnnotation(J.Annotation annotation, ExecutionContext ctx) {
	            J.Annotation a = super.visitAnnotation(annotation, ctx);
	            if (REQUEST_MAPPING_ANNOTATION_MATCHER.matches(a) && getCursor().getParentOrThrow().getValue() instanceof J.MethodDeclaration) {
            		FixAssistMarker fixAssistMarker = new FixAssistMarker(Tree.randomId())
            				.withRecipeId(getRecipeId())
            				.withScope(a.getId());
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

}
