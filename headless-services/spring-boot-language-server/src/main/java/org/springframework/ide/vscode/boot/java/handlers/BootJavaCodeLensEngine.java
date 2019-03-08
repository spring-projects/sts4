/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServerComponents;
import org.springframework.ide.vscode.commons.languageserver.util.CodeLensHandler;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class BootJavaCodeLensEngine implements CodeLensHandler {

	private final BootJavaLanguageServerComponents server;
	private final Collection<CodeLensProvider> codelensProviders;

	public BootJavaCodeLensEngine(BootJavaLanguageServerComponents server, Collection<CodeLensProvider> codelensProviders) {
		this.server = server;
		this.codelensProviders = codelensProviders;
	}

	@Override
	public List<? extends CodeLens> handle(CodeLensParams params) {
		SimpleTextDocumentService documents = server.getTextDocumentService();
		String docURI = params.getTextDocument().getUri();

		if (documents.get(docURI) != null) {
			TextDocument doc = documents.get(docURI).copy();
			// Spring Boot LS get events from boot properties files as well, so filter them out
			if (server.getInterestingLanguages().contains(doc.getLanguageId())) {
				try {
					List<? extends CodeLens> codeLensesResult = provideCodeLenses(doc);
					if (codeLensesResult != null) {
						return codeLensesResult;
					}
				}
				catch (Exception e) {
				}
			}
		}

		return SimpleTextDocumentService.NO_CODELENS;
	}

	private List<? extends CodeLens> provideCodeLenses(TextDocument document) {
		return server.getCompilationUnitCache().withCompilationUnit(document, cu -> {

			if (cu != null) {
				List<CodeLens> result = new ArrayList<>();
				for (CodeLensProvider codeLensProvider : codelensProviders) {
					codeLensProvider.provideCodeLenses(document, cu, result);
				}

				if (result.size() > 0) {
					return result;
				}
			}

			return null;
		});
	}

	public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved) {
		return null;
	}

}
