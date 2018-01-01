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
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;

/**
 * @author Martin Lippert
 */
public class SpringIndexerFunctionBeansTest {

	private AnnotationHierarchyAwareLookup<SymbolProvider> symbolProviders;
	private BootLanguageServerHarness harness;
	private JavaProjectFinder projectFinder;

	@Before
	public void setup() throws Exception {
		symbolProviders = new AnnotationHierarchyAwareLookup<>();
		symbolProviders.put(Annotations.BEAN, new BeansSymbolProvider());
		symbolProviders.put(Annotations.COMPONENT, new ComponentSymbolProvider());

		harness = BootLanguageServerHarness.builder().build();
		projectFinder = harness.getProjectFinder();
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-beans/").toURI()));
	}

	@Test
	public void testScanSimpleFunctionBean() throws Exception {
		SpringIndexerHarness indexer = new SpringIndexerHarness(harness.getServer(), projectFinder, symbolProviders);
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-beans/").toURI());
		indexer.initialize(indexer.wsFolder(directory));

		String uriPrefix = "file://" + directory.getAbsolutePath();
		indexer.assertDocumentSymbols(uriPrefix + "/src/main/java/org/test/FunctionClass.java",
				symbol("@Configuration", "@+ 'functionClass' (@Configuration <: @Component) FunctionClass"),
				symbol("@Bean", "@> 'uppercase' (@Bean) Function<String,String>")
		);
	}

	@Test
	public void testScanSimpleFunctionClass() throws Exception {
		SpringIndexerHarness indexer = new SpringIndexerHarness(harness.getServer(), projectFinder, symbolProviders);
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-beans/").toURI());
		indexer.initialize(indexer.wsFolder(directory));

		String uriPrefix = "file://" + directory.getAbsolutePath();
		indexer.assertDocumentSymbols(uriPrefix + "/src/main/java/org/test/ScannedFunctionClass.java",
				symbol("ScannedFunctionClass", "@> 'scannedFunctionClass' (@Bean) Function<String,String>")
		);
	}


	////////////////////////////////
	// harness code

	private TestSymbolInfo symbol(String coveredText, String label) {
		return new TestSymbolInfo(coveredText, label);
	}
}
