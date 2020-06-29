/*******************************************************************************
 * Copyright (c) 2016, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.util;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightParams;
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpParams;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.languageserver.config.LanguageServerProperties;
import org.springframework.ide.vscode.commons.languageserver.quickfix.Quickfix;
import org.springframework.ide.vscode.commons.util.Assert;
import org.springframework.ide.vscode.commons.util.AsyncRunner;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.CollectorUtil;
import org.springframework.ide.vscode.commons.util.ExceptionUtil;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

import reactor.core.publisher.Mono;

public class SimpleTextDocumentService implements TextDocumentService, DocumentEventListenerManager {

	private static Logger log = LoggerFactory.getLogger(SimpleTextDocumentService.class);
	
	final private SimpleLanguageServer server;
	final private LanguageServerProperties props;
	private Map<String, TrackedDocument> documents = new HashMap<>();
	private ListenerList<TextDocumentContentChange> documentChangeListeners = new ListenerList<>();
	private ListenerList<TextDocument> documentCloseListeners = new ListenerList<>();
	private ListenerList<TextDocument> documentOpenListeners = new ListenerList<>();

	private CompletionHandler completionHandler = null;
	private CompletionResolveHandler completionResolveHandler = null;

	private HoverHandler hoverHandler = null;
	private DefinitionHandler definitionHandler;
	private ReferencesHandler referencesHandler;

	private DocumentSymbolHandler documentSymbolHandler;
	private DocumentHighlightHandler documentHighlightHandler;

	private CodeLensHandler codeLensHandler;
	private CodeLensResolveHandler codeLensResolveHandler;

	private List<Consumer<TextDocumentSaveChange>> documentSaveListeners = ImmutableList.of();
	private AsyncRunner async;


	public SimpleTextDocumentService(SimpleLanguageServer server, LanguageServerProperties props) {
		this.server = server;
		this.props = props;
		this.async = server.getAsync();
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

	public synchronized void onDocumentHighlight(DocumentHighlightHandler h) {
		Assert.isNull("A DocumentHighlightHandler is already set, multiple handlers not supported yet", documentHighlightHandler);
		this.documentHighlightHandler = h;
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
	  async.execute(() -> {
		try {
			VersionedTextDocumentIdentifier docId = params.getTextDocument();
			String url = docId.getUri();
//			Log.debug("didChange: "+url);
			if (url != null) {
				TextDocument doc = getDocument(url);
				if (doc != null) {
					List<TextDocumentContentChangeEvent> changes = params.getContentChanges();
					doc.apply(params);
					didChangeContent(doc, changes);
				}
			}
		} catch (BadLocationException e) {
			log.error("", e);
		}
	  });
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
	  async.execute(() -> {
		TextDocumentItem docId = params.getTextDocument();
		String url = docId.getUri();
		//Log.info("didOpen: "+params.getTextDocument().getUri());
		LanguageId languageId = LanguageId.of(docId.getLanguageId());
		int version = docId.getVersion();
		if (url != null) {

			String text = params.getTextDocument().getText();
			TrackedDocument td = createDocument(url, languageId, version, text).open();
			log.debug("Opened " + td.getOpenCount() + " times: " + url);
			TextDocument doc = td.getDocument();

			documentOpenListeners.fire(doc);

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
	  });
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
	  async.execute(() -> {
		//Log.info("didClose: "+params.getTextDocument().getUri());
		String url = params.getTextDocument().getUri();
		if (url!=null) {
			TrackedDocument doc = documents.get(url);
			if (doc != null) {
				if (doc.close()) {
					log.info("Closed: "+url);
					//Clear diagnostics when a file is closed. This makes the errors disapear when the language is changed for
					// a document (this resulst in a dicClose even as being sent to the language server if that changes make the
					// document go 'out of scope'.
					publishDiagnostics(params.getTextDocument(), ImmutableList.of());
					documentCloseListeners.fire(doc.getDocument());
					documents.remove(url);
				} else {
					log.warn("Close event ignored! Assuming document still open because openCount = "+doc.getOpenCount());
				}
			} else {
				log.warn("Document closed, but it didn't exist! Close event ignored");
			}
		}
	  });
	}

	void didChangeContent(TextDocument doc, List<TextDocumentContentChangeEvent> changes) {
		documentChangeListeners.fire(new TextDocumentContentChange(doc, changes));
	}

	public void onDidOpen(Consumer<TextDocument> l) {
		documentOpenListeners.add(l);
	}

	public void onDidChangeContent(Consumer<TextDocumentContentChange> l) {
		documentChangeListeners.add(l);
	}

	public void onDidClose(Consumer<TextDocument> l) {
		documentCloseListeners.add(l);
	}

	@Override
	public void onDidSave(Consumer<TextDocumentSaveChange> l) {
		ImmutableList.Builder<Consumer<TextDocumentSaveChange>> builder = ImmutableList.builder();
		builder.addAll(documentSaveListeners);
		builder.add(l);
		documentSaveListeners = builder.build();
	}

	public synchronized TextDocument getDocument(String url) {
		TrackedDocument doc = documents.get(url);
		return doc != null ? doc.getDocument() : null;
	}

	private synchronized TrackedDocument createDocument(String url, LanguageId languageId, int version, String text) {
		TrackedDocument existingDoc = documents.get(url);

		if (existingDoc != null) {
			log.warn("Creating document ["+url+"] but it already exists. Reusing existing!");
			return existingDoc;
		}

		TrackedDocument doc = new TrackedDocument(new TextDocument(url, languageId, version, text));
		documents.put(url, doc);
		return doc;
	}

	public final static CompletionList NO_COMPLETIONS = new CompletionList(false, Collections.emptyList());
	public final static Hover NO_HOVER = new Hover(ImmutableList.of(), null);
	public final static List<? extends Location> NO_REFERENCES = ImmutableList.of();
	public final static List<? extends SymbolInformation> NO_SYMBOLS = ImmutableList.of();
	public final static List<? extends CodeLens> NO_CODELENS = ImmutableList.of();
	public final static List<DocumentHighlight> NO_HIGHLIGHTS = ImmutableList.of();

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams position) {
		CompletionHandler h = completionHandler;
		if (h!=null) {
			return completionHandler.handle(position)
					.map(Either::<List<CompletionItem>, CompletionList>forRight)
					.toFuture();
		}
		return CompletableFuture.completedFuture(Either.forRight(NO_COMPLETIONS));
	}

	@Override
	public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
		log.info("Completion item resolve request received: {}", unresolved.getLabel());
		return async.invoke(() -> {
			try {
				CompletionResolveHandler h = completionResolveHandler;
				if (h!=null) {
					log.info("Completion item resolve request starting {}", unresolved.getLabel());
					return h.handle(unresolved);
				}
			} finally {
				log.info("Completion item resolve request terminated.");
			}
			return null;
		});
	}

	@Override
	public CompletableFuture<Hover> hover(HoverParams hoverParams) {
		log.debug("hover requested for {}", hoverParams.getPosition());
		long timeout = props.getHoverTimeout();
		return timeout <= 0 ? async.invoke(() -> computeHover(hoverParams)) : async.invoke(Duration.ofMillis(timeout), () -> computeHover(hoverParams), Mono.fromRunnable(() -> {
			log.error("Hover Request handler timed out after {} ms.", timeout);
		}));
	}
	
	private Hover computeHover(HoverParams hoverParams) {
		try {
			log.debug("hover handler starting");
			HoverHandler h = hoverHandler;
			if (h != null) {
				return hoverHandler.handle(hoverParams);
			}
			log.debug("no hover because there is no handler");
			return null;
		} finally {
			log.debug("hover handler finished");
		}
	}

	@Override
	public CompletableFuture<SignatureHelp> signatureHelp(SignatureHelpParams signatureHelpParams) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(
			DefinitionParams definitionParams) {

		DefinitionHandler h = this.definitionHandler;
		if (h != null) {
			return async.invoke(() -> {
				List<LocationLink> locations = h.handle(definitionParams);
				if (locations==null) {
					// vscode client does not like to receive null result. See: https://github.com/spring-projects/sts4/issues/309
					locations = ImmutableList.of();
				}
				// Workaround for https://github.com/eclipse-theia/theia/issues/6414
				// Theia does not support LocationLink yet
				switch (LspClient.currentClient()) {
					case THEIA:
					case ATOM:
					case INTELLIJ:	
						return Either.forLeft(locations.stream().map(link -> new Location(link.getTargetUri(), link.getTargetRange())).collect(Collectors.toList()));
					default:
						return Either.forRight(locations);
				}
			});
		}
		return CompletableFuture.completedFuture(Either.forLeft(ImmutableList.of()));
	}

	@Override
	public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
	  return async.invoke(() -> {
		ReferencesHandler h = this.referencesHandler;
		if (h != null) {
			List<? extends Location> list = h.handle(params);
			return list != null && list.isEmpty() ? null : list;
		}
		return null;
	  });
	}

	@Override
	public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(DocumentSymbolParams params) {
		return async.invoke(() -> {
			DocumentSymbolHandler h = this.documentSymbolHandler;
			if (h!=null) {
				server.waitForReconcile();
				if (server.hasHierarchicalDocumentSymbolSupport() && h instanceof HierarchicalDocumentSymbolHandler) {
					List<? extends DocumentSymbol> r = ((HierarchicalDocumentSymbolHandler)h).handleHierarchic(params);
					//handle it when symbolHandler is sloppy and returns null instead of empty list.
					return r == null
							? ImmutableList.of()
							: r.stream().map(symbolInfo -> Either.<SymbolInformation, DocumentSymbol>forRight(symbolInfo))
										.collect(Collectors.toList());
				} else {
					List<? extends SymbolInformation> r = h.handle(params);
					//handle it when symbolHandler is sloppy and returns null instead of empty list.
					return r == null
							? ImmutableList.of()
							: r.stream().map(symbolInfo -> Either.<SymbolInformation, DocumentSymbol>forLeft(symbolInfo))
										.collect(Collectors.toList());
				}
			}
			return ImmutableList.of();
		});
	}

	@Override
	public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
	  return async.invoke(() -> {
		TrackedDocument doc = documents.get(params.getTextDocument().getUri());
		if (doc!=null) {
			ImmutableList<Either<Command,CodeAction>> list = doc.getQuickfixes().stream()
				.filter((fix) -> fix.appliesTo(params.getRange(), params.getContext()))
				.map(Quickfix::getCodeAction)
				.map(command -> Either.<Command, CodeAction>forLeft(command))
				.collect(CollectorUtil.toImmutableList());
			return list;
		} else {
			return ImmutableList.of();
		}
	  });
	}

	@Override
	public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
		CodeLensHandler handler = this.codeLensHandler;
		if (handler != null) {
			return async.invoke(() -> handler.handle(params));
		}
		return CompletableFuture.completedFuture(Collections.emptyList());
	}

	@Override
	public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved) {
		CodeLensResolveHandler handler = this.codeLensResolveHandler;
		if (handler != null) {
			return async.invoke(() -> handler.handle(unresolved));
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
	  async.execute(() -> {
		if (documentSaveListeners != null) {
			TextDocumentIdentifier docId = params.getTextDocument();
			String url = docId.getUri();
			log.debug("didSave: "+url);
			if (url != null) {
				TextDocument doc = getDocument(url);
				if (doc != null) {
					for (Consumer<TextDocumentSaveChange> l : documentSaveListeners) {
						l.accept(new TextDocumentSaveChange(doc));
					}
				}
			}
		}
	  });
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

	public void setQuickfixes(TextDocumentIdentifier docId, List<Quickfix<?>> quickfixes) {
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
	public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(DocumentHighlightParams highlightParams) {
	  return async.invoke(() -> {
		DocumentHighlightHandler handler = this.documentHighlightHandler;
		if (handler != null) {
			return handler.handle(highlightParams);
		}
		return NO_HIGHLIGHTS;
	  });
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

	public boolean hasDocumentHighlightHandler() {
		return this.documentHighlightHandler!=null;
	}

	public boolean hasCodeLensHandler() {
		return this.codeLensHandler != null;
	}

	public boolean hasCodeLensResolveProvider() {
		return this.codeLensResolveHandler != null;
	}

	public TextDocument getDocumentSnapshot(TextDocumentIdentifier textDocumentIdentifier) {
		try {
			return async.invoke(() -> {
				TextDocument doc = get(textDocumentIdentifier.getUri());
				if (doc!=null) {
					return doc.copy();
				}
				return null;
			}).get();
		} catch (Exception e) {
			throw ExceptionUtil.unchecked(e);
		}
	}

}
