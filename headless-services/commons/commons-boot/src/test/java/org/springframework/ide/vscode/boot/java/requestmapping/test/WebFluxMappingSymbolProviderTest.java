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
import java.util.Iterator;
import java.util.List;

import org.eclipse.lsp4j.SymbolInformation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.vscode.project.harness.BootJavaLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;

/**
 * @author Martin Lippert
 */
public class WebFluxMappingSymbolProviderTest {

	private BootJavaLanguageServerHarness harness;

	@Before
	public void setup() throws Exception {
		harness = BootJavaLanguageServerHarness.builder().build();
	}

	@Test
	public void testSimpleRequestMappingSymbol() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-webflux-project/").toURI()));
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-webflux-project/").toURI());

		String docUri = directory.toPath().resolve("src/main/java/org/test/UserController.java").toUri().toString();
		List<? extends SymbolInformation> symbols = getSymbols(docUri);
		assertEquals(4, symbols.size());
		assertTrue(containsSymbol(symbols, "@/users", docUri, 19, 1, 19, 74));
		assertTrue(containsSymbol(symbols, "@/users/{username}", docUri, 24, 1, 24, 85));
	}

	@Test
	public void testRoutesMappingSymbols() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-webflux-project/").toURI()));
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-webflux-project/").toURI());

		String docUri = directory.toPath().resolve("src/main/java/org/test/QuoteRouter.java").toUri().toString();
		List<? extends SymbolInformation> symbols = getSymbols(docUri);
		assertEquals(6, symbols.size());
		assertTrue(containsSymbol(symbols, "@/hello", docUri, 22, 23, 22, 81));
		assertTrue(containsSymbol(symbols, "@/echo", docUri, 23, 1, 23, 85));
	}

	private boolean containsSymbol(List<? extends SymbolInformation> symbols, String name, String uri, int startLine, int startCHaracter, int endLine, int endCharacter) {
		for (Iterator<? extends SymbolInformation> iterator = symbols.iterator(); iterator.hasNext();) {
			SymbolInformation symbol = iterator.next();

			if (symbol.getName().equals(name)
					&& symbol.getLocation().getUri().equals(uri)
					&& symbol.getLocation().getRange().getStart().getLine() == startLine
					&& symbol.getLocation().getRange().getStart().getCharacter() == startCHaracter
					&& symbol.getLocation().getRange().getEnd().getLine() == endLine
					&& symbol.getLocation().getRange().getEnd().getCharacter() == endCharacter) {
				return true;
			}
 		}

		return false;
	}

	private List<? extends SymbolInformation> getSymbols(String docUri) {
		return harness.getServerWrapper().getComponents().getSpringIndexer().getSymbols(docUri);
	}
}
