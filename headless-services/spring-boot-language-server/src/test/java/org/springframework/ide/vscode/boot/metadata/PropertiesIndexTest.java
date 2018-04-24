/*******************************************************************************
 * Copyright (c) 2016, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.ProgressService;
import org.springframework.ide.vscode.commons.util.FuzzyMap;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;

/**
 * Sanity test the boot properties index
 * 
 * @author Alex Boyko
 *
 */
public class PropertiesIndexTest {

	private static final String CUSTOM_PROPERTIES_PROJECT = "custom-properties-boot-project";
	
	private ProjectsHarness projects = ProjectsHarness.INSTANCE;
	private ProgressService progressService = (id, msg) -> { /*ignore*/ };
	
	@Test
	public void springStandardPropertyPresent_Maven() throws Exception {
		SpringPropertiesIndexManager indexManager = new SpringPropertiesIndexManager(
				ValueProviderRegistry.getDefault(), null);
		IJavaProject mavenProject = projects.mavenProject(CUSTOM_PROPERTIES_PROJECT);
		FuzzyMap<PropertyInfo> index = indexManager.get(mavenProject, progressService);
		PropertyInfo propertyInfo = index.get("server.port");
		assertNotNull(propertyInfo);
		assertEquals(Integer.class.getName(), propertyInfo.getType());
		assertEquals("port", propertyInfo.getName());
	}

	@Test
	public void customPropertyPresent_Maven() throws Exception {
		SpringPropertiesIndexManager indexManager = new SpringPropertiesIndexManager(
				ValueProviderRegistry.getDefault(), null);
		IJavaProject mavenProject = projects.mavenProject(CUSTOM_PROPERTIES_PROJECT);
		FuzzyMap<PropertyInfo> index = indexManager.get(mavenProject, progressService);
		PropertyInfo propertyInfo = index.get("demo.settings.user");
		assertNotNull(propertyInfo);
		assertEquals(String.class.getName(), propertyInfo.getType());
		assertEquals("user", propertyInfo.getName());
	}

	@Test
	public void propertyNotPresent_Maven() throws Exception {
		SpringPropertiesIndexManager indexManager = new SpringPropertiesIndexManager(
				ValueProviderRegistry.getDefault(), null);
		IJavaProject mavenProject = projects.mavenProject(CUSTOM_PROPERTIES_PROJECT);
		FuzzyMap<PropertyInfo> index = indexManager.get(mavenProject, progressService);
		PropertyInfo propertyInfo = index.get("my.server.port");
		assertNull(propertyInfo);
	}

	@Test @Ignore //ignore because classpath file is going to disapear and pieces already removed
	public void springStandardPropertyPresent_ClasspathFile() throws Exception {
		SpringPropertiesIndexManager indexManager = new SpringPropertiesIndexManager(
				ValueProviderRegistry.getDefault(), null);
		IJavaProject classpathFileProject = projects.javaProjectWithClasspathFile(CUSTOM_PROPERTIES_PROJECT);
		FuzzyMap<PropertyInfo> index = indexManager.get(classpathFileProject, progressService);
		PropertyInfo propertyInfo = index.get("server.port");
		assertNotNull(propertyInfo);
		assertEquals(Integer.class.getName(), propertyInfo.getType());
		assertEquals("port", propertyInfo.getName());
	}

	@Test @Ignore //ignore because classpath file is going to disapear and pieces already removed
	public void customPropertyPresent_ClasspathFile() throws Exception {
		SpringPropertiesIndexManager indexManager = new SpringPropertiesIndexManager(
				ValueProviderRegistry.getDefault(), null);
		IJavaProject classpathFileProject = projects.javaProjectWithClasspathFile(CUSTOM_PROPERTIES_PROJECT);
		FuzzyMap<PropertyInfo> index = indexManager.get(classpathFileProject, progressService);
		PropertyInfo propertyInfo = index.get("demo.settings.user");
		assertNotNull(propertyInfo);
		assertEquals(String.class.getName(), propertyInfo.getType());
		assertEquals("user", propertyInfo.getName());
	}

	@Test @Ignore //ignore because classpath file is going to disapear and pieces already removed
	public void propertyNotPresent_ClasspathFile() throws Exception {
		SpringPropertiesIndexManager indexManager = new SpringPropertiesIndexManager(
				ValueProviderRegistry.getDefault(), null);
		IJavaProject classpathFileProject = projects.javaProjectWithClasspathFile(CUSTOM_PROPERTIES_PROJECT);
		FuzzyMap<PropertyInfo> index = indexManager.get(classpathFileProject, progressService);
		PropertyInfo propertyInfo = index.get("my.server.port");
		assertNull(propertyInfo);
	}
}
