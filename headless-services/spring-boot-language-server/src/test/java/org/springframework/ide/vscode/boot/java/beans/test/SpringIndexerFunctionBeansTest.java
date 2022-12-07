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
package org.springframework.ide.vscode.boot.java.beans.test;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.lsp4j.TextDocumentIdentifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.boot.bootiful.SymbolProviderTestConf;
import org.springframework.ide.vscode.boot.java.beans.BeansSymbolAddOnInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolAddOnInformation;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author Martin Lippert
 */
@ExtendWith(SpringExtension.class)
@BootLanguageServerTest
@Import(SymbolProviderTestConf.class)
public class SpringIndexerFunctionBeansTest {

	@Autowired private BootLanguageServerHarness harness;
	@Autowired private SpringSymbolIndex indexer;
	@Autowired private JavaProjectFinder projectFinder;

	private File directory;

	@BeforeEach
	public void setup() throws Exception {
		harness.intialize(null);

		directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-beans/").toURI());

		String projectDir = directory.toURI().toString();

		// trigger project creation
		projectFinder.find(new TextDocumentIdentifier(projectDir)).get();

		CompletableFuture<Void> initProject = indexer.waitOperation();
		initProject.get(5, TimeUnit.SECONDS);
	}

    @Test
    void testScanSimpleFunctionBean() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/org/test/FunctionClass.java").toUri().toString();
        SpringIndexerHarness.assertDocumentSymbols(indexer, docUri,
                SpringIndexerHarness.symbol("@Configuration", "@+ 'functionClass' (@Configuration <: @Component) FunctionClass"),
                SpringIndexerHarness.symbol("@Bean", "@> 'uppercase' (@Bean) Function<String,String>")
        );

        List<? extends SymbolAddOnInformation> addon = indexer.getAdditonalInformation(docUri);
        assertEquals(2, addon.size());

        assertEquals(1, addon.stream()
                .filter(info -> info instanceof BeansSymbolAddOnInformation)
                .filter(info -> "functionClass".equals(((BeansSymbolAddOnInformation) info).getBeanID()))
                .count());

        assertEquals(1, addon.stream()
                .filter(info -> info instanceof BeansSymbolAddOnInformation)
                .filter(info -> "uppercase".equals(((BeansSymbolAddOnInformation) info).getBeanID()))
                .count());
    }

    @Test
    void testScanSimpleFunctionClass() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/org/test/ScannedFunctionClass.java").toUri().toString();
        SpringIndexerHarness.assertDocumentSymbols(indexer, docUri,
                SpringIndexerHarness.symbol("ScannedFunctionClass", "@> 'scannedFunctionClass' Function<String,String>")
        );
    }

    @Test
    void testScanSpecializedFunctionClass() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/org/test/FunctionFromSpecializedClass.java").toUri().toString();
        SpringIndexerHarness.assertDocumentSymbols(indexer, docUri,
                SpringIndexerHarness.symbol("FunctionFromSpecializedClass", "@> 'functionFromSpecializedClass' Function<String,String>")
        );
    }

    @Test
    void testScanSpecializedFunctionInterface() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/org/test/FunctionFromSpecializedInterface.java").toUri().toString();
        SpringIndexerHarness.assertDocumentSymbols(indexer, docUri,
                SpringIndexerHarness.symbol("FunctionFromSpecializedInterface", "@> 'functionFromSpecializedInterface' Function<String,String>")
        );
    }

    @Test
    void testNoSymbolForAbstractClasses() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/org/test/SpecializedFunctionClass.java").toUri().toString();
        SpringIndexerHarness.assertDocumentSymbols(indexer, docUri);
    }

    @Test
    void testNoSymbolForSubInterfaces() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/org/test/SpecializedFunctionInterface.java").toUri().toString();
        SpringIndexerHarness.assertDocumentSymbols(indexer, docUri);
    }

}
