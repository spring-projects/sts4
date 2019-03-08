/*******************************************************************************
 * Copyright (c) 2016, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.maven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.ide.vscode.commons.java.IField;
import org.springframework.ide.vscode.commons.java.IMethod;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.javadoc.IJavadoc;
import org.springframework.ide.vscode.commons.javadoc.JavaDocProviders;
import org.springframework.ide.vscode.commons.maven.java.MavenJavaProject;
import org.springframework.ide.vscode.commons.util.BasicFileObserver;
import org.springframework.ide.vscode.commons.util.FileObserver;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

@Ignore
//@EnabledOnJre(JAVA_8)
public class HtmlJavadocTest {

	private static FileObserver fileObserver = new BasicFileObserver();
	private static Supplier<MavenJavaProject> projectSupplier = Suppliers.memoize(() -> {
		Path testProjectPath;
		try {
			testProjectPath = Paths.get(HtmlJavadocTest.class.getResource("/gs-rest-service-cors-boot-1.4.1-with-classpath-file").toURI());
			MavenBuilder.newBuilder(testProjectPath).clean().pack().javadoc().skipTests().execute();
			return MavenJavaProject.create(fileObserver, MavenCore.getDefault(), testProjectPath.resolve(MavenCore.POM_XML).toFile(), (uri, cpe) -> JavaDocProviders.createFor(cpe));
		} catch (Exception e) {
			return null;
		}
	});

	@Test
	public void html_testClassJavadoc() throws Exception {
		Assume.assumeTrue(javaVersionHigherThan(6));

		MavenJavaProject project = projectSupplier.get();

		IType type = project.getIndex().findType("java.util.Map");
		assertNotNull(type);
		String expected = String.join("\n",
				"<div class=\"block\">An object that maps keys to values.  A map cannot contain duplicate keys;",
				" each key can map to at most one value."
				);
		IJavadoc javaDoc = type.getJavaDoc();
		assertNotNull(javaDoc);
		assertEquals(expected, javaDoc.getRenderable().toHtml().substring(0, expected.length()));
	}

	@Test
	public void html_testConstructorJavadoc() throws Exception {
		Assume.assumeTrue(javaVersionHigherThan(6));
		MavenJavaProject project = projectSupplier.get();

		IType type = project.getIndex().findType("java.util.ArrayList");
		assertNotNull(type);
		IMethod method = type.getMethod("<init>", Stream.empty());
		assertNotNull(method);

		String expected = String.join("\n",
				"<h4>ArrayList</h4>"
				);
		IJavadoc javaDoc = method.getJavaDoc();
		assertNotNull(javaDoc);
		assertEquals(expected, javaDoc.getRenderable().toHtml().substring(0, expected.length()));

	}

	@Test
	public void html_testEmptyJavadocClass() throws Exception {
		MavenJavaProject project = projectSupplier.get();

		IType type = project.getIndex().findType("hello.Application");
		assertNotNull(type);
		assertNull(type.getJavaDoc());
	}

	@Test
	public void html_testFieldAndMethodJavadocForJar() throws Exception {
		MavenJavaProject project = projectSupplier.get();

		IType type = project.getIndex().findType("org.springframework.boot.SpringApplication");
		assertNotNull(type);

		IField field = type.getField("BANNER_LOCATION_PROPERTY_VALUE");
		assertNotNull(field);
		String expected = String.join("\n",
				"<h4>BANNER_LOCATION_PROPERTY_VALUE</h4>",
				"<pre>public static final&nbsp;<a href=\"https://docs.oracle.com/javase/6/docs/api/java/lang/String.html?is-external=true\" title=\"class or interface in java.lang\">String</a> BANNER_LOCATION_PROPERTY_VALUE</pre>",
				"<div class=\"block\">Default banner location.</div>",
				"<dl>",
				"<dt><span class=\"seeLabel\">See Also:</span></dt>",
				"<dd><a href=\"../../../constant-values.html#org.springframework.boot.SpringApplication.BANNER_LOCATION_PROPERTY_VALUE\">Constant Field Values</a></dd>",
				"</dl>"
			);
		IJavadoc javaDoc = field.getJavaDoc();
		assertNotNull(javaDoc);
		assertEquals(expected, javaDoc.getRenderable().toHtml());

		IMethod method = type.getMethod("getListeners", Stream.empty());
		assertNotNull(method);
		expected = String.join("\n",
				"<h4>getListeners</h4>",
				"<pre>public&nbsp;<a href=\"https://docs.oracle.com/javase/6/docs/api/java/util/Set.html?is-external=true\" title=\"class or interface in java.util\">Set</a>&lt;org.springframework.context.ApplicationListener&lt;?&gt;&gt;&nbsp;getListeners()</pre>",
				"<div class=\"block\">Returns read-only ordered Set of the <code>ApplicationListener</code>s that will be",
				" applied to the SpringApplication and registered with the <code>ApplicationContext</code>",
				" .</div>",
				"<dl>",
				"<dt><span class=\"returnLabel\">Returns:</span></dt>",
				"<dd>the listeners</dd>",
				"</dl>"
			);
		javaDoc = method.getJavaDoc();
		assertNotNull(javaDoc);
		assertEquals(expected, javaDoc.getRenderable().toHtml());
	}

	@Test
	public void html_testInnerClassJavadocForOutputFolder() throws Exception {
		MavenJavaProject project = projectSupplier.get();

		IType type = project.getIndex().findType("hello.Greeting$TestInnerClass");
		assertNotNull(type);
		IJavadoc javaDoc = type.getJavaDoc();
		assertNotNull(javaDoc);
		assertEquals("<div class=\"block\">Comment for inner class</div>", javaDoc.getRenderable().toHtml());

		IField field = type.getField("innerField");
		assertNotNull(field);
		String expected = String.join("\n",
				"<h4>innerField</h4>",
				"<pre>protected&nbsp;int innerField</pre>",
				"<div class=\"block\">Comment for inner field</div>"
			);
		javaDoc = field.getJavaDoc();
		assertNotNull(javaDoc);
		assertEquals(expected, javaDoc.getRenderable().toHtml());

		IMethod method = type.getMethod("getInnerField", Stream.empty());
		assertNotNull(method);
		expected = String.join("\n",
				"<h4>getInnerField</h4>",
				"<pre>public&nbsp;int&nbsp;getInnerField()</pre>",
				"<div class=\"block\">Comment for method inside nested class</div>"
			);
		javaDoc = method.getJavaDoc();
		assertNotNull(javaDoc);
		assertEquals(expected, javaDoc.getRenderable().toHtml());
	}

	@Test
	public void html_testInnerClassLevel2_JavadocForOutputFolder() throws Exception {
		MavenJavaProject project = projectSupplier.get();

		IType type = project.getIndex().findType("hello.Greeting$TestInnerClass$TestInnerClassLevel2");
		assertNotNull(type);
		IJavadoc javaDoc = type.getJavaDoc();
		assertNotNull(javaDoc);
		assertEquals("<div class=\"block\">Comment for level 2 nested class</div>", javaDoc.getRenderable().toHtml());

		IField field = type.getField("innerLevel2Field");
		assertNotNull(field);
		String expected = String.join("\n",
				"<h4>innerLevel2Field</h4>",
				"<pre>protected&nbsp;int innerLevel2Field</pre>",
				"<div class=\"block\">Comment for level 2 inner field</div>"
			);
		javaDoc = field.getJavaDoc();
		assertNotNull(javaDoc);
		assertEquals(expected, javaDoc.getRenderable().toHtml());

		IMethod method = type.getMethod("getInnerLevel2Field", Stream.empty());
		assertNotNull(method);
		expected = String.join("\n",
				"<h4>getInnerLevel2Field</h4>",
				"<pre>public&nbsp;int&nbsp;getInnerLevel2Field()</pre>",
				"<div class=\"block\">Comment for method inside level 2 nested class</div>"
			);
		javaDoc = method.getJavaDoc();
		assertNotNull(javaDoc);
		assertEquals(expected, javaDoc.getRenderable().toHtml());
	}

	@Test
	public void html_testJavadocOutputFolder() throws Exception {
		MavenJavaProject project = projectSupplier.get();
		IType type = project.getIndex().findType("hello.Greeting");

		assertNotNull(type);
		String expected = "<div class=\"block\">Comment for Greeting class</div>";
		IJavadoc javaDoc = type.getJavaDoc();
		assertNotNull(javaDoc);
		assertEquals(expected, javaDoc.getRenderable().toHtml());

		IField field = type.getField("id");
		assertNotNull(field);
		expected = String.join("\n",
				"<h4>id</h4>",
				"<pre>protected final&nbsp;long id</pre>",
				"<div class=\"block\">Comment for id field</div>"
			);
		javaDoc = field.getJavaDoc();
		assertNotNull(javaDoc);
		assertEquals(expected, javaDoc.getRenderable().toHtml());

		IMethod method = type.getMethod("getId", Stream.empty());
		assertNotNull(method);
		expected = String.join("\n",
				"<h4>getId</h4>",
				"<pre>public&nbsp;long&nbsp;getId()</pre>",
				"<div class=\"block\">Comment for getId()</div>"
			);
		javaDoc = method.getJavaDoc();
		assertNotNull(javaDoc);
		assertEquals(expected, javaDoc.getRenderable().toHtml());
	}

	@Test
	public void html_testMethodJavadoc() throws Exception {
		Assume.assumeTrue(javaVersionHigherThan(6));

		MavenJavaProject project = projectSupplier.get();

		IType type = project.getIndex().findType("java.util.ArrayList");
		assertNotNull(type);
		IMethod method = type.getMethod("size", Stream.empty());
		assertNotNull(method);

		String expected = String.join("\n",
				"<h4>size</h4>",
				"<pre>public&nbsp;int&nbsp;size()</pre>",
				"<div class=\"block\">Returns the number of elements in this list.</div>"
			);
		IJavadoc javaDoc = method.getJavaDoc();
		assertNotNull(javaDoc);
		assertEquals(expected, javaDoc.getRenderable().toHtml().substring(0, expected.length()));
	}

	@Test
	public void html_testNestedClassJavadoc() throws Exception {
		Assume.assumeTrue(javaVersionHigherThan(6));

		MavenJavaProject project = projectSupplier.get();

		IType type = project.getIndex().findType("java.util.Map$Entry");
		assertNotNull(type);
		String expected = String.join("\n",
				"<div class=\"block\">A map entry (key-value pair).  The <tt>Map.entrySet</tt> method returns",
				" a collection-view of the map, whose elements are of this class.  The");
		IJavadoc javaDoc = type.getJavaDoc();
		assertNotNull(javaDoc);
		assertEquals(expected, javaDoc.getRenderable().toHtml().substring(0, expected.length()));
	}

	@Test
	public void html_testNoJavadocClass() throws Exception {
		MavenJavaProject project = projectSupplier.get();;

		IType type = project.getIndex().findType("hello.GreetingController");
		assertNotNull(type);
		assertNull(type.getJavaDoc());
	}

	@Test
	public void html_testNoJavadocField() throws Exception {
		MavenJavaProject project = projectSupplier.get();

		IType type = project.getIndex().findType("hello.GreetingController");
		assertNotNull(type);
		IField field = type.getField("template");
		assertNotNull(field);
		String expected = String.join("\n",
				"<h4>template</h4>",
				"<pre>public static final&nbsp;<a href=\"https://docs.oracle.com/javase/8/docs/api/java/lang/String.html?is-external=true\" title=\"class or interface in java.lang\">String</a> template</pre>",
				"<dl>",
				"<dt><span class=\"seeLabel\">See Also:</span></dt>",
				"<dd><a href=\"../constant-values.html#hello.GreetingController.template\">Constant Field Values</a></dd>",
				"</dl>"
			);
		IJavadoc javaDoc = field.getJavaDoc();
		assertNotNull(javaDoc);
		assertEquals(expected, javaDoc.getRenderable().toHtml());
	}

	@Test
	public void html_testNoJavadocMethod() throws Exception {
		MavenJavaProject project = projectSupplier.get();

		IType type = project.getIndex().findType("hello.Application");
		assertNotNull(type);
		IMethod method = type.getMethod("corsConfigurer", Stream.empty());
		assertNotNull(method);
		String expected = String.join("\n",
				"<h4>corsConfigurer</h4>",
				"<pre>@Bean",
				"public&nbsp;org.springframework.web.servlet.config.annotation.WebMvcConfigurer&nbsp;corsConfigurer()</pre>"
			);
		IJavadoc javaDoc = method.getJavaDoc();
		assertNotNull(javaDoc);
		assertEquals(expected, javaDoc.getRenderable().toHtml());
	}

	private static boolean javaVersionHigherThan(int version) {
		String versionStr = MavenCore.getDefault().getJavaRuntimeMinorVersion();
		try {
			return versionStr != null && Integer.valueOf(versionStr) > version;
		} catch (NumberFormatException e) {
			return false;
		}
	}

}
