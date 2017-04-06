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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.springframework.ide.vscode.commons.java.Flags;
import org.springframework.ide.vscode.commons.java.IMethod;
import org.springframework.ide.vscode.commons.java.IPrimitiveType;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.java.IVoidType;
import org.springframework.ide.vscode.commons.maven.java.MavenJavaProject;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import reactor.util.function.Tuple2;

public class JavaIndexTest {
	
	private static LoadingCache<String, MavenJavaProject> mavenProjectsCache = CacheBuilder.newBuilder().build(new CacheLoader<String, MavenJavaProject>() {

		@Override
		public MavenJavaProject load(String projectName) throws Exception {
			Path testProjectPath = Paths.get(DependencyTreeTest.class.getResource("/" + projectName).toURI());
			MavenBuilder.newBuilder(testProjectPath).clean().pack().javadoc().skipTests().execute();
			return new MavenJavaProject(MavenCore.getDefault(), testProjectPath.resolve(MavenCore.POM_XML).toFile());
		}
		
	});
	
	@Test
	public void fuzzySearchNoFilter() throws Exception {
		List<Tuple2<IType, Double>> results = MavenCore.getDefault().getJavaIndexForJreLibs()
				.fuzzySearchTypes("util.Map", null)
				.collectSortedList((o1, o2) -> o2.getT2().compareTo(o1.getT2()))
				.block();
		assertTrue(results.size() > 10);
		assertEquals("java.util.Map", results.get(0).getT1().getFullyQualifiedName());
	}
	
	@Test
	public void fuzzySearchWithFilter() throws Exception {
		List<Tuple2<IType, Double>> results = MavenCore.getDefault().getJavaIndexForJreLibs()
				.fuzzySearchTypes("util.Map", (type) -> Flags.isPrivate(type.getFlags()))
				.collectSortedList((o1, o2) -> o2.getT2().compareTo(o1.getT2()))
				.block();
		assertTrue(results.size() > 10);
		assertEquals("java.util.EnumMap$KeySet", results.get(0).getT1().getFullyQualifiedName());
	}
	
	@Test
	public void fuzzySearchPackage() throws Exception {
		List<Tuple2<String, Double>> results = MavenCore.getDefault().getJavaIndexForJreLibs()
				.fuzzySearchPackages("util")
				.collectSortedList((o1, o2) -> o2.getT2().compareTo(o1.getT2()))
				.block();
		assertTrue(results.size() > 10);
		assertEquals("java.util", results.get(0).getT1());
	}
	
	@Test
	public void findClassInJar() throws Exception {
		MavenJavaProject project = mavenProjectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file");
		IType type = project.getClasspath().findType("org.springframework.test.web.client.ExpectedCount");
		assertNotNull(type);
	}
	
	@Test
	public void findClassInOutputFolder() throws Exception {
		MavenJavaProject project = mavenProjectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file");
		IType type = project.getClasspath().findType("hello.Greeting");
		assertNotNull(type);
	}
	
	@Test
	public void classNotFound() throws Exception {
		MavenJavaProject project = mavenProjectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file");
		IType type = project.getClasspath().findType("hello.NonExistentClass");
		assertNull(type);
	}
	
	@Test
	public void voidMethodNoParams() throws Exception {
		MavenJavaProject project = mavenProjectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file");
		IType type = project.getClasspath().findType("java.util.ArrayList");
		assertNotNull(type);
		IMethod m = type.getMethod("clear", Stream.empty());
		assertEquals("clear", m.getElementName());
		assertEquals(IVoidType.DEFAULT, m.getReturnType());
		assertEquals(0, m.parameters().count());
	}
	
	@Test
	public void voidConstructor() throws Exception {
		MavenJavaProject project = mavenProjectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file");
		IType type = project.getClasspath().findType("java.util.ArrayList");
		assertNotNull(type);		
		IMethod m = type.getMethod("<init>", Stream.empty());
		assertEquals(type.getElementName(), m.getElementName());
		assertEquals(IVoidType.DEFAULT, m.getReturnType());
		assertEquals(0, m.parameters().count());
	}
	
	@Test
	public void constructorMethodWithParams() throws Exception {
		MavenJavaProject project = mavenProjectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file");
		IType type = project.getClasspath().findType("java.util.ArrayList");
		assertNotNull(type);		
		IMethod m = type.getMethod("<init>", Stream.of(IPrimitiveType.INT));
		assertEquals(m.getDeclaringType().getElementName(), m.getElementName());
		assertEquals(IVoidType.DEFAULT, m.getReturnType());
		assertEquals(Collections.singletonList(IPrimitiveType.INT), m.parameters().collect(Collectors.toList()));		
	}
}
