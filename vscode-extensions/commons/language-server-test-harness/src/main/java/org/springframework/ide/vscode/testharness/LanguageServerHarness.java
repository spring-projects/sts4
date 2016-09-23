package org.springframework.ide.vscode.testharness;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.assertj.core.api.Condition;

import io.typefox.lsapi.ClientCapabilitiesImpl;
import io.typefox.lsapi.CompletionItem;
import io.typefox.lsapi.CompletionList;
import io.typefox.lsapi.Diagnostic;
import io.typefox.lsapi.DidChangeTextDocumentParamsImpl;
import io.typefox.lsapi.DidOpenTextDocumentParamsImpl;
import io.typefox.lsapi.InitializeParamsImpl;
import io.typefox.lsapi.InitializeResult;
import io.typefox.lsapi.Position;
import io.typefox.lsapi.PositionImpl;
import io.typefox.lsapi.PublishDiagnosticsParams;
import io.typefox.lsapi.Range;
import io.typefox.lsapi.ServerCapabilities;
import io.typefox.lsapi.TextDocumentContentChangeEventImpl;
import io.typefox.lsapi.TextDocumentItemImpl;
import io.typefox.lsapi.TextDocumentPositionParamsImpl;
import io.typefox.lsapi.VersionedTextDocumentIdentifierImpl;
import io.typefox.lsapi.services.LanguageServer;

public class LanguageServerHarness {

	//Warning this 'harness' is incomplete. Growing it as needed.

	private Random random = new Random();

	private Callable<? extends LanguageServer> factory;

	private LanguageServer server;

	private InitializeResult initResult;
	
	private Map<String,TextDocumentInfo> documents = new HashMap<>();
	private Map<String, PublishDiagnosticsParams> diagnostics = new HashMap<>();

	public LanguageServerHarness(Callable<? extends LanguageServer> factory) throws Exception {
		this.factory = factory;
	}

	public synchronized TextDocumentInfo getOrReadFile(File file) throws Exception {
		String uri = file.toURI().toString();
		TextDocumentInfo d = documents.get(uri);
		if (d==null) {
			documents.put(uri, d = readFile(file));
		}
		return d;
	}

	public TextDocumentInfo readFile(File file) throws Exception {
		byte[] encoded = Files.readAllBytes(file.toPath());
		String content = new String(encoded, getEncoding());
		TextDocumentItemImpl document = new TextDocumentItemImpl();
		document.setText(content);
		document.setUri(file.toURI().toString());
		document.setVersion(getFirstVersion());
		document.setLanguageId(getLanguageId());
		return new TextDocumentInfo(document);
	}
	
	private synchronized TextDocumentItemImpl setDocumentContent(String uri, String newContent) {
		TextDocumentInfo o = documents.get(uri);
		TextDocumentItemImpl n = new TextDocumentItemImpl();
		n.setLanguageId(o.getLanguageId());
		n.setText(newContent);
		n.setVersion(o.getVersion()+1);
		n.setUri(o.getUri());
		documents.put(uri, new TextDocumentInfo(n));
		return n;
	}

	protected Charset getEncoding() {
		return Charset.forName("utf8");
	}

	protected String getLanguageId() {
		return "plaintext";
	}
	
	protected String getFileExtension() {
		return ".txt";
	}
	
	private synchronized void receiveDiagnostics(PublishDiagnosticsParams diags) {
		this.diagnostics.put(diags.getUri(), diags);
	}

	public InitializeResult intialize(File workspaceRoot) throws Exception {
		server = factory.call();
		int parentPid = random.nextInt(40000)+1000;
		InitializeParamsImpl initParams = new InitializeParamsImpl();
		initParams.setRootPath(workspaceRoot== null?null:workspaceRoot.toString());
		initParams.setProcessId(parentPid);
		ClientCapabilitiesImpl clientCap = new ClientCapabilitiesImpl();
		initParams.setCapabilities(clientCap);
		initResult = server.initialize(initParams).get();
		
		server.getTextDocumentService().onPublishDiagnostics(this::receiveDiagnostics);
		return initResult;
	}

	public TextDocumentInfo openDocument(TextDocumentInfo documentInfo) throws Exception {
		DidOpenTextDocumentParamsImpl didOpen = new DidOpenTextDocumentParamsImpl();
		didOpen.setTextDocument(documentInfo.getDocument());
		didOpen.setText(documentInfo.getText());
		didOpen.setUri(documentInfo.getUri());
		server.getTextDocumentService().didOpen(didOpen);
		return documentInfo;
	}
	
