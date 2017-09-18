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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.eclipse.lsp4j.SymbolInformation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServer;
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
import org.springframework.ide.vscode.boot.java.requestmapping.Constants;
import org.springframework.ide.vscode.boot.java.requestmapping.RequestMappingSymbolProvider;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexer;
import org.springframework.ide.vscode.commons.languageserver.java.DefaultJavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.IJavaProjectFinderStrategy;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.maven.MavenCore;
import org.springframework.ide.vscode.commons.maven.MavenProjectFinderStrategy;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.ide.vscode.project.harness.PropertyIndexHarness;

/**
 * @author Martin Lippert
 */
public class SpringIndexerTest {

	private Map<String, SymbolProvider> symbolProviders;
	private JavaProjectFinder projectFinder;
	private LanguageServerHarness<BootJavaLanguageServer> harness;
	private PropertyIndexHarness indexHarness;

	@Before
	public void setup() throws Exception {
		symbolProviders = new HashMap<>();
		symbolProviders.put(Constants.SPRING_REQUEST_MAPPING, new RequestMappingSymbolProvider());

		projectFinder = new DefaultJavaProjectFinder(new IJavaProjectFinderStrategy[] {new MavenProjectFinderStrategy(MavenCore.getDefault())});

		indexHarness = new PropertyIndexHarness();
		harness = new LanguageServerHarness<BootJavaLanguageServer>(new Callable<BootJavaLanguageServer>() {
			@Override
			public BootJavaLanguageServer call() throws Exception {
				BootJavaLanguageServer server = new BootJavaLanguageServer(projectFinder, indexHarness.getIndexProvider());
				return server;
			}
		}) {
			@Override
			protected String getFileExtension() {
				return ".java";
			}
		};
	}

	@Test
	public void testScanningAllAnnotationsSimpleProjectUpfront() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI()));

		SpringIndexer indexer = new SpringIndexer(harness.getServer(), projectFinder, symbolProviders);
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI());
		indexer.scanFiles(directory);

		List<? extends SymbolInformation> allSymbols = indexer.getAllSymbols();

		assertEquals(8, allSymbols.size());

		String uriPrefix = "file://" + directory.getAbsolutePath();

		assertTrue(containsSymbol(allSymbols, "@SpringBootApplication", uriPrefix + "/src/main/java/org/test/MainClass.java", 6, 0, 6, 22));
		assertTrue(containsSymbol(allSymbols, "@/embedded-foo-mapping -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 17, 1, 17, 41));
		assertTrue(containsSymbol(allSymbols, "@/foo-root-mapping -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 24, 0, 24, 36));
		assertTrue(containsSymbol(allSymbols, "@/embedded-foo-mapping -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 27, 1, 27, 41));
		assertTrue(containsSymbol(allSymbols, "@/mapping1 -- (no method defined)", uriPrefix + "/src/main/java/org/test/SimpleMappingClass.java", 6, 1, 6, 28));
		assertTrue(containsSymbol(allSymbols, "@/mapping2 -- (no method defined)", uriPrefix + "/src/main/java/org/test/SimpleMappingClass.java", 11, 1, 11, 28));
		assertTrue(containsSymbol(allSymbols, "@/classlevel -- (no method defined)", uriPrefix + "/src/main/java/org/test/sub/MappingClassSubpackage.java", 4, 0, 4, 30));
		assertTrue(containsSymbol(allSymbols, "@/mapping-subpackage -- (no method defined)", uriPrefix + "/src/main/java/org/test/sub/MappingClassSubpackage.java", 7, 1, 7, 38));
	}

	@Test
	public void testRetrievingSymbolsPerDocument() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI()));

		SpringIndexer indexer = new SpringIndexer(harness.getServer(), projectFinder, symbolProviders);
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI());
		indexer.scanFiles(directory);

		String uriPrefix = "file://" + directory.getAbsolutePath();
		List<? extends SymbolInformation> symbols = indexer.getSymbols(uriPrefix + "/src/main/java/org/test/MainClass.java");
		assertEquals(4, symbols.size());
		assertTrue(containsSymbol(symbols, "@SpringBootApplication", uriPrefix + "/src/main/java/org/test/MainClass.java", 6, 0, 6, 22));
		assertTrue(containsSymbol(symbols, "@/embedded-foo-mapping -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 17, 1, 17, 41));
		assertTrue(containsSymbol(symbols, "@/foo-root-mapping -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 24, 0, 24, 36));
		assertTrue(containsSymbol(symbols, "@/embedded-foo-mapping -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 27, 1, 27, 41));

		symbols = indexer.getSymbols(uriPrefix + "/src/main/java/org/test/SimpleMappingClass.java");
		assertEquals(2, symbols.size());
		assertTrue(containsSymbol(symbols, "@/mapping1 -- (no method defined)", uriPrefix + "/src/main/java/org/test/SimpleMappingClass.java", 6, 1, 6, 28));
		assertTrue(containsSymbol(symbols, "@/mapping2 -- (no method defined)", uriPrefix + "/src/main/java/org/test/SimpleMappingClass.java", 11, 1, 11, 28));

		symbols = indexer.getSymbols(uriPrefix + "/src/main/java/org/test/sub/MappingClassSubpackage.java");
		assertEquals(2, symbols.size());
		assertTrue(containsSymbol(symbols, "@/classlevel -- (no method defined)", uriPrefix + "/src/main/java/org/test/sub/MappingClassSubpackage.java", 4, 0, 4, 30));
		assertTrue(containsSymbol(symbols, "@/mapping-subpackage -- (no method defined)", uriPrefix + "/src/main/java/org/test/sub/MappingClassSubpackage.java", 7, 1, 7, 38));
	}

	@Test
	public void testScanningAllAnnotationsMultiModuleProjectUpfront() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/").toURI()));

		SpringIndexer indexer = new SpringIndexer(harness.getServer(), projectFinder, symbolProviders);
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/").toURI());
		indexer.scanFiles(directory);

		List<? extends SymbolInformation> allSymbols = indexer.getAllSymbols();

		assertEquals(8, allSymbols.size());

		String uriPrefix = "file://" + directory.getAbsolutePath() + "/test-annotation-indexing";

		assertTrue(containsSymbol(allSymbols, "@SpringBootApplication", uriPrefix + "/src/main/java/org/test/MainClass.java", 6, 0, 6, 22));
		assertTrue(containsSymbol(allSymbols, "@/embedded-foo-mapping -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 17, 1, 17, 41));
		assertTrue(containsSymbol(allSymbols, "@/foo-root-mapping -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 24, 0, 24, 36));
		assertTrue(containsSymbol(allSymbols, "@/embedded-foo-mapping -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 27, 1, 27, 41));
		assertTrue(containsSymbol(allSymbols, "@/mapping1 -- (no method defined)", uriPrefix + "/src/main/java/org/test/SimpleMappingClass.java", 6, 1, 6, 28));
		assertTrue(containsSymbol(allSymbols, "@/mapping2 -- (no method defined)", uriPrefix + "/src/main/java/org/test/SimpleMappingClass.java", 11, 1, 11, 28));
		assertTrue(containsSymbol(allSymbols, "@/classlevel -- (no method defined)", uriPrefix + "/src/main/java/org/test/sub/MappingClassSubpackage.java", 4, 0, 4, 30));
		assertTrue(containsSymbol(allSymbols, "@/mapping-subpackage -- (no method defined)", uriPrefix + "/src/main/java/org/test/sub/MappingClassSubpackage.java", 7, 1, 7, 38));
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
