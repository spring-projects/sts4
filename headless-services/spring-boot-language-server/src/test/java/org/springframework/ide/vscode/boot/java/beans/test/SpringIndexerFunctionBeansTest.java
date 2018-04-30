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
package org.springframework.ide.vscode.boot.java.beans.test;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchyAwareLookup;
import org.springframework.ide.vscode.boot.java.beans.BeansSymbolProvider;
import org.springframework.ide.vscode.boot.java.beans.ComponentSymbolProvider;
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexer;
import org.springframework.ide.vscode.project.harness.BootJavaLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;

/**
 * @author Martin Lippert
 */
public class SpringIndexerFunctionBeansTest {

	private AnnotationHierarchyAwareLookup<SymbolProvider> symbolProviders;
	private BootJavaLanguageServerHarness harness;
	private SpringIndexer indexer;
	private File directory;

	@Before
	public void setup() throws Exception {
		symbolProviders = new AnnotationHierarchyAwareLookup<>();
		symbolProviders.put(Annotations.BEAN, new BeansSymbolProvider());
		symbolProviders.put(Annotations.COMPONENT, new ComponentSymbolProvider());

		harness = BootJavaLanguageServerHarness.builder().build();
		harness.intialize(null);
		
		indexer = harness.getServerWrapper().getComponents().getSpringIndexer();
		directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-beans/").toURI());

		String projectDir = directory.toURI().toString();

		// trigger project creation
		harness.getServerWrapper().getComponents().getProjectFinder().find(new TextDocumentIdentifier(projectDir)).get();

		CompletableFuture<Void> initProject = indexer.waitOperation();
		initProject.get(5, TimeUnit.SECONDS);
	}

	@Test
	public void testScanSimpleFunctionBean() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/FunctionClass.java").toUri().toString();
		SpringIndexerHarness.assertDocumentSymbols(indexer, docUri,
				SpringIndexerHarness.symbol("@Configuration", "@+ 'functionClass' (@Configuration <: @Component) FunctionClass"),
				SpringIndexerHarness.symbol("@Bean", "@> 'uppercase' (@Bean) Function<String,String>")
		);
	}

	@Test
	public void testScanSimpleFunctionClass() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/ScannedFunctionClass.java").toUri().toString();
		SpringIndexerHarness.assertDocumentSymbols(indexer, docUri,
				SpringIndexerHarness.symbol("ScannedFunctionClass", "@> 'scannedFunctionClass' Function<String,String>")
		);
	}

	@Test
	public void testScanSpecializedFunctionClass() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/FunctionFromSpecializedClass.java").toUri().toString();
		SpringIndexerHarness.assertDocumentSymbols(indexer, docUri,
				SpringIndexerHarness.symbol("FunctionFromSpecializedClass", "@> 'functionFromSpecializedClass' Function<String,String>")
		);
	}

	@Test
	public void testScanSpecializedFunctionInterface() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/FunctionFromSpecializedInterface.java").toUri().toString();
		SpringIndexerHarness.assertDocumentSymbols(indexer, docUri,
				SpringIndexerHarness.symbol("FunctionFromSpecializedInterface", "@> 'functionFromSpecializedInterface' Function<String,String>")
		);
	}

	@Test
	public void testNoSymbolForAbstractClasses() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/SpecializedFunctionClass.java").toUri().toString();
		SpringIndexerHarness.assertDocumentSymbols(indexer, docUri);
	}

	@Test
	public void testNoSymbolForSubInterfaces() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/SpecializedFunctionInterface.java").toUri().toString();
		SpringIndexerHarness.assertDocumentSymbols(indexer, docUri);
	}

	@Test
	public void testScanInconsistentInterfaceHierarchy() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/LoopedFunctionClass.java").toUri().toString();
		SpringIndexerHarness.assertDocumentSymbols(indexer, docUri);
	}

}
