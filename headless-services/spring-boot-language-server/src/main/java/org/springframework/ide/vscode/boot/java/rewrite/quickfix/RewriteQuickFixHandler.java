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
package org.springframework.ide.vscode.boot.java.rewrite.quickfix;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Result;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J;
import org.springframework.ide.vscode.boot.java.rewrite.ORAstUtils;
import org.springframework.ide.vscode.boot.java.rewrite.ORCompilationUnitCache;
import org.springframework.ide.vscode.boot.java.rewrite.ORDocUtils;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixEdit;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixHandler;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public abstract class RewriteQuickFixHandler implements QuickfixHandler {
	
	final static private Gson gson = new Gson();
	
	@Override
	public QuickfixEdit createEdits(Object p) {
		if (p instanceof JsonElement) {
			List<?> l = gson.fromJson((JsonElement) p, List.class);
			return new QuickfixEdit(perform(l), null);
		}
		return null;
	}

	abstract protected WorkspaceEdit perform(List<?> l);
	
	final protected WorkspaceEdit applyRecipe(ORCompilationUnitCache orCuCache, Recipe r, IJavaProject project, TextDocument doc, Predicate<J> nodeFilter) {
		return doApplyRecipe(orCuCache, ORAstUtils.nodeRecipe(r, nodeFilter), project, doc);
	}
	
	final protected WorkspaceEdit applyVisitor(ORCompilationUnitCache orCuCache, JavaVisitor<ExecutionContext> v, IJavaProject project, TextDocument doc, Predicate<J> nodeFilter) {
		return doApplyRecipe(orCuCache, ORAstUtils.nodeRecipe(v, nodeFilter), project, doc);
	}
	
	final protected WorkspaceEdit doApplyRecipe(ORCompilationUnitCache orCuCache, Recipe r, IJavaProject project, TextDocument doc) {
		String docUri = doc.getId().getUri();
		return orCuCache.withCompilationUnit(project, URI.create(docUri), cu -> {
			if (cu == null) {
				throw new IllegalStateException("Cannot parse Java file: " + docUri);
			}
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
		});
		
	}
	
}
