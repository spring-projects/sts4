/*******************************************************************************
 * Copyright (c) 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.cron;

import java.net.URI;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.data.jpa.queries.QueryJdtAstReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.JdtAstReconciler;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;

public class JdtCronReconciler implements JdtAstReconciler {
	
	private final CronReconciler cronReconciler;
	
	public JdtCronReconciler(CronReconciler cronReconciler) {
		this.cronReconciler = cronReconciler;
	}

	@Override
	public boolean isApplicable(IJavaProject project) {
		return SpringProjectUtil.hasDependencyStartingWith(project, "spring-context", null);
	}

	@Override
	public ProblemType getProblemType() {
		return CronProblemType.SYNTAX;
	}

	@Override
	public ASTVisitor createVisitor(IJavaProject project, URI docURI, CompilationUnit cu,
			IProblemCollector problemCollector, boolean isCompleteAst) {
		return new ASTVisitor() {
			@Override
			public boolean visit(NormalAnnotation node) {
				if (node.getTypeName() != null) {
					String fqn = node.getTypeName().getFullyQualifiedName();
					if (JdtCronSemanticTokensProvider.SCHEDULED_SIMPLE_NAME.equals(fqn) || Annotations.SCHEDULED.equals(fqn)) {
						ITypeBinding typeBinding = node.resolveTypeBinding();
						if (typeBinding != null && Annotations.SCHEDULED.equals(typeBinding.getQualifiedName())) {
							for (Object value : node.values()) {
								if (value instanceof MemberValuePair) {
									MemberValuePair pair = (MemberValuePair) value;
									String name = pair.getName().getFullyQualifiedName();
									if (name != null && "cron".equals(name)) {
										QueryJdtAstReconciler.reconcileExpression(cronReconciler, pair.getValue(), problemCollector);
									}
								}
							}
						}
					}
				}
				return super.visit(node);
			}
		};
	}

}
