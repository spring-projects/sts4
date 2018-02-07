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
package org.springframework.ide.vscode.boot.java.utils.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
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
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServer;
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
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
		symbolProviders.put(Annotations.SPRING_REQUEST_MAPPING, new RequestMappingSymbolProvider());
		harness = BootLanguageServerHarness.builder().build();
	}

	@Test
	public void testScanningAllAnnotationsSimpleProjectUpfront() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI()));

		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI());

		List<? extends SymbolInformation> allSymbols = indexer().getAllSymbols("");

		assertEquals(6, allSymbols.size());

		String docUri = directory.toPath().resolve("src/main/java/org/test/MainClass.java").toUri().toString();
		assertTrue(containsSymbol(allSymbols, "@+ 'mainClass' (@SpringBootApplication <: @SpringBootConfiguration, @Configuration, @Component) MainClass", docUri, 6, 0, 6, 22));
		assertTrue(containsSymbol(allSymbols, "@/embedded-foo-mapping", docUri, 17, 1, 17, 41));
		assertTrue(containsSymbol(allSymbols, "@/foo-root-mapping/embedded-foo-mapping-with-root", docUri, 27, 1, 27, 51));

		docUri = directory.toPath().resolve("src/main/java/org/test/SimpleMappingClass.java").toUri().toString();
		assertTrue(containsSymbol(allSymbols, "@/mapping1", docUri, 6, 1, 6, 28));
		assertTrue(containsSymbol(allSymbols, "@/mapping2", docUri, 11, 1, 11, 28));

		docUri = directory.toPath().resolve("src/main/java/org/test/sub/MappingClassSubpackage.java").toUri().toString();
		assertTrue(containsSymbol(allSymbols, "@/classlevel/mapping-subpackage", docUri, 7, 1, 7, 38));
	}

	@Test
	public void testRetrievingSymbolsPerDocument() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI()));

		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI());

		String docUri = directory.toPath().resolve("src/main/java/org/test/MainClass.java").toUri().toString();
		List<? extends SymbolInformation> symbols = indexer().getSymbols(docUri);
		assertEquals(3, symbols.size());
		assertTrue(containsSymbol(symbols, "@+ 'mainClass' (@SpringBootApplication <: @SpringBootConfiguration, @Configuration, @Component) MainClass", docUri, 6, 0, 6, 22));
		assertTrue(containsSymbol(symbols, "@/embedded-foo-mapping", docUri, 17, 1, 17, 41));
		assertTrue(containsSymbol(symbols, "@/foo-root-mapping/embedded-foo-mapping-with-root", docUri, 27, 1, 27, 51));

		docUri = directory.toPath().resolve("src/main/java/org/test/SimpleMappingClass.java").toUri().toString();
		symbols = indexer().getSymbols(docUri);
		assertEquals(2, symbols.size());
		assertTrue(containsSymbol(symbols, "@/mapping1", docUri, 6, 1, 6, 28));
		assertTrue(containsSymbol(symbols, "@/mapping2", docUri, 11, 1, 11, 28));

		docUri = directory.toPath().resolve("src/main/java/org/test/sub/MappingClassSubpackage.java").toUri().toString();
		symbols = indexer().getSymbols(docUri);
		assertEquals(1, symbols.size());
		assertTrue(containsSymbol(symbols, "@/classlevel/mapping-subpackage", docUri, 7, 1, 7, 38));
	}

	@Test
	public void testScanningAllAnnotationsMultiModuleProjectUpfront() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/").toURI()));

		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing").toURI());

		List<? extends SymbolInformation> allSymbols = indexer().getAllSymbols("");

		assertEquals(6, allSymbols.size());

		String docUri = directory.toPath().resolve("src/main/java/org/test/MainClass.java").toUri().toString();
		assertTrue(containsSymbol(allSymbols, "@+ 'mainClass' (@SpringBootApplication <: @SpringBootConfiguration, @Configuration, @Component) MainClass", docUri, 6, 0, 6, 22));
		assertTrue(containsSymbol(allSymbols, "@/embedded-foo-mapping", docUri, 17, 1, 17, 41));
		assertTrue(containsSymbol(allSymbols, "@/foo-root-mapping/embedded-foo-mapping-with-root", docUri, 27, 1, 27, 51));

		docUri = directory.toPath().resolve("src/main/java/org/test/SimpleMappingClass.java").toUri().toString();
		assertTrue(containsSymbol(allSymbols, "@/mapping1", docUri, 6, 1, 6, 28));
		assertTrue(containsSymbol(allSymbols, "@/mapping2", docUri, 11, 1, 11, 28));

		docUri = directory.toPath().resolve("src/main/java/org/test/sub/MappingClassSubpackage.java").toUri().toString();
		assertTrue(containsSymbol(allSymbols, "@/classlevel/mapping-subpackage", docUri, 7, 1, 7, 38));
	}

	@Test
	public void testUpdateChangedDocument() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI()));

		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI());

		// update document and update index
		String changedDocURI = directory.toPath().resolve("src/main/java/org/test/SimpleMappingClass.java").toUri().toString();

		String newContent = FileUtils.readFileToString(new File(new URI(changedDocURI))).replace("mapping1", "mapping1-CHANGED");
		CompletableFuture<Void> updateFuture = indexer().updateDocument(changedDocURI, newContent);

		updateFuture.get(5, TimeUnit.SECONDS);

		// check for updated index per document
		List<? extends SymbolInformation> symbols = indexer().getSymbols(changedDocURI);
		assertEquals(2, symbols.size());
		assertTrue(containsSymbol(symbols, "@/mapping1-CHANGED", changedDocURI, 6, 1, 6, 36));
		assertTrue(containsSymbol(symbols, "@/mapping2", changedDocURI, 11, 1, 11, 28));

		// check for updated index in all symbols
		List<? extends SymbolInformation> allSymbols = indexer().getAllSymbols("");
		assertEquals(6, allSymbols.size());

		String docUri = directory.toPath().resolve("src/main/java/org/test/MainClass.java").toUri().toString();
		assertTrue(containsSymbol(allSymbols, "@+ 'mainClass' (@SpringBootApplication <: @SpringBootConfiguration, @Configuration, @Component) MainClass", docUri, 6, 0, 6, 22));
		assertTrue(containsSymbol(allSymbols, "@/embedded-foo-mapping", docUri, 17, 1, 17, 41));
		assertTrue(containsSymbol(allSymbols, "@/foo-root-mapping/embedded-foo-mapping-with-root", docUri, 27, 1, 27, 51));

		docUri = directory.toPath().resolve("src/main/java/org/test/SimpleMappingClass.java").toUri().toString();
		assertTrue(containsSymbol(allSymbols, "@/mapping1-CHANGED", docUri, 6, 1, 6, 36));
		assertTrue(containsSymbol(allSymbols, "@/mapping2", docUri, 11, 1, 11, 28));

		docUri = directory.toPath().resolve("src/main/java/org/test/sub/MappingClassSubpackage.java").toUri().toString();
		assertTrue(containsSymbol(allSymbols, "@/classlevel/mapping-subpackage", docUri, 7, 1, 7, 38));
	}

	@Test
	public void testNewDocumentCreated() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI()));
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI());

		String createdDocURI = directory.toPath().resolve("src/main/java/org/test/CreatedClass.java").toUri().toString();

		// check for document to not be created yet
		List<? extends SymbolInformation> symbols = indexer().getSymbols(createdDocURI);
		assertNull(symbols);

		List<? extends SymbolInformation> allSymbols = indexer().getAllSymbols("");
		assertEquals(6, allSymbols.size());

		try {
			// create document and update index
			String content = "package org.test;\n" +
					"\n" +
					"import org.springframework.web.bind.annotation.RequestMapping;\n" +
					"\n" +
					"public class SimpleMappingClass {\n" +
					"	\n" +
					"	@RequestMapping(\"created-mapping1\")\n" +
					"	public String hello1() {\n" +
					"		return \"hello1\";\n" +
					"	}\n" +
					"\n" +
					"	@RequestMapping(\"created-mapping2\")\n" +
					"	public String hello2() {\n" +
					"		return \"hello2\";\n" +
					"	}\n" +
					"\n" +
					"}\n" +
					"";
			FileUtils.write(new File(new URI(createdDocURI)), content);
			CompletableFuture<Void> createFuture = indexer().createDocument(createdDocURI);
			createFuture.get(5, TimeUnit.SECONDS);

			// check for updated index per document
			symbols = indexer().getSymbols(createdDocURI);
			assertEquals(2, symbols.size());
			assertTrue(containsSymbol(symbols, "@/created-mapping1", createdDocURI, 6, 1, 6, 36));
			assertTrue(containsSymbol(symbols, "@/created-mapping2", createdDocURI, 11, 1, 11, 36));

			// check for updated index in all symbols
			allSymbols = indexer().getAllSymbols("");
			assertEquals(8, allSymbols.size());

			String docUri = directory.toPath().resolve("src/main/java/org/test/MainClass.java").toUri().toString();
			assertTrue(containsSymbol(allSymbols, "@+ 'mainClass' (@SpringBootApplication <: @SpringBootConfiguration, @Configuration, @Component) MainClass", docUri, 6, 0, 6, 22));
			assertTrue(containsSymbol(allSymbols, "@/embedded-foo-mapping", docUri, 17, 1, 17, 41));
			assertTrue(containsSymbol(allSymbols, "@/foo-root-mapping/embedded-foo-mapping-with-root", docUri, 27, 1, 27, 51));

			docUri = directory.toPath().resolve("src/main/java/org/test/SimpleMappingClass.java").toUri().toString();
			assertTrue(containsSymbol(allSymbols, "@/mapping1", docUri, 6, 1, 6, 28));
			assertTrue(containsSymbol(allSymbols, "@/mapping2", docUri, 11, 1, 11, 28));

			docUri = directory.toPath().resolve("src/main/java/org/test/sub/MappingClassSubpackage.java").toUri().toString();
			assertTrue(containsSymbol(allSymbols, "@/classlevel/mapping-subpackage", docUri, 7, 1, 7, 38));

			assertTrue(containsSymbol(allSymbols, "@/created-mapping1", createdDocURI, 6, 1, 6, 36));
			assertTrue(containsSymbol(allSymbols, "@/created-mapping2", createdDocURI, 11, 1, 11, 36));
		}
		finally {
			FileUtils.deleteQuietly(new File(new URI(createdDocURI)));
		}
	}

	@Test
	public void testRemoveSymbolsFromDeletedDocument() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI()));
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI());

		// update document and update index
		String deletedDocURI = directory.toPath().resolve("src/main/java/org/test/SimpleMappingClass.java").toUri().toString();
		CompletableFuture<Void> deleteFuture = indexer().deleteDocument(deletedDocURI);
		deleteFuture.get(5, TimeUnit.SECONDS);

		// check for updated index per document
		List<? extends SymbolInformation> symbols = indexer().getSymbols(deletedDocURI);
		assertNull(symbols);

		// check for updated index in all symbols
		List<? extends SymbolInformation> allSymbols = indexer().getAllSymbols("");
		assertEquals(4, allSymbols.size());

		String docUri = directory.toPath().resolve("src/main/java/org/test/MainClass.java").toUri().toString();
		assertTrue(containsSymbol(allSymbols, "@+ 'mainClass' (@SpringBootApplication <: @SpringBootConfiguration, @Configuration, @Component) MainClass", docUri, 6, 0, 6, 22));
		assertTrue(containsSymbol(allSymbols, "@/embedded-foo-mapping", docUri, 17, 1, 17, 41));
		assertTrue(containsSymbol(allSymbols, "@/foo-root-mapping/embedded-foo-mapping-with-root", docUri, 27, 1, 27, 51));

		docUri = directory.toPath().resolve("src/main/java/org/test/sub/MappingClassSubpackage.java").toUri().toString();
		assertTrue(containsSymbol(allSymbols, "@/classlevel/mapping-subpackage", docUri, 7, 1, 7, 38));
	}

	@Test
	public void testFilterSymbolsUsingQueryString() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI()));

		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI());

		List<? extends SymbolInformation> allSymbols = indexer().getAllSymbols("mapp");

		assertEquals(6, allSymbols.size());

		String docUri = directory.toPath().resolve("src/main/java/org/test/MainClass.java").toUri().toString();
		assertTrue(containsSymbol(allSymbols, "@/embedded-foo-mapping", docUri, 17, 1, 17, 41));
		assertTrue(containsSymbol(allSymbols, "@/foo-root-mapping/embedded-foo-mapping-with-root", docUri, 27, 1, 27, 51));

		docUri = directory.toPath().resolve("src/main/java/org/test/SimpleMappingClass.java").toUri().toString();
		assertTrue(containsSymbol(allSymbols, "@/mapping1", docUri, 6, 1, 6, 28));
		assertTrue(containsSymbol(allSymbols, "@/mapping2", docUri, 11, 1, 11, 28));

		docUri = directory.toPath().resolve("src/main/java/org/test/sub/MappingClassSubpackage.java").toUri().toString();
		assertTrue(containsSymbol(allSymbols, "@/classlevel/mapping-subpackage", docUri, 7, 1, 7, 38));
	}

	@Test
	public void testFilterSymbolsUsingQueryStringSplittedResult() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI()));

		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI());

		List<? extends SymbolInformation> allSymbols = indexer().getAllSymbols("@/foo-root-mapping");

		assertEquals(1, allSymbols.size());

		String docUri = directory.toPath().resolve("src/main/java/org/test/MainClass.java").toUri().toString();

		assertTrue(containsSymbol(allSymbols, "@/foo-root-mapping/embedded-foo-mapping-with-root", docUri, 27, 1, 27, 51));
	}

	@Test
	public void testFilterSymbolsUsingQueryStringFullSymbolString() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI()));

		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI());

		List<? extends SymbolInformation> allSymbols = indexer().getAllSymbols("@/foo-root-mapping/embedded-foo-mapping-with-root");

		assertEquals(1, allSymbols.size());

		String docUri = directory.toPath().resolve("src/main/java/org/test/MainClass.java").toUri().toString();

		assertTrue(containsSymbol(allSymbols, "@/foo-root-mapping/embedded-foo-mapping-with-root", docUri, 27, 1, 27, 51));
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
		assertEquals(6, allSymbols.size());

		File pomFile = directory.toPath().resolve(MavenCore.POM_XML).toFile();

		assertFalse(indexer().isInitializing());
		harness.changeFile(pomFile.toURI().toString());
		// Refresh in progress
		assertTrue(indexer().isInitializing());

		allSymbols = indexer().getAllSymbols("");
		assertFalse(indexer().isInitializing());
		assertEquals(6, allSymbols.size());
	}

}
