/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.boot.bootiful.SymbolProviderTestConf;
import org.springframework.ide.vscode.boot.java.utils.SymbolCache;
import org.springframework.ide.vscode.boot.java.utils.SymbolIndexConfig;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Martin Lippert
 */
@RunWith(SpringRunner.class)
@BootLanguageServerTest
@Import(SpringIndexerMultipleFilesTest.TimestampingAwareCacheConfig.class)
public class SpringIndexerMultipleFilesTest {

	// usually, the test config ignores any caching by using the void impl,
	// but here we need the one that implements at least the timestamp caching
	// in order to check the java symbol indexer feature which avoid scanning the
	// same file again even if the timestamp hasn't changed
	public static class TimestampingAwareCacheConfig extends SymbolProviderTestConf {
		@Bean public SymbolCache symbolCache() {
			return new SymbolCacheTimestampsOnly();
		}
	}

	@Autowired private BootLanguageServerHarness harness;
	@Autowired private SpringSymbolIndex indexer;
	@Autowired private JavaProjectFinder projectFinder;

	private File directory;
	private String projectDir;

	@Before
	public void setup() throws Exception {
		harness.intialize(null);
		indexer.configureIndexer(SymbolIndexConfig.builder().scanXml(false).build());

		directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI());
		projectDir = directory.toURI().toString();

		// trigger project creation
		projectFinder.find(new TextDocumentIdentifier(projectDir)).get();

