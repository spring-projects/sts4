/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexer;
import org.springframework.ide.vscode.project.harness.BootJavaLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;

/**
 * @author Martin Lippert
 */
public class RequestMappingSymbolProviderTest {

	private BootJavaLanguageServerHarness harness;
	private SpringIndexer indexer;
	private File directory;

	@Before
	public void setup() throws Exception {
		harness = BootJavaLanguageServerHarness.builder().build();
		
		harness.intialize(null);
		indexer = harness.getServerWrapper().getComponents().getSpringIndexer();
		
		directory = new File(ProjectsHarness.class.getResource("/test-projects/test-request-mapping-symbols/").toURI());
		String projectDir = directory.toURI().toString();
		
		// trigger project creation
		harness.getServerWrapper().getComponents().getProjectFinder().find(new TextDocumentIdentifier(projectDir)).get();

		CompletableFuture<Void> initProject = indexer.waitOperation();
		initProject.get(5, TimeUnit.SECONDS);
	}

	@Test
	public void testSimpleRequestMappingSymbol() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/SimpleMappingClass.java").toUri().toString();
		List<? extends SymbolInformation> symbols = indexer.getSymbols(docUri);
		assertEquals(1, symbols.size());
		assertTrue(containsSymbol(symbols, "@/greeting", docUri, 6, 1, 6, 29));
	}

	@Test
	public void testParentRequestMappingSymbol() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/ParentMappingClass.java").toUri().toString();
		List<? extends SymbolInformation> symbols =  indexer.getSymbols(docUri);
		assertEquals(1, symbols.size());
		assertTrue(containsSymbol(symbols, "@/parent/greeting -- GET", docUri, 8, 1, 8, 47));
	}

	@Test
	public void testEmptyPathWithParentRequestMappingSymbol() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/ParentMappingClass2.java").toUri().toString();
		List<? extends SymbolInformation> symbols =  indexer.getSymbols(docUri);
		assertEquals(1, symbols.size());
		assertTrue(containsSymbol(symbols, "@/parent2 -- GET,POST,DELETE", docUri, 8, 1, 8, 16));
	}

	@Test
	public void testMultiRequestMappingSymbol() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/MultiRequestMappingClass.java").toUri().toString();
		List<? extends SymbolInformation> symbols =  indexer.getSymbols(docUri);
		assertEquals(2, symbols.size());
		assertTrue(containsSymbol(symbols, "@/hello1", docUri, 6, 1, 6, 44));
		assertTrue(containsSymbol(symbols, "@/hello2", docUri, 6, 1, 6, 44));
	}

	@Test
	public void testGetMappingSymbol() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/RequestMethodClass.java").toUri().toString();
		List<? extends SymbolInformation> symbols =  indexer.getSymbols(docUri);
		assertTrue(containsSymbol(symbols, "@/getData -- GET", docUri, 12, 1, 12, 24));
	}

	@Test
	public void testDeleteMappingSymbol() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/RequestMethodClass.java").toUri().toString();
		List<? extends SymbolInformation> symbols =  indexer.getSymbols(docUri);
		assertTrue(containsSymbol(symbols, "@/deleteData -- DELETE",docUri, 20, 1, 20, 30));
	}

	@Test
	public void testPostMappingSymbol() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/RequestMethodClass.java").toUri().toString();
		List<? extends SymbolInformation> symbols =  indexer.getSymbols(docUri);
		assertTrue(containsSymbol(symbols, "@/postData -- POST", docUri, 24, 1, 24, 26));
	}

	@Test
	public void testPutMappingSymbol() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/RequestMethodClass.java").toUri().toString();
		List<? extends SymbolInformation> symbols =  indexer.getSymbols(docUri);
		assertTrue(containsSymbol(symbols, "@/putData -- PUT", docUri, 16, 1, 16, 24));
	}

	@Test
	public void testPatchMappingSymbol() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/RequestMethodClass.java").toUri().toString();
		List<? extends SymbolInformation> symbols = indexer.getSymbols(docUri);
		assertTrue(containsSymbol(symbols, "@/patchData -- PATCH", docUri, 28, 1, 28, 28));
	}

	@Test
	public void testGetRequestMappingSymbol() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/RequestMethodClass.java").toUri().toString();
		List<? extends SymbolInformation> symbols =  indexer.getSymbols(docUri);
		assertTrue(containsSymbol(symbols, "@/getHello -- GET", docUri, 32, 1, 32, 61));
	}

	@Test
	public void testMultiRequestMethodMappingSymbol() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/RequestMethodClass.java").toUri().toString();
		List<? extends SymbolInformation> symbols =  indexer.getSymbols(docUri);
		assertTrue(containsSymbol(symbols, "@/postAndPutHello -- POST,PUT", docUri, 36, 1, 36, 76));
	}
	
	@Test
	public void testMediaTypes() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/RequestMappingMediaTypes.java").toUri().toString();
		List<? extends SymbolInformation> symbols = indexer.getSymbols(docUri);
		assertEquals(7, symbols.size());
		assertTrue(containsSymbol(symbols, "@/consume1 -- HEAD - Accept: testconsume", docUri, 8, 1, 8, 90));
		assertTrue(containsSymbol(symbols, "@/consume2 - Accept: text/plain", docUri, 13, 1, 13, 73));
		assertTrue(containsSymbol(symbols, "@/consume3 - Accept: text/plain,testconsumetype", docUri, 18, 1, 18, 94));
		assertTrue(containsSymbol(symbols, "@/produce1 - Content-Type: testproduce", docUri, 23, 1, 23, 60));
		assertTrue(containsSymbol(symbols, "@/produce2 - Content-Type: text/plain", docUri, 28, 1, 28, 73));
		assertTrue(containsSymbol(symbols, "@/produce3 - Content-Type: text/plain,testproducetype", docUri, 33, 1, 33, 94));
		assertTrue(containsSymbol(symbols, "@/everything - Accept: application/json,text/plain,testconsume - Content-Type: application/json", docUri, 38, 1, 38, 170));
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
