/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.maven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.Test;
import org.springframework.ide.vscode.commons.java.IField;
import org.springframework.ide.vscode.commons.java.IMethod;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.maven.java.MavenJavaProject;
import org.springframework.ide.vscode.commons.maven.java.MavenProjectClasspath;
import org.springframework.ide.vscode.commons.maven.java.MavenProjectClasspath.JavadocProviderTypes;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public class SourceJavadocTest {
	
	private static Supplier<MavenJavaProject> projectSupplier = Suppliers.memoize(() -> {
		Path testProjectPath;
		try {
			MavenProjectClasspath.providerType = JavadocProviderTypes.JAVA_PARSER;
			testProjectPath = Paths.get(SourceJavadocTest.class.getResource("/gs-rest-service-cors-boot-1.4.1-with-classpath-file").toURI());
			return new MavenJavaProject(testProjectPath.resolve(MavenCore.POM_XML).toFile());
		} catch (Exception e) {
			return null;
		}
	});

	@Test
	public void parser_testClassJavadocForJar() throws Exception {
		MavenJavaProject project = projectSupplier.get();
		
		IType type = project.findType("org.springframework.boot.liquibase.LiquibaseServiceLocatorApplicationListener");
		assertNotNull(type);
		String expected = String.join("\n",
				"/**",
				" * {@link ApplicationListener} that replaces the liquibase {@link ServiceLocator} with a"
			);
		assertEquals(expected, type.getJavaDoc().raw().trim().substring(0, expected.length()));
		
		type = project.findType("org.springframework.boot.liquibase.LiquibaseServiceLocatorApplicationListener$LiquibasePresent");
		assertNotNull(type);
		expected = String.join("\n",
				"/**",
				"	 * Inner class to prevent class not found issues.",
				"	 */"
			);
		assertEquals(expected, type.getJavaDoc().raw().trim());
	}

	@Test
	public void parser_testClassJavadocForOutputFolder() throws Exception {
		MavenJavaProject project = projectSupplier.get();
		IType type = project.findType("hello.Greeting");
		
		assertNotNull(type);
		String expected = String.join("\n", 
				"/**",
				" * Comment for Greeting class ",
				" */"
			);
		assertEquals(expected, type.getJavaDoc().raw().trim());
		
		IField field = type.getField("id");
		assertNotNull(field);
		expected = String.join("\n",
				"/**",
				"     * Comment for id field",
				"     */"
			);
		assertEquals(expected, field.getJavaDoc().raw().trim());
		
		IMethod method = type.getMethod("getId", Stream.empty());
		assertNotNull(method);
		expected = String.join("\n",
				"/**",
				"     * Comment for getId()",
				"     */"
			);
		assertEquals(expected, method.getJavaDoc().raw().trim());
	}

	@Test
	public void parser_testFieldAndMethodJavadocForJar() throws Exception {
		MavenJavaProject project = projectSupplier.get();
		
		IType type = project.findType("org.springframework.boot.SpringApplication");
		assertNotNull(type);
		
		IField field = type.getField("BANNER_LOCATION_PROPERTY_VALUE");
		assertNotNull(field);
		String expected = String.join("\n",
				"/**",
				 "	 * Default banner location.",
				 "	 */"
			);
		assertEquals(expected, field.getJavaDoc().raw().trim());
		
		IMethod method = type.getMethod("getListeners", Stream.empty());
		assertNotNull(method);
		expected = String.join("\n",
				"/**",
				"	 * Returns read-only ordered Set of the {@link ApplicationListener}s that will be"
			);
		assertEquals(expected, method.getJavaDoc().raw().trim().substring(0, expected.length()));
	}

	@Test
	public void parser_testInnerClassJavadocForOutputFolder() throws Exception {
		MavenJavaProject project = projectSupplier.get();
		IType type = project.findType("hello.Greeting$TestInnerClass");
		assertNotNull(type);
		assertEquals("/**\n     * Comment for inner class\n     */", type.getJavaDoc().raw().trim());
	
		IField field = type.getField("innerField");
		assertNotNull(field);
		assertEquals("/**\n    \t * Comment for inner field\n    \t */", field.getJavaDoc().raw().trim());
	
		IMethod method = type.getMethod("getInnerField", Stream.empty());
		assertNotNull(method);
		assertEquals("/**\n    \t * Comment for method inside nested class\n    \t */", method.getJavaDoc().raw().trim());
	}

}
