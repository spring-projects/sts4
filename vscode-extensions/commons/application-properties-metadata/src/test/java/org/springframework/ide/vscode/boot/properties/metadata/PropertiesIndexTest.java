/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.properties.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.springframework.ide.vscode.application.properties.metadata.PropertyInfo;
import org.springframework.ide.vscode.application.properties.metadata.SpringPropertiesIndexManager;
import org.springframework.ide.vscode.application.properties.metadata.ValueProviderRegistry;
import org.springframework.ide.vscode.application.properties.metadata.util.FuzzyMap;
import org.springframework.ide.vscode.languageserver.testharness.TestProjectHarness;

/**
 * Sanity test the boot properties index 
 * 
 * @author Alex Boyko
 *
 */
public class PropertiesIndexTest {
	
	private TestProjectHarness projects = new TestProjectHarness();
	
	@Test
	public void springStandardPropertyPresent() throws Exception {
		SpringPropertiesIndexManager indexManager = new SpringPropertiesIndexManager(ValueProviderRegistry.getDefault());
		FuzzyMap<PropertyInfo> index = indexManager.get(projects.mavenProject("demo-1"));
		PropertyInfo propertyInfo = index.get("server.port");
		assertNotNull(propertyInfo);
		assertEquals(Integer.class.getName(), propertyInfo.getType());
		assertEquals("port", propertyInfo.getName());
	}
	
	@Test
	public void customPropertyPresent() throws Exception {
		SpringPropertiesIndexManager indexManager = new SpringPropertiesIndexManager(ValueProviderRegistry.getDefault());
		FuzzyMap<PropertyInfo> index = indexManager.get(projects.mavenProject("demo-1"));
		PropertyInfo propertyInfo = index.get("demo.settings.user");
		assertNotNull(propertyInfo);
		assertEquals(String.class.getName(), propertyInfo.getType());
		assertEquals("user", propertyInfo.getName());
	}
	
	@Test
	public void propertyNotPresent() throws Exception {
		SpringPropertiesIndexManager indexManager = new SpringPropertiesIndexManager(ValueProviderRegistry.getDefault());
		FuzzyMap<PropertyInfo> index = indexManager.get(projects.mavenProject("demo-1"));
		PropertyInfo propertyInfo = index.get("my.server.port");
		assertNull(propertyInfo);
	}
	
}
