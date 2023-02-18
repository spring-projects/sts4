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

import java.util.Set;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Tree;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.MethodInvocation;
import org.openrewrite.java.tree.JavaType.FullyQualified;
import org.openrewrite.java.tree.JavaType.Method;
import org.springframework.context.ApplicationContext;
import org.springframework.ide.vscode.boot.java.Boot2JavaProblemType;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.java.Version;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeCodeActionDescriptor;
import org.springframework.ide.vscode.commons.rewrite.java.FixAssistMarker;
import org.springframework.ide.vscode.commons.rewrite.java.JavaMarkerVisitor;

public class ServerHttpSecurityLambdaDslCodeAction implements RecipeCodeActionDescriptor {

	private static final Version SECURITY_VERSION = new Version(5, 2, 0, null);
	
	private static final String FQN_SERVER_HTTP_SECURITY = "org.springframework.security.config.web.server.ServerHttpSecurity";
	
	private static final Set<String> APPLICABLE_METHOD_NAMES = Set.of(
			"anonymous", "authorizeExchange", "cors", "csrf", "exceptionHandling", "formLogin",
			"headers", "httpBasic", "logout", "oauth2Client", "oauth2Login", "oauth2ResourceServer",
			"redirectToHttps", "requestCache", "x509"
	);

	@Override
	public JavaVisitor<ExecutionContext> getMarkerVisitor(ApplicationContext applicationContext) {
		return new JavaMarkerVisitor<ExecutionContext>() {

			@Override
			public MethodInvocation visitMethodInvocation(MethodInvocation method, ExecutionContext p) {
				MethodInvocation m = super.visitMethodInvocation(method, p);
				Method type = method.getMethodType();
				if (type != null) {
					FullyQualified declaringType = type.getDeclaringType();
					if (declaringType != null && FQN_SERVER_HTTP_SECURITY.equals(declaringType.getFullyQualifiedName()) 
							&& type.getParameterTypes().isEmpty() && APPLICABLE_METHOD_NAMES.contains(m.getSimpleName())
						&& getCursor().getParent(2) != null && getCursor().getParent(2).getValue() instanceof J.MethodInvocation) {
							
						J.MethodInvocation parentInvocation = getCursor().getParent(2).getValue();
						if (!declaringType.equals(parentInvocation.getType())) {
							FixAssistMarker marker = new FixAssistMarker(Tree.randomId(), getId()).withLabel("Consider switching to Lambda DSL syntax");
							m = m.withName(m.getName().withMarkers(m.getName().getMarkers().add(marker)));
						}
					}
				}
				return m;
			}
			
		};
	}

	@Override
	public boolean isApplicable(IJavaProject project) {
		Version version = SpringProjectUtil.getDependencyVersion(project, "spring-security-config");
		return version != null && version.compareTo(SECURITY_VERSION) >= 0;
	}

	@Override
	public ProblemType getProblemType() {
		return Boot2JavaProblemType.JAVA_LAMBDA_DSL;
	}

}