		CompletableFuture<Void> initProject = indexer.waitOperation();
		initProject.get(5, TimeUnit.SECONDS);
	}

	@Test
	public void testUpdateChangedSingleDocumentOnDisc() throws Exception {
		String changedDocURI = directory.toPath().resolve("src/main/java/org/test/SimpleMappingClass.java").toUri().toString();
		File file = new File(new URI(changedDocURI));
		String originalContent = FileUtils.readFileToString(file);
		FileTime modifiedTime = Files.getLastModifiedTime(file.toPath());

		try {
			// update document and update index
			List<? extends SymbolInformation> symbols = indexer.getSymbols(changedDocURI);
			assertTrue(containsSymbol(symbols, "@/mapping1", changedDocURI));
			
			String newContent = originalContent.replace("mapping1", "mapping1-CHANGED");
			FileUtils.writeStringToFile(new File(new URI(changedDocURI)), newContent);
			Files.setLastModifiedTime(file.toPath(), FileTime.fromMillis(modifiedTime.toMillis() + 1000));
			
			TestFileScanListener fileScanListener = new TestFileScanListener();
			indexer.getJavaIndexer().setFileScanListener(fileScanListener);

			CompletableFuture<Void> updateFuture = indexer.updateDocument(changedDocURI, null, "test triggered");
			updateFuture.get(5, TimeUnit.SECONDS);
	
			// check for updated index per document
			symbols = indexer.getSymbols(changedDocURI);
			assertEquals(2, symbols.size());
			assertTrue(containsSymbol(symbols, "@/mapping1-CHANGED", changedDocURI, 6, 1, 6, 36));
			assertTrue(containsSymbol(symbols, "@/mapping2", changedDocURI, 11, 1, 11, 28));
			
			fileScanListener.assertScannedUris(changedDocURI);
			fileScanListener.assertScannedUri(changedDocURI, 1);
		}
		finally {
			FileUtils.writeStringToFile(new File(new URI(changedDocURI)), originalContent);
		}
	}

	@Test
	public void testUpdateChangedMultipleDocumentsOnDisc() throws Exception {
		
		String doc1URI = directory.toPath().resolve("src/main/java/org/test/SimpleMappingClass.java").toUri().toString();
		File file1 = new File(new URI(doc1URI));
		String original1Content = FileUtils.readFileToString(file1);
		FileTime modifiedTime1 = Files.getLastModifiedTime(file1.toPath());

		String doc2URI = directory.toPath().resolve("src/main/java/org/test/MainClass.java").toUri().toString();
		File file2 = new File(new URI(doc2URI));
		String original2Content = FileUtils.readFileToString(file2);
		FileTime modifiedTime2 = Files.getLastModifiedTime(file2.toPath());

		String doc3URI = directory.toPath().resolve("src/main/java/org/test/sub/MappingClassSubpackage.java").toUri().toString();
		File file3 = new File(new URI(doc3URI));
		String original3Content = FileUtils.readFileToString(file3);
		FileTime modifiedTime3 = Files.getLastModifiedTime(file3.toPath());

		try {
			String new1Content = original1Content.replace("mapping1", "mapping1-CHANGED");
			FileUtils.writeStringToFile(new File(new URI(doc1URI)), new1Content);
			Files.setLastModifiedTime(file1.toPath(), FileTime.fromMillis(modifiedTime1.toMillis() + 1000));
			
			String new2Content = original2Content.replace("\"/embedded-foo-mapping\"", "\"/embedded-foo-mapping-CHANGED\"");
			FileUtils.writeStringToFile(new File(new URI(doc2URI)), new2Content);
			Files.setLastModifiedTime(file2.toPath(), FileTime.fromMillis(modifiedTime2.toMillis() + 1000));
			
			String new3Content = original3Content.replace("classlevel", "classlevel-CHANGED");
			FileUtils.writeStringToFile(new File(new URI(doc3URI)), new3Content);
			Files.setLastModifiedTime(file3.toPath(), FileTime.fromMillis(modifiedTime3.toMillis() + 1000));
			
			CompletableFuture<Void> updateFuture = indexer.updateDocuments(new String[] {doc1URI, doc2URI, doc3URI}, "test triggered");
			updateFuture.get(5, TimeUnit.SECONDS);
	
			// check for updated index per document
			List<? extends SymbolInformation> symbols1 = indexer.getSymbols(doc1URI);
			assertEquals(2, symbols1.size());
			assertTrue(containsSymbol(symbols1, "@/mapping1-CHANGED", doc1URI, 6, 1, 6, 36));
			assertTrue(containsSymbol(symbols1, "@/mapping2", doc1URI, 11, 1, 11, 28));
			
			List<? extends SymbolInformation> symbols2 = indexer.getSymbols(doc2URI);
			assertTrue(containsSymbol(symbols2, "@+ 'mainClass' (@SpringBootApplication <: @SpringBootConfiguration, @Configuration, @Component) MainClass", doc2URI, 6, 0, 6, 22));
			assertTrue(containsSymbol(symbols2, "@/embedded-foo-mapping-CHANGED", doc2URI, 17, 1, 17, 49));
			assertTrue(containsSymbol(symbols2, "@/foo-root-mapping/embedded-foo-mapping-with-root", doc2URI, 27, 1, 27, 51));

			List<? extends SymbolInformation> symbols3 = indexer.getSymbols(doc3URI);
			assertTrue(containsSymbol(symbols3, "@/classlevel-CHANGED/mapping-subpackage", doc3URI, 7, 1, 7, 38));
		}
		finally {
			FileUtils.writeStringToFile(new File(new URI(doc1URI)), original1Content);
			FileUtils.writeStringToFile(new File(new URI(doc2URI)), original2Content);
			FileUtils.writeStringToFile(new File(new URI(doc3URI)), original3Content);
		}
	}
	
	@Test
	public void testDontScanUnchangedDocument() throws Exception {
		String unchangedDocURI = directory.toPath().resolve("src/main/java/org/test/SimpleMappingClass.java").toUri().toString();
		
		TestFileScanListener fileScanListener = new TestFileScanListener();
		indexer.getJavaIndexer().setFileScanListener(fileScanListener);

		CompletableFuture<Void> updateFuture = indexer.updateDocuments(new String[] {unchangedDocURI}, "test triggered");
		updateFuture.get(5, TimeUnit.SECONDS);

		fileScanListener.assertScannedUris();
		fileScanListener.assertScannedUri(unchangedDocURI, 0);
		
		List<? extends SymbolInformation> symbols = indexer.getSymbols(unchangedDocURI);
		assertEquals(2, symbols.size());
	}

	@Test
	public void testDontScanUnchangedDocumentAmongMultipleChangedFiles() throws Exception {
		
		String doc1URI = directory.toPath().resolve("src/main/java/org/test/SimpleMappingClass.java").toUri().toString();
		File file1 = new File(new URI(doc1URI));
		String original1Content = FileUtils.readFileToString(file1);
		FileTime modifiedTime1 = Files.getLastModifiedTime(file1.toPath());

		String doc2URI = directory.toPath().resolve("src/main/java/org/test/MainClass.java").toUri().toString();

		String doc3URI = directory.toPath().resolve("src/main/java/org/test/sub/MappingClassSubpackage.java").toUri().toString();
		File file3 = new File(new URI(doc3URI));
		String original3Content = FileUtils.readFileToString(file3);
		FileTime modifiedTime3 = Files.getLastModifiedTime(file3.toPath());

		try {
			String new1Content = original1Content.replace("mapping1", "mapping1-CHANGED");
			FileUtils.writeStringToFile(file1, new1Content);
			Files.setLastModifiedTime(file1.toPath(), FileTime.fromMillis(modifiedTime1.toMillis() + 1000));
			
			String new3Content = original3Content.replace("classlevel", "classlevel-CHANGED");
			FileUtils.writeStringToFile(new File(new URI(doc3URI)), new3Content);
			Files.setLastModifiedTime(file3.toPath(), FileTime.fromMillis(modifiedTime3.toMillis() + 1000));
			
			TestFileScanListener fileScanListener = new TestFileScanListener();
			indexer.getJavaIndexer().setFileScanListener(fileScanListener);

			CompletableFuture<Void> updateFuture = indexer.updateDocuments(new String[] {doc1URI, doc2URI, doc3URI}, "test triggered");
			updateFuture.get(5, TimeUnit.SECONDS);
	
			// check for updated index per document
			List<? extends SymbolInformation> symbols1 = indexer.getSymbols(doc1URI);
			assertEquals(2, symbols1.size());
			assertTrue(containsSymbol(symbols1, "@/mapping1-CHANGED", doc1URI, 6, 1, 6, 36));
			assertTrue(containsSymbol(symbols1, "@/mapping2", doc1URI, 11, 1, 11, 28));
			
			List<? extends SymbolInformation> symbols2 = indexer.getSymbols(doc2URI);
			assertEquals(3, symbols2.size());

			List<? extends SymbolInformation> symbols3 = indexer.getSymbols(doc3URI);
			assertTrue(containsSymbol(symbols3, "@/classlevel-CHANGED/mapping-subpackage", doc3URI, 7, 1, 7, 38));
			
			fileScanListener.assertScannedUris(doc1URI, doc3URI);
			fileScanListener.assertScannedUri(doc1URI, 1);
			fileScanListener.assertScannedUri(doc2URI, 0);
			fileScanListener.assertScannedUri(doc3URI, 1);
		}
		finally {
			FileUtils.writeStringToFile(file1, original1Content);
			FileUtils.writeStringToFile(new File(new URI(doc3URI)), original3Content);
		}
	}

	private boolean containsSymbol(List<? extends SymbolInformation> symbols, String name, String uri) {
		for (Iterator<? extends SymbolInformation> iterator = symbols.iterator(); iterator.hasNext();) {
			SymbolInformation symbol = iterator.next();

			if (
					symbol.getName().equals(name) &&
					symbol.getLocation().getUri().equals(uri)
			) {
				return true;
			}
 		}

		return false;
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
