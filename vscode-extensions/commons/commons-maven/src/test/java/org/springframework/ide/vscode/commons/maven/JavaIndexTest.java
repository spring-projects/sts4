package org.springframework.ide.vscode.commons.maven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.nio.file.Files;
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
import org.springframework.ide.vscode.commons.maven.java.MavenProjectClasspath;
import org.springframework.ide.vscode.commons.maven.java.MavenProjectClasspath.JavadocProviderTypes;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class JavaIndexTest {
	
	private static LoadingCache<String, Path> projectsCache = CacheBuilder.newBuilder().build(new CacheLoader<String, Path>() {

		@Override
		public Path load(String projectName) throws Exception {
			Path testProjectPath = Paths.get(DependencyTreeTest.class.getResource("/" + projectName).toURI());
			MavenBuilder.newBuilder(testProjectPath).clean().pack().javadoc().skipTests().execute();
			return testProjectPath;
		}
		
	});
	
	private static LoadingCache<String, MavenJavaProject> mavenProjectsCache = CacheBuilder.newBuilder().build(new CacheLoader<String, MavenJavaProject>() {

		@Override
		public MavenJavaProject load(String projectName) throws Exception {
			Path testProjectPath = Paths.get(DependencyTreeTest.class.getResource("/" + projectName).toURI());
			return createMavenProject(testProjectPath);
		}
		
	});
	
	private static MavenJavaProject createMavenProject(Path projectPath) throws Exception {
		return new MavenJavaProject(projectPath.resolve(MavenCore.POM_XML).toFile());
	}
	
	@Test
	public void findClassInJar() throws Exception {
		MavenJavaProject project = mavenProjectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file");
		IType type = project.findType("org.springframework.test.web.client.ExpectedCount");
		assertNotNull(type);
	}
	
	@Test
	public void findClassInOutputFolder() throws Exception {
		MavenJavaProject project = mavenProjectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file");
		IType type = project.findType("hello.Greeting");
		assertNotNull(type);
	}
	
	@Test
	public void classNotFound() throws Exception {
		MavenJavaProject project = mavenProjectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file");
		IType type = project.findType("hello.NonExistentClass");
		assertNull(type);
	}
	
	@Test
	public void voidMethodNoParams() throws Exception {
		MavenJavaProject project = mavenProjectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file");
		IType type = project.findType("java.util.ArrayList");
		assertNotNull(type);
		IMethod m = type.getMethod("clear", Stream.empty());
		assertEquals("clear", m.getElementName());
		assertEquals(IVoidType.DEFAULT, m.getReturnType());
		assertEquals(0, m.parameters().count());
	}
	
	@Test
	public void voidConstructor() throws Exception {
		MavenJavaProject project = mavenProjectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file");
		IType type = project.findType("java.util.ArrayList");
		assertNotNull(type);		
		IMethod m = type.getMethod("<init>", Stream.empty());
		assertEquals(type.getElementName(), m.getElementName());
		assertEquals(IVoidType.DEFAULT, m.getReturnType());
		assertEquals(0, m.parameters().count());
	}
	
	@Test
	public void constructorMethodWithParams() throws Exception {
		MavenJavaProject project = mavenProjectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file");
		IType type = project.findType("java.util.ArrayList");
		assertNotNull(type);		
		IMethod m = type.getMethod("<init>", Stream.of(IPrimitiveType.INT));
		assertEquals(m.getDeclaringType().getElementName(), m.getElementName());
		assertEquals(IVoidType.DEFAULT, m.getReturnType());
		assertEquals(Collections.singletonList(IPrimitiveType.INT), m.parameters().collect(Collectors.toList()));		
	}
	
	@Test
	public void parser_testClassJavadocForOutputFolder() throws Exception {
		MavenProjectClasspath.providerType = JavadocProviderTypes.JAVA_PARSER;
		MavenJavaProject project = createMavenProject(projectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file"));
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
	public void parser_testInnerClassJavadocForOutputFolder() throws Exception {
		MavenProjectClasspath.providerType = JavadocProviderTypes.JAVA_PARSER;
		MavenJavaProject project = createMavenProject(projectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file"));
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

	@Test
	public void parser_testClassJavadocForJar() throws Exception {
		MavenProjectClasspath.providerType = JavadocProviderTypes.JAVA_PARSER;
		MavenJavaProject project = createMavenProject(projectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file"));
		
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
	public void parser_testFieldAndMethodJavadocForJar() throws Exception {
		MavenProjectClasspath.providerType = JavadocProviderTypes.JAVA_PARSER;
		MavenJavaProject project = createMavenProject(projectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file"));
		
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
	public void roaster_testClassJavadocForOutputFolder() throws Exception {
		MavenProjectClasspath.providerType = JavadocProviderTypes.ROASTER;
		MavenJavaProject project = createMavenProject(projectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file"));
		IType type = project.findType("hello.Greeting");
		
		assertNotNull(type);
		assertEquals("Comment for Greeting class", type.getJavaDoc().raw());
		
		IField field = type.getField("id");
		assertNotNull(field);
		assertEquals("Comment for id field", field.getJavaDoc().raw());
		
		IMethod method = type.getMethod("getId", Stream.empty());
		assertNotNull(method);
		assertEquals("Comment for getId()", method.getJavaDoc().raw());
	}

	@Test
	public void roaster_testInnerClassJavadocForOutputFolder() throws Exception {
		MavenProjectClasspath.providerType = JavadocProviderTypes.ROASTER;
		MavenJavaProject project = createMavenProject(projectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file"));
		IType type = project.findType("hello.Greeting$TestInnerClass");
		assertNotNull(type);
		assertEquals("Comment for inner class", type.getJavaDoc().raw());

		IField field = type.getField("innerField");
		assertNotNull(field);
		assertEquals("Comment for inner field", field.getJavaDoc().raw());

		IMethod method = type.getMethod("getInnerField", Stream.empty());
		assertNotNull(method);
		assertEquals("Comment for method inside nested class", method.getJavaDoc().raw());
	}

	@Test
	public void roaster_testClassJavadocForJar() throws Exception {
		MavenProjectClasspath.providerType = JavadocProviderTypes.ROASTER;

		MavenJavaProject project = createMavenProject(projectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file"));
		
		IType type = project.findType("org.springframework.boot.liquibase.LiquibaseServiceLocatorApplicationListener");
		assertNotNull(type);
		String expected = "{@link ApplicationListener}  that replaces the liquibase  {@link ServiceLocator}  with a";
		assertEquals(expected, type.getJavaDoc().raw().substring(0, expected.length()));
		
		type = project.findType("org.springframework.boot.liquibase.LiquibaseServiceLocatorApplicationListener$LiquibasePresent");
		assertNotNull(type);
		assertEquals("Inner class to prevent class not found issues.", type.getJavaDoc().raw());
	}
	
	@Test
	public void roaster_testFieldAndMethodJavadocForJar() throws Exception {
		MavenProjectClasspath.providerType = JavadocProviderTypes.ROASTER;

		MavenJavaProject project = createMavenProject(projectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file"));
		
		IType type = project.findType("org.springframework.boot.SpringApplication");
		assertNotNull(type);
		
		IField field = type.getField("BANNER_LOCATION_PROPERTY_VALUE");
		assertNotNull(field);
		assertEquals("Default banner location.", field.getJavaDoc().raw());
		
		IMethod method = type.getMethod("getListeners", Stream.empty());
		assertNotNull(method);
		String expected = "Returns read-only ordered Set of the  {@link ApplicationListener} s that will be";
		assertEquals(expected, method.getJavaDoc().raw().substring(0, expected.length()));
	}

	
	@Test
	public void html_testClassJavadoc() throws Exception {
		MavenProjectClasspath.providerType = JavadocProviderTypes.HTML;

		MavenJavaProject project = createMavenProject(projectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file"));
		
		IType type = project.findType("java.util.Map");
		assertNotNull(type);
		String expected = String.join("\n",
				"<div class=\"block\">An object that maps keys to values.  A map cannot contain duplicate keys;",
				" each key can map to at most one value."
				);
		assertEquals(expected, type.getJavaDoc().getRenderable().toHtml().substring(0, expected.length()));
	}

	@Test
	public void html_testNestedClassJavadoc() throws Exception {
		MavenProjectClasspath.providerType = JavadocProviderTypes.HTML;

		MavenJavaProject project = createMavenProject(projectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file"));
		
		IType type = project.findType("java.util.Map$Entry");
		assertNotNull(type);
		String expected = String.join("\n",
				"<div class=\"block\">A map entry (key-value pair).  The <tt>Map.entrySet</tt> method returns",
				" a collection-view of the map, whose elements are of this class.  The");
		assertEquals(expected, type.getJavaDoc().getRenderable().toHtml().substring(0, expected.length()));
	}
	
	@Test
	public void html_testMethodJavadoc() throws Exception {
		MavenProjectClasspath.providerType = JavadocProviderTypes.HTML;

		MavenJavaProject project = createMavenProject(projectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file"));
		
		IType type = project.findType("java.util.ArrayList");
		assertNotNull(type);
		IMethod method = type.getMethod("size", Stream.empty());
		assertNotNull(method);
		
		String expected = String.join("\n",
				"<h4>size</h4>",
				"<pre>public&nbsp;int&nbsp;size()</pre>",
				"<div class=\"block\">Returns the number of elements in this list.</div>"
			);
		assertEquals(expected, method.getJavaDoc().getRenderable().toHtml().substring(0, expected.length()));
	}
	
	@Test
	public void html_testConstructorJavadoc() throws Exception {
		MavenProjectClasspath.providerType = JavadocProviderTypes.HTML;

		MavenJavaProject project = createMavenProject(projectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file"));
		
		IType type = project.findType("java.util.ArrayList");
		assertNotNull(type);
		IMethod method = type.getMethod("<init>", Stream.empty());
		assertNotNull(method);
		
		String expected = String.join("\n",
				"<h4>ArrayList</h4>"
				);
		assertEquals(expected, method.getJavaDoc().getRenderable().toHtml().substring(0, expected.length()));
		
	}
	
	@Test
	public void html_testFieldAndMethodJavadocForJar() throws Exception {
		MavenProjectClasspath.providerType = JavadocProviderTypes.HTML;

		MavenJavaProject project = createMavenProject(projectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file"));
		
		IType type = project.findType("org.springframework.boot.SpringApplication");
		assertNotNull(type);
		
		IField field = type.getField("BANNER_LOCATION_PROPERTY_VALUE");
		assertNotNull(field);
		String expected = String.join("\n",
				"<h4>BANNER_LOCATION_PROPERTY_VALUE</h4>",
				"<pre>public static final&nbsp;<a href=\"http://docs.oracle.com/javase/6/docs/api/java/lang/String.html?is-external=true\" title=\"class or interface in java.lang\">String</a> BANNER_LOCATION_PROPERTY_VALUE</pre>",
				"<div class=\"block\">Default banner location.</div>",
				"<dl>",
				"<dt><span class=\"seeLabel\">See Also:</span></dt>",
				"<dd><a href=\"../../../constant-values.html#org.springframework.boot.SpringApplication.BANNER_LOCATION_PROPERTY_VALUE\">Constant Field Values</a></dd>",
				"</dl>"
			);
		assertEquals(expected, field.getJavaDoc().getRenderable().toHtml());
		
		IMethod method = type.getMethod("getListeners", Stream.empty());
		assertNotNull(method);
		expected = String.join("\n", 
				"<h4>getListeners</h4>",
				"<pre>public&nbsp;<a href=\"http://docs.oracle.com/javase/6/docs/api/java/util/Set.html?is-external=true\" title=\"class or interface in java.util\">Set</a>&lt;org.springframework.context.ApplicationListener&lt;?&gt;&gt;&nbsp;getListeners()</pre>",
				"<div class=\"block\">Returns read-only ordered Set of the <code>ApplicationListener</code>s that will be",
				" applied to the SpringApplication and registered with the <code>ApplicationContext</code>",
				" .</div>",
				"<dl>",
				"<dt><span class=\"returnLabel\">Returns:</span></dt>",
				"<dd>the listeners</dd>",
				"</dl>"
			);
		assertEquals(expected, method.getJavaDoc().getRenderable().toHtml());
	}

	@Test
	public void html_testJavadocOutputFolder() throws Exception {
		MavenProjectClasspath.providerType = JavadocProviderTypes.HTML;
		Path projectPath = projectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file");
		MavenJavaProject project = createMavenProject(projectPath);
		IType type = project.findType("hello.Greeting");
		
		assertNotNull(type);
		String expected = "<div class=\"block\">Comment for Greeting class</div>";
		assertEquals(expected, type.getJavaDoc().getRenderable().toHtml());
		
		IField field = type.getField("id");
		assertNotNull(field);
		expected = String.join("\n",
				"<h4>id</h4>",
				"<pre>protected final&nbsp;long id</pre>",
				"<div class=\"block\">Comment for id field</div>"
			);
		assertEquals(expected, field.getJavaDoc().getRenderable().toHtml());
		
		IMethod method = type.getMethod("getId", Stream.empty());
		assertNotNull(method);
		expected = String.join("\n",
				"<h4>getId</h4>",
				"<pre>public&nbsp;long&nbsp;getId()</pre>",
				"<div class=\"block\">Comment for getId()</div>"
			);
		assertEquals(expected, method.getJavaDoc().getRenderable().toHtml());
	}
	
	@Test
	public void html_testInnerClassJavadocForOutputFolder() throws Exception {
		MavenProjectClasspath.providerType = JavadocProviderTypes.HTML;
		Path projectPath = projectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file");
		MavenJavaProject project = createMavenProject(projectPath);
		
		IType type = project.findType("hello.Greeting$TestInnerClass");
		assertNotNull(type);
		assertEquals("<div class=\"block\">Comment for inner class</div>", type.getJavaDoc().getRenderable().toHtml());

		IField field = type.getField("innerField");
		assertNotNull(field);
		String expected = String.join("\n", 
				"<h4>innerField</h4>",
				"<pre>protected&nbsp;int innerField</pre>",
				"<div class=\"block\">Comment for inner field</div>"
			);
		assertEquals(expected, field.getJavaDoc().getRenderable().toHtml());

		IMethod method = type.getMethod("getInnerField", Stream.empty());
		assertNotNull(method);
		expected = String.join("\n",
				"<h4>getInnerField</h4>",
				"<pre>public&nbsp;int&nbsp;getInnerField()</pre>",
				"<div class=\"block\">Comment for method inside nested class</div>"
			);
		assertEquals(expected, method.getJavaDoc().getRenderable().toHtml());
	}

	@Test
	public void html_testInnerClassLevel2_JavadocForOutputFolder() throws Exception {
		MavenProjectClasspath.providerType = JavadocProviderTypes.HTML;
		Path projectPath = projectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file");
		MavenJavaProject project = createMavenProject(projectPath);
		
		IType type = project.findType("hello.Greeting$TestInnerClass$TestInnerClassLevel2");
		assertNotNull(type);
		assertEquals("<div class=\"block\">Comment for level 2 nested class</div>", type.getJavaDoc().getRenderable().toHtml());

		IField field = type.getField("innerLevel2Field");
		assertNotNull(field);
		String expected = String.join("\n", 
				"<h4>innerLevel2Field</h4>",
				"<pre>protected&nbsp;int innerLevel2Field</pre>",
				"<div class=\"block\">Comment for level 2 inner field</div>"
			);
		assertEquals(expected, field.getJavaDoc().getRenderable().toHtml());

		IMethod method = type.getMethod("getInnerLevel2Field", Stream.empty());
		assertNotNull(method);
		expected = String.join("\n",
				"<h4>getInnerLevel2Field</h4>",
				"<pre>public&nbsp;int&nbsp;getInnerLevel2Field()</pre>",
				"<div class=\"block\">Comment for method inside level 2 nested class</div>"
			);
		assertEquals(expected, method.getJavaDoc().getRenderable().toHtml());
	}

	@Test
	public void html_testNoJavadocClass() throws Exception {
		MavenProjectClasspath.providerType = JavadocProviderTypes.HTML;
		Path projectPath = projectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file");
		MavenJavaProject project = createMavenProject(projectPath);
		
		IType type = project.findType("hello.GreetingController");
		assertNotNull(type);
		assertNull(type.getJavaDoc());
	}

	@Test
	public void html_testEmptyJavadocClass() throws Exception {
		MavenProjectClasspath.providerType = JavadocProviderTypes.HTML;
		Path projectPath = projectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file");
		MavenJavaProject project = createMavenProject(projectPath);
		
		IType type = project.findType("hello.Application");
		assertNotNull(type);
		assertNull(type.getJavaDoc());
	}

	@Test
	public void html_testNoJavadocMethod() throws Exception {
		MavenProjectClasspath.providerType = JavadocProviderTypes.HTML;
		Path projectPath = projectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file");
		MavenJavaProject project = createMavenProject(projectPath);
		
		IType type = project.findType("hello.Application");
		assertNotNull(type);
		IMethod method = type.getMethod("corsConfigurer", Stream.empty());
		assertNotNull(method);
		String expected = String.join("\n", 
				"<h4>corsConfigurer</h4>",
				"<pre>@Bean",
				"public&nbsp;org.springframework.web.servlet.config.annotation.WebMvcConfigurer&nbsp;corsConfigurer()</pre>"
			);
		assertEquals(expected, method.getJavaDoc().getRenderable().toHtml());
	}

	@Test
	public void html_testNoJavadocField() throws Exception {
		MavenProjectClasspath.providerType = JavadocProviderTypes.HTML;
		Path projectPath = projectsCache.get("gs-rest-service-cors-boot-1.4.1-with-classpath-file");
		MavenJavaProject project = createMavenProject(projectPath);
		Files.list(project.getOutputFolder().getParent().resolve("site")).forEach(System.out::println);
		
		IType type = project.findType("hello.GreetingController");
		assertNotNull(type);
		IField field = type.getField("template");
		assertNotNull(field);
		String expected = String.join("\n", 
				"<h4>template</h4>",
				"<pre>public static final&nbsp;<a href=\"http://docs.oracle.com/javase/8/docs/api/java/lang/String.html?is-external=true\" title=\"class or interface in java.lang\">String</a> template</pre>",
				"<dl>",
				"<dt><span class=\"seeLabel\">See Also:</span></dt>",
				"<dd><a href=\"../constant-values.html#hello.GreetingController.template\">Constant Field Values</a></dd>",
				"</dl>"
			);
		assertEquals(expected, field.getJavaDoc().getRenderable().toHtml());
	}
}