	public TextDocumentInfo openDocument(File file) throws Exception {
		return openDocument(getOrReadFile(file));
	}

	public TextDocumentInfo changeDocument(String uri, String newContent) throws Exception {
		TextDocumentItemImpl textDocument = setDocumentContent(uri, newContent);
		DidChangeTextDocumentParamsImpl didChange = new DidChangeTextDocumentParamsImpl();
		VersionedTextDocumentIdentifierImpl version = new VersionedTextDocumentIdentifierImpl();
		version.setUri(uri);
		version.setVersion(textDocument.getVersion());
		didChange.setTextDocument(version);
		switch (getDocumentSyncMode()) {
		case ServerCapabilities.SYNC_NONE:
			break; //nothing todo
		case ServerCapabilities.SYNC_INCREMENTAL:
			throw new IllegalStateException("Incremental sync not yet supported by this test harness");
		case ServerCapabilities.SYNC_FULL:
			TextDocumentContentChangeEventImpl change = new TextDocumentContentChangeEventImpl();
			change.setText(newContent);
			didChange.setContentChanges(Collections.singletonList(change));
			break;
		default:
			throw new IllegalStateException("Unkown SYNC mode: "+getDocumentSyncMode());
		}
		server.getTextDocumentService().didChange(didChange);
		return documents.get(uri);
	}

	private int getDocumentSyncMode() {
		Integer mode = initResult.getCapabilities().getTextDocumentSync();
		return mode==null ? ServerCapabilities.SYNC_NONE : mode;
	}

	public PublishDiagnosticsParams getDiagnostics(TextDocumentInfo doc) {
		return diagnostics.get(doc.getUri());
	}
	
	public static Condition<Diagnostic> isDiagnosticWithSeverity(int severity) {
		return new Condition<>(
				(d) -> d.getSeverity()==severity,
				"Diagnostic with severity '"+severity+"'"
		); 
	}

	public static Condition<Diagnostic> isDiagnosticCovering(TextDocumentInfo doc, String string) {
		return new Condition<>(
				(d) -> isDiagnosticCovering(d, doc, string),
				"Diagnostic covering '"+string+"'"
		); 
	}

	public static final Condition<Diagnostic> isWarning = isDiagnosticWithSeverity(Diagnostic.SEVERITY_WARNING);

	public static boolean isDiagnosticCovering(Diagnostic diag, TextDocumentInfo doc, String string) {
		Range rng = diag.getRange();
		String actualText = doc.getText(rng);
		return string.equals(actualText);
	}

	public static Condition<Diagnostic> isDiagnosticOnLine(int line) {
		return new Condition<>(
				(d) -> d.getRange().getStart().getLine()==line,
				"Diagnostic on line "+line
		); 
	}

	public CompletionList getCompletions(TextDocumentInfo doc, Position cursor) throws Exception {
		TextDocumentPositionParamsImpl params = new TextDocumentPositionParamsImpl();
		params.setPosition(toImpl(cursor));
		params.setTextDocument(doc.getId());
		return server.getTextDocumentService().completion(params).get();
	}

	private PositionImpl toImpl(Position pos) {
		if (pos instanceof PositionImpl) {
			return (PositionImpl) pos;
		} else {
			PositionImpl imp = new PositionImpl();
			imp.setCharacter(pos.getCharacter());
			imp.setLine(pos.getLine());
			return imp;
		}
	}

	private CompletionItem resolveCompletionItem(CompletionItem unresolved) {
		try {
			return server.getTextDocumentService().resolveCompletionItem(unresolved).get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public List<CompletionItem> resolveCompletions(CompletionList completions) {
		return completions.getItems().stream()
		.map(this::resolveCompletionItem)
		.collect(Collectors.toList());
	}

	public Editor newEditor(String contents) throws Exception {
		return new Editor(this, contents);
	}

	public synchronized TextDocumentInfo createWorkingCopy(String contents) throws Exception {
		TextDocumentItemImpl doc = new TextDocumentItemImpl();
		doc.setLanguageId(getLanguageId());
		doc.setText(contents);
		doc.setUri(createTempUri());
		doc.setVersion(getFirstVersion());
		TextDocumentInfo docinfo = new TextDocumentInfo(doc);
		documents.put(docinfo.getUri(), docinfo);
		return docinfo;
	}

	protected int getFirstVersion() {
		return 1;
	}

	protected String createTempUri() throws Exception {
		return File.createTempFile("workingcopy", getFileExtension()).toURI().toString();
	}

}
