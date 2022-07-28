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
package org.springframework.ide.vscode.commons.rewrite.java;

import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeTree;
import org.openrewrite.java.tree.TypeUtils;

public class PreciseBeanType extends Recipe {

    private final static String BEAN = "org.springframework.context.annotation.Bean";

    private final static String MSG_KEY = "returnType";

    @Override
    public String getDisplayName() {
        return "Replace Bean method return types with concrete types being returned. This is required for Spring 6 AOT";
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getSingleSourceApplicableTest() {
        return new UsesType<ExecutionContext>(BEAN);
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {
            @Override
            public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext executionContext) {
                J.MethodDeclaration m = super.visitMethodDeclaration(method, executionContext);
                if (isBeanMethod(m)) {
                    Object o = getCursor().pollMessage(MSG_KEY);
                    if (o != null) {
                        if (!o.equals(method.getReturnTypeExpression().getType())) {
                        	if (o instanceof JavaType.FullyQualified) {
                        		JavaType.FullyQualified actualType = (JavaType.FullyQualified) o;
                                if (m.getReturnTypeExpression() instanceof J.Identifier) {
                                    J.Identifier identifierReturnExpr = (J.Identifier) m.getReturnTypeExpression();
                                    maybeAddImport(actualType);
                                    if (identifierReturnExpr.getType() instanceof JavaType.FullyQualified) {
                                        maybeRemoveImport((JavaType.FullyQualified) identifierReturnExpr.getType());
                                    }
                                    m = m.withReturnTypeExpression(identifierReturnExpr
                                            .withType(actualType)
                                            .withSimpleName(actualType.getClassName())
                                    );
                                } else if (m.getReturnTypeExpression() instanceof J.ParameterizedType) {
                                    J.ParameterizedType parameterizedType = (J.ParameterizedType) m.getReturnTypeExpression();
                                    maybeAddImport(actualType);
                                    if (parameterizedType.getType() instanceof JavaType.FullyQualified) {
                                        maybeRemoveImport((JavaType.FullyQualified) parameterizedType.getType());
                                    }
                                    m = m.withReturnTypeExpression(parameterizedType
                                            .withType(actualType)
                                            .withClazz(TypeTree.build(actualType.getClassName()).withType(actualType))
                                    );
                                }
                        		
                        	} else if (o instanceof JavaType.Array) {
                        		JavaType.Array actualType = (JavaType.Array) o;
                                if (m.getReturnTypeExpression() instanceof J.ArrayType && actualType.getElemType() instanceof JavaType.FullyQualified) {
                                	JavaType.FullyQualified actualElementType = (JavaType.FullyQualified) actualType.getElemType();
                                    J.ArrayType arrayType = (J.ArrayType) m.getReturnTypeExpression();
                                    maybeAddImport(actualElementType);
                                    if (arrayType.getElementType() instanceof JavaType.FullyQualified) {
                                        maybeRemoveImport((JavaType.FullyQualified) arrayType.getElementType());
                                    }
                                    m = m.withReturnTypeExpression(arrayType
                                            .withElementType(TypeTree.build(actualElementType.getClassName()).withType(actualType))
                                    );
                                }
                        	}
                        }
                    }
                }
                return m;
            }

            private boolean isBeanMethod(J.MethodDeclaration m) {
                return m.getLeadingAnnotations().stream().anyMatch(a -> TypeUtils.isOfClassType(a.getType(), BEAN));
            }

            @Override
            public J.Return visitReturn(J.Return _return, ExecutionContext executionContext) {
                Cursor methodCursor = getCursor();
                while (methodCursor != null && !(methodCursor.getValue() instanceof J.Lambda || methodCursor.getValue() instanceof J.MethodDeclaration)) {
                    methodCursor = methodCursor.getParent();
                }
                if (methodCursor != null && methodCursor.getValue() instanceof J.MethodDeclaration) {
                    methodCursor.putMessage(MSG_KEY, _return.getExpression().getType());
                }
                return super.visitReturn(_return, executionContext);
            }
        };
    }
}
