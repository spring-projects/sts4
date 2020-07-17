/*******************************************************************************
 * Copyright (c) 2017, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.gotosymbol.dialogs;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4e.LanguageServiceAccessor.LSPDocumentInfo;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageServer;
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
	
	private Supplier<LSPDocumentInfo> info;

	public InFileSymbolsProvider(Supplier<LSPDocumentInfo> info) {
		super();
		this.info = info;
	}

	@Override
	public List<Either<SymbolInformation, DocumentSymbol>> fetchFor(String query) throws Exception {
		CompletableFuture<LanguageServer> server = getServer();
		String uri = getUri();
		if (server != null && uri != null) {
			DocumentSymbolParams params = new DocumentSymbolParams(
					new TextDocumentIdentifier(uri));
			CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> symbolsFuture = server
					.get()
					.getTextDocumentService().documentSymbol(params);
			List<Either<SymbolInformation, DocumentSymbol>> symbols = symbolsFuture.get();
			return symbols == null ? ImmutableList.of() : ImmutableList.copyOf(symbols);
		}
		return ImmutableList.of();
	}

	private String getUri() {
		if (this.info != null) {
			LSPDocumentInfo info = this.info.get();
			if (info != null) {
				return info.getFileUri().toString();
			}
		}
		return null;
	}
	
	private CompletableFuture<LanguageServer> getServer() throws Exception {
		if (this.info != null && this.info.get() != null) {
			return this.info.get().getInitializedLanguageClient();
		}
		return null;
	}
	
	public static SymbolsProvider createFor(LiveExpression<DocumentData> documentData) {
		Supplier<LSPDocumentInfo> inf = () -> {
			DocumentData data = documentData.getValue();
			if (data != null) {
				IDocument document = data.getDocument();
				return getLSPDocumentInfo(document);
			}
			return null;
		};
		return new InFileSymbolsProvider(inf);
	}

	public static SymbolsProvider createFor(ITextEditor textEditor) {
		IDocument document = LSPEclipseUtils.getDocument(textEditor);
		LSPDocumentInfo info = getLSPDocumentInfo(document);
		if (info != null) {
			return new InFileSymbolsProvider(() -> info);
		}
		return null;
	}

	private static LSPDocumentInfo getLSPDocumentInfo(IDocument document) {
		if (document!=null) {
			Collection<LSPDocumentInfo> infos = LanguageServiceAccessor.getLSPDocumentInfosFor(
					document,
					capabilities -> Boolean.TRUE.equals(capabilities.getDocumentSymbolProvider()));
			if (infos.isEmpty()) {
				return null;
			}
			// TODO maybe consider better strategy such as iterating on all LS until we have a good result
			return infos.iterator().next();
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
			String uri = getUri();
			if (uri != null) {
				return uri.toString().equals(symbolUri);
			}
		}
		return false;
	}
}
