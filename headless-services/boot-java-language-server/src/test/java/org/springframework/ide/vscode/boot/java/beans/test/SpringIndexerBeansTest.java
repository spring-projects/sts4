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
package org.springframework.ide.vscode.boot.java.beans.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.lsp4j.SymbolInformation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchyAwareFactoryManager;
import org.springframework.ide.vscode.boot.java.beans.BeansSymbolProvider;
import org.springframework.ide.vscode.boot.java.beans.ComponentSymbolProvider;
import org.springframework.ide.vscode.boot.java.beans.test.SpringIndexerHarness.TestSymbolInfo;
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexer;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.multiroot.WorkspaceFolder;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;

import com.google.common.collect.ImmutableList;

/**
 * @author Martin Lippert
 */
public class SpringIndexerBeansTest {

	private AnnotationHierarchyAwareFactoryManager<SymbolProvider> symbolProviders;
	private BootLanguageServerHarness harness;
	private JavaProjectFinder projectFinder;

	@Before
	public void setup() throws Exception {
		symbolProviders = new AnnotationHierarchyAwareFactoryManager<>();
		symbolProviders.put(Annotations.BEAN, new BeansSymbolProvider());
		symbolProviders.putFactory(Annotations.COMPONENT, ComponentSymbolProvider::new);

		harness = BootLanguageServerHarness.builder().build();
		projectFinder = harness.getProjectFinder();
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-beans/").toURI()));
	}

	@Test
	public void testScanSimpleConfigurationClass() throws Exception {
		SpringIndexerHarness indexer = new SpringIndexerHarness(harness.getServer(), projectFinder, symbolProviders);
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-beans/").toURI());
		indexer.initialize(indexer.wsFolder(directory));

		String uriPrefix = "file://" + directory.getAbsolutePath();
		indexer.assertDocumentSymbols(uriPrefix + "/src/main/java/org/test/SimpleConfiguration.java",
				symbol("@Configuration", "@+ 'simpleConfiguration' (@+Component) SimpleConfiguration"),
				symbol("@Configuration", "@+ 'simpleConfiguration' (@+Configuration) SimpleConfiguration"),
				symbol("@Configuration", "@+ 'simpleConfiguration' (@Configuration) SimpleConfiguration"),
				symbol("@Bean", "@+ 'simpleBean' (@Bean) BeanClass")
		);
	}

	@Test public void testScanSpecialConfigurationClass() throws Exception {
		SpringIndexerHarness indexer = new SpringIndexerHarness(harness.getServer(), projectFinder, symbolProviders);
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-beans/").toURI());
		indexer.initialize(indexer.wsFolder(directory));

		String uriPrefix = "file://" + directory.getAbsolutePath();
		String docUri = uriPrefix + "/src/main/java/org/test/SpecialConfiguration.java";
		indexer.assertDocumentSymbols(docUri,
				symbol("@Configuration", "@+ 'specialConfiguration' (@+Component) SpecialConfiguration"),
				symbol("@Configuration", "@+ 'specialConfiguration' (@+Configuration) SpecialConfiguration"),
				symbol("@Configuration", "@+ 'specialConfiguration' (@Configuration) SpecialConfiguration"),

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
	public void testScanSimpleFunctionBean() throws Exception {
		SpringIndexerHarness indexer = new SpringIndexerHarness(harness.getServer(), projectFinder, symbolProviders);
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-beans/").toURI());
		indexer.initialize(indexer.wsFolder(directory));

		String uriPrefix = "file://" + directory.getAbsolutePath();
		indexer.assertDocumentSymbols(uriPrefix + "/src/main/java/org/test/FunctionClass.java",
				symbol("@Configuration", "@+ 'functionClass' (@+Component) FunctionClass"),
				symbol("@Configuration", "@+ 'functionClass' (@+Configuration) FunctionClass"),
				symbol("@Configuration", "@+ 'functionClass' (@Configuration) FunctionClass"),

				symbol("@Bean", "@> 'uppercase' (@Bean) Function<String,String>")
		);
	}

	@Test
	public void testScanSimpleComponentClass() throws Exception {
		SpringIndexerHarness indexer = new SpringIndexerHarness(harness.getServer(), projectFinder, symbolProviders);
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-beans/").toURI());
		indexer.initialize(indexer.wsFolder(directory));

		String uriPrefix = "file://" + directory.getAbsolutePath();
		indexer.assertDocumentSymbols(uriPrefix + "/src/main/java/org/test/SimpleComponent.java",
				symbol("@Component", "@+ 'simpleComponent' (@+Component) SimpleComponent"),
				symbol("@Component", "@+ 'simpleComponent' (@Component) SimpleComponent")
		);
//		List<? extends SymbolInformation> symbols = indexer.getSymbols(uriPrefix + "/src/main/java/org/test/SimpleComponent.java");
//		assertEquals(1, symbols.size());
//		assertTrue(containsSymbol(symbols, "@+ 'simpleComponent' (@Component) SimpleComponent", uriPrefix + "/src/main/java/org/test/SimpleComponent.java", 4, 0, 4, 10));
	}

	@Test public void testScanSimpleControllerClass() throws Exception {
		SpringIndexerHarness indexer = new SpringIndexerHarness(harness.getServer(), projectFinder, symbolProviders);
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-beans/").toURI());
		indexer.initialize(indexer.wsFolder(directory));

		String uriPrefix = "file://" + directory.getAbsolutePath();
		String docUri = uriPrefix + "/src/main/java/org/test/SimpleController.java";
		indexer.assertDocumentSymbols(docUri,
				symbol("@Controller", "@+ 'simpleController' (@+Component) SimpleController"),
				symbol("@Controller", "@+ 'simpleController' (@+Controller) SimpleController"),
				symbol("@Controller", "@+ 'simpleController' (@Controller) SimpleController")
		);
	}

	@Test public void testScanRestControllerClass() throws Exception {
		SpringIndexerHarness indexer = new SpringIndexerHarness(harness.getServer(), projectFinder, symbolProviders);
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-beans/").toURI());
		indexer.initialize(indexer.wsFolder(directory));

		String uriPrefix = "file://" + directory.getAbsolutePath();
		String docUri = uriPrefix + "/src/main/java/org/test/SimpleRestController.java";
		indexer.assertDocumentSymbols(docUri,
				symbol("@RestController", "@+ 'simpleRestController' (@+Component) SimpleRestController"),
				symbol("@RestController", "@+ 'simpleRestController' (@+Controller) SimpleRestController"),
				symbol("@RestController", "@+ 'simpleRestController' (@+RestController) SimpleRestController"),
				symbol("@RestController", "@+ 'simpleRestController' (@RestController) SimpleRestController")
		);
	}


	////////////////////////////////
	// harness code

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

	private TestSymbolInfo symbol(String coveredText, String label) {
		return new TestSymbolInfo(coveredText, label);
	}
}
