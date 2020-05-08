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
package org.springframework.ide.vscode.languageserver.testharness;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.assertj.core.api.Condition;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.ApplyWorkspaceEditResponse;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemCapabilities;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.CreateFile;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.DeleteFile;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolCapabilities;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.ExecuteCommandCapabilities;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.RenameFile;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.ResourceOperationKind;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.TextDocumentSyncOptions;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceClientCapabilities;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.WorkspaceEditCapabilities;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.util.LanguageServerTestListener;
import org.springframework.ide.vscode.commons.languageserver.util.Settings;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.protocol.CursorMovement;
import org.springframework.ide.vscode.commons.protocol.HighlightParams;
import org.springframework.ide.vscode.commons.protocol.ProgressParams;
import org.springframework.ide.vscode.commons.protocol.STS4LanguageClient;
import org.springframework.ide.vscode.commons.protocol.java.ClasspathListenerParams;
import org.springframework.ide.vscode.commons.protocol.java.JavaCodeCompleteData;
import org.springframework.ide.vscode.commons.protocol.java.JavaCodeCompleteParams;
import org.springframework.ide.vscode.commons.protocol.java.JavaDataParams;
import org.springframework.ide.vscode.commons.protocol.java.JavaSearchParams;
import org.springframework.ide.vscode.commons.protocol.java.JavaTypeHierarchyParams;
import org.springframework.ide.vscode.commons.protocol.java.TypeData;
import org.springframework.ide.vscode.commons.protocol.java.TypeDescriptorData;
import org.springframework.ide.vscode.commons.util.ExceptionUtil;
import org.springframework.ide.vscode.commons.util.IOUtil;
import org.springframework.ide.vscode.commons.util.UriUtil;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import reactor.core.publisher.Mono;

public class LanguageServerHarness {

	//Warning this 'harness' is incomplete. Growing it as needed.

	private Random random = new Random();

	private final LanguageId defaultLanguageId;

	private final SimpleLanguageServer server;

	private InitializeResult initResult;

	private Map<String,TextDocumentInfo> documents = new HashMap<>();
	private Multimap<String, CompletableFuture<HighlightParams>> highlights = MultimapBuilder.hashKeys().linkedListValues().build();
	private Map<String, PublishDiagnosticsParams> diagnostics = new HashMap<>();
	private List<Editor> activeEditors = new ArrayList<>();
	private Gson gson = new Gson();

	private boolean enableHierarchicalDocumentSymbols = false;


	public LanguageServerHarness(SimpleLanguageServer server, LanguageId defaultLanguageId) {
		this.defaultLanguageId = defaultLanguageId;
		this.server = server;
	}

	public static final Duration HIGHLIGHTS_TIMEOUT = Duration.ofMillis(10_000L); //Why so long?

//	public static LanguageServerHarness<SimpleLanguageServer> create(String extensionId, LanguageServerInitializer initializer) throws Exception {
//		Callable<SimpleLanguageServer> factory = () -> {
//			SimpleLanguageServer s = new SimpleLanguageServer(extensionId);
//			initializer.initialize(s);
//			return s;
//		};
//		return new LanguageServerHarness<>(factory);
//	}

//	public LanguageServerHarness(Callable<S> factory) throws Exception {
//		this(factory, LanguageId.PLAINTEXT);
//	}

	public synchronized TextDocumentInfo getOrReadFile(File file, String languageId) throws Exception {
		String uri = file.toURI().toString();
		TextDocumentInfo d = documents.get(uri);
		if (d==null) {
			documents.put(uri, d = readFile(file, languageId));
		}
		return d;
	}

	public TextDocumentInfo readFile(File file, String languageId) throws Exception {
		byte[] encoded = Files.readAllBytes(file.toPath());
		String content = new String(encoded, getEncoding());
		TextDocumentItem document = new TextDocumentItem();
		document.setText(content);
		document.setUri(file.toURI().toString());
		document.setVersion(getFirstVersion());
		document.setLanguageId(languageId);
		return new TextDocumentInfo(document);
	}

