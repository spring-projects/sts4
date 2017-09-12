/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.springframework.ide.vscode.commons.languageserver.quickfix.Quickfix;
import org.springframework.ide.vscode.commons.util.Assert;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class SimpleTextDocumentService implements TextDocumentService {

	final private SimpleLanguageServer server;
	private Map<String, TrackedDocument> documents = new HashMap<>();
	private ListenerList<TextDocumentContentChange> documentChangeListeners = new ListenerList<>();

	private CompletionHandler completionHandler = null;
	private CompletionResolveHandler completionResolveHandler = null;

	private HoverHandler hoverHandler = null;
	private DefinitionHandler definitionHandler;
	private ReferencesHandler referencesHandler;
	private DocumentSymbolHandler documentSymbolHandler;

	private CodeLensHandler codeLensHandler;
	private CodeLensResolveHandler codeLensResolveHandler;

	private Consumer<TextDocumentSaveChange> documentSaveListener;

	public SimpleTextDocumentService(SimpleLanguageServer server) {
		this.server = server;
	}

	public synchronized void onHover(HoverHandler h) {
		Assert.isNull("A hover handler is already set, multiple handlers not supported yet", hoverHandler);
		this.hoverHandler = h;
	}

	public synchronized void onCodeLens(CodeLensHandler h) {
		Assert.isNull("A code lens handler is already set, multiple handlers not supported yet", codeLensHandler);
		this.codeLensHandler = h;
	}

	public synchronized void onCodeLensResolve(CodeLensResolveHandler h) {
		Assert.isNull("A code lens resolve handler is already set, multiple handlers not supported yet", codeLensResolveHandler);
		this.codeLensResolveHandler = h;
	}

	public synchronized void onDocumentSymbol(DocumentSymbolHandler h) {
		Assert.isNull("A DocumentSymbolHandler is already set, multiple handlers not supported yet", documentSymbolHandler);
		this.documentSymbolHandler = h;
	}

	 public synchronized void onCompletion(CompletionHandler h) {
		Assert.isNull("A completion handler is already set, multiple handlers not supported yet", completionHandler);
		this.completionHandler = h;
	}

	public synchronized void onCompletionResolve(CompletionResolveHandler h) {
		Assert.isNull("A completionResolveHandler handler is already set, multiple handlers not supported yet", completionResolveHandler);
		this.completionResolveHandler = h;
	}

	public synchronized void onDefinition(DefinitionHandler h) {
		Assert.isNull("A defintion handler is already set, multiple handlers not supported yet", definitionHandler);
		this.definitionHandler = h;
	}

	public synchronized void onReferences(ReferencesHandler h) {
		Assert.isNull("A references handler is already set, multiple handlers not supported yet", referencesHandler);
		this.referencesHandler = h;
	}

	/**
	 * Gets all documents this service is tracking, generally these are the documents that have been opened / changed,
	 * and not yet closed.
	 */
	public synchronized Collection<TextDocument> getAll() {
		return documents.values().stream()
				.map((td) -> td.getDocument())
				.collect(Collectors.toList());
	}

	@Override
	public final void didChange(DidChangeTextDocumentParams params) {
		try {
			VersionedTextDocumentIdentifier docId = params.getTextDocument();
			String url = docId.getUri();
//			Log.debug("didChange: "+url);
			if (url!=null) {
				TextDocument doc = getDocument(url);
				List<TextDocumentContentChangeEvent> changes = params.getContentChanges();
				doc.apply(params);
				didChangeContent(doc, changes);
			}
		} catch (BadLocationException e) {
			Log.log(e);
		}
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		TextDocumentItem docId = params.getTextDocument();
		String url = docId.getUri();
		//Log.info("didOpen: "+params.getTextDocument().getUri());
		LanguageId languageId = LanguageId.of(docId.getLanguageId());
		int version = docId.getVersion();
		if (url!=null) {
			String text = params.getTextDocument().getText();
			TrackedDocument td = createDocument(url, languageId, version, text).open();
//			Log.info("Opened "+td.getOpenCount()+" times: "+url);
			TextDocument doc = td.getDocument();
			TextDocumentContentChangeEvent change = new TextDocumentContentChangeEvent() {
				@Override
				public Range getRange() {
					return null;
				}

				@Override
				public Integer getRangeLength() {
					return null;
				}

				@Override
				public String getText() {
					return text;
				}
			};
			TextDocumentContentChange evt = new TextDocumentContentChange(doc, ImmutableList.of(change));
			documentChangeListeners.fire(evt);
		}
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		//Log.info("didClose: "+params.getTextDocument().getUri());
		String url = params.getTextDocument().getUri();
		if (url!=null) {
			TrackedDocument doc = documents.get(url);
			if (doc!=null) {
				if (doc.close()) {
					Log.info("Closed: "+url);
					//Clear diagnostics when a file is closed. This makes the errors disapear when the language is changed for
					// a document (this resulst in a dicClose even as being sent to the language server if that changes make the
					// document go 'out of scope'.
					publishDiagnostics(params.getTextDocument(), ImmutableList.of());
					documents.remove(url);
				} else {
					Log.warn("Close event ignored! Assuming document still open because openCount = "+doc.getOpenCount());
				}
			} else {
				Log.warn("Document closed, but it didn't exist! Close event ignored");
			}
		}
	}

	void didChangeContent(TextDocument doc, List<TextDocumentContentChangeEvent> changes) {
		documentChangeListeners.fire(new TextDocumentContentChange(doc, changes));
	}

	public void onDidChangeContent(Consumer<TextDocumentContentChange> l) {
		documentChangeListeners.add(l);
	}

	public void onDidSave(Consumer<TextDocumentSaveChange> l) {
		documentSaveListener=l;
	}

	public synchronized TextDocument getDocument(String url) {
		TrackedDocument doc = documents.get(url);
		if (doc==null) {
			Log.warn("Trying to get document ["+url+"] but it did not exists. Creating it with language-id 'plaintext'");
			doc = createDocument(url, LanguageId.PLAINTEXT, 0, "");
		}
		return doc.getDocument();
	}

	private synchronized TrackedDocument createDocument(String url, LanguageId languageId, int version, String text) {
		TrackedDocument existingDoc = documents.get(url);
		if (existingDoc!=null) {
			Log.warn("Creating document ["+url+"] but it already exists. Reusing existing!");
			return existingDoc;
		}
		TrackedDocument doc = new TrackedDocument(new TextDocument(url, languageId, version, text));
		documents.put(url, doc);
		return doc;
	}

	public final static CompletionList NO_COMPLETIONS = new CompletionList(false, Collections.emptyList());
	public final static CompletableFuture<Hover> NO_HOVER = CompletableFuture.completedFuture(new Hover(ImmutableList.of(), null));
	public final static CompletableFuture<List<? extends Location>> NO_REFERENCES = CompletableFuture.completedFuture(ImmutableList.of());
	public final static List<? extends SymbolInformation> NO_SYMBOLS = ImmutableList.of();
	public final static CompletableFuture<List<? extends CodeLens>> NO_CODELENS = CompletableFuture.completedFuture(ImmutableList.of());

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(TextDocumentPositionParams position) {
		CompletionHandler h = completionHandler;
		if (h!=null) {
			return completionHandler.handle(position)
			.thenApply(Either::forRight);
		}
		return CompletableFuture.completedFuture(Either.forRight(NO_COMPLETIONS));
	}

	@Override
	public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
		CompletionResolveHandler h = completionResolveHandler;
		if (h!=null) {
			return h.handle(unresolved);
		}
		return null;
	}

	@Override
	public CompletableFuture<Hover> hover(TextDocumentPositionParams position) {
		HoverHandler h = hoverHandler;
		if (h!=null) {
			return hoverHandler.handle(position);
		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<SignatureHelp> signatureHelp(TextDocumentPositionParams position) {
		return CompletableFuture.completedFuture(null);
	}

	@SuppressWarnings({ "unchecked"})
	@Override
	public CompletableFuture<List<? extends Location>> definition(TextDocumentPositionParams position) {
		DefinitionHandler h = this.definitionHandler;
		if (h!=null) {
			Object r = h.handle(position); //YUCK!
			return (CompletableFuture<List<? extends Location>>) r;
		}
		return CompletableFuture.completedFuture(Collections.emptyList());
	}

	@Override
	public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
		ReferencesHandler h = this.referencesHandler;
		if (h != null) {
			return h.handle(params);
		}
		return CompletableFuture.completedFuture(Collections.emptyList());
	}

	@Override
	public CompletableFuture<List<? extends SymbolInformation>> documentSymbol(DocumentSymbolParams params) {
		DocumentSymbolHandler documentSymbolHandler = this.documentSymbolHandler;
		if (documentSymbolHandler==null) {
			return CompletableFuture.completedFuture(ImmutableList.of());
		}
		return Mono.fromCallable(() -> {
			server.waitForReconcile();
			return documentSymbolHandler.handle(params);
		})
		.toFuture()
		.thenApply(l -> (List<? extends SymbolInformation>)l);
	}

	@Override
	public CompletableFuture<List<? extends Command>> codeAction(CodeActionParams params) {
		TrackedDocument doc = documents.get(params.getTextDocument().getUri());
		if (doc!=null) {
			return Flux.fromIterable(doc.getQuickfixes())
					.filter((fix) -> fix.appliesTo(params.getRange(), params.getContext()))
					.map(Quickfix::getCodeAction)
					.collectList()
					.toFuture()
					.thenApply(l -> (List<? extends Command>) l);
		} else {
			return CompletableFuture.completedFuture(ImmutableList.of());
		}
	}

	@Override
	public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
		CodeLensHandler handler = this.codeLensHandler;
		if (handler != null) {
			return handler.handle(params);
		}
		return CompletableFuture.completedFuture(Collections.emptyList());
	}

	@Override
	public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved) {
		CodeLensResolveHandler handler = this.codeLensResolveHandler;
		if (handler != null) {
			return handler.handle(unresolved);
		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
		return CompletableFuture.completedFuture(Collections.emptyList());
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> rangeFormatting(DocumentRangeFormattingParams params) {
		return CompletableFuture.completedFuture(Collections.emptyList());
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(DocumentOnTypeFormattingParams params) {
		return CompletableFuture.completedFuture(Collections.emptyList());
	}

	@Override
	public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {
		// Workaround for PT 147263283, where error markers in STS are lost on document save.
		// STS 3.9.0 does not use the LSP4E editor for edit manifest.yml, which correctly retains error markers after save.
		// Instead, because the LSP4E editor is missing support for hovers and completions, STS 3.9.0 uses its own manifest editor
		// which extends the YEdit editor. This YEdit editor has a problem, where on save, all error markers are deleted.
		// When STS uses the LSP4E editor and no longer needs its own YEdit-based editor, the issue with error markers disappearing
		// on save should not be a problem anymore, and the workaround below will no longer be needed.
		if (documentSaveListener != null) {
			TextDocumentIdentifier docId = params.getTextDocument();
			String url = docId.getUri();
			Log.debug("didSave: "+url);
			if (url!=null) {
				TextDocument doc = getDocument(url);
				documentSaveListener.accept(new TextDocumentSaveChange(doc));
			}
		}
	}

	public void publishDiagnostics(TextDocumentIdentifier docId, Collection<Diagnostic> diagnostics) {
		LanguageClient client = server.getClient();
		if (client!=null && diagnostics!=null) {
			PublishDiagnosticsParams params = new PublishDiagnosticsParams();
			params.setUri(docId.getUri());
			params.setDiagnostics(ImmutableList.copyOf(diagnostics));
			client.publishDiagnostics(params);
		}
	}

	public void setQuickfixes(TextDocumentIdentifier docId, List<Quickfix> quickfixes) {
		TrackedDocument td = documents.get(docId.getUri());
		if (td!=null) {
			td.setQuickfixes(quickfixes);
		}
	}

	public synchronized TextDocument get(TextDocumentPositionParams params) {
		return get(params.getTextDocument().getUri());
	}

	public synchronized TextDocument get(String uri) {
		TrackedDocument td = documents.get(uri);
		return td == null ? null : td.getDocument();
	}

	@Override
	public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(TextDocumentPositionParams position) {
		return CompletableFuture.completedFuture(Collections.emptyList());
	}

	public boolean hasDefinitionHandler() {
		return definitionHandler!=null;
	}

	public boolean hasReferencesHandler() {
		return this.referencesHandler!=null;
	}

	public boolean hasDocumentSymbolHandler() {
		return this.documentSymbolHandler!=null;
	}

	public boolean hasCodeLensHandler() {
		return this.codeLensHandler != null;
	}

	public boolean hasCodeLensResolveProvider() {
		return this.codeLensResolveHandler != null;
	}

}
