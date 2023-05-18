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

import java.util.List;

import org.openrewrite.ExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J.MethodInvocation;
import org.openrewrite.java.tree.J.VariableDeclarations;
import org.openrewrite.java.tree.JavaType.FullyQualified;
import org.springframework.ide.vscode.boot.java.Boot2JavaProblemType;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.java.Version;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.rewrite.config.MarkerVisitorContext;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeCodeActionDescriptor;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeScope;
import org.springframework.ide.vscode.commons.rewrite.java.FixAssistMarker;
import org.springframework.ide.vscode.commons.rewrite.java.FixDescriptor;
import org.springframework.ide.vscode.commons.rewrite.java.JavaMarkerVisitor;

public class AuthorizeHttpRequestsCodeAction implements RecipeCodeActionDescriptor {

	private static final String ID = "org.openrewrite.java.spring.boot2.AuthorizeHttpRequests";

	private static final MethodMatcher MATCH_AUTHORIZE_REQUESTS = new MethodMatcher(
			"org.springframework.security.config.annotation.web.builders.HttpSecurity authorizeRequests(..)");

	private static final String AUTHORIZE_REQUESTS_PROBLEM_LABEL = "HttpSecurity API 'authorizeRequests(...)' is outdated";

	private static final String AUTHORIZE_REQUESTS_FIX_LABEL = "Replace with 'authorizeHttpRequests(...)' and related types";

	private static final String CLASS_FIX_LABEL_TEMPLATE = "Replace with %s and use 'HttpSecurity.authorizeHttpRequests(...) and related types";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public ProblemType getProblemType() {
		return Boot2JavaProblemType.HTTP_SECIRITY_AUTHORIZE_HTTP_REQUESTS;
	}

	@Override
	public JavaVisitor<ExecutionContext> getMarkerVisitor(MarkerVisitorContext context) {
		return new JavaMarkerVisitor<>() {

			@Override
			public MethodInvocation visitMethodInvocation(MethodInvocation method, ExecutionContext p) {
				MethodInvocation m = super.visitMethodInvocation(method, p);
				if (MATCH_AUTHORIZE_REQUESTS.matches(method)) {
					String uri = getCursor().firstEnclosing(SourceFile.class).getSourcePath().toUri().toASCIIString();
					FixAssistMarker marker = new FixAssistMarker(Tree.randomId(), getId())
							.withLabel(AUTHORIZE_REQUESTS_PROBLEM_LABEL).withFixes(
									new FixDescriptor(ID, List.of(uri),
											RecipeCodeActionDescriptor.buildLabel(AUTHORIZE_REQUESTS_FIX_LABEL,
													RecipeScope.FILE))
											.withRecipeScope(RecipeScope.FILE),
									new FixDescriptor(ID, List.of(uri), RecipeCodeActionDescriptor
											.buildLabel(AUTHORIZE_REQUESTS_FIX_LABEL, RecipeScope.PROJECT))
											.withRecipeScope(RecipeScope.PROJECT));
					m = m.withName(m.getName().withMarkers(m.getName().getMarkers().add(marker)));
				}
				return m;
			}

			@Override
			public VariableDeclarations visitVariableDeclarations(VariableDeclarations multiVariable,
					ExecutionContext p) {
				VariableDeclarations mv = super.visitVariableDeclarations(multiVariable, p);
				FullyQualified type = mv.getTypeAsFullyQualified();
				if (type != null) {
					String replacementClass = null;
					switch (type.getFullyQualifiedName()) {
					case "org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer":
					case "org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer":
						replacementClass = "AuthorizeHttpRequestsConfigurer";
						break;
					case "org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer$ExpressionInterceptUrlRegistry":
						replacementClass = "AuthorizationManagerRequestMatcherRegistry";
					}
					if (replacementClass != null) {
						String uri = getCursor().firstEnclosing(SourceFile.class).getSourcePath().toUri()
								.toASCIIString();
						FixAssistMarker marker = new FixAssistMarker(Tree.randomId(), getId())
								.withLabel("Use of type '" + type.getClassName() + "' is outdated").withFixes(
										new FixDescriptor(ID, List.of(uri),
												RecipeCodeActionDescriptor.buildLabel(
														String.format(CLASS_FIX_LABEL_TEMPLATE, replacementClass),
														RecipeScope.FILE))
												.withRecipeScope(RecipeScope.FILE),
										new FixDescriptor(ID, List.of(uri),
												RecipeCodeActionDescriptor.buildLabel(
														String.format(CLASS_FIX_LABEL_TEMPLATE, replacementClass),
														RecipeScope.PROJECT))
												.withRecipeScope(RecipeScope.PROJECT));

						mv = mv.withTypeExpression(
								mv.getTypeExpression().withMarkers(mv.getTypeExpression().getMarkers().add(marker)));
					}
				}
				return mv;
			}

		};
	}

	@Override
	public boolean isApplicable(IJavaProject project) {
		Version version = SpringProjectUtil.getDependencyVersion(project, "spring-security-config");
		return version != null && version.compareTo(new Version(5, 6, 0, null)) >= 0;
	}

}
