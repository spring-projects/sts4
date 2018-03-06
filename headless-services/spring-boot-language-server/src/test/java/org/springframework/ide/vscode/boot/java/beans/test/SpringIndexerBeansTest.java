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
public class SpringIndexerBeansTest {

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
	public void testScanSimpleConfigurationClass() throws Exception {
		SpringIndexerHarness indexer = createIndexerHarness();
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-beans/").toURI());
		indexer.initialize(indexer.wsFolder(directory));

		String docUri = directory.toPath().resolve("src/main/java/org/test/SimpleConfiguration.java").toUri().toString();
		indexer.assertDocumentSymbols(docUri,
				symbol("@Configuration", "@+ 'simpleConfiguration' (@Configuration <: @Component) SimpleConfiguration"),
				symbol("@Bean", "@+ 'simpleBean' (@Bean) BeanClass")
		);
	}

	@Test public void testScanSpecialConfigurationClass() throws Exception {
		SpringIndexerHarness indexer = createIndexerHarness();
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-beans/").toURI());
		indexer.initialize(indexer.wsFolder(directory));

		String docUri = directory.toPath().resolve("src/main/java/org/test/SpecialConfiguration.java").toUri().toString();
		indexer.assertDocumentSymbols(docUri,
				symbol("@Configuration", "@+ 'specialConfiguration' (@Configuration <: @Component) SpecialConfiguration"),

				// @Bean("implicitNamedBean")
				symbol("implicitNamedBean", "@+ 'implicitNamedBean' (@Bean) BeanClass"),

				// @Bean(value="valueBean")
				symbol("valueBean", "@+ 'valueBean' (@Bean) BeanClass"),

				// @Bean(value= {"valueBean1", "valueBean2"})
				symbol("valueBean1", "@+ 'valueBean1' (@Bean) BeanClass"),
				symbol("valueBean2", "@+ 'valueBean2' (@Bean) BeanClass"),

				// @Bean(name="namedBean")
				symbol("namedBean", "@+ 'namedBean' (@Bean) BeanClass"),

				// @Bean(name= {"namedBean1", "namedBean2"})
				symbol("namedBean1", "@+ 'namedBean1' (@Bean) BeanClass"),
				symbol("namedBean2", "@+ 'namedBean2' (@Bean) BeanClass")
		);
	}

	@Test
	public void testScanAbstractBeanConfiguration() throws Exception {
		SpringIndexerHarness indexer = createIndexerHarness();
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-beans/").toURI());
		indexer.initialize(indexer.wsFolder(directory));

		String docUri = directory.toPath().resolve("src/main/java/org/test/AbstractBeanConfiguration.java").toUri().toString();
		indexer.assertDocumentSymbols(docUri,
				symbol("@Configuration", "@+ 'abstractBeanConfiguration' (@Configuration <: @Component) AbstractBeanConfiguration")
		);
	}

	@Test
	public void testScanSimpleComponentClass() throws Exception {
		SpringIndexerHarness indexer = createIndexerHarness();
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-beans/").toURI());
		indexer.initialize(indexer.wsFolder(directory));

		String docUri = directory.toPath().resolve("src/main/java/org/test/SimpleComponent.java").toUri().toString();
		indexer.assertDocumentSymbols(docUri,
				symbol("@Component", "@+ 'simpleComponent' (@Component) SimpleComponent")
		);
	}

	@Test public void testScanSimpleControllerClass() throws Exception {
		SpringIndexerHarness indexer = createIndexerHarness();
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-beans/").toURI());
		indexer.initialize(indexer.wsFolder(directory));

		String docUri = directory.toPath().resolve("src/main/java/org/test/SimpleController.java").toUri().toString();
		indexer.assertDocumentSymbols(docUri,
				symbol("@Controller", "@+ 'simpleController' (@Controller <: @Component) SimpleController")
		);
	}

	@Test public void testScanRestControllerClass() throws Exception {
		SpringIndexerHarness indexer = createIndexerHarness();
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-beans/").toURI());
		indexer.initialize(indexer.wsFolder(directory));

		String docUri = directory.toPath().resolve("src/main/java/org/test/SimpleRestController.java").toUri().toString();
		indexer.assertDocumentSymbols(docUri,
				symbol("@RestController", "@+ 'simpleRestController' (@RestController <: @Controller, @Component) SimpleRestController")
		);
	}


	////////////////////////////////
	// harness code
	
	private SpringIndexerHarness createIndexerHarness() {
		return new SpringIndexerHarness(harness.getServer(), harness.getServerParams(), symbolProviders);
	}

	private TestSymbolInfo symbol(String coveredText, String label) {
		return new TestSymbolInfo(coveredText, label);
	}
}
