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
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.project.harness.BootJavaLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;

/**
 * @author Martin Lippert
 */
public class SpringIndexerBeansTest {

	private AnnotationHierarchyAwareLookup<SymbolProvider> symbolProviders;
	private BootJavaLanguageServerHarness harness;
	private File directory;
	private SpringIndexer indexer;

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
		IJavaProject project = harness.getServerWrapper().getComponents().getProjectFinder().find(new TextDocumentIdentifier(projectDir)).get();

		CompletableFuture<Void> initProject = indexer.initializeProject(project);
		initProject.get(5, TimeUnit.SECONDS);
	}

	@Test
	public void testScanSimpleConfigurationClass() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/SimpleConfiguration.java").toUri().toString();
		SpringIndexerHarness.assertDocumentSymbols(indexer, docUri,
				SpringIndexerHarness.symbol("@Configuration", "@+ 'simpleConfiguration' (@Configuration <: @Component) SimpleConfiguration"),
				SpringIndexerHarness.symbol("@Bean", "@+ 'simpleBean' (@Bean) BeanClass")
		);
	}

	@Test public void testScanSpecialConfigurationClass() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/SpecialConfiguration.java").toUri().toString();
		SpringIndexerHarness.assertDocumentSymbols(indexer, docUri,
				SpringIndexerHarness.symbol("@Configuration", "@+ 'specialConfiguration' (@Configuration <: @Component) SpecialConfiguration"),

				// @Bean("implicitNamedBean")
				SpringIndexerHarness.symbol("implicitNamedBean", "@+ 'implicitNamedBean' (@Bean) BeanClass"),

				// @Bean(value="valueBean")
				SpringIndexerHarness.symbol("valueBean", "@+ 'valueBean' (@Bean) BeanClass"),

				// @Bean(value= {"valueBean1", "valueBean2"})
				SpringIndexerHarness.symbol("valueBean1", "@+ 'valueBean1' (@Bean) BeanClass"),
				SpringIndexerHarness.symbol("valueBean2", "@+ 'valueBean2' (@Bean) BeanClass"),

				// @Bean(name="namedBean")
				SpringIndexerHarness.symbol("namedBean", "@+ 'namedBean' (@Bean) BeanClass"),

				// @Bean(name= {"namedBean1", "namedBean2"})
				SpringIndexerHarness.symbol("namedBean1", "@+ 'namedBean1' (@Bean) BeanClass"),
				SpringIndexerHarness.symbol("namedBean2", "@+ 'namedBean2' (@Bean) BeanClass")
		);
	}

	@Test
	public void testScanAbstractBeanConfiguration() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/AbstractBeanConfiguration.java").toUri().toString();
		SpringIndexerHarness.assertDocumentSymbols(indexer, docUri,
				SpringIndexerHarness.symbol("@Configuration", "@+ 'abstractBeanConfiguration' (@Configuration <: @Component) AbstractBeanConfiguration")
		);
	}

	@Test
	public void testScanSimpleComponentClass() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/SimpleComponent.java").toUri().toString();
		SpringIndexerHarness.assertDocumentSymbols(indexer, docUri,
				SpringIndexerHarness.symbol("@Component", "@+ 'simpleComponent' (@Component) SimpleComponent")
		);
	}

	@Test
	public void testScanSimpleControllerClass() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/SimpleController.java").toUri().toString();
		SpringIndexerHarness.assertDocumentSymbols(indexer, docUri,
				SpringIndexerHarness.symbol("@Controller", "@+ 'simpleController' (@Controller <: @Component) SimpleController")
		);
	}

	@Test
	public void testScanRestControllerClass() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/SimpleRestController.java").toUri().toString();
		SpringIndexerHarness.assertDocumentSymbols(indexer, docUri,
				SpringIndexerHarness.symbol("@RestController", "@+ 'simpleRestController' (@RestController <: @Controller, @Component) SimpleRestController")
		);
	}

}
