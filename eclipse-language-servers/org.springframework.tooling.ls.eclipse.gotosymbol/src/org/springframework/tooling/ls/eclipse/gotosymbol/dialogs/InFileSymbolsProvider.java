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
package org.springframework.tooling.ls.eclipse.gotosymbol.dialogs;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4e.LanguageServiceAccessor.LSPDocumentInfo;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.ui.texteditor.ITextEditor;
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

	public InFileSymbolsProvider(Supplier<LSPDocumentInfo> target) {
		super();
		this.info = target;
	}
	
	@Override
	public List<Either<SymbolInformation, DocumentSymbol>> fetchFor(String query) throws Exception {
		LSPDocumentInfo info = this.info.get();
		if (info!=null) {
			DocumentSymbolParams params = new DocumentSymbolParams(
					new TextDocumentIdentifier(info.getFileUri().toString()));
			CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> symbolsFuture = info.getLanguageClient()
					.getTextDocumentService().documentSymbol(params);
			List<Either<SymbolInformation, DocumentSymbol>> symbols = symbolsFuture.get();
			return symbols == null ? ImmutableList.of() : ImmutableList.copyOf(symbols);
		}
		return ImmutableList.of();
	}

	public static SymbolsProvider createFor(LiveExpression<IResource> rsrc) {
		LiveExpression<LSPDocumentInfo> target = rsrc.apply(r -> {
			IDocument document = LSPEclipseUtils.getDocument(r);
			return getLSPDocumentInfo(document);
		});
		return new InFileSymbolsProvider(target::getValue);
	}

	public static SymbolsProvider createFor(ITextEditor textEditor) {
		IDocument document = LSPEclipseUtils.getDocument(textEditor);
		LSPDocumentInfo info = getLSPDocumentInfo(document);
		if (info!=null) {
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
		LSPDocumentInfo info = this.info.get();
		if (info!=null && symbol != null && symbol.getLocation() != null) {
			String symbolUri = symbol.getLocation().getUri();
			return info.getFileUri().toString().equals(symbolUri);
		}
		return false;
	}
}
