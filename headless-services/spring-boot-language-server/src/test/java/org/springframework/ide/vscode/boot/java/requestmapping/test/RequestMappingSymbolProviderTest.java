/*******************************************************************************
 * Copyright (c) 2017, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.boot.bootiful.SymbolProviderTestConf;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexerJavaDependencyTracker;
import org.springframework.ide.vscode.boot.java.utils.test.TestFileScanListener;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.util.UriUtil;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.ImmutableSet;

/**
 * @author Martin Lippert
 */
@RunWith(SpringRunner.class)
@BootLanguageServerTest
@Import(SymbolProviderTestConf.class)
public class RequestMappingSymbolProviderTest {
	
	@Autowired private BootLanguageServerHarness harness;
	@Autowired private JavaProjectFinder projectFinder;
	@Autowired private SpringSymbolIndex indexer;

	private File directory;

	@Before
	public void setup() throws Exception {
		harness.intialize(null);

		directory = new File(ProjectsHarness.class.getResource("/test-projects/test-request-mapping-symbols/").toURI());
		String projectDir = directory.toURI().toString();

		// trigger project creation
		projectFinder.find(new TextDocumentIdentifier(projectDir)).get();

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
	public void testSimpleRequestMappingSymbolFromConstantInDifferentClass() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/SimpleMappingClassWithConstantInDifferentClass.java").toUri().toString();
		String constantsUri = directory.toPath().resolve("src/main/java/org/test/Constants.java").toUri().toString();
		List<? extends SymbolInformation> symbols = indexer.getSymbols(docUri);
		assertEquals(1, symbols.size());
		assertTrue(containsSymbol(symbols, "@/path/from/constant", docUri, 6, 1, 6, 48));

		//Verify whether dependency tracker logics works properly for this example.
		SpringIndexerJavaDependencyTracker dt = indexer.getJavaIndexer().getDependencyTracker();
		assertEquals(ImmutableSet.of("Lorg/test/Constants;"), dt.getAllDependencies().get(UriUtil.toFileString(docUri)));
		
		TestFileScanListener fileScanListener = new TestFileScanListener();
		indexer.getJavaIndexer().setFileScanListener(fileScanListener);

		CompletableFuture<Void> updateFuture = indexer.updateDocument(constantsUri, FileUtils.readFileToString(UriUtil.toFile(constantsUri)), "test triggered");
		updateFuture.get(5, TimeUnit.SECONDS);
		
		fileScanListener.assertScannedUris(constantsUri, docUri);
		fileScanListener.assertScannedUri(constantsUri, 1);
		fileScanListener.assertScannedUri(docUri, 1);
	}
	
	@Test
	public void testUpdateDocumentWithConstantFromDifferentClass() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/SimpleMappingClassWithConstantInDifferentClass.java").toUri().toString();
		String constantsUri = directory.toPath().resolve("src/main/java/org/test/Constants.java").toUri().toString();
		List<? extends SymbolInformation> symbols = indexer.getSymbols(docUri);
		assertEquals(1, symbols.size());
		assertTrue(containsSymbol(symbols, "@/path/from/constant", docUri, 6, 1, 6, 48));

		//Verify whether dependency tracker logics works properly for this example.
		SpringIndexerJavaDependencyTracker dt = indexer.getJavaIndexer().getDependencyTracker();
		assertEquals(ImmutableSet.of("Lorg/test/Constants;"), dt.getAllDependencies().get(UriUtil.toFileString(docUri)));
		
		TestFileScanListener fileScanListener = new TestFileScanListener();
		indexer.getJavaIndexer().setFileScanListener(fileScanListener);

		CompletableFuture<Void> updateFuture = indexer.updateDocument(docUri, FileUtils.readFileToString(UriUtil.toFile(docUri)), "test triggered");
		updateFuture.get(5, TimeUnit.SECONDS);
		
		assertEquals(ImmutableSet.of("Lorg/test/Constants;"), dt.getAllDependencies().get(UriUtil.toFileString(docUri)));

