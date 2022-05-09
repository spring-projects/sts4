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

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionCapabilities;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.openrewrite.java.spring.boot2.UnnecessarySpringExtension;
import org.springframework.ide.vscode.boot.java.reconcilers.UnnecessarySpringExtensionReconciler;
import org.springframework.ide.vscode.boot.java.rewrite.ORCompilationUnitCache;
import org.springframework.ide.vscode.boot.java.rewrite.RewriteRefactorings;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.LspClient;
import org.springframework.ide.vscode.commons.languageserver.util.LspClient.Client;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.text.IRegion;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class UnnecessarySpringExtensionCodeAction extends AbstractRewriteJavaCodeAction {
	
	private static final String CODE_ACTION_ID = "RemoveUnnecessarySpringExtension";

	public UnnecessarySpringExtensionCodeAction(SimpleLanguageServer server, JavaProjectFinder projectFinder,
			RewriteRefactorings rewriteRefactorings, ORCompilationUnitCache orCuCache) {
		super(server, projectFinder, rewriteRefactorings, orCuCache, CODE_ACTION_ID);
	}
	
	protected boolean isSupported(CodeActionCapabilities capabilities, CodeActionContext context) {
		// Default case is anything non-quick fix related.
		if (isResolve(capabilities, "edit")) {
			if (context.getOnly() != null) {
				return context.getOnly().contains(CodeActionKind.Refactor) || context.getOnly().contains(CodeActionKind.QuickFix);
			} else {
				return true;
			}
		}
		return false;
	}

	@Override
	protected List<Either<Command, CodeAction>> provideCodeActions(CodeActionContext context, TextDocument doc,
			IRegion region, IJavaProject project, CompilationUnit cu, ASTNode node) {
		for (; node != null && !(node instanceof Annotation); node = node.getParent()) {
			// nothing
		}
		if (node instanceof Annotation) {
			Annotation a = (Annotation) node;
			ITypeBinding binding = a.resolveTypeBinding();
			if (binding != null && UnnecessarySpringExtensionReconciler.isUnnecessarySpringExtensionAnnotation(project,
					a, binding)) {
				Builder<Either<Command, CodeAction>> listBuilder = ImmutableList.builder();
				if (context.getOnly() == null) {
					if (LspClient.currentClient() == Client.ECLIPSE) {
						if (context.getDiagnostics().isEmpty()) {
							listBuilder.add(createRefactoringForProject(doc.getUri()));
						} else {
							listBuilder.add(createQuickfixForFile(doc.getUri()));
						}
					} else {
						listBuilder.add(createQuickfixForFile(doc.getUri()));
						listBuilder.add(createRefactoringForProject(doc.getUri()));
					}
				} else {
					if (context.getOnly().contains(CodeActionKind.QuickFix)) {
						listBuilder.add(createQuickfixForFile(doc.getUri()));
					}
					if (context.getOnly().contains(CodeActionKind.Refactor)) {
						listBuilder.add(createRefactoringForProject(doc.getUri()));
					}
				}
				return listBuilder.build();
			}
		}
		return null;
	}
	
	private Either<Command, CodeAction> createQuickfixForFile(String docUri) {
		return Either.forRight(createCodeAction(CodeActionKind.QuickFix,
				"Remove unnecessary @SpringExtension in file", List.of(docUri, false)));
	}

	private Either<Command, CodeAction> createRefactoringForProject(String docUri) {
		return Either.forRight(createCodeAction(CodeActionKind.Refactor,
				"Remove unnecessary @SpringExtension in project", List.of(docUri, true)));
	}
	
	@Override
	public WorkspaceEdit perform(List<?> args) {
		return perform(args, () -> new UnnecessarySpringExtension());
	}

}
