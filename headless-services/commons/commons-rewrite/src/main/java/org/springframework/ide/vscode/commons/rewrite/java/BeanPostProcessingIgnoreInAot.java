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

import java.util.ArrayList;
import java.util.List;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

public class BeanPostProcessingIgnoreInAot extends Recipe {

    private static final String RUNTIME_BEAN_POST_PROCESSOR = "org.springframework.beans.factory.config.BeanPostProcessor";
    private static final String COMPILE_BEAN_POST_PROCESSOR = "org.springframework.beans.factory.aot.BeanRegistrationAotProcessor";
    public static final String METHOD_NAME = "isBeanExcludedFromAotProcessing";

    @Override
    public String getDisplayName() {
        return "Prevent BeanPostProcessor from being ignored in AOT";
    }

    @Override
    public String getDescription() {
        return "Bean implementing both 'BeanPostProcessor' and 'BeanRegistrationAotProcessor' and does not return 'false' from 'isBeanExcludedFromAotProcessing()' method then Spring's behavior is to IGNORE the `BeanPostProcessor`."
        		+ " Recipe adds 'isBeanExcludedFromAotProcessing()' implemetation returning 'false' if necessary";
    }

    public static boolean isApplicableClass(J.ClassDeclaration classDecl) {
        JavaType.FullyQualified type = classDecl.getType();
        if (classDecl.getImplements() != null) {
            return classDecl.getImplements().stream()
                    .anyMatch(f -> TypeUtils.isAssignableTo(RUNTIME_BEAN_POST_PROCESSOR, type)
                            && TypeUtils.isAssignableTo(COMPILE_BEAN_POST_PROCESSOR, type));
        }
        return false;
    }

    public static boolean isApplicableMethod(J.MethodDeclaration m) {
        return METHOD_NAME.equals(m.getSimpleName()) && m.getParameters().size() == 1 && m.getParameters().get(0) instanceof J.Empty;
    }

    public static boolean isReturnTrue(J.MethodDeclaration m) {
        List<J.Return> returnStatements = findReturnStatementsInMethod(m);
        if (returnStatements.size() == 1) {
            J.Return returnStatement = returnStatements.get(0);
            if (returnStatement.getExpression() instanceof J.Literal) {
                if (((J.Literal) returnStatement.getExpression()).getValue() == Boolean.TRUE) {
                    // Method returns "true" boolean value constant
                    return true;
                }
            } else if (returnStatement.getExpression() instanceof J.FieldAccess) {
                J.FieldAccess fa = (J.FieldAccess) returnStatement.getExpression();
                if (fa.getName() != null && fa.getName().getSimpleName().equals("TRUE")) {
                    if (fa.getTarget() != null) {
                        JavaType.FullyQualified type = TypeUtils.asFullyQualified(fa.getTarget().getType());
                        // Method returns Boolean.TRUE
                        if ("java.lang.Boolean".equals(type.getFullyQualifiedName())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getApplicableTest() {
        return new JavaIsoVisitor<ExecutionContext>() {
            @Override
            public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext executionContext) {
                if (isApplicableClass(classDecl)) {
                    return classDecl.withMarkers(classDecl.getMarkers().searchResult());
                }
                return super.visitClassDeclaration(classDecl, executionContext);
            }
        };
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {

            private JavaTemplate createTemplate() {
                return JavaTemplate.builder(this::getCursor, "@Override\n"
                                + "public boolean isBeanExcludedFromAotProcessing() {\n"
                                + " return false;\n"
                                + "}\n"
                        )
                        .javaParser(() -> JavaParser.fromJavaVersion().build())
                        .build();
            }

            @Override
            public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext executionContext) {
                J.ClassDeclaration c = super.visitClassDeclaration(classDecl, executionContext);
                if (isApplicableClass(classDecl)) {
                    J.MethodDeclaration method = c.getBody().getStatements().stream().filter(J.MethodDeclaration.class::isInstance).map(J.MethodDeclaration.class::cast).filter(BeanPostProcessingIgnoreInAot::isApplicableMethod).findFirst().orElse(null);
                    if (method == null) {
                        J.Block body = c.getBody().withTemplate(createTemplate(), classDecl.getBody().getCoordinates().addMethodDeclaration((m1, m2) -> 1));
                        c = c.withBody(body);
                    }
                }
                return c;
            }

            @Override
            public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext executionContext) {
                J.MethodDeclaration m = super.visitMethodDeclaration(method, executionContext);
                if (isApplicableMethod(m)) {
                    J.ClassDeclaration c = getCursor().firstEnclosing(J.ClassDeclaration.class);
                    if (c != null && isApplicableClass(c) && isReturnTrue(m)) {
                        m = m.withTemplate(createTemplate(), m.getCoordinates().replace());
                    }
                }
                return m;
            }
        };
    }

    private static List<J.Return> findReturnStatementsInMethod(J.MethodDeclaration m) {
        List<J.Return> returnStatements = new ArrayList<>();
        new JavaIsoVisitor<List<J.Return>>() {

            @Override
            public J.Return visitReturn(J.Return _return, List<J.Return> returns) {
                if (getCursor().firstEnclosing(J.MethodDeclaration.class) == m) {
                    returns.add(_return);
                }
                return _return;
            }
        }.visit(m, returnStatements);
        return returnStatements;
    }
}