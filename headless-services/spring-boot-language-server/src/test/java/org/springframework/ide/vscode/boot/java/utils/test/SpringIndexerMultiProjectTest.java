/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.util.UriUtil;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Martin Lippert
 */
@RunWith(SpringRunner.class)
@BootLanguageServerTest
@Import(SymbolProviderTestConf.class)
public class SpringIndexerMultiProjectTest {

	@Autowired private BootLanguageServerHarness harness;
	@Autowired private SpringSymbolIndex indexer;
	@Autowired private JavaProjectFinder projectFinder;

	private String projectUri1;
	private String projectUri2;

	@Before
	public void setup() throws Exception {
		harness.intialize(null);
		indexer.configureIndexer(SymbolIndexConfig.builder().scanXml(false).build());

		projectUri1 = UriUtil.toUri(new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-large-multiproject-1/").toURI())).toString();
		projectFinder.find(new TextDocumentIdentifier(projectUri1)).get();

		projectUri2 = UriUtil.toUri(new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-large-multiproject-2").toURI())).toString();
		projectFinder.find(new TextDocumentIdentifier(projectUri2)).get();

		CompletableFuture<Void> initProject = indexer.waitOperation();
		initProject.get(5, TimeUnit.SECONDS);
	}

	@Test
	public void testQueryingAllSymbolsWithRegularLimit() throws Exception {
		List<? extends SymbolInformation> symbols = indexer.getAllSymbols("");
		assertEquals(50, symbols.size());
	}

	@Test
	public void testQueryingAllSymbolsWithNoLimit() throws Exception {
		List<? extends SymbolInformation> symbols = indexer.getAllSymbols("*");
		assertEquals(220, symbols.size());

		int count1 = 0;
		int count2 = 0;

		for (SymbolInformation symbol : symbols) {
			if (symbol.getLocation().getUri().startsWith(projectUri1)) {
				count1++;
			}
			else if (symbol.getLocation().getUri().startsWith(projectUri2)) {
				count2++;
			}
		}

		assertEquals(110, count1);
		assertEquals(110, count2);
	}

	@Test
	public void testQueryingSymbolsForSpecificProjectWithRegularLimit() throws Exception {
		List<? extends SymbolInformation> symbols = indexer.getAllSymbols("locationPrefix:" + projectUri2);
		assertEquals(50, symbols.size());

		for (SymbolInformation symbol : symbols) {
			assertTrue(symbol.getLocation().getUri().startsWith(projectUri2));
		}
	}

	@Test
	public void testQueryingSymbolsForSpecificProjectWithNoLimit() throws Exception {
		List<? extends SymbolInformation> symbols = indexer.getAllSymbols("locationPrefix:" + projectUri2 + "?*");
		assertEquals(110, symbols.size());

		for (SymbolInformation symbol : symbols) {
			assertTrue(symbol.getLocation().getUri().startsWith(projectUri2));
		}
	}

	@Test
	public void testQueryingSymbolsForSpecificProjectWithQuery() throws Exception {
		List<? extends SymbolInformation> symbols = indexer.getAllSymbols("locationPrefix:" + projectUri2 + "?seventhWowSuperBean");
		assertEquals(10, symbols.size());

		for (SymbolInformation symbol : symbols) {
			assertTrue(symbol.getLocation().getUri().startsWith(projectUri2));
		}
	}

}
