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

import java.util.List;
import java.util.Optional;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.spring.BeanMethodsNotPublic;
import org.springframework.ide.vscode.boot.java.rewrite.ORCompilationUnitCache;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class BeanMethodNoPublicQuickFixHandler extends RewriteQuickFixHandler {

	final private SimpleLanguageServer server;
	final private JavaProjectFinder projectFinder;
	final private ORCompilationUnitCache orCuCache;


	public BeanMethodNoPublicQuickFixHandler(SimpleLanguageServer server, JavaProjectFinder projectFinder,
			ORCompilationUnitCache orCuCache) {
		this.server = server;
		this.projectFinder = projectFinder;
		this.orCuCache = orCuCache;
	}


	@Override
	protected WorkspaceEdit perform(List<?> args) {
		SimpleTextDocumentService documents = server.getTextDocumentService();
		String docUri = (String) args.get(0);
		String methodMatch = (String) args.get(1);
		TextDocument doc = documents.getLatestSnapshot(docUri);
		
		Optional<IJavaProject> project = projectFinder.find(new TextDocumentIdentifier(docUri));
		
		if (project.isPresent()) {
			MethodMatcher matcher = new MethodMatcher(methodMatch);
			return applyRecipe(orCuCache, new BeanMethodsNotPublic(), project.get(), doc, t -> {
				if (t instanceof org.openrewrite.java.tree.J.MethodDeclaration) {
					org.openrewrite.java.tree.J.MethodDeclaration m = (org.openrewrite.java.tree.J.MethodDeclaration) t;
					return matcher.matches(m.getMethodType());
				}
				return false;
			});
		}
		
		return null;
	}


}
