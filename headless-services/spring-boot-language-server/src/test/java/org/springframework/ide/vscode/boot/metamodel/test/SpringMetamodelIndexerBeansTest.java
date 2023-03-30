/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.metamodel.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.boot.bootiful.SymbolProviderTestConf;
import org.springframework.ide.vscode.boot.index.Bean;
import org.springframework.ide.vscode.boot.index.InjectionPoint;
import org.springframework.ide.vscode.boot.index.SpringMetamodelIndex;
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
public class SpringMetamodelIndexerBeansTest {

	@Autowired
	private BootLanguageServerHarness harness;
	@Autowired
	private JavaProjectFinder projectFinder;

	private File directory;
	@Autowired
	private SpringMetamodelIndex springIndex;
	@Autowired
	private SpringSymbolIndex indexer;

	@BeforeEach
	public void setup() throws Exception {
		harness.intialize(null);

		directory = new File(ProjectsHarness.class.getResource("/test-projects/test-spring-indexing/").toURI());

		String projectDir = directory.toURI().toString();

		// trigger project creation
		projectFinder.find(new TextDocumentIdentifier(projectDir)).get();

		CompletableFuture<Void> initProject = indexer.waitOperation();
		initProject.get(5, TimeUnit.SECONDS);
	}

	@Test
	void testSpringIndexExists() throws Exception {
		assertNotNull(springIndex);
	}

	@Test
	void testBeansNameAndTypeFromBeanAnnotatedMethod() {
		Bean[] beans = springIndex.getBeans("bean1");

		assertEquals(1, beans.length);
		assertEquals("bean1", beans[0].getName());
		assertEquals("org.test.BeanClass1", beans[0].getType());
	}

	@Test
	void testBeansNameAndTypeFromComponentAnnotatedClassExists() {
		Bean[] beans = springIndex.getBeans("constructorInjectionService");

		assertEquals(1, beans.length);
		assertEquals("constructorInjectionService", beans[0].getName());
		assertEquals("org.test.injections.ConstructorInjectionService", beans[0].getType());
	}

	@Test
	void testBeansNameAndTypeFromConfigurationAnnotatedClassExists() {
		Bean[] beans = springIndex.getBeans("configurationWithoutInjection");

		assertEquals(1, beans.length);
		assertEquals("configurationWithoutInjection", beans[0].getName());
		assertEquals("org.test.injections.ConfigurationWithoutInjection", beans[0].getType());
	}

	@Test
	void testBeansDefintionLocationFromBeanAnnotatedMethod() {
		Bean[] beans = springIndex.getBeans("bean1");

		String docUri = directory.toPath().resolve("src/main/java/org/test/MainClass.java").toUri().toString();
		Location location = new Location(docUri, new Range(new Position(13, 1), new Position(13, 6)));
		assertEquals(location, beans[0].getLocation());
	}

	@Test
	void testBeansDefintionLocationFromComponentAnnotatedClass() {
		Bean[] beans = springIndex.getBeans("constructorInjectionService");

		String docUri = directory.toPath().resolve("src/main/java/org/test/injections/ConstructorInjectionService.java").toUri().toString();
		Location location = new Location(docUri, new Range(new Position(6, 0), new Position(6, 8)));
		assertEquals(location, beans[0].getLocation());
	}

	@Test
	void testBeansDefinitionLocationFromConfigurationAnnotatedClass() {
		Bean[] beans = springIndex.getBeans("configurationWithoutInjection");
		assertEquals(1, beans.length);

		String docUri = directory.toPath().resolve("src/main/java/org/test/injections/ConfigurationWithoutInjection.java").toUri().toString();
		assertEquals(docUri, beans[0].getLocation().getUri());
	}
	
	@Test
	void testBeanNoInjectionPointsFromBeanAnnotatedMethod() {
		Bean[] beans = springIndex.getBeans("beanWithoutInjections");
		assertEquals(1, beans.length);

		InjectionPoint[] injectionPoints = beans[0].getInjectionPoints();
		assertEquals(0, injectionPoints.length);
	}
	
	@Test
	void testBeanInjectionPointsFromBeanAnnotatedMethod() {
		Bean[] beans = springIndex.getBeans("manualBeanWithConstructor");
		assertEquals(1, beans.length);

		String docUri = directory.toPath().resolve("src/main/java/org/test/injections/ConfigurationWithInjections.java").toUri().toString();

		InjectionPoint[] injectionPoints = beans[0].getInjectionPoints();
		assertEquals(2, injectionPoints.length);
		
		assertEquals("bean1", injectionPoints[0].getName());
		assertEquals("org.test.BeanClass1", injectionPoints[0].getType());
		Location ip1Location = new Location(docUri, new Range(new Position(12, 73), new Position(12, 78)));
		assertEquals(ip1Location, injectionPoints[0].getLocation());
		
		assertEquals("bean2", injectionPoints[1].getName());
		assertEquals("org.test.BeanClass2", injectionPoints[1].getType());
		Location ip2Location = new Location(docUri, new Range(new Position(12, 91), new Position(12, 96)));
		assertEquals(ip2Location, injectionPoints[1].getLocation());
	}

	@Test
	void testBeanInjectionPointsFromConstructor() {
		Bean[] beans = springIndex.getBeans("constructorInjectionService");
		assertEquals(1, beans.length);

		String docUri = directory.toPath().resolve("src/main/java/org/test/injections/ConstructorInjectionService.java").toUri().toString();

		InjectionPoint[] injectionPoints = beans[0].getInjectionPoints();
		assertEquals(2, injectionPoints.length);
		
		assertEquals("bean1", injectionPoints[0].getName());
		assertEquals("org.test.BeanClass1", injectionPoints[0].getType());
		Location ip1Location = new Location(docUri, new Range(new Position(12, 47), new Position(12, 52)));
		assertEquals(ip1Location, injectionPoints[0].getLocation());
		
		assertEquals("bean2", injectionPoints[1].getName());
		assertEquals("org.test.BeanClass2", injectionPoints[1].getType());
		Location ip2Location = new Location(docUri, new Range(new Position(12, 65), new Position(12, 70)));
		assertEquals(ip2Location, injectionPoints[1].getLocation());
	}

	@Test
	void testBeanInjectionPointsFromAutowiredFields() {
		Bean[] beans = springIndex.getBeans("autowiredInjectionService");
		assertEquals(1, beans.length);

		String docUri = directory.toPath().resolve("src/main/java/org/test/injections/AutowiredInjectionService.java").toUri().toString();

		InjectionPoint[] injectionPoints = beans[0].getInjectionPoints();
		assertEquals(2, injectionPoints.length);
		
		assertEquals("bean1", injectionPoints[0].getName());
		assertEquals("org.test.BeanClass1", injectionPoints[0].getType());
		Location ip1Location = new Location(docUri, new Range(new Position(10, 31), new Position(10, 36)));
		assertEquals(ip1Location, injectionPoints[0].getLocation());
		
		assertEquals("bean2", injectionPoints[1].getName());
		assertEquals("org.test.BeanClass2", injectionPoints[1].getType());
		Location ip2Location = new Location(docUri, new Range(new Position(11, 31), new Position(11, 36)));
		assertEquals(ip2Location, injectionPoints[1].getLocation());
	}

}
