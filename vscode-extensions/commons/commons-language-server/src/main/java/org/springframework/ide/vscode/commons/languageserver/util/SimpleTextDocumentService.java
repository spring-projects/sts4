package org.springframework.ide.vscode.commons.languageserver.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Logger;

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
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.springframework.ide.vscode.commons.util.Assert;
import org.springframework.ide.vscode.commons.util.Futures;

public class SimpleTextDocumentService implements TextDocumentService {

	private static final Logger LOG = Logger.getLogger(SimpleTextDocumentService.class.getName());

	final private SimpleLanguageServer server;
	private Map<String, TextDocument> documents = new HashMap<>();
	private ListenerList<TextDocumentContentChange> documentChangeListeners = new ListenerList<>();
	private CompletionHandler completionHandler = null;
	private CompletionResolveHandler completionResolveHandler = null;

	public SimpleTextDocumentService(SimpleLanguageServer server) {
		this.server = server;
	}

	public synchronized void onCompletion(CompletionHandler h) {
		Assert.isNull("A completion handler is already set, multiple handlers not supported yet", completionHandler);
		this.completionHandler = h;
	}

	public synchronized void onCompletionResolve(CompletionResolveHandler h) {
		Assert.isNull("A completionResolveHandler handler is already set, multiple handlers not supported yet", completionResolveHandler);
		this.completionResolveHandler = h;
	}

	/**
	 * Gets all documents this service is tracking, generally these are the documents that have been opened / changed,
	 * and not yet closed.
	 */
	public synchronized Collection<TextDocument> getAll() {
		return new ArrayList<>(documents.values());
	}

	@Override
	public final void didChange(DidChangeTextDocumentParams params) {
		VersionedTextDocumentIdentifier docId = params.getTextDocument();
		String url = docId.getUri();
		if (url!=null) {
			LOG.info("Document changed: "+url);
			TextDocument doc = getOrCreateDocument(url);
			for (TextDocumentContentChangeEvent change : params.getContentChanges()) {
				doc.apply(change);
				didChangeContent(doc, change);
			}
		}
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		//LOG.info("didOpen: "+params);
		//Example message:
		//{
		//   "jsonrpc":"2.0",
		//   "method":"textDocument/didOpen",
		//   "params":{
		//      "textDocument":{
		//         "uri":"file:///home/kdvolder/tmp/hello-java/hello.txt",
		//         "languageId":"plaintext",
		//         "version":1,
		//         "text":"This is some text ya-all o\nsss typescript\n"
		//      }
		//   }
		//}
		String url = params.getTextDocument().getUri();
		if (url!=null) {
			String text = params.getTextDocument().getText();
			TextDocument doc = getOrCreateDocument(url);
			doc.setText(text);
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
			TextDocumentContentChange evt = new TextDocumentContentChange(doc, change);
			documentChangeListeners.fire(evt);
		}
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		System.out.println("closing: "+params.getTextDocument().getUri());
		String url = params.getTextDocument().getUri();
		if (url!=null) {
			documents.remove(url);
		}
	}


	void didChangeContent(TextDocument doc, TextDocumentContentChangeEvent change) {
		documentChangeListeners.fire(new TextDocumentContentChange(doc, change));
	}

	public void onDidChangeContent(Consumer<TextDocumentContentChange> l) {
		documentChangeListeners.add(l);
	}

	private synchronized TextDocument getOrCreateDocument(String url) {
		TextDocument doc = documents.get(url);
		if (doc==null) {
			documents.put(url, doc = new TextDocument(url));
		}
		return doc;
	}

	public final static CompletableFuture<CompletionList> NO_COMPLETIONS = Futures.of(
			new CompletionList(false, Collections.emptyList()));


	@Override
	public CompletableFuture<CompletionList> completion(TextDocumentPositionParams position) {
		CompletionHandler h = completionHandler;
		if (h!=null) {
			return completionHandler.handle(position);
		}
		return NO_COMPLETIONS;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<SignatureHelp> signatureHelp(TextDocumentPositionParams position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<List<? extends Location>> definition(TextDocumentPositionParams position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<List<? extends SymbolInformation>> documentSymbol(DocumentSymbolParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<List<? extends Command>> codeAction(CodeActionParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> rangeFormatting(DocumentRangeFormattingParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(DocumentOnTypeFormattingParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {
		// TODO Auto-generated method stub

	}

	public void publishDiagnostics(TextDocument doc, List<Diagnostic> diagnostics) {
		LanguageClient client = server.getClient();
		if (client!=null && diagnostics!=null) {
			PublishDiagnosticsParams params = new PublishDiagnosticsParams();
			params.setUri(doc.getUri());
			params.setDiagnostics(diagnostics);
			client.publishDiagnostics(params);
		}
	}

	public synchronized TextDocument get(TextDocumentPositionParams params) {
		return documents.get(params.getTextDocument().getUri());
	}

	@Override
	public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(TextDocumentPositionParams position) {
		// TODO Auto-generated method stub
		return null;
	}

}
