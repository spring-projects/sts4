/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.value.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.lsp4j.CompletionItem;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServer;
import org.springframework.ide.vscode.boot.java.value.ValueCompletionProcessor;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.FileBasedJavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.CompositeJavaProjectFinder;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.ide.vscode.project.harness.PropertyIndexHarness;

/**
 * @author Martin Lippert
 */
public class ValueCompletionTest {

	private LanguageServerHarness<BootJavaLanguageServer> harness;
	private IJavaProject testProject;

	private Editor editor;

	private PropertyIndexHarness indexHarness;

	@Before
	public void setup() throws Exception {
		testProject = ProjectsHarness.INSTANCE.mavenProject("test-annotations");
		indexHarness = new PropertyIndexHarness();

		harness = new LanguageServerHarness<BootJavaLanguageServer>(() -> {
			BootJavaLanguageServer server = new BootJavaLanguageServer(
					new CompositeJavaProjectFinder(new ArrayList<>(Collections.singleton(new FileBasedJavaProjectFinder() {
							@Override
							public IJavaProject find(File doc) {
								return getTestProject();
							}
					}))),
					indexHarness.getIndexProvider()
			);
			return server;
		}) {
			@Override
			protected String getFileExtension() {
				return ".java";
			}
		};
		harness.intialize(null);
	}

	private IJavaProject getTestProject() {
		return testProject;
	}

	@Test
	public void testPrefixIdentification() {
		ValueCompletionProcessor processor = new ValueCompletionProcessor(null);

		assertEquals("pre", processor.identifyPropertyPrefix("pre", 3));
		assertEquals("pre", processor.identifyPropertyPrefix("prefix", 3));
		assertEquals("", processor.identifyPropertyPrefix("", 0));
		assertEquals("pre", processor.identifyPropertyPrefix("$pre", 4));

		assertEquals("", processor.identifyPropertyPrefix("${pre", 0));
		assertEquals("", processor.identifyPropertyPrefix("${pre", 1));
		assertEquals("", processor.identifyPropertyPrefix("${pre", 2));
		assertEquals("p", processor.identifyPropertyPrefix("${pre", 3));
		assertEquals("pr", processor.identifyPropertyPrefix("${pre", 4));
	}

	@Test
	public void testEmptyBracketsCompletion() throws Exception {
		prepareCase("@Value(\"onField\")", "@Value(<*>)");
		prepareDefaultIndexData();

		assertAnnotationCompletions(
				"@Value(\"${data.prop2}\"<*>)",
				"@Value(\"${else.prop3}\"<*>)",
				"@Value(\"${spring.prop1}\"<*>)");
	}

	@Test
	public void testEmptyBracketsCompletionWithParamName() throws Exception {
		prepareCase("@Value(\"onField\")", "@Value(value=<*>)");
		prepareDefaultIndexData();

		assertAnnotationCompletions(
				"@Value(value=\"${data.prop2}\"<*>)",
				"@Value(value=\"${else.prop3}\"<*>)",
				"@Value(value=\"${spring.prop1}\"<*>)");
	}

	@Test
	public void testEmptyBracketsCompletionWithWrongParamName() throws Exception {
		prepareCase("@Value(\"onField\")", "@Value(another=<*>)");
		prepareDefaultIndexData();
		assertAnnotationCompletions();
	}

	@Test
	public void testOnlyDollarNoQoutesCompletion() throws Exception {
		prepareCase("@Value(\"onField\")", "@Value($<*>)");
		prepareDefaultIndexData();

		assertAnnotationCompletions(
				"@Value(\"${data.prop2}\"<*>)",
				"@Value(\"${else.prop3}\"<*>)",
				"@Value(\"${spring.prop1}\"<*>)");
	}

	@Test
	public void testOnlyDollarNoQoutesWithParamCompletion() throws Exception {
		prepareCase("@Value(\"onField\")", "@Value(value=$<*>)");
		prepareDefaultIndexData();

		assertAnnotationCompletions(
				"@Value(value=\"${data.prop2}\"<*>)",
				"@Value(value=\"${else.prop3}\"<*>)",
				"@Value(value=\"${spring.prop1}\"<*>)");
	}

	@Test
	public void testOnlyDollarCompletion() throws Exception {
		prepareCase("@Value(\"onField\")", "@Value(\"$<*>\")");
		prepareDefaultIndexData();

		assertAnnotationCompletions(
				"@Value(\"${data.prop2}<*>\")",
				"@Value(\"${else.prop3}<*>\")",
				"@Value(\"${spring.prop1}<*>\")");
	}

	@Test
	public void testOnlyDollarWithParamCompletion() throws Exception {
		prepareCase("@Value(\"onField\")", "@Value(value=\"$<*>\")");
		prepareDefaultIndexData();

		assertAnnotationCompletions(
				"@Value(value=\"${data.prop2}<*>\")",
				"@Value(value=\"${else.prop3}<*>\")",
				"@Value(value=\"${spring.prop1}<*>\")");
	}

	@Test
	public void testDollarWithBracketsCompletion() throws Exception {
		prepareCase("@Value(\"onField\")", "@Value(\"${<*>}\")");
		prepareDefaultIndexData();

		assertAnnotationCompletions(
				"@Value(\"${data.prop2<*>}\")",
				"@Value(\"${else.prop3<*>}\")",
				"@Value(\"${spring.prop1<*>}\")");
	}

