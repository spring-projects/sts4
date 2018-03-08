/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.requestmapping.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.util.List;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Range;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.languageserver.testharness.TextDocumentInfo;
import org.springframework.ide.vscode.project.harness.BootJavaLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;

/**
 * @author Martin Lippert
 */
@SuppressWarnings("deprecation")
public class WebFluxCodeLensProviderTest {

	private BootJavaLanguageServerHarness harness;

	@Before
	public void setup() throws Exception {
		harness = BootJavaLanguageServerHarness.builder().build();
	}

	@Test
	public void testRoutesCodeLenses() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-webflux-project/").toURI()));
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-webflux-project/").toURI());

		String docUri = directory.toPath().resolve("src/main/java/org/test/QuoteHandler.java").toUri().toString();
		TextDocumentInfo doc = harness.getOrReadFile(new File(new URI(docUri)), LanguageId.JAVA.toString());
		TextDocumentInfo openedDoc = harness.openDocument(doc);
		
		List<? extends CodeLens> codeLenses = harness.getCodeLenses(openedDoc);

		assertEquals(4, codeLenses.size());
		
		assertTrue(containsCodeLens(codeLenses, "GET /hello", 25, 29, 25, 34));
		assertTrue(containsCodeLens(codeLenses, "POST /echo", 30, 29, 30, 33));
		assertTrue(containsCodeLens(codeLenses, "GET /quotes", 35, 29, 35, 41));
		assertTrue(containsCodeLens(codeLenses, "GET /quotes", 41, 29, 41, 40));
	}

	private boolean containsCodeLens(List<? extends CodeLens> codeLenses, String commandTitle, int startLine, int startPosition, int endLine, int endPosition) {
		for (CodeLens codeLens : codeLenses) {
			Command command = codeLens.getCommand();
			Range range = codeLens.getRange();
			if (command.getTitle().equals(commandTitle)
					&& range.getStart().getLine() == startLine
					&& range.getStart().getCharacter() == startPosition
					&& range.getEnd().getLine() == endLine
					&& range.getEnd().getCharacter() == endPosition) {
				return true;
			}
		}
		
		return false;
	}

}
