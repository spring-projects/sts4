/*******************************************************************************
 * Copyright (c) 2017, 2023 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.gotosymbol.dialogs;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServers;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceSymbolLocation;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.ui.texteditor.ITextEditor;
import org.springframework.tooling.ls.eclipse.gotosymbol.dialogs.SelectionTracker.DocumentData;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

import com.google.common.collect.ImmutableList;

/**
 * SymbolsProvider that only looks for symbols in current file using the LSP 'document/symbols' api.
 * 
 * @author Kris De Volder
 */
@SuppressWarnings("restriction")
public class InFileSymbolsProvider implements SymbolsProvider {
	
	private IDocument doc;

	public InFileSymbolsProvider(IDocument doc) {
		super();
		this.doc = doc;
	}

	@Override
	public List<SymbolContainer> fetchFor(String query) throws Exception {
		if (doc != null) {
			DocumentSymbolParams params = new DocumentSymbolParams(new TextDocumentIdentifier(LSPEclipseUtils.toUri(doc).toASCIIString()));

			CompletableFuture<List<List<Either<SymbolInformation, DocumentSymbol>>>> symbolsFuture = LanguageServers
					.forDocument(doc)
					.withFilter(capabilities -> LSPEclipseUtils.hasCapability(capabilities.getDocumentSymbolProvider()))
					.collectAll(ls -> ls.getTextDocumentService().documentSymbol(params));
				
			List<SymbolContainer> symbols = symbolsFuture.get().stream()
					.flatMap(s -> s.stream())
					.map(either -> either.isLeft() ? SymbolContainer.fromSymbolInformation(either.getLeft()) : SymbolContainer.fromDocumentSymbol(either.getRight()))
					.collect(Collectors.toList());

			return symbols == null ? ImmutableList.of() : ImmutableList.copyOf(symbols);
		}
		return ImmutableList.of();
	}

	public static SymbolsProvider createFor(LiveExpression<DocumentData> documentData) {
		DocumentData data = documentData.getValue();
		return new InFileSymbolsProvider(data == null ? null : data.getDocument());
	}

	public static SymbolsProvider createFor(ITextEditor textEditor) {
		IDocument document = LSPEclipseUtils.getDocument(textEditor);
		return new InFileSymbolsProvider(document);
	}

	@Override
	public String getName() {
		return "Symbols in File";
	}

	@Override
	public boolean fromFile(SymbolContainer symbol) {
		if (symbol != null) {
			URI uri = LSPEclipseUtils.toUri(doc);
			if (symbol.isSymbolInformation() && symbol.getSymbolInformation().getLocation() != null) {
				String symbolUri = symbol.getSymbolInformation().getLocation().getUri();

				if (uri != null) {
					return uri.toASCIIString().equals(symbolUri);
				}
			}
			else if (symbol.isWorkspaceSymbol()) {
				Either<Location, WorkspaceSymbolLocation> location = symbol.getWorkspaceSymbol().getLocation();
				if (location.isLeft()) {
					if (uri != null) {
						return uri.toASCIIString().equals(location.getLeft().getUri());
					}
				}
				else {
					if (uri != null) {
						return uri.toASCIIString().equals(location.getRight().getUri());
					}
				}
			}
		}
		return false;
	}
}
