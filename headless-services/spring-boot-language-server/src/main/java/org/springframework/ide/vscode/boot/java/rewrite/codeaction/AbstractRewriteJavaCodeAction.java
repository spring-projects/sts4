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
import java.util.Optional;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionCapabilities;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionResolveSupportCapabilities;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.openrewrite.Recipe;
import org.openrewrite.Result;
import org.springframework.ide.vscode.boot.java.handlers.JavaCodeAction;
import org.springframework.ide.vscode.boot.java.rewrite.ORCompilationUnitCache;
import org.springframework.ide.vscode.boot.java.rewrite.ORDocUtils;
import org.springframework.ide.vscode.boot.java.rewrite.RewriteRefactorings;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public abstract class AbstractRewriteJavaCodeAction implements JavaCodeAction {

	protected ORCompilationUnitCache orCuCache;
	protected String codeActionId;
	protected SimpleLanguageServer server;
	protected JavaProjectFinder projectFinder;

	public AbstractRewriteJavaCodeAction(SimpleLanguageServer server, JavaProjectFinder projectFinder,
			RewriteRefactorings rewriteRefactorings, ORCompilationUnitCache orCuCache, String codeActionId) {
		this.server = server;
		this.projectFinder = projectFinder;
		this.orCuCache = orCuCache;
		this.codeActionId = codeActionId;
		rewriteRefactorings.addRefactoring(codeActionId, this::perform);
	}

	protected CodeAction createCodeAction(String title, List<?> arguments) {
		CodeAction ca = new CodeAction();
		ca.setKind(CodeActionKind.Refactor);
		ca.setTitle(title);
		ca.setData(new RewriteRefactorings.Data(codeActionId, arguments));
		return ca;
	}
	
	protected WorkspaceEdit applyRecipe(Recipe r, TextDocument doc, org.openrewrite.java.tree.J.CompilationUnit cu) {
		List<Result> results = r.run(List.of(cu));
		if (!results.isEmpty() && results.get(0).getAfter() != null) {
			Optional<TextDocumentEdit> edit = ORDocUtils.computeTextDocEdit(doc, results.get(0));
			return edit.map(e -> {
				WorkspaceEdit workspaceEdit = new WorkspaceEdit();
				workspaceEdit.setDocumentChanges(List.of(Either.forLeft(e)));
				return workspaceEdit;
			}).orElse(null);
		}
		return null;
	}
	
	protected static boolean isResolve(CodeActionCapabilities capabilities, String property) {
		if (capabilities != null) {
			CodeActionResolveSupportCapabilities resolveSupport = capabilities.getResolveSupport();
			if (resolveSupport != null) {
				List<String> properties = resolveSupport.getProperties();
				return properties != null && properties.contains(property);
			}
		}
		return false;
	}

}
