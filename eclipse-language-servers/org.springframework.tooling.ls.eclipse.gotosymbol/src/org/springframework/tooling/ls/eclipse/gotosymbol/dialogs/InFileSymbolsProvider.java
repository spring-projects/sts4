/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.gotosymbol.dialogs;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4e.LanguageServiceAccessor.LSPDocumentInfo;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.ui.texteditor.ITextEditor;

import com.google.common.collect.ImmutableList;

/**
 * SymbolsProvider that only looks for symbols in current file using the LSP 'document/symbols' api.
 * 
 * @author Kris De Volder
 */
@SuppressWarnings("restriction")
public class InFileSymbolsProvider implements SymbolsProvider {
	
	private LSPDocumentInfo info;

	public InFileSymbolsProvider(LSPDocumentInfo target) {
		super();
		this.info = target;
	}
	
	@Override
	public List<Either<SymbolInformation, DocumentSymbol>> fetchFor(String query) throws Exception {
		DocumentSymbolParams params = new DocumentSymbolParams(
				new TextDocumentIdentifier(info.getFileUri().toString()));
		CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> symbolsFuture = info.getLanguageClient()
				.getTextDocumentService().documentSymbol(params);
		List<Either<SymbolInformation, DocumentSymbol>> symbols = symbolsFuture.get();
		return symbols == null ? ImmutableList.of() : ImmutableList.copyOf(symbols);
	}

	public static SymbolsProvider createFor(ITextEditor textEditor) {
		Collection<LSPDocumentInfo> infos = LanguageServiceAccessor.getLSPDocumentInfosFor(
				LSPEclipseUtils.getDocument(textEditor),
				capabilities -> Boolean.TRUE.equals(capabilities.getDocumentSymbolProvider()));
		if (infos.isEmpty()) {
			return null;
		}
		// TODO maybe consider better strategy such as iterating on all LS until we have a good result
		LSPDocumentInfo info = infos.iterator().next();
		if (info!=null) {
			return new InFileSymbolsProvider(info);
		}
		return null;
	}

	@Override
	public String getName() {
		return "Symbols in File";
	}

	@Override
	public boolean fromFile(SymbolInformation symbol) {
		if (symbol != null && symbol.getLocation() != null) {
			String symbolUri = symbol.getLocation().getUri();
			return info.getFileUri().toString().equals(symbolUri);
		}
		return false;
	}
}
