/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.commons.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.lsp4j.MarkupContent;
import org.junit.After;
import org.junit.Test;
import org.springframework.ide.vscode.commons.protocol.java.JavaDataParams;
import org.springframework.tooling.ls.eclipse.commons.STS4LanguageClientImpl;

public class JavadocTest {
	
	private static STS4LanguageClientImpl client = new STS4LanguageClientImpl();
	
	@After
	public void tearDown() throws Exception {
		TestUtils.deleteAllProjects();
	}
	
	@Test
	public void classJavadoc() throws Exception {
		IJavaProject project = TestUtils.createTestProject("simple-java-project");
		assertNotNull(project);
		IType type = project.findType("com.sample.SampleJavadoc");
		assertNotNull(type);
		
		String expectedBindingKey = "Lcom/sample/SampleJavadoc;";
		assertEquals(expectedBindingKey, type.getKey());
		
		JavaDataParams params = new JavaDataParams(project.getProject().getLocation().toFile().toURI().toString(), expectedBindingKey, false);
		MarkupContent response = client.javadoc(params).get(1, TimeUnit.SECONDS);
		assertEquals("**Sample class**", response.getValue());
	}
	
	@Test
	public void fieldJavadoc() throws Exception {
		IJavaProject project = TestUtils.createTestProject("simple-java-project");
		assertNotNull(project);
		IType type = project.findType("com.sample.SampleJavadoc");
		assertNotNull(type);
		IField field = type.getField("number");
		assertNotNull(field);
		
		String expectedBindingKey = "Lcom/sample/SampleJavadoc;.number";
		assertEquals(expectedBindingKey, field.getKey());
		
		JavaDataParams params = new JavaDataParams(project.getProject().getLocation().toFile().toURI().toString(), expectedBindingKey, false);
		MarkupContent response = client.javadoc(params).get(1, TimeUnit.SECONDS);
		assertEquals("**Sample field**", response.getValue());
	}

	@Test
	public void methodJavadoc() throws Exception {
		IJavaProject project = TestUtils.createTestProject("simple-java-project");
		assertNotNull(project);
		IType type = project.findType("com.sample.SampleJavadoc");
		assertNotNull(type);
		IMethod method = type.getMethod("getNumber", new String[0]);
		assertNotNull(method);
		
		String expectedBindingKey = "Lcom/sample/SampleJavadoc;.getNumber()V";
		assertEquals(expectedBindingKey, method.getKey());
		
		JavaDataParams params = new JavaDataParams(project.getProject().getLocation().toFile().toURI().toString(), expectedBindingKey, false);
		MarkupContent response = client.javadoc(params).get(1, TimeUnit.SECONDS);
		assertEquals("**Sample getter**", response.getValue());
	}
	
}
