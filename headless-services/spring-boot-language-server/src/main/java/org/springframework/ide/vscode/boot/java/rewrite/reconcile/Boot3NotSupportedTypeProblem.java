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
import org.openrewrite.Tree;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.MethodInvocation;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.JavaType.FullyQualified;
import org.openrewrite.java.tree.JavaType.Method;
import org.openrewrite.java.tree.NameTree;
import org.openrewrite.java.tree.TypeUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.ide.vscode.boot.java.Boot3JavaProblemType;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeSpringJavaProblemDescriptor;
import org.springframework.ide.vscode.commons.rewrite.java.FixAssistMarker;

public class Boot3NotSupportedTypeProblem implements RecipeSpringJavaProblemDescriptor {
	
	private static final List<String> TYPE_FQNAMES = List.of(
			"org.springframework.web.multipart.commons.CommonsMultipartResolver",
			"java.lang.SecurityManager",
			"java.security.AccessControlException"
	);

	@Override
	public JavaVisitor<ExecutionContext> getMarkerVisitor(ApplicationContext applicationContext) {
		return new JavaIsoVisitor<>() {
            @Override
            public J.Identifier visitIdentifier(J.Identifier ident, ExecutionContext executionContext) {
                if (ident.getType() != null &&
                        getCursor().firstEnclosing(J.Import.class) == null &&
                        getCursor().firstEnclosing(J.FieldAccess.class) == null &&
                        !(getCursor().getParentOrThrow().getValue() instanceof J.ParameterizedType)) {
                    JavaType.FullyQualified type = TypeUtils.asFullyQualified(ident.getType());
                    for (String fqName : TYPE_FQNAMES) {
                        if (typeMatches(true, fqName, type) &&
                                ident.getSimpleName().equals(type.getClassName())) {
                            return ident.withMarkers(ident.getMarkers().add(new FixAssistMarker(Tree.randomId(), getId()).withLabel(createLabel(fqName))));
                        }
                    }
                }
                return super.visitIdentifier(ident, executionContext);
            }

            @Override
            public <N extends NameTree> N visitTypeName(N name, ExecutionContext ctx) {
                N n = super.visitTypeName(name, ctx);
                JavaType.FullyQualified type = TypeUtils.asFullyQualified(n.getType());
                for (String fqName : TYPE_FQNAMES) {
                    if (typeMatches(true, fqName, type) &&
                            getCursor().firstEnclosing(J.Import.class) == null) {
                        return n.withMarkers(n.getMarkers().add(new FixAssistMarker(Tree.randomId(), getId()).withLabel(createLabel(fqName))));
                    }
                }
                return n;
            }

            @Override
            public J.FieldAccess visitFieldAccess(J.FieldAccess fieldAccess, ExecutionContext ctx) {
                J.FieldAccess fa = (J.FieldAccess) super.visitFieldAccess(fieldAccess, ctx);
                JavaType.FullyQualified type = TypeUtils.asFullyQualified(fa.getTarget().getType());
                for (String fqName : TYPE_FQNAMES) {
                    if (typeMatches(true, fqName, type) &&
                            fa.getName().getSimpleName().equals("class")) {
                        return fa.withMarkers(fa.getMarkers().add(new FixAssistMarker(Tree.randomId(), getId()).withLabel(createLabel(fqName))));
                    }
                }
                return fa;
            }
            
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
            	MethodInvocation m = super.visitMethodInvocation(method, ctx);
				Method methodType = m.getMethodType();
				if (methodType != null) {
					FullyQualified fqType = TypeUtils.asFullyQualified(methodType.getReturnType());
					if (fqType != null) {
		                for (String fqName : TYPE_FQNAMES) {
		                    if (typeMatches(true, fqName, fqType)) {
		                        return m.withMarkers(m.getMarkers().add(new FixAssistMarker(Tree.randomId(), getId()).withLabel(createLabel(fqName))));
		                    }
		                }
					}
				}
				return m;
            }
		};
	}
	
	private static String createLabel(String type) {
		StringBuilder sb = new StringBuilder();
		sb.append("'");
		sb.append(type);
		sb.append("' not supported as of Spring Boot 3");
		return sb.toString();
	}
	
	private static boolean typeMatches(boolean checkAssignability, String fqName, JavaType.FullyQualified test) {
		return test != null && (checkAssignability ? test.isAssignableTo(fqName) : fqName.equals(test.getFullyQualifiedName()));
	}

	@Override
	public boolean isApplicable(IJavaProject project) {
		return springBootVersionGreaterOrEqual(3, 0, 0).test(project);
	}

	@Override
	public ProblemType getProblemType() {
		return Boot3JavaProblemType.JAVA_TYPE_NOT_SUPPORTED;
	}

}
