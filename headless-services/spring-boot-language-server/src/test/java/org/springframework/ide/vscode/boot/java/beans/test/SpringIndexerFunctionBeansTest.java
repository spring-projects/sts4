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

import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchyAwareLookup;
import org.springframework.ide.vscode.boot.java.beans.BeansSymbolProvider;
import org.springframework.ide.vscode.boot.java.beans.ComponentSymbolProvider;
import org.springframework.ide.vscode.boot.java.beans.test.SpringIndexerHarness.TestSymbolInfo;
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
import org.springframework.ide.vscode.project.harness.BootJavaLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;

/**
 * @author Martin Lippert
 */
public class SpringIndexerFunctionBeansTest {

	private AnnotationHierarchyAwareLookup<SymbolProvider> symbolProviders;
	private BootJavaLanguageServerHarness harness;

	@Before
	public void setup() throws Exception {
		symbolProviders = new AnnotationHierarchyAwareLookup<>();
		symbolProviders.put(Annotations.BEAN, new BeansSymbolProvider());
		symbolProviders.put(Annotations.COMPONENT, new ComponentSymbolProvider());

		harness = BootJavaLanguageServerHarness.builder().build();
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-beans/").toURI()));
	}

	@Test
	public void testScanSimpleFunctionBean() throws Exception {
		SpringIndexerHarness indexer = createIndexerHarness();
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-beans/").toURI());
		indexer.initialize(indexer.wsFolder(directory));

		String docUri = directory.toPath().resolve("src/main/java/org/test/FunctionClass.java").toUri().toString();
		indexer.assertDocumentSymbols(docUri,
				symbol("@Configuration", "@+ 'functionClass' (@Configuration <: @Component) FunctionClass"),
				symbol("@Bean", "@> 'uppercase' (@Bean) Function<String,String>")
		);
	}

	@Test
	public void testScanSimpleFunctionClass() throws Exception {
		SpringIndexerHarness indexer = createIndexerHarness();
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-beans/").toURI());
		indexer.initialize(indexer.wsFolder(directory));

		String docUri = directory.toPath().resolve("src/main/java/org/test/ScannedFunctionClass.java").toUri().toString();
		indexer.assertDocumentSymbols(docUri,
				symbol("ScannedFunctionClass", "@> 'scannedFunctionClass' Function<String,String>")
		);
	}

	@Test
	public void testScanSpecializedFunctionClass() throws Exception {
		SpringIndexerHarness indexer = createIndexerHarness();
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-beans/").toURI());
		indexer.initialize(indexer.wsFolder(directory));

		String docUri = directory.toPath().resolve("src/main/java/org/test/FunctionFromSpecializedClass.java").toUri().toString();
		indexer.assertDocumentSymbols(docUri,
				symbol("FunctionFromSpecializedClass", "@> 'functionFromSpecializedClass' Function<String,String>")
		);
	}

	@Test
	public void testScanSpecializedFunctionInterface() throws Exception {
		SpringIndexerHarness indexer = createIndexerHarness();
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-beans/").toURI());
		indexer.initialize(indexer.wsFolder(directory));

		String docUri = directory.toPath().resolve("src/main/java/org/test/FunctionFromSpecializedInterface.java").toUri().toString();
		indexer.assertDocumentSymbols(docUri,
				symbol("FunctionFromSpecializedInterface", "@> 'functionFromSpecializedInterface' Function<String,String>")
		);
	}

	@Test
	public void testNoSymbolForAbstractClasses() throws Exception {
		SpringIndexerHarness indexer = createIndexerHarness();
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-beans/").toURI());
		indexer.initialize(indexer.wsFolder(directory));

		String docUri = directory.toPath().resolve("src/main/java/org/test/SpecializedFunctionClass.java").toUri().toString();
		indexer.assertDocumentSymbols(docUri);
	}

	@Test
	public void testNoSymbolForSubInterfaces() throws Exception {
		SpringIndexerHarness indexer = createIndexerHarness();
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-beans/").toURI());
		indexer.initialize(indexer.wsFolder(directory));

		String docUri = directory.toPath().resolve("src/main/java/org/test/SpecializedFunctionInterface.java").toUri().toString();
		indexer.assertDocumentSymbols(docUri);
	}

	@Test
	public void testScanInconsistentInterfaceHierarchy() throws Exception {
		SpringIndexerHarness indexer = createIndexerHarness();
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-beans/").toURI());
		indexer.initialize(indexer.wsFolder(directory));

		String docUri = directory.toPath().resolve("src/main/java/org/test/LoopedFunctionClass.java").toUri().toString();
		indexer.assertDocumentSymbols(docUri);
	}

	////////////////////////////////
	// harness code

	private TestSymbolInfo symbol(String coveredText, String label) {
		return new TestSymbolInfo(coveredText, label);
	}

	private SpringIndexerHarness createIndexerHarness() {
		return new SpringIndexerHarness(harness.getServer(), harness.getServerParams(), symbolProviders);
	}

}
