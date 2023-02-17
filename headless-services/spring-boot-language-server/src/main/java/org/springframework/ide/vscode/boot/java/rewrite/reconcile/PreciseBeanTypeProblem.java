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

import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.Return;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;
import org.openrewrite.marker.Range;
import org.springframework.context.ApplicationContext;
import org.springframework.ide.vscode.boot.java.SpringAotJavaProblemType;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeCodeActionDescriptor;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeScope;
import org.springframework.ide.vscode.commons.rewrite.java.FixAssistMarker;
import org.springframework.ide.vscode.commons.rewrite.java.FixDescriptor;
import org.springframework.ide.vscode.commons.rewrite.java.JavaMarkerVisitor;

public class PreciseBeanTypeProblem implements RecipeCodeActionDescriptor {

	private static final String RECIPE_ID = "org.openrewrite.java.spring.boot3.PreciseBeanType";

	private static final String LABEL = "Ensure concrete bean type";
	
	private static final String MSG_KEY = "returnType";

	@Override
	public JavaVisitor<ExecutionContext> getMarkerVisitor(ApplicationContext applicationContext) {
		return new JavaMarkerVisitor<>() {
			
            public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext executionContext) {
                J.MethodDeclaration m = super.visitMethodDeclaration(method, executionContext);
                if (m.getLeadingAnnotations().stream().anyMatch(a -> TypeUtils.isOfClassType(a.getType(), "org.springframework.context.annotation.Bean"))) {
                    Object o = getCursor().pollMessage(MSG_KEY);
                    if (o != null && !areTypesEqual((JavaType) o, m.getReturnTypeExpression().getType())) {
                    	if ((o instanceof JavaType.FullyQualified && m.getReturnTypeExpression().getType() instanceof JavaType.FullyQualified)
                    			|| (o instanceof JavaType.Array && m.getReturnTypeExpression().getType() instanceof JavaType.Array)) {
                    		
							String uri = getCursor().firstEnclosing(SourceFile.class).getSourcePath().toUri().toASCIIString();
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
                        	m = m.withReturnTypeExpression(m.getReturnTypeExpression().withMarkers(m.getReturnTypeExpression().getMarkers().add(marker)));
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
	
	private static boolean areTypesEqual(JavaType a, JavaType b) {
		if (a instanceof JavaType.Parameterized && b instanceof JavaType.Parameterized) {
			JavaType.Parameterized ap = (JavaType.Parameterized) a;
			JavaType.Parameterized bp = (JavaType.Parameterized) b;
			if (ap.getTypeParameters().size() != bp.getTypeParameters().size()) {
				return false;
			}
			if (areTypesEqual(ap.getType(), bp.getType())) {
				for (int i = 0; i < ap.getTypeParameters().size(); i++) {
					if (!areTypesEqual(ap.getTypeParameters().get(i), bp.getTypeParameters().get(i))) {
						return false;
					}
				}
			}
		} else if (a instanceof JavaType.Parameterized && b instanceof JavaType.FullyQualified) {
			return areTypesEqual(((JavaType.Parameterized) a).getType(), b);
		} else if (a instanceof JavaType.FullyQualified && b instanceof JavaType.Parameterized) {
			return areTypesEqual(a, ((JavaType.Parameterized) b).getType());
		} else if (a instanceof JavaType.Array && b instanceof JavaType.Array) {
			return areTypesEqual(((JavaType.Array) a).getElemType(), ((JavaType.Array) b).getElemType());
		}
		if (a instanceof JavaType.GenericTypeVariable || b instanceof JavaType.GenericTypeVariable) {
			return true;
		}
		if (a == JavaType.Primitive.String && b instanceof JavaType.FullyQualified) {
			return JavaType.Primitive.String.getClassName().equals(((JavaType.FullyQualified) b).getFullyQualifiedName());
		}
		if (b == JavaType.Primitive.String && b instanceof JavaType.FullyQualified) {
			return JavaType.Primitive.String.getClassName().equals(((JavaType.FullyQualified) a).getFullyQualifiedName());
		}
		return a.equals(b);
	}

	@Override
	public boolean isApplicable(IJavaProject project) {
		return springBootVersionGreaterOrEqual(3, 0, 0).test(project);
	}

	@Override
	public SpringAotJavaProblemType getProblemType() {
		return SpringAotJavaProblemType.JAVA_CONCRETE_BEAN_TYPE;
	}

}
