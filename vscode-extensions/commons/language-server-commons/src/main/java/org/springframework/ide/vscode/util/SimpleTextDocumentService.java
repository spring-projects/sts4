package org.springframework.ide.vscode.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Logger;

import io.typefox.lsapi.CodeActionParams;
import io.typefox.lsapi.CodeLens;
import io.typefox.lsapi.CodeLensParams;
import io.typefox.lsapi.Command;
import io.typefox.lsapi.CompletionItem;
import io.typefox.lsapi.CompletionList;
import io.typefox.lsapi.DidChangeTextDocumentParams;
import io.typefox.lsapi.DidCloseTextDocumentParams;
import io.typefox.lsapi.DidOpenTextDocumentParams;
import io.typefox.lsapi.DidSaveTextDocumentParams;
import io.typefox.lsapi.DocumentFormattingParams;
import io.typefox.lsapi.DocumentHighlight;
import io.typefox.lsapi.DocumentOnTypeFormattingParams;
import io.typefox.lsapi.DocumentRangeFormattingParams;
import io.typefox.lsapi.DocumentSymbolParams;
import io.typefox.lsapi.Hover;
import io.typefox.lsapi.Location;
import io.typefox.lsapi.PublishDiagnosticsParams;
import io.typefox.lsapi.Range;
import io.typefox.lsapi.ReferenceParams;
import io.typefox.lsapi.RenameParams;
import io.typefox.lsapi.SignatureHelp;
import io.typefox.lsapi.SymbolInformation;
import io.typefox.lsapi.TextDocumentContentChangeEvent;
import io.typefox.lsapi.TextDocumentPositionParams;
import io.typefox.lsapi.TextEdit;
import io.typefox.lsapi.VersionedTextDocumentIdentifier;
import io.typefox.lsapi.WorkspaceEdit;
import io.typefox.lsapi.impl.DiagnosticImpl;
import io.typefox.lsapi.impl.PublishDiagnosticsParamsImpl;
import io.typefox.lsapi.services.TextDocumentService;

public class SimpleTextDocumentService implements TextDocumentService {
	
    private static final Logger LOG = Logger.getLogger(SimpleTextDocumentService.class.getName());
    
    private Consumer<PublishDiagnosticsParams> publishDiagnostics = (p) -> {};
    
	private Map<String, TextDocument> documents = new HashMap<>();
	private ListenerList<TextDocumentContentChange> documentChangeListeners = new ListenerList<>();
	private CompletionHandler completionHandler = null;
	private CompletionResolveHandler completionResolveHandler = null;
	
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

	@Override
	public CompletableFuture<CompletionList> completion(TextDocumentPositionParams position) {
		CompletionHandler h = completionHandler;
		if (h!=null) {
			return completionHandler.handle(position);
		}
		return null; //TODO: does caller handle nulls? Or do we need to provide something that create a empty completion list?
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
	public CompletableFuture<DocumentHighlight> documentHighlight(TextDocumentPositionParams position) {
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

	@Override
	public synchronized void onPublishDiagnostics(Consumer<PublishDiagnosticsParams> callback) {
		publishDiagnostics = publishDiagnostics.andThen(callback);
	}

	public void publishDiagnostics(TextDocument doc, List<DiagnosticImpl> diagnostics) {
		if (diagnostics!=null) {
			PublishDiagnosticsParamsImpl params = new PublishDiagnosticsParamsImpl();
			params.setUri(doc.getUri());
			params.setDiagnostics(diagnostics);
			publishDiagnostics.accept(params);
		}
	}

}