	private synchronized TextDocumentItem setDocumentContent(String uri, String newContent) {
		TextDocumentInfo o = documents.get(uri);
		TextDocumentItem n = new TextDocumentItem();
		n.setLanguageId(o.getLanguageId().getId());
		n.setText(newContent);
		n.setVersion(o.getVersion()+1);
		n.setUri(o.getUri());
		documents.put(uri, new TextDocumentInfo(n));
		return n;
	}

	protected Charset getEncoding() {
		return Charset.forName("utf8");
	}

	protected LanguageId getDefaultLanguageId() {
		return defaultLanguageId;
	}

	protected String getFileExtension() {
		return ".txt";
	}

	private synchronized void receiveDiagnostics(PublishDiagnosticsParams diags) {
		this.diagnostics.put(diags.getUri(), diags);
	}

	private void receiveHighlights(HighlightParams highlights) {
		Collection<CompletableFuture<HighlightParams>>requestors = ImmutableList.of();
		synchronized (this) {
			String uri = highlights.getDoc().getUri();
			if (uri!=null) {
				requestors = ImmutableList.copyOf(this.highlights.get(uri));
				//Carefull!! Must make a copy above. Because the returned collection is cleared when we call removeAll below.
				this.highlights.removeAll(uri); //futures can only be completed once, so no point holding any longer
			}
		}
		for (CompletableFuture<HighlightParams> future : requestors) {
			future.complete(highlights);
		}
	}

	public void ensureInitialized() throws Exception {
		if (initResult==null) {
			intialize(null);
		}
	}

