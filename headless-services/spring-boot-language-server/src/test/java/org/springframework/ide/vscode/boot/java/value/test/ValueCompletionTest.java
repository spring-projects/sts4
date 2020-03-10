/*******************************************************************************
 * Copyright (c) 2017, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.value.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.gradle.internal.impldep.com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.app.BootLanguageServerParams;
import org.springframework.ide.vscode.boot.bootiful.AdHocPropertyHarnessTestConf;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.boot.editor.harness.AdHocPropertyHarness;
import org.springframework.ide.vscode.boot.editor.harness.PropertyIndexHarness;
import org.springframework.ide.vscode.boot.java.links.SourceLinkFactory;
import org.springframework.ide.vscode.boot.java.links.SourceLinks;
import org.springframework.ide.vscode.boot.java.utils.CompilationUnitCache;
import org.springframework.ide.vscode.boot.java.utils.SymbolCache;
import org.springframework.ide.vscode.boot.java.utils.SymbolCacheVoid;
import org.springframework.ide.vscode.boot.java.value.ValueCompletionProcessor;
import org.springframework.ide.vscode.boot.metadata.ValueProviderRegistry;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.maven.java.MavenJavaProject;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Martin Lippert
 */
@RunWith(SpringRunner.class)
@BootLanguageServerTest
@Import({AdHocPropertyHarnessTestConf.class, ValueCompletionTest.TestConf.class})
public class ValueCompletionTest {

	@Autowired private BootLanguageServerHarness harness;
	@Autowired private IJavaProject testProject;
	@Autowired private JavaProjectFinder projectFinder;

	private Editor editor;

	@Autowired private PropertyIndexHarness indexHarness;
	@Autowired private AdHocPropertyHarness adHocProperties;

	@Configuration
	static class TestConf {

		//Somewhat strange test setup, test provides a specific test project.
		//The project finder finds this test project,
		//But it is not used in the indexProvider/harness.
		//this is a bit odd... but we preserved the strangeness how it was.

		@Bean MavenJavaProject testProject() throws Exception {
			return ProjectsHarness.INSTANCE.mavenProject("test-annotations");
		}

		@Bean PropertyIndexHarness indexHarness(ValueProviderRegistry valueProviders) {
			return new PropertyIndexHarness(valueProviders);
		}

		@Bean JavaProjectFinder projectFinder(MavenJavaProject testProject) {
			return new JavaProjectFinder() {

				@Override
				public Optional<IJavaProject> find(TextDocumentIdentifier doc) {
					return Optional.ofNullable(testProject);
				}

				@Override
				public Collection<? extends IJavaProject> all() {
					// TODO Auto-generated method stub
					return testProject == null ? Collections.emptyList() : ImmutableList.of(testProject);
				}
			};
		}

		@Bean BootLanguageServerHarness harness(SimpleLanguageServer server, BootLanguageServerParams serverParams, PropertyIndexHarness indexHarness, JavaProjectFinder projectFinder) throws Exception {
			return new BootLanguageServerHarness(server, serverParams, indexHarness, projectFinder, LanguageId.JAVA, ".java");
		}

		@Bean BootLanguageServerParams serverParams(SimpleLanguageServer server, JavaProjectFinder projectFinder, ValueProviderRegistry valueProviders, PropertyIndexHarness indexHarness) {
			BootLanguageServerParams testDefaults = BootLanguageServerHarness.createTestDefault(server, valueProviders);
			return new BootLanguageServerParams(
					projectFinder,
					ProjectObserver.NULL,
					indexHarness.getIndexProvider(),
					testDefaults.typeUtilProvider
			);
		}

		@Bean SymbolCache symbolCache() {
			return new SymbolCacheVoid();
		}

		@Bean SourceLinks sourceLinks(SimpleTextDocumentService documents, CompilationUnitCache cuCache) {
			return SourceLinkFactory.NO_SOURCE_LINKS;
		}

	}

	@Before
	public void setup() throws Exception {
		harness.intialize(null);
	}

	private IJavaProject getTestProject() {
		return testProject;
	}

	@Test
	public void testPrefixIdentification() {
		ValueCompletionProcessor processor = new ValueCompletionProcessor(projectFinder, null, null);

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

	@Test
	public void adHoc() throws Exception {
		prepareDefaultIndexData();
		Editor editor = harness.newEditor(LanguageId.JAVA,
				"package org.test;\n" +
				"\n" +
				"import org.springframework.beans.factory.annotation.Value;\n" +
				"\n" +
				"public class TestValueCompletion {\n" +
				"	\n" +
				"	@Value(\"<*>\")\n" +
				"	private String value1;\n" +
				"}"
		);

		//There are no 'ad-hoc' properties yet. So should only suggest the default ones.
		editor.assertContextualCompletions(
				"<*>"
				, //==>
				"${data.prop2}<*>",
				"${else.prop3}<*>",
				"${spring.prop1}<*>"
		);

		adHocProperties.add("spring.ad-hoc.thingy");
		adHocProperties.add("spring.ad-hoc.other-thingy");
		adHocProperties.add("spring.prop1"); //should not suggest this twice!
		editor.assertContextualCompletions(
				"<*>"
				, //==>
				"${data.prop2}<*>",
				"${else.prop3}<*>",
				"${spring.ad-hoc.other-thingy}<*>",
				"${spring.ad-hoc.thingy}<*>",
				"${spring.prop1}<*>"
		);

		editor.assertContextualCompletions(
				"adhoc<*>"
				, //==>
				"${spring.ad-hoc.thingy}<*>",
				"${spring.ad-hoc.other-thingy}<*>"
		);
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
			if (!clonedEditor.getText().contains(expectedCompleted)) {
				fail("Not found '"+expectedCompleted+"' in \n"+clonedEditor.getText());
			}
		}

		assertEquals(i, completions.size());
	}

}
