/*******************************************************************************
 * Copyright (c) 2017, 2019 Pivotal, Inc.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
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
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.boot.bootiful.SymbolProviderTestConf;
import org.springframework.ide.vscode.boot.java.utils.SymbolIndexConfig;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.util.Assert;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Martin Lippert
 */
@RunWith(SpringRunner.class)
@BootLanguageServerTest
@Import(SymbolProviderTestConf.class)
public class SpringIndexerTest {

	@Autowired private BootLanguageServerHarness harness;
	@Autowired private SpringSymbolIndex indexer;
	@Autowired private JavaProjectFinder projectFinder;

	private File directory;
	private String projectDir;
	private IJavaProject project;

	@Before
	public void setup() throws Exception {
		harness.intialize(null);
		indexer.configureIndexer(SymbolIndexConfig.builder().scanXml(false).build());

		directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI());
		projectDir = directory.toURI().toString();

		// trigger project creation
		project = projectFinder.find(new TextDocumentIdentifier(projectDir)).get();

		CompletableFuture<Void> initProject = indexer.waitOperation();
		initProject.get(5, TimeUnit.SECONDS);
	}

	@Test
	public void testScanningAllAnnotationsSimpleProjectUpfront() throws Exception {
		List<? extends SymbolInformation> allSymbols = indexer.getAllSymbols("");

		assertEquals(7, allSymbols.size());

		String docUri = directory.toPath().resolve("src/main/java/org/test/MainClass.java").toUri().toString();
		assertTrue(containsSymbol(allSymbols, "@+ 'mainClass' (@SpringBootApplication <: @SpringBootConfiguration, @Configuration, @Component) MainClass", docUri, 6, 0, 6, 22));
		assertTrue(containsSymbol(allSymbols, "@/embedded-foo-mapping", docUri, 17, 1, 17, 41));
		assertTrue(containsSymbol(allSymbols, "@/foo-root-mapping/embedded-foo-mapping-with-root", docUri, 27, 1, 27, 51));

		docUri = directory.toPath().resolve("src/main/java/org/test/SimpleMappingClass.java").toUri().toString();
		assertTrue(containsSymbol(allSymbols, "@/mapping1", docUri, 6, 1, 6, 28));
		assertTrue(containsSymbol(allSymbols, "@/mapping2", docUri, 11, 1, 11, 28));

		docUri = directory.toPath().resolve("src/main/java/org/test/sub/MappingClassSubpackage.java").toUri().toString();
		assertTrue(containsSymbol(allSymbols, "@/classlevel/mapping-subpackage", docUri, 7, 1, 7, 38));

		docUri = directory.toPath().resolve("src/main/java/org/test/ClassWithDefaultSymbol.java").toUri().toString();
		assertTrue(containsSymbol(allSymbols, "@Configurable", docUri, 4, 0, 4, 13));
	}
	
	@Test
	public void testScanTestJavaSources() throws Exception {
		indexer.configureIndexer(SymbolIndexConfig.builder().scanTestJavaSources(true).build());
		
		List<? extends SymbolInformation> allSymbols = indexer.getAllSymbols("");
		assertEquals(8, allSymbols.size());
		String docUri = directory.toPath().resolve("src/test/java/demo/ApplicationTests.java").toUri().toString();
		assertTrue(containsSymbol(allSymbols, "@SpringBootTest", docUri, 8, 0, 8, 15));
		
		indexer.configureIndexer(SymbolIndexConfig.builder().scanTestJavaSources(false).build());
		allSymbols = indexer.getAllSymbols("");
		assertEquals(7, allSymbols.size());
		assertFalse(containsSymbol(allSymbols, "@SpringBootTest", docUri, 8, 0, 8, 15));
	}

	@Test
	public void testRetrievingSymbolsPerDocument() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/MainClass.java").toUri().toString();
		List<? extends SymbolInformation> symbols = indexer.getSymbols(docUri);
		assertEquals(3, symbols.size());
		assertTrue(containsSymbol(symbols, "@+ 'mainClass' (@SpringBootApplication <: @SpringBootConfiguration, @Configuration, @Component) MainClass", docUri, 6, 0, 6, 22));
		assertTrue(containsSymbol(symbols, "@/embedded-foo-mapping", docUri, 17, 1, 17, 41));
		assertTrue(containsSymbol(symbols, "@/foo-root-mapping/embedded-foo-mapping-with-root", docUri, 27, 1, 27, 51));

		docUri = directory.toPath().resolve("src/main/java/org/test/SimpleMappingClass.java").toUri().toString();
		symbols = indexer.getSymbols(docUri);
		assertEquals(2, symbols.size());
		assertTrue(containsSymbol(symbols, "@/mapping1", docUri, 6, 1, 6, 28));
		assertTrue(containsSymbol(symbols, "@/mapping2", docUri, 11, 1, 11, 28));

		docUri = directory.toPath().resolve("src/main/java/org/test/sub/MappingClassSubpackage.java").toUri().toString();
		symbols = indexer.getSymbols(docUri);
		assertEquals(1, symbols.size());
		assertTrue(containsSymbol(symbols, "@/classlevel/mapping-subpackage", docUri, 7, 1, 7, 38));
	}

	@Test
	public void testScanningAllAnnotationsMultiModuleProjectUpfront() throws Exception {
		List<? extends SymbolInformation> allSymbols = indexer.getAllSymbols("");

		assertEquals(7, allSymbols.size());

		String docUri = directory.toPath().resolve("src/main/java/org/test/MainClass.java").toUri().toString();
		assertTrue(containsSymbol(allSymbols, "@+ 'mainClass' (@SpringBootApplication <: @SpringBootConfiguration, @Configuration, @Component) MainClass", docUri, 6, 0, 6, 22));
		assertTrue(containsSymbol(allSymbols, "@/embedded-foo-mapping", docUri, 17, 1, 17, 41));
		assertTrue(containsSymbol(allSymbols, "@/foo-root-mapping/embedded-foo-mapping-with-root", docUri, 27, 1, 27, 51));

		docUri = directory.toPath().resolve("src/main/java/org/test/SimpleMappingClass.java").toUri().toString();
		assertTrue(containsSymbol(allSymbols, "@/mapping1", docUri, 6, 1, 6, 28));
		assertTrue(containsSymbol(allSymbols, "@/mapping2", docUri, 11, 1, 11, 28));

		docUri = directory.toPath().resolve("src/main/java/org/test/sub/MappingClassSubpackage.java").toUri().toString();
		assertTrue(containsSymbol(allSymbols, "@/classlevel/mapping-subpackage", docUri, 7, 1, 7, 38));


		docUri = directory.toPath().resolve("src/main/java/org/test/ClassWithDefaultSymbol.java").toUri().toString();
		assertTrue(containsSymbol(allSymbols, "@Configurable", docUri, 4, 0, 4, 13));
	}

	@Test
	public void testUpdateChangedDocument() throws Exception {
		// update document and update index
		String changedDocURI = directory.toPath().resolve("src/main/java/org/test/SimpleMappingClass.java").toUri().toString();

		assertTrue(containsSymbol(indexer.getSymbols(changedDocURI), "@/mapping1", changedDocURI));

		String newContent = FileUtils.readFileToString(new File(new URI(changedDocURI))).replace("mapping1", "mapping1-CHANGED");
		CompletableFuture<Void> updateFuture = indexer.updateDocument(changedDocURI, newContent, "test triggered");
		updateFuture.get(5, TimeUnit.SECONDS);

		// check for updated index per document
		List<? extends SymbolInformation> symbols = indexer.getSymbols(changedDocURI);
		assertEquals(2, symbols.size());
		assertTrue(containsSymbol(symbols, "@/mapping1-CHANGED", changedDocURI, 6, 1, 6, 36));
		assertTrue(containsSymbol(symbols, "@/mapping2", changedDocURI, 11, 1, 11, 28));

		// check for updated index in all symbols
		List<? extends SymbolInformation> allSymbols = indexer.getAllSymbols("");
		assertEquals(7, allSymbols.size());

		String docUri = directory.toPath().resolve("src/main/java/org/test/MainClass.java").toUri().toString();
		assertTrue(containsSymbol(allSymbols, "@+ 'mainClass' (@SpringBootApplication <: @SpringBootConfiguration, @Configuration, @Component) MainClass", docUri, 6, 0, 6, 22));
		assertTrue(containsSymbol(allSymbols, "@/embedded-foo-mapping", docUri, 17, 1, 17, 41));
		assertTrue(containsSymbol(allSymbols, "@/foo-root-mapping/embedded-foo-mapping-with-root", docUri, 27, 1, 27, 51));

		docUri = directory.toPath().resolve("src/main/java/org/test/SimpleMappingClass.java").toUri().toString();
		assertTrue(containsSymbol(allSymbols, "@/mapping1-CHANGED", docUri, 6, 1, 6, 36));
		assertTrue(containsSymbol(allSymbols, "@/mapping2", docUri, 11, 1, 11, 28));

		docUri = directory.toPath().resolve("src/main/java/org/test/sub/MappingClassSubpackage.java").toUri().toString();
		assertTrue(containsSymbol(allSymbols, "@/classlevel/mapping-subpackage", docUri, 7, 1, 7, 38));

		docUri = directory.toPath().resolve("src/main/java/org/test/ClassWithDefaultSymbol.java").toUri().toString();
		assertTrue(containsSymbol(allSymbols, "@Configurable", docUri, 4, 0, 4, 13));
	}

	@Test
	public void testNewDocumentCreated() throws Exception {
		String createdDocURI = directory.toPath().resolve("src/main/java/org/test/CreatedClass.java").toUri().toString();
		// check for document to not be created yet
		List<? extends SymbolInformation> symbols = indexer.getSymbols(createdDocURI);
		assertNotNull(symbols);
		assertEquals(0, symbols.size());

		List<? extends SymbolInformation> allSymbols = indexer.getAllSymbols("");
		assertEquals(7, allSymbols.size());

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
			CompletableFuture<Void> createFuture = indexer.createDocument(createdDocURI);
			createFuture.get(5, TimeUnit.SECONDS);

			// check for updated index per document
			symbols = indexer.getSymbols(createdDocURI);
			assertEquals(2, symbols.size());
			assertTrue(containsSymbol(symbols, "@/created-mapping1", createdDocURI, 6, 1, 6, 36));
			assertTrue(containsSymbol(symbols, "@/created-mapping2", createdDocURI, 11, 1, 11, 36));

			// check for updated index in all symbols
			allSymbols = indexer.getAllSymbols("");
			assertEquals(9, allSymbols.size());

			String docUri = directory.toPath().resolve("src/main/java/org/test/MainClass.java").toUri().toString();
			assertTrue(containsSymbol(allSymbols, "@+ 'mainClass' (@SpringBootApplication <: @SpringBootConfiguration, @Configuration, @Component) MainClass", docUri, 6, 0, 6, 22));
			assertTrue(containsSymbol(allSymbols, "@/embedded-foo-mapping", docUri, 17, 1, 17, 41));
			assertTrue(containsSymbol(allSymbols, "@/foo-root-mapping/embedded-foo-mapping-with-root", docUri, 27, 1, 27, 51));

			docUri = directory.toPath().resolve("src/main/java/org/test/SimpleMappingClass.java").toUri().toString();
			assertTrue(containsSymbol(allSymbols, "@/mapping1", docUri, 6, 1, 6, 28));
			assertTrue(containsSymbol(allSymbols, "@/mapping2", docUri, 11, 1, 11, 28));

			docUri = directory.toPath().resolve("src/main/java/org/test/sub/MappingClassSubpackage.java").toUri().toString();
			assertTrue(containsSymbol(allSymbols, "@/classlevel/mapping-subpackage", docUri, 7, 1, 7, 38));

			docUri = directory.toPath().resolve("src/main/java/org/test/ClassWithDefaultSymbol.java").toUri().toString();
			assertTrue(containsSymbol(allSymbols, "@Configurable", docUri, 4, 0, 4, 13));

			assertTrue(containsSymbol(allSymbols, "@/created-mapping1", createdDocURI, 6, 1, 6, 36));
			assertTrue(containsSymbol(allSymbols, "@/created-mapping2", createdDocURI, 11, 1, 11, 36));
		}
		finally {
			FileUtils.deleteQuietly(new File(new URI(createdDocURI)));
		}
	}

	@Test
	public void testRemoveSymbolsFromDeletedDocument() throws Exception {
		// update document and update index
		String deletedDocURI = directory.toPath().resolve("src/main/java/org/test/SimpleMappingClass.java").toUri().toString();

		assertFalse(indexer.getSymbols(deletedDocURI).isEmpty()); //We have symbols before deletion?
		CompletableFuture<Void> deleteFuture = indexer.deleteDocument(deletedDocURI);
		deleteFuture.get(5, TimeUnit.HOURS);

		// check for updated index per document
		Assert.noElements(indexer.getSymbols(deletedDocURI));

		// check for updated index in all symbols
		List<? extends SymbolInformation> allSymbols = indexer.getAllSymbols("");
		assertEquals(5, allSymbols.size());

		String docUri = directory.toPath().resolve("src/main/java/org/test/MainClass.java").toUri().toString();
		assertTrue(containsSymbol(allSymbols, "@+ 'mainClass' (@SpringBootApplication <: @SpringBootConfiguration, @Configuration, @Component) MainClass", docUri, 6, 0, 6, 22));
		assertTrue(containsSymbol(allSymbols, "@/embedded-foo-mapping", docUri, 17, 1, 17, 41));
		assertTrue(containsSymbol(allSymbols, "@/foo-root-mapping/embedded-foo-mapping-with-root", docUri, 27, 1, 27, 51));

		docUri = directory.toPath().resolve("src/main/java/org/test/sub/MappingClassSubpackage.java").toUri().toString();
		assertTrue(containsSymbol(allSymbols, "@/classlevel/mapping-subpackage", docUri, 7, 1, 7, 38));

		docUri = directory.toPath().resolve("src/main/java/org/test/ClassWithDefaultSymbol.java").toUri().toString();
		assertTrue(containsSymbol(allSymbols, "@Configurable", docUri, 4, 0, 4, 13));
	}

	@Test
	public void testFilterSymbolsUsingQueryString() throws Exception {
		List<? extends SymbolInformation> allSymbols = indexer.getAllSymbols("mapp");

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
		List<? extends SymbolInformation> allSymbols = indexer.getAllSymbols("@/foo-root-mapping");

		assertEquals(1, allSymbols.size());

		String docUri = directory.toPath().resolve("src/main/java/org/test/MainClass.java").toUri().toString();

		assertTrue(containsSymbol(allSymbols, "@/foo-root-mapping/embedded-foo-mapping-with-root", docUri, 27, 1, 27, 51));
	}

	@Test
	public void testFilterSymbolsUsingQueryStringFullSymbolString() throws Exception {
		List<? extends SymbolInformation> allSymbols = indexer.getAllSymbols("@/foo-root-mapping/embedded-foo-mapping-with-root");

		assertEquals(1, allSymbols.size());

		String docUri = directory.toPath().resolve("src/main/java/org/test/MainClass.java").toUri().toString();

		assertTrue(containsSymbol(allSymbols, "@/foo-root-mapping/embedded-foo-mapping-with-root", docUri, 27, 1, 27, 51));
	}

	@Test
	public void testDeleteProject() throws Exception {
		List<? extends SymbolInformation> allSymbols = indexer.getAllSymbols("");
		assertEquals(7, allSymbols.size());

		CompletableFuture<Void> deleteProject = indexer.deleteProject(project);
		deleteProject.get(5, TimeUnit.SECONDS);

		allSymbols = indexer.getAllSymbols("");
		assertEquals(0, allSymbols.size());
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
