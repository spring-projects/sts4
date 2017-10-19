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
package org.springframework.ide.vscode.boot.java.utils.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.eclipse.lsp4j.SymbolInformation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServer;
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
import org.springframework.ide.vscode.boot.java.requestmapping.Constants;
import org.springframework.ide.vscode.boot.java.requestmapping.RequestMappingSymbolProvider;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexer;
import org.springframework.ide.vscode.commons.maven.MavenCore;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;

/**
 * @author Martin Lippert
 */
public class SpringIndexerTest {

	private Map<String, SymbolProvider> symbolProviders;
	private LanguageServerHarness<BootJavaLanguageServer> harness;

	private SpringIndexer indexer() {
		return harness.getServer().getSpringIndexer();
	}


	@Before
	public void setup() throws Exception {
		symbolProviders = new HashMap<>();
		symbolProviders.put(Constants.SPRING_REQUEST_MAPPING, new RequestMappingSymbolProvider());
		harness = BootLanguageServerHarness.builder().build();
	}

	@Test
	public void testScanningAllAnnotationsSimpleProjectUpfront() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI()));

		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI());

		List<? extends SymbolInformation> allSymbols = indexer().getAllSymbols("");

		assertEquals(8, allSymbols.size());

		String uriPrefix = "file://" + directory.getAbsolutePath();

		assertTrue(containsSymbol(allSymbols, "@SpringBootApplication", uriPrefix + "/src/main/java/org/test/MainClass.java", 6, 0, 6, 22));
		assertTrue(containsSymbol(allSymbols, "@/embedded-foo-mapping -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 17, 1, 17, 41));
		assertTrue(containsSymbol(allSymbols, "@/foo-root-mapping -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 24, 0, 24, 36));
		assertTrue(containsSymbol(allSymbols, "@/foo-root-mapping/embedded-foo-mapping-with-root -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 27, 1, 27, 51));
		assertTrue(containsSymbol(allSymbols, "@/mapping1 -- (no method defined)", uriPrefix + "/src/main/java/org/test/SimpleMappingClass.java", 6, 1, 6, 28));
		assertTrue(containsSymbol(allSymbols, "@/mapping2 -- (no method defined)", uriPrefix + "/src/main/java/org/test/SimpleMappingClass.java", 11, 1, 11, 28));
		assertTrue(containsSymbol(allSymbols, "@/classlevel -- (no method defined)", uriPrefix + "/src/main/java/org/test/sub/MappingClassSubpackage.java", 4, 0, 4, 30));
		assertTrue(containsSymbol(allSymbols, "@/classlevel/mapping-subpackage -- (no method defined)", uriPrefix + "/src/main/java/org/test/sub/MappingClassSubpackage.java", 7, 1, 7, 38));
	}

	@Test
	public void testRetrievingSymbolsPerDocument() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI()));

		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI());

		String uriPrefix = "file://" + directory.getAbsolutePath();
		List<? extends SymbolInformation> symbols = indexer().getSymbols(uriPrefix + "/src/main/java/org/test/MainClass.java");
		assertEquals(4, symbols.size());
		assertTrue(containsSymbol(symbols, "@SpringBootApplication", uriPrefix + "/src/main/java/org/test/MainClass.java", 6, 0, 6, 22));
		assertTrue(containsSymbol(symbols, "@/embedded-foo-mapping -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 17, 1, 17, 41));
		assertTrue(containsSymbol(symbols, "@/foo-root-mapping -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 24, 0, 24, 36));
		assertTrue(containsSymbol(symbols, "@/foo-root-mapping/embedded-foo-mapping-with-root -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 27, 1, 27, 51));

		symbols = indexer().getSymbols(uriPrefix + "/src/main/java/org/test/SimpleMappingClass.java");
		assertEquals(2, symbols.size());
		assertTrue(containsSymbol(symbols, "@/mapping1 -- (no method defined)", uriPrefix + "/src/main/java/org/test/SimpleMappingClass.java", 6, 1, 6, 28));
		assertTrue(containsSymbol(symbols, "@/mapping2 -- (no method defined)", uriPrefix + "/src/main/java/org/test/SimpleMappingClass.java", 11, 1, 11, 28));

		symbols = indexer().getSymbols(uriPrefix + "/src/main/java/org/test/sub/MappingClassSubpackage.java");
		assertEquals(2, symbols.size());
		assertTrue(containsSymbol(symbols, "@/classlevel -- (no method defined)", uriPrefix + "/src/main/java/org/test/sub/MappingClassSubpackage.java", 4, 0, 4, 30));
		assertTrue(containsSymbol(symbols, "@/classlevel/mapping-subpackage -- (no method defined)", uriPrefix + "/src/main/java/org/test/sub/MappingClassSubpackage.java", 7, 1, 7, 38));
	}

	@Test
	public void testScanningAllAnnotationsMultiModuleProjectUpfront() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/").toURI()));

		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/").toURI());

		List<? extends SymbolInformation> allSymbols = indexer().getAllSymbols("");

		assertEquals(8, allSymbols.size());

		String uriPrefix = "file://" + directory.getAbsolutePath() + "/test-annotation-indexing";

		assertTrue(containsSymbol(allSymbols, "@SpringBootApplication", uriPrefix + "/src/main/java/org/test/MainClass.java", 6, 0, 6, 22));
		assertTrue(containsSymbol(allSymbols, "@/embedded-foo-mapping -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 17, 1, 17, 41));
		assertTrue(containsSymbol(allSymbols, "@/foo-root-mapping -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 24, 0, 24, 36));
		assertTrue(containsSymbol(allSymbols, "@/foo-root-mapping/embedded-foo-mapping-with-root -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 27, 1, 27, 51));
		assertTrue(containsSymbol(allSymbols, "@/mapping1 -- (no method defined)", uriPrefix + "/src/main/java/org/test/SimpleMappingClass.java", 6, 1, 6, 28));
		assertTrue(containsSymbol(allSymbols, "@/mapping2 -- (no method defined)", uriPrefix + "/src/main/java/org/test/SimpleMappingClass.java", 11, 1, 11, 28));
		assertTrue(containsSymbol(allSymbols, "@/classlevel -- (no method defined)", uriPrefix + "/src/main/java/org/test/sub/MappingClassSubpackage.java", 4, 0, 4, 30));
		assertTrue(containsSymbol(allSymbols, "@/classlevel/mapping-subpackage -- (no method defined)", uriPrefix + "/src/main/java/org/test/sub/MappingClassSubpackage.java", 7, 1, 7, 38));
	}

	@Test
	public void testUpdateChangedDocument() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI()));

		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI());

		// update document and update index
		String changedDocURI = "file://" + directory.getAbsolutePath() + "/src/main/java/org/test/SimpleMappingClass.java";
		String newContent = FileUtils.readFileToString(new File(new URI(changedDocURI))).replace("mapping1", "mapping1-CHANGED");
		CompletableFuture<Void> updateFuture = indexer().updateDocument(changedDocURI, newContent);

		updateFuture.get(5, TimeUnit.SECONDS);

		// check for updated index per document
		List<? extends SymbolInformation> symbols = indexer().getSymbols(changedDocURI);
		assertEquals(2, symbols.size());
		assertTrue(containsSymbol(symbols, "@/mapping1-CHANGED -- (no method defined)", changedDocURI, 6, 1, 6, 36));
		assertTrue(containsSymbol(symbols, "@/mapping2 -- (no method defined)", changedDocURI, 11, 1, 11, 28));

		// check for updated index in all symbols
		List<? extends SymbolInformation> allSymbols = indexer().getAllSymbols("");
		assertEquals(8, allSymbols.size());

		String uriPrefix = "file://" + directory.getAbsolutePath();

		assertTrue(containsSymbol(allSymbols, "@SpringBootApplication", uriPrefix + "/src/main/java/org/test/MainClass.java", 6, 0, 6, 22));
		assertTrue(containsSymbol(allSymbols, "@/embedded-foo-mapping -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 17, 1, 17, 41));
		assertTrue(containsSymbol(allSymbols, "@/foo-root-mapping -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 24, 0, 24, 36));
		assertTrue(containsSymbol(allSymbols, "@/foo-root-mapping/embedded-foo-mapping-with-root -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 27, 1, 27, 51));
		assertTrue(containsSymbol(allSymbols, "@/mapping1-CHANGED -- (no method defined)", uriPrefix + "/src/main/java/org/test/SimpleMappingClass.java", 6, 1, 6, 36));
		assertTrue(containsSymbol(allSymbols, "@/mapping2 -- (no method defined)", uriPrefix + "/src/main/java/org/test/SimpleMappingClass.java", 11, 1, 11, 28));
		assertTrue(containsSymbol(allSymbols, "@/classlevel -- (no method defined)", uriPrefix + "/src/main/java/org/test/sub/MappingClassSubpackage.java", 4, 0, 4, 30));
		assertTrue(containsSymbol(allSymbols, "@/classlevel/mapping-subpackage -- (no method defined)", uriPrefix + "/src/main/java/org/test/sub/MappingClassSubpackage.java", 7, 1, 7, 38));
	}

	@Test
	public void testFilterSymbolsUsingQueryString() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI()));

		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI());

		List<? extends SymbolInformation> allSymbols = indexer().getAllSymbols("mapp");

		assertEquals(6, allSymbols.size());

		String uriPrefix = "file://" + directory.getAbsolutePath();

		assertTrue(containsSymbol(allSymbols, "@/embedded-foo-mapping -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 17, 1, 17, 41));
		assertTrue(containsSymbol(allSymbols, "@/foo-root-mapping -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 24, 0, 24, 36));
		assertTrue(containsSymbol(allSymbols, "@/foo-root-mapping/embedded-foo-mapping-with-root -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 27, 1, 27, 51));
		assertTrue(containsSymbol(allSymbols, "@/mapping1 -- (no method defined)", uriPrefix + "/src/main/java/org/test/SimpleMappingClass.java", 6, 1, 6, 28));
		assertTrue(containsSymbol(allSymbols, "@/mapping2 -- (no method defined)", uriPrefix + "/src/main/java/org/test/SimpleMappingClass.java", 11, 1, 11, 28));
		assertTrue(containsSymbol(allSymbols, "@/classlevel/mapping-subpackage -- (no method defined)", uriPrefix + "/src/main/java/org/test/sub/MappingClassSubpackage.java", 7, 1, 7, 38));
	}

	@Test
	public void testFilterSymbolsUsingQueryStringSplittedResult() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI()));

		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI());

		List<? extends SymbolInformation> allSymbols = indexer().getAllSymbols("@/foo-root-mapping -- (no method defined)");

		assertEquals(2, allSymbols.size());

		String uriPrefix = "file://" + directory.getAbsolutePath();

		assertTrue(containsSymbol(allSymbols, "@/foo-root-mapping -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 24, 0, 24, 36));
		assertTrue(containsSymbol(allSymbols, "@/foo-root-mapping/embedded-foo-mapping-with-root -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 27, 1, 27, 51));
	}

	@Test
	public void testFilterSymbolsUsingQueryStringFullSymbolString() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI()));

		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI());

		List<? extends SymbolInformation> allSymbols = indexer().getAllSymbols("@/foo-root-mapping/embedded-foo-mapping-with-root -- (no method defined)");

		assertEquals(1, allSymbols.size());

		String uriPrefix = "file://" + directory.getAbsolutePath();

		assertTrue(containsSymbol(allSymbols, "@/foo-root-mapping/embedded-foo-mapping-with-root -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 27, 1, 27, 51));
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

	@Test
	public void testRefreshOnProjectChange() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI()));

		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI());

		List<? extends SymbolInformation> allSymbols = indexer().getAllSymbols("");
		assertEquals(8, allSymbols.size());

		File pomFile = directory.toPath().resolve(MavenCore.POM_XML).toFile();

		assertFalse(indexer().isInitializing());
		harness.changeFile(pomFile.toURI().toString());
		// Refresh in progress
		assertTrue(indexer().isInitializing());

		allSymbols = indexer().getAllSymbols("");
		assertFalse(indexer().isInitializing());
		assertEquals(8, allSymbols.size());
	}

}
