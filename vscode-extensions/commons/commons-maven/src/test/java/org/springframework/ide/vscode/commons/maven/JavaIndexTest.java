package org.springframework.ide.vscode.commons.maven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.springframework.ide.vscode.commons.java.IField;
import org.springframework.ide.vscode.commons.java.IMethod;
import org.springframework.ide.vscode.commons.java.IPrimitiveType;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.java.IVoidType;
import org.springframework.ide.vscode.commons.maven.java.MavenJavaProject;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class JavaIndexTest {
	
	private static LoadingCache<String, MavenJavaProject> projectsCache = CacheBuilder.newBuilder().build(new CacheLoader<String, MavenJavaProject>() {

		@Override
		public MavenJavaProject load(String projectName) throws Exception {
			Path testProjectPath = Paths.get(DependencyTreeTest.class.getResource("/" + projectName).toURI());
			MavenCore.buildMavenProject(testProjectPath);
			return new MavenJavaProject(testProjectPath.resolve(MavenCore.POM_XML).toFile());
		}
		
	});
	
	@Test
	public void findClassInJar() throws Exception {
		MavenJavaProject project = projectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file");
		IType type = project.findType("org.springframework.test.web.client.ExpectedCount");
		assertNotNull(type);
	}
	
	@Test
	public void findClassInOutputFolder() throws Exception {
		MavenJavaProject project = projectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file");
		IType type = project.findType("hello.Greeting");
		assertNotNull(type);
	}
	
	@Test
	public void classNotFound() throws Exception {
		MavenJavaProject project = projectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file");
		IType type = project.findType("hello.NonExistentClass");
		assertNull(type);
	}
	
	@Test
	public void voidMethodNoParams() throws Exception {
		MavenJavaProject project = projectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file");
		IType type = project.findType("java.util.ArrayList");
		assertNotNull(type);
		IMethod m = type.getMethod("clear", Stream.empty());
		assertEquals("clear", m.getElementName());
		assertEquals(IVoidType.DEFAULT, m.getReturnType());
		assertEquals(0, m.parameters().count());
	}
	
	@Test
	public void voidConstructor() throws Exception {
		MavenJavaProject project = projectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file");
		IType type = project.findType("java.util.ArrayList");
		assertNotNull(type);		
		IMethod m = type.getMethod("<init>", Stream.empty());
		assertEquals("<init>", m.getElementName());
		assertEquals(IVoidType.DEFAULT, m.getReturnType());
		assertEquals(0, m.parameters().count());
	}
	
	@Test
	public void constructorMethodWithParams() throws Exception {
		MavenJavaProject project = projectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file");
		IType type = project.findType("java.util.ArrayList");
		assertNotNull(type);		
		IMethod m = type.getMethod("<init>", Stream.of(IPrimitiveType.INT));
		assertEquals("<init>", m.getElementName());
		assertEquals(IVoidType.DEFAULT, m.getReturnType());
		assertEquals(Collections.singletonList(IPrimitiveType.INT), m.parameters().collect(Collectors.toList()));		
	}
	
	@Test
	public void testClassJavadocForOutputFolder() throws Exception {
		MavenJavaProject project = projectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file");
		IType type = project.findType("hello.Greeting");
		
		assertNotNull(type);	
		assertEquals("* Comment for Greeting class", type.getJavaDoc().raw().trim());
		
		IField field = type.getField("id");
		assertNotNull(field);
		assertEquals("* Comment for id field", field.getJavaDoc().raw().trim());
		
		IMethod method = type.getMethod("getId", Stream.empty());
		assertNotNull(method);
		assertEquals("* Comment for getId()", method.getJavaDoc().raw().trim());
	}

	@Test
	public void testInnerClassJavadocForOutputFolder() throws Exception {
		MavenJavaProject project = projectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file");
		IType type = project.findType("hello.Greeting$TestInnerClass");
		assertNotNull(type);
		assertEquals("* Comment for inner class", type.getJavaDoc().raw().trim());

		IField field = type.getField("innerField");
		assertNotNull(field);
		assertEquals("* Comment for inner field", field.getJavaDoc().raw().trim());

		IMethod method = type.getMethod("getInnerField", Stream.empty());
		assertNotNull(method);
		assertEquals("* Comment for method inside nested class", method.getJavaDoc().raw().trim());
	}

	@Test
	public void testClassJavadocForJar() throws Exception {
		MavenJavaProject project = projectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file");
		
		IType type = project.findType("org.springframework.boot.liquibase.LiquibaseServiceLocatorApplicationListener");
		assertNotNull(type);	
		String expectedPrefix = "* {@link ApplicationListener} that replaces the liquibase {@link ServiceLocator} with a";
		assertEquals(expectedPrefix, type.getJavaDoc().raw().trim().substring(0, expectedPrefix.length()));
		
		type = project.findType("org.springframework.boot.liquibase.LiquibaseServiceLocatorApplicationListener$LiquibasePresent");
		assertNotNull(type);	
		assertEquals("* Inner class to prevent class not found issues.", type.getJavaDoc().raw().trim());
	}
	
	@Test
	public void testFieldAndMethodJavadocForJar() throws Exception {
		MavenJavaProject project = projectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file");
		
		IType type = project.findType("org.springframework.boot.SpringApplication");
		assertNotNull(type);
		
		IField field = type.getField("BANNER_LOCATION_PROPERTY_VALUE");
		assertNotNull(field);
		assertEquals("* Default banner location.", field.getJavaDoc().raw().trim());
		
		IMethod method = type.getMethod("getListeners", Stream.empty());
		assertNotNull(method);
		String expectedPrefix = "* Returns read-only ordered Set of the {@link ApplicationListener}s that will be";
		assertEquals(expectedPrefix, method.getJavaDoc().raw().trim().substring(0, expectedPrefix.length()));
	}
	
}
