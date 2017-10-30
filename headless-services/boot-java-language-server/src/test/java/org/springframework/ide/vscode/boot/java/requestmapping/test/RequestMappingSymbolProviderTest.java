/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
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
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;

/**
 * @author Martin Lippert
 */
public class RequestMappingSymbolProviderTest {

	private BootLanguageServerHarness harness;

	@Before
	public void setup() throws Exception {
		harness = BootLanguageServerHarness.builder().build();
	}

	@Test
	public void testSimpleRequestMappingSymbol() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-request-mapping-symbols/").toURI()));

		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-request-mapping-symbols/").toURI());

		String uriPrefix = "file://" + directory.getAbsolutePath();
		List<? extends SymbolInformation> symbols = harness.getServer().getSpringIndexer().getSymbols(uriPrefix + "/src/main/java/org/test/SimpleMappingClass.java");
		assertEquals(1, symbols.size());
		assertTrue(containsSymbol(symbols, "@/greeting -- (no method defined)", uriPrefix + "/src/main/java/org/test/SimpleMappingClass.java", 6, 1, 6, 29));
	}

	@Test
	public void testParentRequestMappingSymbol() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-request-mapping-symbols/").toURI()));

		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-request-mapping-symbols/").toURI());

		String uriPrefix = "file://" + directory.getAbsolutePath();
		List<? extends SymbolInformation> symbols =  harness.getServer().getSpringIndexer().getSymbols(uriPrefix + "/src/main/java/org/test/ParentMappingClass.java");
		assertEquals(2, symbols.size());
		assertTrue(containsSymbol(symbols, "@/parent -- GET,POST,DELETE", uriPrefix + "/src/main/java/org/test/ParentMappingClass.java", 5, 0, 5, 58));
		assertTrue(containsSymbol(symbols, "@/parent/greeting -- GET", uriPrefix + "/src/main/java/org/test/ParentMappingClass.java", 8, 1, 8, 47));
	}

	@Test
	public void testMultiRequestMappingSymbol() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-request-mapping-symbols/").toURI()));

		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-request-mapping-symbols/").toURI());

		String uriPrefix = "file://" + directory.getAbsolutePath();
		List<? extends SymbolInformation> symbols =  harness.getServer().getSpringIndexer().getSymbols(uriPrefix + "/src/main/java/org/test/MultiRequestMappingClass.java");
		assertEquals(2, symbols.size());
		assertTrue(containsSymbol(symbols, "@/hello1 -- (no method defined)", uriPrefix + "/src/main/java/org/test/MultiRequestMappingClass.java", 6, 1, 6, 44));
		assertTrue(containsSymbol(symbols, "@/hello2 -- (no method defined)", uriPrefix + "/src/main/java/org/test/MultiRequestMappingClass.java", 6, 1, 6, 44));
	}

	@Test
	public void testGetMappingSymbol() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-request-mapping-symbols/").toURI()));

		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-request-mapping-symbols/").toURI());

		String uriPrefix = "file://" + directory.getAbsolutePath();
		List<? extends SymbolInformation> symbols =  harness.getServer().getSpringIndexer().getSymbols(uriPrefix + "/src/main/java/org/test/RequestMethodClass.java");
		assertTrue(containsSymbol(symbols, "@/getData -- GET", uriPrefix + "/src/main/java/org/test/RequestMethodClass.java", 12, 1, 12, 24));
	}

	@Test
	public void testDeleteMappingSymbol() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-request-mapping-symbols/").toURI()));

		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-request-mapping-symbols/").toURI());

		String uriPrefix = "file://" + directory.getAbsolutePath();
		List<? extends SymbolInformation> symbols =  harness.getServer().getSpringIndexer().getSymbols(uriPrefix + "/src/main/java/org/test/RequestMethodClass.java");
		assertTrue(containsSymbol(symbols, "@/deleteData -- DELETE", uriPrefix + "/src/main/java/org/test/RequestMethodClass.java", 20, 1, 20, 30));
	}

	@Test
	public void testPostMappingSymbol() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-request-mapping-symbols/").toURI()));

		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-request-mapping-symbols/").toURI());

		String uriPrefix = "file://" + directory.getAbsolutePath();
		List<? extends SymbolInformation> symbols =  harness.getServer().getSpringIndexer().getSymbols(uriPrefix + "/src/main/java/org/test/RequestMethodClass.java");
		assertTrue(containsSymbol(symbols, "@/postData -- POST", uriPrefix + "/src/main/java/org/test/RequestMethodClass.java", 24, 1, 24, 26));
	}

	@Test
	public void testPutMappingSymbol() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-request-mapping-symbols/").toURI()));

		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-request-mapping-symbols/").toURI());

		String uriPrefix = "file://" + directory.getAbsolutePath();
		List<? extends SymbolInformation> symbols =  harness.getServer().getSpringIndexer().getSymbols(uriPrefix + "/src/main/java/org/test/RequestMethodClass.java");
		assertTrue(containsSymbol(symbols, "@/putData -- PUT", uriPrefix + "/src/main/java/org/test/RequestMethodClass.java", 16, 1, 16, 24));
	}

	@Test
	public void testPatchMappingSymbol() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-request-mapping-symbols/").toURI()));

		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-request-mapping-symbols/").toURI());

		String uriPrefix = "file://" + directory.getAbsolutePath();
		List<? extends SymbolInformation> symbols =  harness.getServer().getSpringIndexer().getSymbols(uriPrefix + "/src/main/java/org/test/RequestMethodClass.java");
		assertTrue(containsSymbol(symbols, "@/patchData -- PATCH", uriPrefix + "/src/main/java/org/test/RequestMethodClass.java", 28, 1, 28, 28));
	}

	@Test
	public void testGetRequestMappingSymbol() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-request-mapping-symbols/").toURI()));

		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-request-mapping-symbols/").toURI());

		String uriPrefix = "file://" + directory.getAbsolutePath();
		List<? extends SymbolInformation> symbols =  harness.getServer().getSpringIndexer().getSymbols(uriPrefix + "/src/main/java/org/test/RequestMethodClass.java");
		assertTrue(containsSymbol(symbols, "@/getHello -- GET", uriPrefix + "/src/main/java/org/test/RequestMethodClass.java", 32, 1, 32, 61));
	}

	@Test
	public void testMultiRequestMethodMappingSymbol() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-request-mapping-symbols/").toURI()));

		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-request-mapping-symbols/").toURI());

		String uriPrefix = "file://" + directory.getAbsolutePath();
		List<? extends SymbolInformation> symbols =  harness.getServer().getSpringIndexer().getSymbols(uriPrefix + "/src/main/java/org/test/RequestMethodClass.java");
		assertTrue(containsSymbol(symbols, "@/postAndPutHello -- POST,PUT", uriPrefix + "/src/main/java/org/test/RequestMethodClass.java", 36, 1, 36, 76));
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

}
