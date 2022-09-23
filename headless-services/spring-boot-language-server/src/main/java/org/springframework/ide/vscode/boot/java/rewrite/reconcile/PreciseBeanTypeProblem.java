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

import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Tree;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.Return;
import org.openrewrite.marker.Range;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;
import org.springframework.ide.vscode.boot.java.Boot3JavaProblemType;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeCodeActionDescriptor;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeScope;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeSpringJavaProblemDescriptor;
import org.springframework.ide.vscode.commons.rewrite.java.FixAssistMarker;

public class PreciseBeanTypeProblem implements RecipeSpringJavaProblemDescriptor {

	private static final String LABEL = "Ensure concrete bean type";
	
	private static final String MSG_KEY = "returnType";

	@Override
	public String getRecipeId() {
		return "org.openrewrite.java.spring.boot3.PreciseBeanType";
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
		return new JavaIsoVisitor<>() {
			
            public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext executionContext) {
                J.MethodDeclaration m = super.visitMethodDeclaration(method, executionContext);
                if (m.getLeadingAnnotations().stream().anyMatch(a -> TypeUtils.isOfClassType(a.getType(), "org.springframework.context.annotation.Bean"))) {
                    Object o = getCursor().pollMessage(MSG_KEY);
                    if (o != null && !o.equals(m.getReturnTypeExpression().getType())) {
                    	if ((o instanceof JavaType.FullyQualified && m.getReturnTypeExpression().getType() instanceof JavaType.FullyQualified)
                    			|| (o instanceof JavaType.Array && m.getReturnTypeExpression().getType() instanceof JavaType.Array)) {
                        	m = m.withReturnTypeExpression(m.getReturnTypeExpression().withMarkers(m.getReturnTypeExpression().getMarkers().add(
                        			new FixAssistMarker(Tree.randomId()).withScope(m.getMarkers().findFirst(Range.class).get()).withRecipeId(getRecipeId()))));
                    	}
                    }
                }
                return m;
            }


			@Override
			public Return visitReturn(Return _return, ExecutionContext executionContext) {
                Cursor methodCursor = getCursor();
                if (_return.getExpression() != null) {
                    while (methodCursor != null && !(methodCursor.getValue() instanceof J.Lambda || methodCursor.getValue() instanceof J.MethodDeclaration)) {
                        methodCursor = methodCursor.getParent();
                    }
                    if (methodCursor != null && methodCursor.getValue() instanceof J.MethodDeclaration) {
                        methodCursor.putMessage(MSG_KEY, _return.getExpression().getType());
                    }
                }
                return super.visitReturn(_return, executionContext);
			}
			
		};
	}

	@Override
	public boolean isApplicable(IJavaProject project) {
		return springBootVersionGreaterOrEqual(3, 0, 0).test(project);
	}

	@Override
	public Boot3JavaProblemType getProblemType() {
		return Boot3JavaProblemType.JAVA_CONCRETE_BEAN_TYPE;
	}

}
