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
package org.springframework.ide.vscode.boot.java.reconcilers;

import java.net.URI;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleType;
import org.openrewrite.java.spring.security5.AuthorizeHttpRequests;
import org.springframework.ide.vscode.boot.java.Boot2JavaProblemType;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.java.Version;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixRegistry;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblemImpl;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeScope;
import org.springframework.ide.vscode.commons.rewrite.java.FixDescriptor;

public class AuthorizeHttpRequestsReconciler implements JdtAstReconciler {

	private static final String FQN_HTTP_SECURITY = "org.springframework.security.config.annotation.web.builders.HttpSecurity";

	private static final String AUTHORIZE_REQUESTS = "authorizeRequests";

	private static final String AUTHORIZE_REQUESTS_PROBLEM_LABEL = "HttpSecurity API 'authorizeRequests(...)' is outdated";

	private static final String AUTHORIZE_REQUESTS_FIX_LABEL = "Replace with 'authorizeHttpRequests(...)' and related types";

	private static final String CLASS_FIX_LABEL_TEMPLATE = "Replace with %s and use 'HttpSecurity.authorizeHttpRequests(...) and related types";

	private static final String FQN_AUTH_REQ_CONFIG = "org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer";

	private static final String FQN_EXPR_AUTH_CONFIG = "org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer";

	private static final String FQN_EXPR_INTERCEPT_REG = "org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer.ExpressionInterceptUrlRegistry";

	private QuickfixRegistry registry;

	public AuthorizeHttpRequestsReconciler(QuickfixRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void reconcile(IJavaProject project, URI docUri, CompilationUnit cu, IProblemCollector problemCollector,
			boolean isCompleteAst) throws RequiredCompleteAstException {
		if (isCompleteAst) {
			cu.accept(new ASTVisitor() {

				@Override
				public boolean visit(MethodInvocation node) {
					if (AUTHORIZE_REQUESTS.equals(node.getName().getIdentifier()) && node.arguments().isEmpty()) {
						ITypeBinding type = node.getExpression().resolveTypeBinding();
						if (type != null && FQN_HTTP_SECURITY.equals(type.getQualifiedName())) {
							ReconcileProblemImpl problem = new ReconcileProblemImpl(getProblemType(),
									AUTHORIZE_REQUESTS_PROBLEM_LABEL, node.getName().getStartPosition(),
									node.getName().getLength());
							String uri = docUri.toASCIIString();
							String id = AuthorizeHttpRequests.class.getName();
							ReconcileUtils
									.setRewriteFixes(registry, problem, List.of(
											new FixDescriptor(id, List.of(uri),
													ReconcileUtils.buildLabel(AUTHORIZE_REQUESTS_FIX_LABEL,
															RecipeScope.FILE))
													.withRecipeScope(RecipeScope.FILE),
											new FixDescriptor(id, List.of(uri),
													ReconcileUtils.buildLabel(AUTHORIZE_REQUESTS_FIX_LABEL,
															RecipeScope.PROJECT))
													.withRecipeScope(RecipeScope.PROJECT)));
							problemCollector.accept(problem);
							return false;
						}
					}
					return true;
				}

				@Override
				public boolean visit(SimpleType node) {
					String replacementClass = null;
					if (ReconcileUtils.isApplicableTypeWithoutResolving(cu,
							List.of(FQN_AUTH_REQ_CONFIG, FQN_EXPR_AUTH_CONFIG), node.getName())) {
						replacementClass = "AuthorizeHttpRequestsConfigurer";
					} else if (ReconcileUtils.isApplicableTypeWithoutResolving(cu, List.of(FQN_EXPR_INTERCEPT_REG),
							node.getName())) {
						replacementClass = "AuthorizationManagerRequestMatcherRegistry";
					}
					if (replacementClass != null) {
						ReconcileProblemImpl problem = new ReconcileProblemImpl(getProblemType(),
								"Use of type '" + node.getName().getFullyQualifiedName() + "' is outdated",
								node.getName().getStartPosition(), node.getName().getLength());
						String uri = docUri.toASCIIString();
						String id = AuthorizeHttpRequests.class.getName();
						ReconcileUtils
								.setRewriteFixes(registry, problem, List.of(
										new FixDescriptor(id, List.of(uri),
												ReconcileUtils.buildLabel(String.format(CLASS_FIX_LABEL_TEMPLATE,
														replacementClass), RecipeScope.FILE))
												.withRecipeScope(RecipeScope.FILE),
										new FixDescriptor(id, List.of(uri),
												ReconcileUtils.buildLabel(
														String.format(CLASS_FIX_LABEL_TEMPLATE, replacementClass),
														RecipeScope.PROJECT))
												.withRecipeScope(RecipeScope.PROJECT)));
						problemCollector.accept(problem);
						return false;
					}
					return super.visit(node);
				}

			});
		} else {
			boolean needsFullAst = ReconcileUtils.isAnyTypeUsed(cu, List.of(
					FQN_HTTP_SECURITY,
					FQN_AUTH_REQ_CONFIG,
					FQN_EXPR_AUTH_CONFIG,
					FQN_EXPR_INTERCEPT_REG
			));
			if (needsFullAst) {
				throw new RequiredCompleteAstException();
			}
		}
	}

	@Override
	public boolean isApplicable(IJavaProject project) {
		Version version = SpringProjectUtil.getDependencyVersion(project, "spring-security-config");
		return version != null && version.compareTo(new Version(5, 6, 0, null)) >= 0;
	}

	@Override
	public ProblemType getProblemType() {
		return Boot2JavaProblemType.HTTP_SECURITY_AUTHORIZE_HTTP_REQUESTS;
	}

}
