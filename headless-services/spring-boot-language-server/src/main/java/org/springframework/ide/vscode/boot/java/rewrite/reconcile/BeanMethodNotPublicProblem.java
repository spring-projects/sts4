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

import java.util.List;

import org.openrewrite.ExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.java.AnnotationMatcher;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeUtils;
import org.openrewrite.marker.Range;
import org.springframework.context.ApplicationContext;
import org.springframework.ide.vscode.boot.java.Boot2JavaProblemType;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeCodeActionDescriptor;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeScope;
import org.springframework.ide.vscode.commons.rewrite.java.FixAssistMarker;
import org.springframework.ide.vscode.commons.rewrite.java.FixDescriptor;

public class BeanMethodNotPublicProblem implements RecipeCodeActionDescriptor {
	
    private static final String ID = "org.openrewrite.java.spring.BeanMethodsNotPublic";
    
    private static final String LABEL = "Remove 'public' from @Bean method";

	private static final AnnotationMatcher BEAN_ANNOTATION_MATCHER = new AnnotationMatcher("@org.springframework.context.annotation.Bean");
    
	@Override
	public JavaVisitor<ExecutionContext> getMarkerVisitor(ApplicationContext applicationContext) {
		return new JavaIsoVisitor<ExecutionContext>() {

	        @Override
	        public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext executionContext) {
	            J.MethodDeclaration m = super.visitMethodDeclaration(method, executionContext);

	            if (m.getAllAnnotations().stream().anyMatch(BEAN_ANNOTATION_MATCHER::matches)
	                    && Boolean.FALSE.equals(TypeUtils.isOverride(method.getMethodType()))) {
	                // mark public modifier
					String uri = getCursor().firstEnclosing(SourceFile.class).getSourcePath().toUri().toASCIIString();
	        		FixAssistMarker fixAssistMarker = new FixAssistMarker(Tree.randomId(), getId())
	        				.withFixes(
	        						new FixDescriptor(ID, List.of(uri), LABEL)
	        							.withRangeScope(m.getMarkers().findFirst(Range.class).get())
	        							.withRecipeScope(RecipeScope.NODE),
	        						new FixDescriptor(ID, List.of(uri), RecipeCodeActionDescriptor.buildLabel(LABEL, RecipeScope.FILE))
	        							.withRecipeScope(RecipeScope.FILE),
		        					new FixDescriptor(ID, List.of(uri), RecipeCodeActionDescriptor.buildLabel(LABEL, RecipeScope.PROJECT))
	        							.withRecipeScope(RecipeScope.PROJECT)
	        							
	        				);
	            	m = m.withModifiers(ListUtils.map(m.getModifiers(), modifier -> {
	            		if (modifier.getType() == J.Modifier.Type.Public) {
	            			return modifier.withMarkers(modifier.getMarkers().add(fixAssistMarker));
	            		}
	            		return modifier;
	            	}));
	            }
	            return m;
	        }

	    };
	}

	@Override
	public boolean isApplicable(IJavaProject project) {
		return SpringProjectUtil.getDependencyVersion(project, SpringProjectUtil.SPRING_BOOT) != null;
	}

	@Override
	public Boot2JavaProblemType getProblemType() {
		return Boot2JavaProblemType.JAVA_PUBLIC_BEAN_METHOD;
	}

}
