/*******************************************************************************
 * Copyright (c) 2017, 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.handlers.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.boot.bootiful.SymbolProviderTestConf;
import org.springframework.ide.vscode.boot.java.handlers.QueryCodeLensProvider;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.ExecuteCommandHandler;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.languageserver.testharness.TextDocumentInfo;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.google.gson.JsonPrimitive;

/**
 * @author Udayani V
 */
@SuppressWarnings("deprecation")
@ExtendWith(SpringExtension.class)
@BootLanguageServerTest
@Import(SymbolProviderTestConf.class)
public class QueryCodeLensProviderTest {

	@Autowired
	private BootLanguageServerHarness harness;
	@Autowired
	private JavaProjectFinder projectFinder;
	@Autowired
	private SpringSymbolIndex indexer;
	private SimpleLanguageServer server;

	private ArgumentCaptor<ExecuteCommandHandler> commandHandlerCaptor;
	private QueryCodeLensProvider queryCodeLensProvider;
	private File directory;

	@BeforeEach
	public void setup() throws Exception {
		harness.intialize(null);
		directory = new File(ProjectsHarness.class.getResource("/test-projects/test-spel-query-codelense/").toURI());
		String projectDir = directory.toURI().toString();
		server = mock(SimpleLanguageServer.class);
		commandHandlerCaptor = ArgumentCaptor.forClass(ExecuteCommandHandler.class);
		queryCodeLensProvider = new QueryCodeLensProvider(projectFinder, server);

		// trigger project creation
		projectFinder.find(new TextDocumentIdentifier(projectDir)).get();

		CompletableFuture<Void> initProject = indexer.waitOperation();
		initProject.get(5, TimeUnit.SECONDS);

		verify(server).onCommand(eq(QueryCodeLensProvider.CMD_ENABLE_COPILOT_FEATURES), commandHandlerCaptor.capture());
	}

	@Test
	public void testShowCodeLensesTrueForQuery() throws Exception {

		setCommandParamsHandler(true);

		String docUri = directory.toPath().resolve("src/main/java/org/test/OwnerRepository.java").toUri().toString();
		TextDocumentInfo doc = harness.getOrReadFile(new File(new URI(docUri)), LanguageId.JAVA.getId());
		TextDocumentInfo openedDoc = harness.openDocument(doc);

		List<? extends CodeLens> codeLenses = harness.getCodeLenses(openedDoc);

		assertEquals(3, codeLenses.size());

		assertTrue(containsCodeLens(codeLenses.get(0), QueryCodeLensProvider.EXPLAIN_QUERY_TITLE, 9, 8, 9, 108));
		assertTrue(containsCodeLens(codeLenses.get(1), QueryCodeLensProvider.EXPLAIN_QUERY_TITLE, 13, 8, 13, 39));
		assertTrue(containsCodeLens(codeLenses.get(2), QueryCodeLensProvider.EXPLAIN_QUERY_TITLE, 17, 14, 17, 92));
	}

	@Test
	public void testShowCodeLensesTrueForSpel() throws Exception {

		// Simulate the command execution with true parameter
		setCommandParamsHandler(true);

		String docUri = directory.toPath().resolve("src/main/java/org/test/SpelController.java").toUri().toString();
		TextDocumentInfo doc = harness.getOrReadFile(new File(new URI(docUri)), LanguageId.JAVA.getId());
		TextDocumentInfo openedDoc = harness.openDocument(doc);

		List<? extends CodeLens> codeLenses = harness.getCodeLenses(openedDoc);

		assertEquals(2, codeLenses.size());

		assertTrue(containsCodeLens(codeLenses.get(0), QueryCodeLensProvider.EXPLAIN_SPEL_TITLE, 13, 17, 13, 111));
		assertTrue(containsCodeLens(codeLenses.get(1), QueryCodeLensProvider.EXPLAIN_SPEL_TITLE, 16, 11, 16, 142));
	}
	
	@Test
	public void testShowCodeLensesFalseForQuery() throws Exception {
		
		setCommandParamsHandler(false);
		
		String docUri = directory.toPath().resolve("src/main/java/org/test/OwnerRepository.java").toUri().toString();
		TextDocumentInfo doc = harness.getOrReadFile(new File(new URI(docUri)), LanguageId.JAVA.getId());
		TextDocumentInfo openedDoc = harness.openDocument(doc);
		
		List<? extends CodeLens> codeLenses = harness.getCodeLenses(openedDoc);

		assertEquals(0, codeLenses.size());
	}
	
	@Test
	public void testShowCodeLensesFalseForSpel() throws Exception {
		
		setCommandParamsHandler(false);
		
		String docUri = directory.toPath().resolve("src/main/java/org/test/SpelController.java").toUri().toString();
		TextDocumentInfo doc = harness.getOrReadFile(new File(new URI(docUri)), LanguageId.JAVA.getId());
		TextDocumentInfo openedDoc = harness.openDocument(doc);
		
		List<? extends CodeLens> codeLenses = harness.getCodeLenses(openedDoc);

		assertEquals(0, codeLenses.size());
	}
	
	private void setCommandParamsHandler(boolean value) throws InterruptedException, ExecutionException {
		ExecuteCommandHandler handler = commandHandlerCaptor.getValue();
		ExecuteCommandParams params = new ExecuteCommandParams();
		params.setArguments(Collections.singletonList(new JsonPrimitive(value)));
		handler.handle(params).get();
	}

	private boolean containsCodeLens(CodeLens codeLenses, String commandTitle, int startLine, int startPosition,
			int endLine, int endPosition) {
		Command command = codeLenses.getCommand();
		Range range = codeLenses.getRange();
		if (command.getTitle().equals(commandTitle) && range.getStart().getLine() == startLine
				&& range.getStart().getCharacter() == startPosition && range.getEnd().getLine() == endLine
				&& range.getEnd().getCharacter() == endPosition) {
			return true;
		}
		return false;
	}
}