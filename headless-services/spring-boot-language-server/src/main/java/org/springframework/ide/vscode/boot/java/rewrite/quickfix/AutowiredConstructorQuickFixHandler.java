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

import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.openrewrite.Result;
import org.openrewrite.java.spring.NoAutowiredOnConstructor;
import org.springframework.ide.vscode.boot.java.rewrite.ORAstUtils;
import org.springframework.ide.vscode.boot.java.rewrite.ORCompilationUnitCache;
import org.springframework.ide.vscode.boot.java.rewrite.ORDocUtils;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class AutowiredConstructorQuickFixHandler extends RewriteQuickFixHandler {
	
	final private SimpleLanguageServer server;
	final private JavaProjectFinder projectFinder;
	final private ORCompilationUnitCache orCuCache;

	public AutowiredConstructorQuickFixHandler(SimpleLanguageServer server, JavaProjectFinder projectFinder, ORCompilationUnitCache orCuCache) {
		super();
		this.server = server;
		this.projectFinder = projectFinder;
		this.orCuCache = orCuCache;
	}

	protected WorkspaceEdit perform(List<?> args) {
		SimpleTextDocumentService documents = server.getTextDocumentService();
		String docUri = (String) args.get(0);
		String classFqName = (String) args.get(1);
		TextDocument doc = documents.getLatestSnapshot(docUri);
		
		Optional<IJavaProject> project = projectFinder.find(new TextDocumentIdentifier(docUri));
		
		if (project.isPresent()) {
			return removeUnnecessaryAutowiredFromConstructor(project.get(), doc, classFqName);
		}
		
		return null;
	}
	
	private WorkspaceEdit removeUnnecessaryAutowiredFromConstructor(IJavaProject project, TextDocument doc, String classFqName) {
		String docUri = doc.getId().getUri();
		return orCuCache.withCompilationUnit(project, URI.create(docUri), cu -> {
			if (cu == null) {
				throw new IllegalStateException("Cannot parse Java file: " + docUri);
			}
			List<Result> results = ORAstUtils.nodeRecipe(new NoAutowiredOnConstructor(), t -> {
				if (t instanceof org.openrewrite.java.tree.J.ClassDeclaration) {
					org.openrewrite.java.tree.J.ClassDeclaration c = (org.openrewrite.java.tree.J.ClassDeclaration) t;
					return c.getType() != null && classFqName.equals(c.getType().getFullyQualifiedName());
				}
				return false;
			}).run(List.of(cu));
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
