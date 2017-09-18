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
public class RequestMappingSymbolProviderTest {

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
	public void testSimpleRequestMappingSymbol() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-request-mapping-symbols/").toURI()));

		SpringIndexer indexer = new SpringIndexer(harness.getServer(), projectFinder, symbolProviders);
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-request-mapping-symbols/").toURI());
		indexer.scanFiles(directory);

		String uriPrefix = "file://" + directory.getAbsolutePath();
		List<? extends SymbolInformation> symbols = indexer.getSymbols(uriPrefix + "/src/main/java/org/test/SimpleMappingClass.java");
		assertEquals(1, symbols.size());
		assertTrue(containsSymbol(symbols, "@/greeting -- (no method defined)", uriPrefix + "/src/main/java/org/test/SimpleMappingClass.java", 6, 1, 6, 29));
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
