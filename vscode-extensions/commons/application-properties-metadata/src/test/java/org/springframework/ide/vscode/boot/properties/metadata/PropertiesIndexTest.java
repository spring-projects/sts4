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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.ide.vscode.boot.properties.util.FuzzyMap;

/**
 * Sanity test the boot properties index 
 * 
 * @author Alex Boyko
 *
 */
public class PropertiesIndexTest {
	
	/**
	 * Build test project demo-1 if it's not built already
	 * @throws Exception
	 */
	@BeforeClass
	public static void initTestProject() throws Exception {
		Path testProjectPath = Paths.get(PropertiesIndexTest.class.getResource("/demo-1").toURI());
		if (!Files.exists(testProjectPath.resolve("classpath.txt"))) {
			testProjectPath.resolve("mvnw").toFile().setExecutable(true);
			int result = Runtime.getRuntime().exec("./mvnw clean package", null, testProjectPath.toFile()).waitFor();
			if (result != 0) {
				throw new RuntimeException("Failed to build test project");
			}
		}
	}
	
	@Test
	public void springStandardPropertyPresent() throws Exception {
		SpringPropertiesIndexManager indexManager = new SpringPropertiesIndexManager(ValueProviderRegistry.getDefault());
		FuzzyMap<PropertyInfo> index = indexManager.get(Paths.get(getClass().getResource("/demo-1").toURI()));
		PropertyInfo propertyInfo = index.get("server.port");
		assertNotNull(propertyInfo);
		assertEquals(Integer.class.getName(), propertyInfo.getType());
		assertEquals("port", propertyInfo.getName());
	}
	
	@Test
	public void customPropertyPresent() throws Exception {
		SpringPropertiesIndexManager indexManager = new SpringPropertiesIndexManager(ValueProviderRegistry.getDefault());
		FuzzyMap<PropertyInfo> index = indexManager.get(Paths.get(getClass().getResource("/demo-1").toURI()));
		PropertyInfo propertyInfo = index.get("demo.settings.user");
		assertNotNull(propertyInfo);
		assertEquals(String.class.getName(), propertyInfo.getType());
		assertEquals("user", propertyInfo.getName());
	}
	
	@Test
	public void propertyNotPresent() throws Exception {
		SpringPropertiesIndexManager indexManager = new SpringPropertiesIndexManager(ValueProviderRegistry.getDefault());
		FuzzyMap<PropertyInfo> index = indexManager.get(Paths.get(getClass().getResource("/demo-1").toURI()));
		PropertyInfo propertyInfo = index.get("my.server.port");
		assertNull(propertyInfo);
	}
	
}
