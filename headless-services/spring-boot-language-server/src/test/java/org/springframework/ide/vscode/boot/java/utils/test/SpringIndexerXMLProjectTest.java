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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.lsp4j.WorkspaceSymbol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.app.BootLanguageServerBootApp;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.boot.bootiful.XmlBeansTestConf;
import org.springframework.ide.vscode.boot.java.beans.BeansSymbolAddOnInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolAddOnInformation;
import org.springframework.ide.vscode.boot.java.utils.SymbolIndexConfig;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.languageserver.starter.LanguageServerAutoConf;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

/**
 * @author Martin Lippert
 */
@OverrideAutoConfiguration(enabled=false)
@Import({LanguageServerAutoConf.class, XmlBeansTestConf.class})
@SpringBootTest(classes={
		BootLanguageServerBootApp.class
})
@DirtiesContext(classMode=ClassMode.AFTER_EACH_TEST_METHOD)
public class SpringIndexerXMLProjectTest {

	@Autowired private BootLanguageServerHarness harness;
	@Autowired private SpringSymbolIndex indexer;
	@Autowired private MockProjectObserver projectObserver;

	private ProjectsHarness projects = ProjectsHarness.INSTANCE;
	private File directory;
	private IJavaProject project;

	@BeforeEach
	public void setup() throws Exception {
		harness.intialize(null);
		indexer.configureIndexer(SymbolIndexConfig.builder()
				.scanXml(true)
				.xmlScanFolders(new String[] { "src/main", "config" })
				.build());

		project = projects.mavenProject("test-annotation-indexing-xml-project");
		harness.useProject(project);
		directory = Paths.get(project.getLocationUri()).toFile();
		
		// trigger project creation
		projectObserver.doWithListeners(l -> l.created(project));
		
		CompletableFuture<Void> initProject = indexer.waitOperation();
		initProject.get(5, TimeUnit.SECONDS);
	}

    @Test
    void testScanningSimpleSpringXMLConfig() throws Exception {
        List<? extends WorkspaceSymbol> allSymbols = indexer.getAllSymbols("");

        assertEquals(5, allSymbols.size());

        String docUri = directory.toPath().resolve("config/simple-spring-config.xml").toUri().toString();
        assertTrue(SpringIndexerTest.containsSymbol(allSymbols, "@+ 'transactionManager' DataSourceTransactionManager", docUri, 6, 14, 6, 37));
        assertTrue(SpringIndexerTest.containsSymbol(allSymbols, "@+ 'jdbcTemplate' JdbcTemplate", docUri, 8, 14, 8, 31));
        assertTrue(SpringIndexerTest.containsSymbol(allSymbols, "@+ 'namedParameterJdbcTemplate' NamedParameterJdbcTemplate", docUri, 12, 14, 12, 45));
        assertTrue(SpringIndexerTest.containsSymbol(allSymbols, "@+ 'persistenceExceptionTranslationPostProcessor' PersistenceExceptionTranslationPostProcessor", docUri, 18, 10, 18, 97));

        List<? extends SymbolAddOnInformation> addon = indexer.getAdditonalInformation(docUri);
        assertEquals(4, addon.size());

        assertEquals(1, addon.stream()
                .filter(info -> info instanceof BeansSymbolAddOnInformation)
                .filter(info -> "transactionManager".equals(((BeansSymbolAddOnInformation) info).getBeanID()))
                .count());

        assertEquals(1, addon.stream()
                .filter(info -> info instanceof BeansSymbolAddOnInformation)
                .filter(info -> "jdbcTemplate".equals(((BeansSymbolAddOnInformation) info).getBeanID()))
                .count());

        assertEquals(1, addon.stream()
                .filter(info -> info instanceof BeansSymbolAddOnInformation)
                .filter(info -> "namedParameterJdbcTemplate".equals(((BeansSymbolAddOnInformation) info).getBeanID()))
                .count());

        assertEquals(1, addon.stream()
                .filter(info -> info instanceof BeansSymbolAddOnInformation)
                .filter(info -> "persistenceExceptionTranslationPostProcessor".equals(((BeansSymbolAddOnInformation) info).getBeanID()))
                .count());


        String beansOnClasspathDocUri = directory.toPath().resolve("src/main/resources/beans.xml").toUri().toString();
        assertTrue(SpringIndexerTest.containsSymbol(allSymbols, "@+ 'sb' SimpleBean", beansOnClasspathDocUri, 6, 14, 6, 21));

        addon = indexer.getAdditonalInformation(beansOnClasspathDocUri);
        assertEquals(1, addon.size());
        assertEquals("sb", ((BeansSymbolAddOnInformation) addon.get(0)).getBeanID());
    }

    @Test
    void testReindexXMLConfig() throws Exception {
        List<? extends WorkspaceSymbol> allSymbols = indexer.getAllSymbols("");
        assertEquals(5, allSymbols.size());

        indexer.configureIndexer(SymbolIndexConfig.builder()
                .scanXml(true)
                .build());
        allSymbols = indexer.getAllSymbols("");
        assertEquals(0, allSymbols.size());

        indexer.configureIndexer(SymbolIndexConfig.builder()
                .scanXml(true)
                .xmlScanFolders(new String[]{  "src/main"})
                .build());
        allSymbols = indexer.getAllSymbols("");
        assertEquals(1, allSymbols.size());

        indexer.configureIndexer(SymbolIndexConfig.builder()
                .scanXml(true)
                .xmlScanFolders(new String[]{"config", "src/main"})
                .build());
        allSymbols = indexer.getAllSymbols("");
        assertEquals(5, allSymbols.size());

        indexer.configureIndexer(SymbolIndexConfig.builder()
                .scanXml(true)
                .xmlScanFolders(new String[]{"config"})
                .build());
        allSymbols = indexer.getAllSymbols("");
        assertEquals(4, allSymbols.size());

        indexer.configureIndexer(SymbolIndexConfig.builder()
                .scanXml(false)
                .xmlScanFolders(new String[]{"config", "src/main"})
                .build());
        allSymbols = indexer.getAllSymbols("");
        assertEquals(0, allSymbols.size());

        indexer.configureIndexer(SymbolIndexConfig.builder()
                .scanXml(true)
                .xmlScanFolders(new String[]{"config", "src/main"})
                .build());
        allSymbols = indexer.getAllSymbols("");
        assertEquals(5, allSymbols.size());

        indexer.configureIndexer(SymbolIndexConfig.builder()
                .scanXml(true)
                .xmlScanFolders(new String[0])
                .build());
        allSymbols = indexer.getAllSymbols("");
        assertEquals(0, allSymbols.size());

        indexer.configureIndexer(SymbolIndexConfig.builder()
                .scanXml(true)
                .xmlScanFolders(new String[0])
                .build());
        allSymbols = indexer.getAllSymbols("    ");
        assertEquals(0, allSymbols.size());
    }

}