	@Test
	public void testDollarWithBracketsWithParamCompletion() throws Exception {
		prepareCase("@Value(\"onField\")", "@Value(value=\"${<*>}\")");
		prepareDefaultIndexData();

		assertAnnotationCompletions(
				"@Value(value=\"${data.prop2<*>}\")",
				"@Value(value=\"${else.prop3<*>}\")",
				"@Value(value=\"${spring.prop1<*>}\")");
	}

	@Test
	public void testEmptyStringLiteralCompletion() throws Exception {
		prepareCase("@Value(\"onField\")", "@Value(\"<*>\")");
		prepareDefaultIndexData();

		assertAnnotationCompletions(
				"@Value(\"${data.prop2}<*>\")",
				"@Value(\"${else.prop3}<*>\")",
				"@Value(\"${spring.prop1}<*>\")");
	}

	@Test
	public void testPlainPrefixCompletion() throws Exception {
		prepareCase("@Value(\"onField\")", "@Value(spri<*>)");
		prepareDefaultIndexData();

		assertAnnotationCompletions(
				"@Value(\"${spring.prop1}\"<*>)");
	}

	@Test
	public void testQoutedPrefixCompletion() throws Exception {
		prepareCase("@Value(\"onField\")", "@Value(\"spri<*>\")");
		prepareDefaultIndexData();

		assertAnnotationCompletions(
				"@Value(\"${spring.prop1}<*>\")");
	}

	@Test
	public void testRandomSpelExpressionNoCompletion() throws Exception {
		prepareCase("@Value(\"onField\")", "@Value(\"#{<*>}\")");
		prepareDefaultIndexData();

		assertAnnotationCompletions(
				"@Value(\"#{${data.prop2}<*>}\")",
				"@Value(\"#{${else.prop3}<*>}\")",
				"@Value(\"#{${spring.prop1}<*>}\")");
	}

	@Test
	public void testRandomSpelExpressionWithPropertyDollar() throws Exception {
		prepareCase("@Value(\"onField\")", "@Value(\"#{345$<*>}\")");
		prepareDefaultIndexData();

		assertAnnotationCompletions(
				"@Value(\"#{345${data.prop2}<*>}\")",
				"@Value(\"#{345${else.prop3}<*>}\")",
				"@Value(\"#{345${spring.prop1}<*>}\")");
	}

	@Test
	public void testRandomSpelExpressionWithPropertyDollerWithoutClosindBracket() throws Exception {
		prepareCase("@Value(\"onField\")", "@Value(\"#{345${<*>}\")");
		prepareDefaultIndexData();

		assertAnnotationCompletions(
				"@Value(\"#{345${data.prop2}<*>}\")",
				"@Value(\"#{345${else.prop3}<*>}\")",
				"@Value(\"#{345${spring.prop1}<*>}\")");
	}

	@Test
	public void testRandomSpelExpressionWithPropertyDollerWithClosingBracket() throws Exception {
		prepareCase("@Value(\"onField\")", "@Value(\"#{345${<*>}}\")");
		prepareDefaultIndexData();

		assertAnnotationCompletions(
				"@Value(\"#{345${data.prop2<*>}}\")",
				"@Value(\"#{345${else.prop3<*>}}\")",
				"@Value(\"#{345${spring.prop1<*>}}\")");
	}

	@Test
	public void testRandomSpelExpressionWithPropertyPrefixWithoutClosingBracket() throws Exception {
		prepareCase("@Value(\"onField\")", "@Value(\"#{345${spri<*>}\")");
		prepareDefaultIndexData();

		assertAnnotationCompletions(
				"@Value(\"#{345${spring.prop1}<*>}\")");
	}

	@Test
	public void testRandomSpelExpressionWithPropertyPrefixWithClosingBracket() throws Exception {
		prepareCase("@Value(\"onField\")", "@Value(\"#{345${spri<*>}}\")");
		prepareDefaultIndexData();

		assertAnnotationCompletions(
				"@Value(\"#{345${spring.prop1<*>}}\")");
	}

	private void prepareDefaultIndexData() {
		indexHarness.data("spring.prop1", "java.lang.String", null, null);
		indexHarness.data("data.prop2", "java.lang.String", null, null);
		indexHarness.data("else.prop3", "java.lang.String", null, null);
	}

	private void prepareCase(String selectedAnnotation, String annotationStatementBeforeTest) throws Exception {
		InputStream resource = this.getClass().getResourceAsStream("/test-projects/test-annotations/src/main/java/org/test/TestValueCompletion.java");
		String content = IOUtils.toString(resource);

		content = content.replace(selectedAnnotation, annotationStatementBeforeTest);
		editor = new Editor(harness, content, LanguageId.JAVA);
	}

	private void assertAnnotationCompletions(String... completedAnnotations) throws Exception {
		List<CompletionItem> completions = editor.getCompletions();
		int i = 0;
		for (String expectedCompleted : completedAnnotations) {
			Editor clonedEditor = editor.clone();
			clonedEditor.apply(completions.get(i++));
			assertTrue(clonedEditor.getText().contains(expectedCompleted));
		}

		assertEquals(i, completions.size());
	}

}
