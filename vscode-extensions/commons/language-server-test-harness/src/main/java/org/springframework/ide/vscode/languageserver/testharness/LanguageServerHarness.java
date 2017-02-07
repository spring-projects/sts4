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

package org.springframework.ide.vscode.languageserver.testharness;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.assertj.core.api.Condition;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.springframework.ide.vscode.commons.languageserver.LanguageIds;
import org.springframework.ide.vscode.commons.languageserver.ProgressParams;
import org.springframework.ide.vscode.commons.languageserver.STS4LanguageClient;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;

public class LanguageServerHarness {

	//Warning this 'harness' is incomplete. Growing it as needed.

	private Random random = new Random();

	private Callable<? extends SimpleLanguageServer> factory;
	private String defaultLanguageId;

	private SimpleLanguageServer server;

	private InitializeResult initResult;

	private Map<String,TextDocumentInfo> documents = new HashMap<>();
	private Map<String, PublishDiagnosticsParams> diagnostics = new HashMap<>();


	public LanguageServerHarness(Callable<? extends SimpleLanguageServer> factory, String defaultLanguageId) {
		this.factory = factory;
		this.defaultLanguageId = defaultLanguageId;
	}

	public LanguageServerHarness(Callable<? extends SimpleLanguageServer> factory) throws Exception {
		this(factory, LanguageIds.PLAINTEXT);
	}

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

	protected String getDefaultLanguageId() {
		return defaultLanguageId;
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
		InitializeParams initParams = new InitializeParams();
		initParams.setRootPath(workspaceRoot== null?null:workspaceRoot.toString());
		initParams.setProcessId(parentPid);
		ClientCapabilities clientCap = new ClientCapabilities();
		initParams.setCapabilities(clientCap);
		initResult = server.initialize(initParams).get();
		if (server instanceof LanguageClientAware) {
			((LanguageClientAware) server).connect(new STS4LanguageClient() {
				@Override
				public void telemetryEvent(Object object) {
					// TODO Auto-generated method stub

				}

				@Override
				public CompletableFuture<Void> showMessageRequest(ShowMessageRequestParams requestParams) {
					// TODO Auto-generated method stub
					return null;
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
				public void logMessage(MessageParams message) {
					// TODO Auto-generated method stub

				}

				@Override
				public void progress(ProgressParams progressEvent) {
					// TODO Auto-generated method stub

				}
			});

		}
		return initResult;
	}

	public TextDocumentInfo openDocument(TextDocumentInfo documentInfo) throws Exception {
		DidOpenTextDocumentParams didOpen = new DidOpenTextDocumentParams();
		didOpen.setTextDocument(documentInfo.getDocument());
		if (server!=null) {
			server.getTextDocumentService().didOpen(didOpen);
		}
		return documentInfo;
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
		if (server!=null) {
			server.getTextDocumentService().didChange(didChange);
		}
		return documents.get(uri);
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
		if (server!=null) {
			server.getTextDocumentService().didChange(didChange);
		}
		return documents.get(uri);
	}

	private TextDocumentSyncKind getDocumentSyncMode() {
		if (initResult!=null) {
			TextDocumentSyncKind mode = initResult.getCapabilities().getTextDocumentSync();
			if (mode!=null) {
				return mode;
			}
		}
		return TextDocumentSyncKind.None;
	}

	public PublishDiagnosticsParams getDiagnostics(TextDocumentInfo doc) throws Exception {
		this.server.waitForReconcile();
		return diagnostics.get(doc.getUri());
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
		TextDocumentPositionParams params = new TextDocumentPositionParams();
		params.setPosition(cursor);
		params.setTextDocument(doc.getId());
		server.waitForReconcile();
		return server.getTextDocumentService().completion(params).get();
	}

	public Hover getHover(TextDocumentInfo document, Position cursor) throws Exception {

		TextDocumentPositionParams params = new TextDocumentPositionParams();
		params.setPosition(cursor);
		params.setTextDocument(document.getId());
		return server.getTextDocumentService().hover(params ).get();
	}


	public CompletionItem resolveCompletionItem(CompletionItem unresolved) {
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
		return new Editor(this, contents, getDefaultLanguageId());
	}

	public Editor newEditor(String languageId, String contents) throws Exception {
		return new Editor(this, contents, languageId);
	}

	public synchronized TextDocumentInfo createWorkingCopy(String contents, String languageId) throws Exception {
		TextDocumentItem doc = new TextDocumentItem();
		doc.setLanguageId(languageId);
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

	public List<? extends Location> getDefinitions(TextDocumentPositionParams params) throws Exception {
		server.waitForReconcile(); //goto definitions relies on reconciler infos! Must wait or race condition breaking tests occasionally.
		return server.getTextDocumentService().definition(params).get();
	}

}
