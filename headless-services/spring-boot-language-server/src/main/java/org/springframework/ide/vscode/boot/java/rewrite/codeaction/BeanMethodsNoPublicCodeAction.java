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
package org.springframework.ide.vscode.boot.java.rewrite.codeaction;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.openrewrite.java.spring.BeanMethodsNotPublic;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.reconcilers.BeanMethodNotPublicReconciler;
import org.springframework.ide.vscode.boot.java.rewrite.ORCompilationUnitCache;
import org.springframework.ide.vscode.boot.java.rewrite.RewriteRefactorings;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.java.Version;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.text.IRegion;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class BeanMethodsNoPublicCodeAction extends AbstractRewriteJavaCodeAction {

	private static final String REMOVE_PUBLIC_FROM_BEAN_METHODS_IN_FILE = "Remove 'public' from @Bean methods in file";
	private static final String REMOVE_PUBLIC_FROM_BEAN_METHODS_IN_PROJECT = "Remove 'public' from @Bean methods in project";
	private static final String CODE_ACTION_ID = "RemovePublicFromBeanMethod";

	public BeanMethodsNoPublicCodeAction(SimpleLanguageServer server, JavaProjectFinder projectFinder,
			RewriteRefactorings rewriteRefactorings, ORCompilationUnitCache orCuCache) {
		super(server, projectFinder, rewriteRefactorings, orCuCache, CODE_ACTION_ID);
	}
	
	@Override
	protected List<Either<Command, CodeAction>> provideCodeActions(CodeActionContext context, TextDocument doc,
			IRegion region, IJavaProject project, CompilationUnit cu, ASTNode node) {
		for (; node != null && !(node instanceof MethodDeclaration); node = node.getParent()) {
			// nothing
		}
		if (node instanceof MethodDeclaration) {
			MethodDeclaration m = (MethodDeclaration) node;
			IMethodBinding binding = m.resolveBinding();
			if (binding != null) {
				Optional<IAnnotationBinding> beanAnnotationPresent = Arrays.stream(binding.getAnnotations())
						.filter(a -> Annotations.BEAN.equals(a.getAnnotationType().getQualifiedName()))
						.findFirst();
				if (beanAnnotationPresent.isPresent() && BeanMethodNotPublicReconciler.isNotOverridingPublicMethod(binding)) {
					Version version = SpringProjectUtil.getDependencyVersion(project, SpringProjectUtil.SPRING_BOOT);
					if (version.getMajor() >= 2) {
						return List.of(
								Either.forRight(createCodeAction(REMOVE_PUBLIC_FROM_BEAN_METHODS_IN_FILE, List.of(doc.getUri(), false))),
								Either.forRight(createCodeAction(REMOVE_PUBLIC_FROM_BEAN_METHODS_IN_PROJECT, List.of(doc.getUri(), true)))
						);
					}
				}
			}
		}
		return null;
	}

	@Override
	public WorkspaceEdit perform(List<?> args) {
		return perform(args, () -> new BeanMethodsNotPublic());
	}

}