		fileScanListener.assertScannedUris(docUri);
		fileScanListener.assertScannedUri(constantsUri, 0);
		fileScanListener.assertScannedUri(docUri, 1);
	}
	
	@Test
	public void testCyclicalRequestMappingDependency() throws Exception {
		//Cyclical dependency:
		//file a => file b => file a
		//This has the potential to cause infinite loop.
		
		String pingUri = directory.toPath().resolve("src/main/java/org/test/PingConstantRequestMapping.java").toUri().toString();
		String pongUri = directory.toPath().resolve("src/main/java/org/test/PongConstantRequestMapping.java").toUri().toString();

		assertSymbol(pingUri, "@/pong -- GET", "@GetMapping(PongConstantRequestMapping.PONG)");
		assertSymbol(pongUri, "@/ping -- GET", "@GetMapping(PingConstantRequestMapping.PING)");
		
		TestFileScanListener fileScanListener = new TestFileScanListener();
		indexer.getJavaIndexer().setFileScanListener(fileScanListener);
		
		CompletableFuture<Void> updateFuture = indexer.updateDocument(pingUri, null, "test triggered");
		updateFuture.get(5, TimeUnit.SECONDS);

		fileScanListener.assertScannedUris(pingUri, pongUri);
		
		fileScanListener.reset();
		fileScanListener.assertScannedUris(/*none*/);

		CompletableFuture<Void> updateFuture2 = indexer.updateDocument(pongUri, null, "test triggered");
		updateFuture2.get(5, TimeUnit.SECONDS);

		fileScanListener.assertScannedUris(pingUri, pongUri);
	}

	@Test
	public void testSimpleRequestMappingSymbolFromConstantInSameClass() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/SimpleMappingClassWithConstantInSameClass.java").toUri().toString();
		List<? extends SymbolInformation> symbols = indexer.getSymbols(docUri);
		assertEquals(1, symbols.size());
		assertTrue(containsSymbol(symbols, "@/request/mapping/path/from/same/class/constant", docUri, 8, 1, 8, 52));
		
		SpringIndexerJavaDependencyTracker dt = indexer.getJavaIndexer().getDependencyTracker();
		assertEquals(ImmutableSet.of(), dt.getAllDependencies().get(UriUtil.toFileString(docUri)));
	}

	@Test
	public void testSimpleRequestMappingSymbolFromConstantInBinaryType() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/SimpleMappingClassWithConstantFromBinaryType.java").toUri().toString();
		List<? extends SymbolInformation> symbols = indexer.getSymbols(docUri);
		assertEquals(1, symbols.size());
		assertTrue(containsSymbol(symbols, "@/(inferred)", docUri, 7, 1, 7, 53));
		
		SpringIndexerJavaDependencyTracker dt = indexer.getJavaIndexer().getDependencyTracker();
		assertEquals(ImmutableSet.of(), dt.getAllDependencies().get(UriUtil.toFileString(docUri)));
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
	
	private void assertSymbol(String docUri, String name, String coveredText) throws Exception {
		List<? extends SymbolInformation> symbols = indexer.getSymbols(docUri);
		Optional<? extends SymbolInformation> maybeSymbol = symbols.stream().filter(s -> name.equals(s.getName())).findFirst();
		assertTrue(maybeSymbol.isPresent());
		
		TextDocument doc = new TextDocument(docUri, LanguageId.JAVA);
		doc.setText(FileUtils.readFileToString(UriUtil.toFile(docUri)));
		
		SymbolInformation symbol = maybeSymbol.get();
		Location loc = symbol.getLocation();
		assertEquals(docUri, loc.getUri());
		int start = doc.toOffset(loc.getRange().getStart());
		int end = doc.toOffset(loc.getRange().getEnd());
		String actualCoveredText = doc.textBetween(start, end);
		assertEquals(coveredText, actualCoveredText);
	}
}
