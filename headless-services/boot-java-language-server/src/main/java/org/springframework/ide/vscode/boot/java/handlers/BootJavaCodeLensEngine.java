/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectManager;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class BootJavaCodeLensEngine {

	private final SimpleLanguageServer server;
	private final JavaProjectManager projectFinder;

	public BootJavaCodeLensEngine(SimpleLanguageServer server, JavaProjectManager projectFinder) {
		this.server = server;
		this.projectFinder = projectFinder;
	}

	public CompletableFuture<List<? extends CodeLens>> createCodeLenses(CodeLensParams params) {
		SimpleTextDocumentService documents = server.getTextDocumentService();
		String docURI = params.getTextDocument().getUri();

		if (documents.get(docURI) != null) {
			TextDocument doc = documents.get(docURI).copy();
			try {
				CompletableFuture<List<? extends CodeLens>> codeLensesResult = provideCodeLenses(doc);
				if (codeLensesResult != null) {
					return codeLensesResult;
				}
			}
			catch (Exception e) {
			}
		}

		return SimpleTextDocumentService.NO_CODELENS;
	}

	private CompletableFuture<List<? extends CodeLens>> provideCodeLenses(TextDocument doc) {
		List<CodeLens> result = new ArrayList<>();

		CodeLens codeLens = new CodeLens();

		Range range = new Range();
		range.setStart(new Position(5, 0));
		range.setEnd(new Position(5, 10));
		codeLens.setRange(range);

		Command command = new Command("my first awesome code lens", "my first code lens command");
		codeLens.setCommand(command);

		codeLens.setData("some data");

		result.add(codeLens);
		return CompletableFuture.completedFuture(result);
	}

	public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved) {
		return null;
	}

}
