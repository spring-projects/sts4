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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolInformation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
import org.springframework.ide.vscode.boot.java.requestmapping.Constants;
import org.springframework.ide.vscode.boot.java.requestmapping.RequestMappingSymbolProvider;
import org.springframework.ide.vscode.boot.java.utils.AnnotationIndexer;
import org.springframework.ide.vscode.commons.languageserver.java.DefaultJavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.IJavaProjectFinderStrategy;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.maven.MavenCore;
import org.springframework.ide.vscode.commons.maven.MavenProjectFinderStrategy;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;

/**
 * @author Martin Lippert
 */
public class AnnotationIndexerTest {

	private Map<String, SymbolProvider> symbolProviders;
	private JavaProjectFinder projectFinder;

	@Before
	public void setup() throws Exception {
		symbolProviders = new HashMap<>();
		symbolProviders.put(Constants.SPRING_REQUEST_MAPPING, new RequestMappingSymbolProvider());

		projectFinder = new DefaultJavaProjectFinder(new IJavaProjectFinderStrategy[] {new MavenProjectFinderStrategy(MavenCore.getDefault())});
	}

	@Test
	public void testScanningAllAnnotationsSimpleProjectUpfront() throws Exception {
		AnnotationIndexer indexer = new AnnotationIndexer(projectFinder, symbolProviders);
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI());
		indexer.scanFiles(directory);

		List<? extends SymbolInformation> allSymbols = indexer.getAllSymbols();

		assertEquals(8, allSymbols.size());

		String uriPrefix = "file://" + directory.getAbsolutePath();

		assertSymbol(allSymbols.get(0), "@SpringBootApplication", uriPrefix + "/src/main/java/org/test/MainClass.java", 6, 0, 6, 22);
		assertSymbol(allSymbols.get(1), "@/embedded-foo-mapping -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 17, 1, 17, 41);
		assertSymbol(allSymbols.get(2), "@/foo-root-mapping -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 24, 0, 24, 36);
		assertSymbol(allSymbols.get(3), "@/embedded-foo-mapping -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 27, 1, 27, 41);
		assertSymbol(allSymbols.get(4), "@/mapping1 -- (no method defined)", uriPrefix + "/src/main/java/org/test/SimpleMappingClass.java", 6, 1, 6, 28);
		assertSymbol(allSymbols.get(5), "@/mapping2 -- (no method defined)", uriPrefix + "/src/main/java/org/test/SimpleMappingClass.java", 11, 1, 11, 28);
		assertSymbol(allSymbols.get(6), "@/classlevel -- (no method defined)", uriPrefix + "/src/main/java/org/test/sub/MappingClassSubpackage.java", 4, 0, 4, 30);
		assertSymbol(allSymbols.get(7), "@/mapping-subpackage -- (no method defined)", uriPrefix + "/src/main/java/org/test/sub/MappingClassSubpackage.java", 7, 1, 7, 38);
	}

	@Test
	public void testScanningAllAnnotationsMultiModuleProjectUpfront() throws Exception {
		AnnotationIndexer indexer = new AnnotationIndexer(projectFinder, symbolProviders);
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/").toURI());
		indexer.scanFiles(directory);

		List<? extends SymbolInformation> allSymbols = indexer.getAllSymbols();

		assertEquals(8, allSymbols.size());

		String uriPrefix = "file://" + directory.getAbsolutePath() + "/test-annotation-indexing";

		assertSymbol(allSymbols.get(0), "@SpringBootApplication", uriPrefix + "/src/main/java/org/test/MainClass.java", 6, 0, 6, 22);
		assertSymbol(allSymbols.get(1), "@/embedded-foo-mapping -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 17, 1, 17, 41);
		assertSymbol(allSymbols.get(2), "@/foo-root-mapping -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 24, 0, 24, 36);
		assertSymbol(allSymbols.get(3), "@/embedded-foo-mapping -- (no method defined)", uriPrefix + "/src/main/java/org/test/MainClass.java", 27, 1, 27, 41);
		assertSymbol(allSymbols.get(4), "@/mapping1 -- (no method defined)", uriPrefix + "/src/main/java/org/test/SimpleMappingClass.java", 6, 1, 6, 28);
		assertSymbol(allSymbols.get(5), "@/mapping2 -- (no method defined)", uriPrefix + "/src/main/java/org/test/SimpleMappingClass.java", 11, 1, 11, 28);
		assertSymbol(allSymbols.get(6), "@/classlevel -- (no method defined)", uriPrefix + "/src/main/java/org/test/sub/MappingClassSubpackage.java", 4, 0, 4, 30);
		assertSymbol(allSymbols.get(7), "@/mapping-subpackage -- (no method defined)", uriPrefix + "/src/main/java/org/test/sub/MappingClassSubpackage.java", 7, 1, 7, 38);
	}

	private void assertSymbol(SymbolInformation symbol, String name, String uri, int startLine, int startCHaracter, int endLine, int endCharacter) {
		assertEquals(name, symbol.getName());

		Location location = symbol.getLocation();
		assertEquals("symbol: " + symbol.getName(), uri, location.getUri());
		assertEquals("symbol: " + symbol.getName(), startLine, location.getRange().getStart().getLine());
		assertEquals("symbol: " + symbol.getName(), startCHaracter, location.getRange().getStart().getCharacter());
		assertEquals("symbol: " + symbol.getName(), endLine, location.getRange().getEnd().getLine());
		assertEquals("symbol: " + symbol.getName(), endCharacter, location.getRange().getEnd().getCharacter());

	}

}