	public InitializeResult intialize(File workspaceRoot) throws Exception {
		int parentPid = random.nextInt(40000)+1000;
		InitializeParams initParams = new InitializeParams();
		if (workspaceRoot!=null) {
			initParams.setRootPath(workspaceRoot.toString());
			initParams.setRootUri(UriUtil.toUri(workspaceRoot).toString());
		}
		initParams.setProcessId(parentPid);
		ClientCapabilities clientCap = new ClientCapabilities();
		TextDocumentClientCapabilities textCap = new TextDocumentClientCapabilities();
		DocumentSymbolCapabilities documentSymbolCap = new DocumentSymbolCapabilities();
		documentSymbolCap.setHierarchicalDocumentSymbolSupport(enableHierarchicalDocumentSymbols);
		textCap.setDocumentSymbol(documentSymbolCap);
		CompletionCapabilities completionCap = new CompletionCapabilities(new CompletionItemCapabilities(true));
		textCap.setCompletion(completionCap);
		clientCap.setTextDocument(textCap);
		WorkspaceClientCapabilities workspaceCap = new WorkspaceClientCapabilities();
		workspaceCap.setApplyEdit(true);
		ExecuteCommandCapabilities exeCap = new ExecuteCommandCapabilities();
		exeCap.setDynamicRegistration(true);
		workspaceCap.setExecuteCommand(exeCap);
		WorkspaceEditCapabilities workspaceEdit = new WorkspaceEditCapabilities();
		workspaceEdit.setDocumentChanges(true);
		workspaceEdit.setResourceOperations(Arrays.asList(ResourceOperationKind.Create, ResourceOperationKind.Delete, ResourceOperationKind.Rename));
		workspaceCap.setWorkspaceEdit(workspaceEdit);
		clientCap.setWorkspace(workspaceCap);
		initParams.setCapabilities(clientCap);
		initResult = getServer().initialize(initParams).get();
		if (getServer() instanceof LanguageClientAware) {
			((LanguageClientAware) getServer()).connect(new STS4LanguageClient() {
				@Override
				public void telemetryEvent(Object object) {
					// TODO Auto-generated method stub

				}

				@Override
				public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
					// TODO Auto-generated method stub
					return CompletableFuture.completedFuture(new MessageActionItem("Some Message Request Answer"));
				}

				@Override
				public void showMessage(MessageParams messageParams) {
					// TODO Auto-generated method stub

				}

				@Override
				public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
					receiveDiagnostics(diagnostics);
				}

				@Override
				public void highlight(HighlightParams highlights) {
					receiveHighlights(highlights);
				}

				@Override
				public void logMessage(MessageParams message) {
					// TODO Auto-generated method stub

				}

				@Override
				public CompletableFuture<ApplyWorkspaceEditResponse> applyEdit(ApplyWorkspaceEditParams params) {
					return Mono.fromCallable(() -> {
						perform(params.getEdit());
						return new ApplyWorkspaceEditResponse(true);
					}).toFuture();
				}

				@Override
				public CompletableFuture<Void> registerCapability(RegistrationParams params) {
					return CompletableFuture.completedFuture(null);
				}

				@Override
				public void progress(ProgressParams progressEvent) {
					// TODO Auto-generated method stub
				}

				@Override
				public CompletableFuture<Object> moveCursor(CursorMovement cursorMovement) {
					for (Editor editor : activeEditors) {
						if (editor.getUri().equals(cursorMovement.getUri())) {
							editor.setCursor(cursorMovement.getPosition());
							return CompletableFuture.completedFuture(new ApplyWorkspaceEditResponse(true));
						}
					}
					return CompletableFuture.completedFuture(new ApplyWorkspaceEditResponse(false));
				}

				@Override
				public CompletableFuture<Object> addClasspathListener(
						ClasspathListenerParams params) {
					return CompletableFuture.completedFuture("ok");
				}

				@Override
				public CompletableFuture<Object> removeClasspathListener(
						ClasspathListenerParams classpathListenerParams) {
					return CompletableFuture.completedFuture("ok");
				}

				@Override
				public CompletableFuture<MarkupContent> javadoc(JavaDataParams params) {
					return CompletableFuture.completedFuture(null);
				}

				@Override
				public CompletableFuture<TypeData> javaType(JavaDataParams params) {
					return CompletableFuture.completedFuture(null);
				}

				@Override
				public CompletableFuture<String> javadocHoverLink(JavaDataParams params) {
					return CompletableFuture.completedFuture(null);
				}

				@Override
				public CompletableFuture<Location> javaLocation(JavaDataParams params) {
					return CompletableFuture.completedFuture(null);
				}

				@Override
				public CompletableFuture<List<TypeDescriptorData>> javaSearchTypes(JavaSearchParams params) {
					return CompletableFuture.completedFuture(Collections.emptyList());
				}

				@Override
				public CompletableFuture<List<String>> javaSearchPackages(JavaSearchParams params) {
					return CompletableFuture.completedFuture(Collections.emptyList());
				}

				@Override
				public CompletableFuture<List<Either<TypeDescriptorData, TypeData>>> javaSubTypes(JavaTypeHierarchyParams params) {
					return CompletableFuture.completedFuture(Collections.emptyList());
				}

				@Override
				public CompletableFuture<List<Either<TypeDescriptorData, TypeData>>> javaSuperTypes(JavaTypeHierarchyParams params) {
					return CompletableFuture.completedFuture(Collections.emptyList());
				}

				@Override
				public CompletableFuture<List<JavaCodeCompleteData>> javaCodeComplete(JavaCodeCompleteParams params) {
					return CompletableFuture.completedFuture(Collections.emptyList());
				}

			});

		}
		getServer().initialized();
		return initResult;
	}

	public TextDocumentInfo openDocument(TextDocumentInfo documentInfo) throws Exception {
		DidOpenTextDocumentParams didOpen = new DidOpenTextDocumentParams();
		didOpen.setTextDocument(documentInfo.getDocument());
		if (getServer()!=null) {
			getServer().getTextDocumentService().didOpen(didOpen);
		}
		return documentInfo;
	}

	public void closeDocument(TextDocumentIdentifier id) {
		DidCloseTextDocumentParams didClose = new DidCloseTextDocumentParams(id);
		if (getServer() != null) {
			getServer().getTextDocumentService().didClose(didClose);
		}
	}

	public TextDocumentInfo openDocument(File file, String languageId) throws Exception {
		return openDocument(getOrReadFile(file, languageId));
	}

	public synchronized TextDocumentInfo changeDocument(String uri, int start, int end, String replaceText) {
		TextDocumentInfo oldDoc = documents.get(uri);
		String oldContent = oldDoc.getText();
		String newContent = oldContent.substring(0, start) + replaceText + oldContent.substring(end);
		TextDocumentItem textDocument = setDocumentContent(uri, newContent);
		DidChangeTextDocumentParams didChange = new DidChangeTextDocumentParams();
		VersionedTextDocumentIdentifier version = new VersionedTextDocumentIdentifier();
		version.setUri(uri);
		version.setVersion(textDocument.getVersion());
		didChange.setTextDocument(version);
		switch (getDocumentSyncMode()) {
		case None:
			break; //nothing todo
		case Incremental: {
			TextDocumentContentChangeEvent change = new TextDocumentContentChangeEvent();
			change.setRange(new Range(oldDoc.toPosition(start), oldDoc.toPosition(end)));
			change.setRangeLength(end-start);
			change.setText(replaceText);
			didChange.setContentChanges(Collections.singletonList(change));
			break;
		}
		case Full: {
			TextDocumentContentChangeEvent change = new TextDocumentContentChangeEvent();
			change.setText(newContent);
			didChange.setContentChanges(Collections.singletonList(change));
			break;
		}
		default:
			throw new IllegalStateException("Unkown SYNC mode: "+getDocumentSyncMode());
		}
		if (getServer()!=null) {
			getServer().getTextDocumentService().didChange(didChange);
		}
		return documents.get(uri);
	}

	public synchronized void changeFile(String uri) {
		FileEvent fileEvent = new FileEvent(uri, FileChangeType.Changed);
		getServer().getWorkspaceService().didChangeWatchedFiles(new DidChangeWatchedFilesParams(Arrays.asList(fileEvent)));
	}

	public synchronized void createFile(String uri) {
		FileEvent fileEvent = new FileEvent(uri, FileChangeType.Created);
		getServer().getWorkspaceService().didChangeWatchedFiles(new DidChangeWatchedFilesParams(Arrays.asList(fileEvent)));
	}

	public synchronized void deleteFile(String uri) {
		FileEvent fileEvent = new FileEvent(uri, FileChangeType.Deleted);
		getServer().getWorkspaceService().didChangeWatchedFiles(new DidChangeWatchedFilesParams(Arrays.asList(fileEvent)));
	}

	public TextDocumentInfo changeDocument(String uri, String newContent) throws Exception {
		TextDocumentItem textDocument = setDocumentContent(uri, newContent);
		DidChangeTextDocumentParams didChange = new DidChangeTextDocumentParams();
		VersionedTextDocumentIdentifier version = new VersionedTextDocumentIdentifier();
		version.setUri(uri);
		version.setVersion(textDocument.getVersion());
		didChange.setTextDocument(version);
		switch (getDocumentSyncMode()) {
		case None:
			break; //nothing todo
		case Incremental:
		case Full:
			TextDocumentContentChangeEvent change = new TextDocumentContentChangeEvent();
			change.setText(newContent);
			didChange.setContentChanges(Collections.singletonList(change));
			break;
		default:
			throw new IllegalStateException("Unkown SYNC mode: "+getDocumentSyncMode());
		}
		if (getServer()!=null) {
			getServer().getTextDocumentService().didChange(didChange);
		}
		return documents.get(uri);
	}

	private TextDocumentSyncKind getDocumentSyncMode() {
		if (initResult!=null) {
			Either<TextDocumentSyncKind, TextDocumentSyncOptions> mode = initResult.getCapabilities().getTextDocumentSync();
			if (mode.isLeft()) {
				return mode.getLeft();
			} else {
				throw new IllegalStateException("Harness doesn't support fancy Sync options yet!");
			}
		}
		return TextDocumentSyncKind.None;
	}

	public PublishDiagnosticsParams getDiagnostics(TextDocumentInfo doc) throws Exception {
		waitForReconcile();
		return diagnostics.get(doc.getUri());
	}

	public synchronized Future<HighlightParams> getHighlightsFuture(TextDocumentInfo doc) {
		CompletableFuture<HighlightParams> future = new CompletableFuture<HighlightParams>();
		highlights.put(doc.getUri(), future);
		return future;
	}

	public HighlightParams getHighlights(TextDocumentInfo doc) throws Exception {
		return getHighlights(true, doc);
	}

	/**
	 * Set expectServerHighlights to false if NO highlights are expected from the server (for example, test cases
	 * that test that no highlights are received from the server because there are no running apps).
	 * @param expectServerHighlights false if NOT expecting any highlights from the server
	 * @param doc
	 * @return highlights, if they are expected, or null if they are not expected.
	 * @throws Exception
	 */
	public HighlightParams getHighlights(boolean expectServerHighlights, TextDocumentInfo doc) throws Exception {
		try {
			return getHighlightsFuture(doc).get(HIGHLIGHTS_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			// highlight requestor will timeout if the server does not send any highlights. This
			// is not always an error. For example, if there are no initial running apps, the server will
			// NOT send highlights (see PT 156688501), so in this case we expect to time out as part of
			// the expected behaviour
			if (!expectServerHighlights) {
				return null;
			}
			else {
				throw e;
			}
		}
	}

	public static Condition<Diagnostic> isDiagnosticWithSeverity(DiagnosticSeverity severity) {
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

	public static final Condition<Diagnostic> isWarning = isDiagnosticWithSeverity(DiagnosticSeverity.Warning);

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
		CompletionParams params = new CompletionParams();
		params.setPosition(cursor);
		params.setTextDocument(doc.getId());
		waitForReconcile();
		Either<List<CompletionItem>, CompletionList> completions = getServer().getTextDocumentService().completion(params).get();
		if (completions.isLeft()) {
			List<CompletionItem> list = completions.getLeft();
			return new CompletionList(false, list);
		} else /* sompletions.isRight() */ {
			return completions.getRight();
		}
	}

	private void waitForReconcile() throws Exception {
		getServer().getAsync().waitForAll();
		getServer().waitForReconcile();
	}

	public Hover getHover(TextDocumentInfo document, Position cursor) throws Exception {
		HoverParams params = new HoverParams();
		params.setPosition(cursor);
		params.setTextDocument(document.getId());
		return getServer().getTextDocumentService().hover(params ).get();
	}

	public List<? extends CodeLens> getCodeLenses(TextDocumentInfo document) throws Exception {
		CodeLensParams params = new CodeLensParams();
		params.setTextDocument(document.getId());
		return getServer().getTextDocumentService().codeLens(params).get();
	}


	public CompletionItem resolveCompletionItem(CompletionItem maybeUnresolved) {
		if (getServer().hasLazyCompletionResolver()) {
			try {
				return getServer().getTextDocumentService().resolveCompletionItem(maybeUnresolved).get();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return maybeUnresolved;
	}

	public List<CompletionItem> resolveCompletions(CompletionList completions) {
		return completions.getItems().stream()
		.map(this::resolveCompletionItem)
		.collect(Collectors.toList());
	}

	/**
	 * Create editor with 'default' language id.
	 */
	public Editor newEditor(String contents) throws Exception {
		ensureInitialized();
		return newEditor(getDefaultLanguageId(), contents);
	}

	public synchronized Editor newEditorWithExt(LanguageId languageId, String extension, String contents) throws Exception {
		ensureInitialized();
		Editor editor = new Editor(this, contents, languageId, extension);
		activeEditors.add(editor);
		return editor;
	}

	public synchronized Editor newEditor(LanguageId languageId, String contents) throws Exception {
		ensureInitialized();
		Editor editor = new Editor(this, contents, languageId);
		activeEditors.add(editor);
		return editor;
	}


	public synchronized Editor newEditor(LanguageId languageId, String contents, String resourceUri) throws Exception {
		ensureInitialized();
		TextDocumentInfo doc = docFromResource(contents, resourceUri, languageId);
		Editor editor = new Editor(this, doc, contents, languageId);
		activeEditors.add(editor);
		return editor;
	}

	public synchronized TextDocumentInfo docFromResource(String contents, String resourceUri, LanguageId languageId) throws Exception {
		TextDocumentItem doc = new TextDocumentItem();
		doc.setLanguageId(languageId.getId());
		doc.setText(contents);
		doc.setUri(resourceUri);
		doc.setVersion(getFirstVersion());
		TextDocumentInfo docinfo = new TextDocumentInfo(doc);
		documents.put(docinfo.getUri(), docinfo);
		return docinfo;
	}

	public synchronized TextDocumentInfo createWorkingCopy(String contents, LanguageId languageId, String extension) throws Exception {
		TextDocumentItem doc = new TextDocumentItem();
		doc.setLanguageId(languageId.getId());
		doc.setText(contents);
		doc.setUri(createTempUri(extension));
		doc.setVersion(getFirstVersion());
		TextDocumentInfo docinfo = new TextDocumentInfo(doc);
		documents.put(docinfo.getUri(), docinfo);
		return docinfo;
	}

	protected int getFirstVersion() {
		return 1;
	}

	public String createTempUri(String extension) throws Exception {
		if (extension == null) {
			extension = getFileExtension();
		}
		return File.createTempFile("workingcopy", extension).toURI().toString();
	}

	public void assertCompletion(String textBefore, String expectTextAfter) throws Exception {
		Editor editor = newEditor(textBefore);
		List<CompletionItem> completions = editor.getCompletions();
		assertNotNull(completions);
		assertFalse(completions.isEmpty());
		CompletionItem completion = editor.getFirstCompletion();
		editor.apply(completion);
		assertEquals(expectTextAfter, editor.getText());
	}

	public void assertCompletions(String textBefore, String... expectTextAfter) throws Exception {
		Editor editor = newEditor(textBefore);
		StringBuilder expect = new StringBuilder();
		StringBuilder actual = new StringBuilder();
		for (String after : expectTextAfter) {
			expect.append(after);
			expect.append("\n-------------------\n");
		}

		List<? extends CompletionItem> completions = editor.getCompletions();
		for (CompletionItem ci : completions) {
			editor = newEditor(textBefore);
			editor.apply(ci);
			actual.append(editor.getText());
			actual.append("\n-------------------\n");
		}
		assertEquals(expect.toString(), actual.toString());
	}

	public void assertCompletionDisplayString(String editorContents, String expected) throws Exception {
		Editor editor = newEditor(editorContents);
		CompletionItem completion = editor.getFirstCompletion();
		assertEquals(expected, completion.getLabel());
	}

	public List<? extends LocationLink> getDefinitions(DefinitionParams params) throws Exception {
		waitForReconcile(); //goto definitions relies on reconciler infos! Must wait or race condition breaking tests occasionally.
		return getServer().getTextDocumentService().definition(params).get().getRight();
	}

	public static void assertDocumentation(String expected, CompletionItem completion) {
		assertEquals(expected, getDocString(completion));
	}

	public static String getDocString(CompletionItem completion) {
		if (completion!=null) {
			Either<String, MarkupContent> doc = completion.getDocumentation();
			if (doc.isLeft()) {
				return doc.getLeft();
			} else {
				return doc.getRight().getValue();
			}
		}
		return null;
	}


	public List<CodeAction> getCodeActions(TextDocumentInfo doc, Diagnostic problem) throws Exception {
		CodeActionContext context = new CodeActionContext(ImmutableList.of(problem));
		List<Either<Command, org.eclipse.lsp4j.CodeAction>> actions =
				getServer().getTextDocumentService().codeAction(new CodeActionParams(doc.getId(), problem.getRange(), context)).get();
		return actions.stream()
				.map(e -> e.getLeft())
				.map((command) -> new CodeAction(this, command))
				.collect(Collectors.toList());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void perform(Command command) throws Exception {
		List<Object> args = command.getArguments();
		//Note convert the params to a 'typeless' Object because that is more representative on how it will be
		// received when we get it in a real client/server setting (i.e. parsed from json).
		JsonArray jsonArray = gson.toJsonTree(args).getAsJsonArray();
		List<Object> untypedParams = new ArrayList<>(jsonArray.size());
		jsonArray.forEach(e -> untypedParams.add(e));
		getServer().getWorkspaceService()
			.executeCommand(new ExecuteCommandParams(command.getCommand(), untypedParams))
			.get();
	}

	private void perform(WorkspaceEdit workspaceEdit) throws Exception {
		if (workspaceEdit.getDocumentChanges() == null) {
			for (Entry<String, List<TextEdit>> entry : workspaceEdit.getChanges().entrySet()) {
				String uri = entry.getKey();
				TextDocumentInfo document = documents.get(uri);
				assertNotNull("Can't apply edits to non-existing document: "+uri, document);

				TextDocument workingDocument = new TextDocument(uri, document.getLanguageId());
				workingDocument.setText(document.getText());
				DocumentEdits edits = new DocumentEdits(workingDocument, false);
				for (TextEdit edit : entry.getValue()) {
					Range range = edit.getRange();
					edits.replace(document.toOffset(range.getStart()), document.toOffset(range.getEnd()), edit.getNewText());
				}
				edits.apply(workingDocument);
				Editor editor = getOpenEditor(uri);
				if (editor!=null) {
					editor.setRawText(workingDocument.get());
				} else {
					changeDocument(uri, workingDocument.get());
				}
			}
		} else {
			for (Either<TextDocumentEdit, ResourceOperation> edit : workspaceEdit.getDocumentChanges()) {
				if (edit.isLeft()) {
					TextDocumentEdit docEdit = edit.getLeft();
					String uri = docEdit.getTextDocument().getUri();
					TextDocumentInfo docInfo = documents.get(uri);
					// IMORTANT: Document version is ignored for now
					Path path = Paths.get(URI.create(uri));
					String content = docInfo == null ? IOUtil.toString(Files.newInputStream(path)) : docInfo.getText();
					TextDocument workingDocument = new TextDocument(uri, docInfo == null ? (LanguageId) null : docInfo.getLanguageId(), 0, content);
					DocumentEdits edits = new DocumentEdits(workingDocument, false);
					for (TextEdit textEdit : docEdit.getEdits()) {
						Range range = textEdit.getRange();
						edits.replace(workingDocument.toOffset(range.getStart()), workingDocument.toOffset(range.getEnd()), textEdit.getNewText());
						edits.apply(workingDocument);
					}
					Editor editor = getOpenEditor(uri);
					if (editor!=null) {
						editor.setRawText(workingDocument.get());
					} else {
						if (docInfo == null) {
							Files.write(path, workingDocument.get().getBytes());
						} else {
							changeDocument(uri, workingDocument.get());
						}
					}
				} else if (edit.isRight()) {
					ResourceOperation resourceOperation = edit.getRight();
					if (resourceOperation instanceof CreateFile) {
						CreateFile createFileOp = (CreateFile) resourceOperation;

						Path path = Paths.get(URI.create(createFileOp.getUri()));
						if (Files.exists(path)) {
							if (createFileOp.getOptions().getOverwrite() || !createFileOp.getOptions().getIgnoreIfExists()) {
								String content = IOUtil.toString(Files.newInputStream(path));
								Files.write(path, content.getBytes());
							}
						} else {
							if (!Files.exists(path.getParent())) {
								Files.createDirectories(path.getParent());
							}
							Files.createFile(path);
						}
					} else if (resourceOperation instanceof RenameFile) {
						RenameFile rename = (RenameFile) resourceOperation;
						Path oldPath = Paths.get(URI.create(rename.getOldUri()));
						Path newPath = Paths.get(URI.create(rename.getNewUri()));
						String content = IOUtil.toString(Files.newInputStream(oldPath));
						Files.delete(oldPath);
						if (!Files.exists(newPath.getParent())) {
							Files.createDirectories(newPath.getParent());
						}
						Files.createFile(newPath);
						Files.write(newPath, content.getBytes());
					} else if (resourceOperation instanceof DeleteFile) {
						DeleteFile delete = (DeleteFile) resourceOperation;
						Files.delete(Paths.get(URI.create(delete.getUri())));
					}
				}
			}
		}
	}

	private static void createFile(Path path) {

	}

	private Editor getOpenEditor(String uri) {
		List<Editor> editors = getOpenEditors(uri);
		if (editors.isEmpty()) {
			return null;
		} else if (editors.size()>1) {
			throw new IllegalStateException("Multiple active editors on the same uri. The harness doesn't handle that yet!");
		}
		return editors.get(0);
	}

	private synchronized List<Editor> getOpenEditors(String uri) {
		return activeEditors.stream()
				.filter((editor) -> uri.equals(editor.getUri()))
				.collect(Collectors.toList());
	}

	public List<? extends DocumentSymbol> getHierarchicalDocumentSymbols(TextDocumentInfo document) throws Exception {
		waitForReconcile(); //TODO: if the server works properly this shouldn't be needed it should do that internally itself somehow.
		DocumentSymbolParams params = new DocumentSymbolParams(document.getId());
		return getServer().getTextDocumentService().documentSymbol(params).get().stream().map(e -> e.getRight()).collect(Collectors.toList());
	}

	public List<? extends SymbolInformation> getDocumentSymbols(TextDocumentInfo document) throws Exception {
		waitForReconcile(); //TODO: if the server works properly this shouldn't be needed it should do that internally itself somehow.
		DocumentSymbolParams params = new DocumentSymbolParams(document.getId());
		return getServer().getTextDocumentService().documentSymbol(params).get().stream().map(e -> e.getLeft()).collect(Collectors.toList());
	}

	/**
	 * Blocks the reconciler thread until a specific point in time explicitly controlled by the test.
	 */
	public SynchronizationPoint reconcilerThreadStart() {
		CompletableFuture<Void> blocker = new CompletableFuture<>();
		getServer().setTestListener(new LanguageServerTestListener() {
			@Override
			public void reconcileStarted(String uri, int version) {
				try {
					blocker.get();
				} catch (Exception e) {
					throw ExceptionUtil.unchecked(e);
				}
			}
		});
		return new SynchronizationPoint() {
			@Override public void unblock() {
				blocker.complete(null);
			}
			@Override public Future<Void> reached() {
				return blocker;
			}
		};
	}

	/**
	 * Create a new editor and populate contents from a file found on the (test) classpath.
	 */
	public Editor newEditorFromClasspath(String resourcePath) throws Exception {
		try (InputStream is = LanguageServerHarness.class.getResourceAsStream(resourcePath)) {
			if (is==null) {
				fail("Couldn't find the resource: "+resourcePath);
			}
			return newEditor(IOUtil.toString(is));
		}
	}

	/**
	 * Creates an editor for the given file URI. Note that the file URI must have "file" scheme
	 * @param docUri
	 * @param languageId
	 * @return
	 * @throws Exception
	 */
	public Editor newEditorFromFileUri(String docUri, LanguageId languageId) throws Exception {
		URI fileUri = new URI(docUri);
		assertTrue("Document URI is missing 'file' scheme: " + docUri,
				fileUri.getScheme() != null && fileUri.getScheme().contains("file"));

		Path path = Paths.get(fileUri);
		String content = new String(Files.readAllBytes(path));
		return newEditor(languageId, content, docUri);
	}

	public void changeConfiguration(Settings settings) {
		getServer().getWorkspaceService().didChangeConfiguration(new DidChangeConfigurationParams(settings));
	}

	public SimpleLanguageServer getServer() {
		return server==null ? null : server.getServer();
	}

	public void enableHierarchicalDocumentSymbols(boolean b) {
		this.enableHierarchicalDocumentSymbols = b;
	}

	public Collection<SymbolInformation> getWorkspaceSymbols(String query) throws Exception {
		WorkspaceSymbolParams params = new WorkspaceSymbolParams(query);
		List<? extends SymbolInformation> r = server.getWorkspaceService().symbol(params).get();
		return ImmutableList.copyOf(r);
	}

	public void assertWorkspaceSymbols(String query, String... expectedSymbols) throws Exception {
		Set<String> actualSymbols = getWorkspaceSymbols(query).stream().map(sym -> sym.getName()).collect(Collectors.toSet());
		assertEquals(ImmutableSet.copyOf(expectedSymbols), actualSymbols);
	}
}
