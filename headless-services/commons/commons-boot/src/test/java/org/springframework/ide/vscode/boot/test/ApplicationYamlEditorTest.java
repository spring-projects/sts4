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
package org.springframework.ide.vscode.boot.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.ide.vscode.languageserver.testharness.Editor.INDENTED_COMPLETION;

import java.time.Duration;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Diagnostic;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.ide.vscode.boot.BootLanguageServer;
import org.springframework.ide.vscode.boot.BootLanguageServerParams;
import org.springframework.ide.vscode.boot.editor.harness.AbstractPropsEditorTest;
import org.springframework.ide.vscode.boot.editor.harness.StyledStringMatcher;
import org.springframework.ide.vscode.boot.java.handlers.RunningAppProvider;
import org.springframework.ide.vscode.boot.java.utils.SpringLiveHoverWatchdog;
import org.springframework.ide.vscode.boot.metadata.CachingValueProvider;
import org.springframework.ide.vscode.boot.metadata.PropertyInfo;
import org.springframework.ide.vscode.boot.properties.BootPropertiesLanguageServerComponents;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.composable.ComposableLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.StringUtil;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.languageserver.testharness.Editor;

/**
 * This class is a placeholder where we will attempt to copy and port
 * as many tests a possible from
 * org.springframework.ide.eclipse.boot.properties.editor.test.YamlEditorTests
 *
 * @author Kris De Volder
 */
public class ApplicationYamlEditorTest extends AbstractPropsEditorTest {

	////////////////////////////////////////////////////////////////////////////////////////

	@Test public void bug_153144391() throws Exception {
		//See: https://www.pivotaltracker.com/story/show/153144391
		useProject(createPredefinedMavenProject("empty-boot-1.3.0-app"));

		Editor editor = newEditor(
				"spring:\n" +
				"  application:\n" +
				"    name: chatter-web-ui\n" +
				"  cloud:\n" +
				"    stream:\n" +
				"      bindings:\n" +
				"        output:\n" +
				"          destination: chat\n" +
				"        input:\n" +
				"          destination: chat\n" +
				"jackloca<*>"
		);
		editor.assertCompletions(INDENTED_COMPLETION,
				"spring:\n" +
				"  application:\n" +
				"    name: chatter-web-ui\n" +
				"  cloud:\n" +
				"    stream:\n" +
				"      bindings:\n" +
				"        output:\n" +
				"          destination: chat\n" +
				"        input:\n" +
				"          destination: chat\n" +
				"  jackson:\n" +
				"    locale: <*>"
		);
	}

	@Test public void linterRunsOnDocumentOpenAndChange() throws Exception {
		Editor editor = newEditor(
				"somemap: val\n"+
				"- sequence"
		);

		editor.assertProblems(
				"-|expected <block end>"
		);

		editor.setText(
				"- sequence\n" +
				"zomemap: val"
		);

		editor.assertProblems(
				"z|expected <block end>"
		);
	}

	///////////////////// ported tests from old STS code base ////////////////////////////////////////////////

	@Test public void testHovers() throws Exception {
		defaultTestData();
		Editor editor = newEditor(
				"spring:\n" +
				"  application:\n" +
				"    name: foofoo\n" +
				"  beyond:\n" +
				"    the-valid: range\n" +
				"    \n" +
				"server:\n" +
				"  port: 8888"
		);

		editor.assertIsHoverRegion("name");
		editor.assertIsHoverRegion("port");

		editor.assertHoverContains("name", "**spring.application.name**");
		editor.assertHoverContains("port", "**server.port**");
		editor.assertHoverContains("8888", "**server.port**"); // hover over value show info about corresponding key. Is this logical?

		editor.assertNoHover("beyond");
		editor.assertNoHover("the-valid");
		editor.assertNoHover("range");

		//Note currently, these provide no hovers, but maybe (some of them) should if we index proprty sources and not just the
		// properties themselves.
		editor.assertNoHover("spring");
		editor.assertNoHover("application");
		editor.assertNoHover("server");


		//Test for the case where we can't produce an AST for editor text
		editor = newEditor(
				"- syntax\n" +
				"error:\n"
		);
		editor.assertNoHover("syntax");
		editor.assertNoHover("error");
	}

	@Test public void testHoverInfoForEnumValueInMapKey() throws Exception {
		Editor editor;
		IJavaProject project = createPredefinedMavenProject("empty-boot-1.3.0-app");
		useProject(project);

		//This test will fail if source jars haven't been downloaded.
		//Probably that is why it fails in CI build?
		//Let's check:
//		IType type = project.findType("com.fasterxml.jackson.databind.SerializationFeature");
//		System.out.println(">>> source for: "+type.getFullyQualifiedName());
//		System.out.println(downloadSources(type));
//		System.out.println("<<< source for: "+type.getFullyQualifiedName());

		editor = newEditor(
				"spring:\n" +
				"  jackson:\n" +
				"    serialization:\n" +
				"      indent-output: true"
		);
		editor.assertIsHoverRegion("indent-output");
		editor.assertHoverContains("indent-output", "allows enabling (or disabling) indentation");

		//Also try that it works when spelled in all upper-case
		editor = newEditor(
				"spring:\n" +
				"  jackson:\n" +
				"    serialization:\n" +
				"      INDENT_OUTPUT: true"
		);
		editor.assertIsHoverRegion("INDENT_OUTPUT");
		editor.assertHoverContains("INDENT_OUTPUT", "allows enabling (or disabling) indentation");
	}

	@Test public void testHoverInfoForEnumValueInMapKeyCompletion() throws Exception {
		IJavaProject project = createPredefinedMavenProject("empty-boot-1.3.0-app");
		useProject(project);

		//This test will fail if source jars haven't been downloaded.
		//Probably that is why it fails in CI build?
		//Let's check:
//		IType type = project.findType("com.fasterxml.jackson.databind.SerializationFeature");
//		System.out.println(">>> source for: "+type.getFullyQualifiedName());
//		System.out.println(downloadSources(type));
//		System.out.println("<<< source for: "+type.getFullyQualifiedName());

		assertCompletionDetails(
				"spring:\n" +
				"  jackson:\n" +
				"    serialization:\n" +
				"      ind<*>"
				, // ==========
				"indent-output"
				, // ==>
				"boolean",
				"allows enabling (or disabling) indentation"
		);
	}

	@Test public void testHoverInfoForValueHintCompletion() throws Exception {
		data("my.bonus", "java.lang.String", null, "Bonus type")
		.valueHint("small", "A small bonus. For a little extra incentive.")
		.valueHint("large", "An large bonus. For the ones who deserve it.")
		.valueHint("exorbitant", "Truly outrageous. Who deserves a bonus like that?");

		assertCompletionDetails(
				"my:\n" +
				"  bonus: l<*>"
				, // ==========
				"large"
				, // ==>
				"String",
				"For the ones who deserve it"
		);
	}

	@Test public void testHoverInfoForValueHint() throws Exception {
		data("my.bonus", "java.lang.String", null, "Bonus type")
		.valueHint("small", "A small bonus. For a little extra incentive.")
		.valueHint("large", "An large bonus. For the ones who deserve it.")
		.valueHint("exorbitant", "Truly outrageous. Who deserves a bonus like that?");

		Editor editor = newEditor(
				"#comment here\n" +
				"my:\n" +
				"  bonus: large\n"
		);

		editor.assertIsHoverRegion("large");
		editor.assertHoverContains("large", "For the ones who deserve it");
	}


	@Ignore @Test public void testUserDefinedHoversandLinkTargets() throws Exception {
		useProject(createPredefinedMavenProject("enums-boot-1.3.2-app"));
		data("foo.link-tester", "demo.LinkTestSubject", null, "for testing different Pojo link cases");
		Editor editor = newEditor(
				"#A comment at the start\n" +
				"foo:\n" +
				"  data:\n" +
				"    wavelen: 666\n" +
				"    name: foo\n" +
				"    next: green\n" +
				"  link-tester:\n" +
				"    has-it-all: nice\n" +
				"    strange: weird\n" +
				"    getter-only: getme\n"
		);

		editor.assertHoverContains("data", "Pojo"); // description from json metadata

		// NOTE: This may be failing because javadoc may not be obtained from a private member
		editor.assertHoverContains("wavelen", "JavaDoc from field"); // javadoc from field
		editor.assertHoverContains("name", "Set the name"); // javadoc from setter
		editor.assertHoverContains("next", "Get the next"); // javadoc from getter

		editor.assertLinkTargets("data", "demo.FooProperties.setdata(ColorData)");
		editor.assertLinkTargets("wavelen", "demo.ColorData.setWavelen(double)");

	}

	@Ignore @Test public void testHyperlinkTargets() throws Exception {
		IJavaProject p = createPredefinedMavenProject("tricky-getters-boot-1.3.1-app");
		useProject(p);

		Editor editor = newEditor(
				"server:\n"+
				"  port: 888\n" +
				"spring:\n" +
				"  datasource:\n" +
				"    login-timeout: 1000\n" +
				"flyway:\n" +
				"  init-sqls: a,b,c\n"
		);

		editor.assertLinkTargets("port",
				"org.springframework.boot.autoconfigure.web.ServerProperties.setPort(Integer)"
		);
		editor.assertLinkTargets("login-",
				"org.springframework.boot.autoconfigure.jdbc.DataSourceConfigMetadata.hikariDataSource()",
				"org.springframework.boot.autoconfigure.jdbc.DataSourceConfigMetadata.tomcatDataSource()",
				"org.springframework.boot.autoconfigure.jdbc.DataSourceConfigMetadata.dbcpDataSource()"
		);
		editor.assertLinkTargets("init-sql",
				"org.springframework.boot.autoconfigure.flyway.FlywayProperties.setInitSqls(List<String>)");
	}

	@Test public void testReconcile() throws Exception {
		defaultTestData();
		Editor editor = newEditor(
				"server:\n" +
				"  port: \n" +
				"    extracrap: 8080\n" +
				"logging:\n"+
				"  level:\n" +
				"    com.acme: INFO\n" +
				"  snuggem: what?\n" +
				"bogus:\n" +
				"  no: \n" +
				"    good: true\n"
		);
		System.out.println(editor);
		editor.assertProblems(
				"extracrap: 8080|Expecting a 'int' but got a 'Mapping' node",
				"snuggem|Unknown property",
				"bogus|Unknown property"
		);
	}

	@Test public void test_STS_4140_StringArrayReconciling() throws Exception {
		defaultTestData();

		Editor editor;

		//Case 0: very focussed test for easy debugging
		editor = newEditor(
			"flyway:\n" +
			"  schemas: [MEMBER_VERSION, MEMBER]"
		);
		editor.assertProblems(/*NONE*/);

		//Case 1: String[] as a flow sequence
		editor = newEditor(
			"something-bad: wrong\n"+
			"flyway:\n" +
			"  url: jdbc:h2:file:~/localdb;IGNORECASE=TRUE;mv_store=false\n" +
			"  user: admin\n" +
			"  password: admin\n" +
			"  schemas: [MEMBER_VERSION, MEMBER]"
		);
		editor.assertProblems(
				"something-bad|Unknown property"
				//No other problems should be reported
		);

		//Case 1: String[] as a block sequence
		editor = newEditor(
			"something-bad: wrong\n"+
			"flyway:\n" +
			"  url: jdbc:h2:file:~/localdb;IGNORECASE=TRUE;mv_store=false\n" +
			"  user: admin\n" +
			"  password: admin\n" +
			"  schemas:\n" +
			"    - MEMBER_VERSION\n" +
			"    - MEMBER"
		);
		editor.assertProblems(
				"something-bad|Unknown property"
				//No other problems should be reported
		);
	}

	@Test public void test_STS_4140_StringArrayCompletion() throws Exception {
		defaultTestData();

		assertCompletion(
				"#some comment\n" +
				"flyway:\n" +
				"  sch<*>\n"
				, //=>
				"#some comment\n" +
				"flyway:\n" +
				"  schemas:\n" +
				"  - <*>\n"
		);
	}

	@Test public void testReconcileIntegerScalar() throws Exception {
		data("server.port", "java.lang.Integer", null, "Port of server");
		data("server.threads", "java.lang.Integer", null, "Number of threads for server threadpool");
		Editor editor = newEditor(
				"server:\n" +
				"  port: \n" +
				"    8888\n" +
				"  threads:\n" +
				"    not-a-number\n"
		);
		editor.assertProblems(
				"not-a-number|Expecting a 'int'"
		);
	}

	@Test public void testReconcileExpectMapping() throws Exception {
		defaultTestData();
		Editor editor = newEditor(
				"server:\n" +
				"  - a\n" +
				"  - b\n"
		);
		editor.assertProblems(
				"- a\n  - b|Expecting a 'Mapping' node but got a 'Sequence' node"
		);
	}

	@Test public void testReconcileExpectScalar() throws Exception {
		defaultTestData();
		Editor editor = newEditor(
				"server:\n" +
				"  ? - a\n" +
				"    - b\n" +
				"  : c"
		);
		editor.assertProblems(
				"- a\n    - b|Expecting a 'Scalar' node but got a 'Sequence' node"
		);
	}

	@Test public void testReconcileCamelCaseBasic() throws Exception {
		Editor editor;
		data("something.with-many-parts", "java.lang.Integer", "For testing tolerance of camelCase", null);
		data("something.with-parts.and-more", "java.lang.Integer", "For testing tolerance of camelCase", null);

		//Nothing special... not using camel case
		editor = newEditor(
				"#Comment for good measure\n" +
				"something:\n" +
				"  with-many-parts: not-a-number"
		);
		editor.assertProblems(
				"not-a-number|Expecting a 'int'"
		);

		//Now check that reconcile also tolerates camel case and reports no error for it
		editor = newEditor(
				"#Comment for good measure\n" +
				"something:\n" +
				"  withManyParts: 123"
		);
		editor.assertProblems(/*NONE*/);

		//Now check that reconciler traverses camelCase and reports errors assigning to camelCase names
		editor = newEditor(
				"#Comment for good measure\n" +
				"something:\n" +
				"  withManyParts: not-a-number"
		);
		editor.assertProblems(
				"not-a-number|Expecting a 'int'"
		);

		//Now check also that camelcase tolerance works if its not in the end of the path
		editor = newEditor(
				"#Comment for good measure\n" +
				"something:\n" +
				"  withParts:\n" +
				"    andMore: not-a-number\n" +
				"    bad: wrong\n" +
				"    something-bad: also wrong\n"
		);
		editor.assertProblems(
				"not-a-number|Expecting a 'int'",
				"bad|Unknown property",
				"something-bad|Unknown property"
		);
	}

	@Test public void testContentAssistCamelCaseBasic() throws Exception {
		data("something.with-many-parts", "java.lang.Integer", "For testing tolerance of camelCase", null);
		data("something.with-parts.and-more", "java.lang.Integer", "For testing tolerance of camelCase", null);

		assertCompletion(
				"something:\n" +
				"  withParts:\n" +
				"    <*>",
				// =>
				"something:\n" +
				"  withParts:\n" +
				"    and-more: <*>"
		);
	}

	@Test public void testReconcileCamelCaseBeanProp() throws Exception {
		Editor editor;

		IJavaProject p = createPredefinedMavenProject("tricky-getters-boot-1.3.1-app");
		useProject(p);

		data("demo.bean", "demo.CamelCaser", "For testing tolerance of camelCase", null);

		//Nothing special... not using camel case
		editor = newEditor(
				"#Comment for good measure\n" +
				"demo:\n" +
				"  bean:\n"+
				"    the-answer-to-everything: not-a-number\n"
		);
		editor.assertProblems(
				"not-a-number|Expecting a 'int'"
		);

		//Now check that reconcile also tolerates camel case and reports no error for it
		editor = newEditor(
				"#Comment for good measure\n" +
				"demo:\n" +
				"  bean:\n"+
				"    theAnswerToEverything: 42\n"
		);
		editor.assertProblems(/*NONE*/);

		//Now check that reconciler traverses camelCase and reports errors assigning to camelCase names
		editor = newEditor(
				"#Comment for good measure\n" +
				"demo:\n" +
				"  bean:\n"+
				"    theAnswerToEverything: not-a-number\n"
		);
		editor.assertProblems(
				"not-a-number|Expecting a 'int'"
		);

		//Now check also that camelcase tolerance works if its not in the end of the path
		editor = newEditor(
				"#Comment for good measure\n" +
				"demo:\n" +
				"  bean:\n"+
				"    theAnswerToEverything: not-a-number\n"+
				"    theLeft:\n"+
				"      bad: wrong\n"+
				"      theAnswerToEverything: not-this\n"
		);
		editor.assertProblems(
				"not-a-number|Expecting a 'int'",
				"bad|Unknown property",
				"not-this|Expecting a 'int'"
		);
	}

	@Test public void testContentAssistCamelCaseBeanProp() throws Exception {
		IJavaProject p = createPredefinedMavenProject("tricky-getters-boot-1.3.1-app");
		useProject(p);

		data("demo.bean", "demo.CamelCaser", "For testing tolerance of camelCase", null);

		assertCompletion(
				"demo:\n" +
				"  bean:\n" +
				"    theLeft:\n" +
				"      answer<*>\n",
				// =>
				"demo:\n" +
				"  bean:\n" +
				"    theLeft:\n" +
				"      the-answer-to-everything: <*>\n"
		);
	}

	@Ignore @Test public void testContentAssistCamelCaseInsertInExistingContext() throws Exception {
		data("demo-bean.first", "java.lang.String", "For testing tolerance of camelCase", null);
		data("demo-bean.second", "java.lang.String", "For testing tolerance of camelCase", null);

		//Confirm first it works correctly without camelizing complications
//		assertCompletion(
//				"demo-bean:\n" +
//				"  first: numero uno\n" +
//				"something-else: separator\n"+
//				"sec<*>",
//				// =>
//				"demo-bean:\n" +
//				"  first: numero uno\n" +
//				"  second: <*>\n" +
//				"something-else: separator\n"
//		);

		//Now with camels
		assertCompletion(
				"demoBean:\n" +
				"  first: numero uno\n" +
				"something-else: separator\n"+
				"sec<*>",
				// =>
				"demoBean:\n" +
				"  first: numero uno\n" +
				"  second: <*>\n" +
				"something-else: separator\n"
		);
	}


	@Test public void testReconcileBeanPropName() throws Exception {
		IJavaProject p = createPredefinedMavenProject("boot-1.2.1-app-properties-list-of-pojo");
		useProject(p);
		assertNotNull(p.getClasspath().findType("demo.Foo"));
		data("some-foo", "demo.Foo", null, "some Foo pojo property");
		Editor editor = newEditor(
				"some-foo:\n" +
				"  name: Good\n" +
				"  bogus: Bad\n" +
				"  ? - a\n"+
				"    - b\n"+
				"  : Weird\n"
		);
		editor.assertProblems(
				"bogus|Unknown property 'bogus' for type 'demo.Foo'",
				"- a\n    - b|Expecting a bean-property name for object of type 'demo.Foo' "
							+ "but got a 'Sequence' node"
		);
	}

	@Test public void testIgnoreSpringExpression() throws Exception {
		defaultTestData();
		Editor editor = newEditor(
				"server:\n" +
				"  port: ${random.int}\n" + //should not be an error
				"  bad: wrong\n"
		);
		editor.assertProblems(
				"bad|Unknown property"
		);
	}

	@Test public void testReconcilePojoArray() throws Exception {
		IJavaProject p = createPredefinedMavenProject("boot-1.2.1-app-properties-list-of-pojo");
		useProject(p);
		assertNotNull(p.getClasspath().findType("demo.Foo"));

		{
			Editor editor = newEditor(
					"token-bad-guy: problem\n"+
					"volder:\n" +
					"  foo:\n" +
					"    list:\n"+
					"      - name: Kris\n" +
					"        description: Kris\n" +
					"        roles:\n" +
					"          - Developer\n" +
					"          - Admin\n" +
					"        bogus: Bad\n"
			);

			editor.assertProblems(
					"token-bad-guy|Unknown property",
					//'name' is ok
					//'description' is ok
					"bogus|Unknown property 'bogus' for type 'demo.Foo'"
			);
		}

		{ //Pojo array can also be entered as a map with integer keys

			Editor editor = newEditor(
					"token-bad-guy: problem\n"+
					"volder:\n" +
					"  foo:\n" +
					"    list:\n"+
					"      0:\n"+
					"        name: Kris\n" +
					"        description: Kris\n" +
					"        roles:\n" +
					"          0: Developer\n" +
					"          one: Admin\n" +
					"        bogus: Bad\n"
			);

			editor.assertProblems(
					"token-bad-guy|Unknown property",
					"one|Expecting a 'int' but got 'one'",
					"bogus|Unknown property 'bogus' for type 'demo.Foo'"
			);

		}

	}

	@Test public void testReconcileSequenceGotAtomicType() throws Exception {
		defaultTestData();
		Editor editor = newEditor(
				"liquibase:\n" +
				"  enabled:\n" +
				"    - element\n"
		);
		editor.assertProblems(
				"- element|Expecting a 'boolean' but got a 'Sequence' node"
		);
	}

	@Test public void testReconcileSequenceGotMapType() throws Exception {
		data("the-map", "java.util.Map<java.lang.String,java.lang.String>", null, "Nice mappy");
		Editor editor = newEditor(
				"the-map:\n" +
				"  - a\n" +
				"  - b\n"
		);
		editor.assertProblems(
				"- a\n  - b|Expecting a 'Map<String, String>' but got a 'Sequence' node"
		);
	}

	@Test public void testEnumPropertyReconciling() throws Exception {
		IJavaProject p = createPredefinedMavenProject("enums-boot-1.3.2-app");
		useProject(p);
		assertNotNull(p.getClasspath().findType("demo.Color"));

		data("foo.color", "demo.Color", null, "A foonky colour");
		Editor editor = newEditor(
				"foo:\n"+
				"  color: BLUE\n" +
				"---\n" +
				"foo:\n" +
				"  color: RED\n" +
				"---\n" +
				"foo:\n" +
				"  color: GREEN\n" +
				"---\n" +
				"foo:\n" +
				"  color:\n" +
				"    bad: BLUE\n" +
				"---\n" +
				"foo:\n" +
				"  color: Bogus\n"
		);

		editor.assertProblems(
				"bad: BLUE|Expecting a 'demo.Color",
				"Bogus|Expecting a 'demo.Color"
		);

		/*
		 * TODO: if enums are not sorted by the 3rd party java indexing lib then
		 * perform the commented out test rather than the above
		 */
		//		editor.assertProblems(
//				"bad: BLUE|Expecting a 'demo.Color[RED, GREEN, BLUE]' but got a 'Mapping' node",
//				"Bogus|Expecting a 'demo.Color[RED, GREEN, BLUE]' but got 'Bogus'"
//		);

	}

	@Test public void testReconcileSkipIfNoMetadata() throws Exception {
		Editor editor = newEditor(
				"foo:\n"+
				"  color: BLUE\n" +
				"  color: RED\n" + //technically not allowed to bind same key twice but we don' check this
				"  color: GREEN\n" +
				"  color:\n" +
				"    bad: BLUE\n" +
				"  color: Bogus\n"
		);
		assertTrue(isEmptyMetadata());
		editor.assertProblems(/*NONE*/);
	}

	@Test public void testReconcileCatchesParseError() throws Exception {
		defaultTestData();
		Editor editor = newEditor(
				"somemap: val\n"+
				"- sequence"
		);
		editor.assertProblems(
				"-|expected <block end>"
		);
	}

	@Test public void testReconcileCatchesScannerError() throws Exception {
		defaultTestData();
		Editor editor = newEditor(
				"somemap: \"quotes not closed\n"
		);
		editor.assertProblems(
				"|unexpected end of stream"
		);
	}

	@Test public void testContentAssistSimple() throws Exception {
		defaultTestData();
		assertCompletion("port<*>",
				"server:\n"+
				"  port: <*>");
		assertCompletion(
				"#A comment\n" +
				"port<*>",
				"#A comment\n" +
				"server:\n"+
				"  port: <*>");
		assertCompletions(
				"server:\n" +
				"  nonsense<*>\n"
				//=> nothing
		);
	}

	@Test public void testContentAssistNullContext() throws Exception {
		defaultTestData();
		assertCompletions(
				"#A comment\n" +
				"foo:\n" +
				"  data:\n" +
				"    bogus:\n" +
				"      <*>"
				// => nothing
		);
	}

	@Test public void testContentAssistNested() throws Exception {
		data("server.port", "java.lang.Integer", null, "Server http port");
		data("server.address", "java.lang.String", "localhost", "Server host address");

		assertCompletion(
				"server:\n"+
				"  port: 8888\n" +
				"  <*>"
				,
				"server:\n"+
				"  port: 8888\n" +
				"  address: <*>"
		);

		assertCompletion(
					"server:\n"+
					"  <*>"
					,
					"server:\n"+
					"  address: <*>"
		);

		assertCompletion(
				"server:\n"+
				"  a<*>"
				,
				"server:\n"+
				"  address: <*>"
		);

		assertCompletion(
				"server:\n"+
				"  <*>\n" +
				"  port: 8888"
				,
				"server:\n"+
				"  address: <*>\n" +
				"  port: 8888"
		);

		assertCompletion(
				"server:\n"+
				"  a<*>\n" +
				"  port: 8888"
				,
				"server:\n"+
				"  address: <*>\n" +
				"  port: 8888"
		);

	}

	@Ignore @Test public void testContentAssistNestedSameLine() throws Exception {
		data("server.port", "java.lang.Integer", null, "Server http port");

		assertCompletion(
				"server: <*>"
				,
				"server: \n" +
				"  port: <*>"
		);

		assertCompletion(
				"#something before this stuff\n" +
				"server: <*>"
				,
				"#something before this stuff\n" +
				"server: \n" +
				"  port: <*>"
		);
	}

	@Ignore @Test public void testContentAssistInsertCompletionElsewhere() throws Exception {
		defaultTestData();

		assertCompletion(
				"server:\n" +
				"  port: 8888\n" +
				"  address: \n" +
				"  servlet-path: \n" +
				"spring:\n" +
				"  activemq:\n" +
				"something-else: great\n" +
				"aopauto<*>"
			,
				"server:\n" +
				"  port: 8888\n" +
				"  address: \n" +
				"  servlet-path: \n" +
				"spring:\n" +
				"  activemq:\n" +
				"  aop:\n" +
				"    auto: <*>\n" +
				"something-else: great\n"
		);

		assertCompletion(
					"server:\n"+
					"  address: localhost\n"+
					"something: nice\n"+
					"po<*>"
					,
					"server:\n"+
					"  address: localhost\n"+
					"  port: <*>\n" +
					"something: nice\n"
		);
	}

	@Ignore @Test public void testContentAssistInsertCompletionElsewhereInEmptyParent() throws Exception {
		data("server.port", "java.lang.Integer", null, "Server http port");
		data("server.address", "String", "localhost", "Server host address");

		assertCompletion(
				"#comment\n" +
				"server:\n" +
				"something:\n" +
				"  more\n" +
				"po<*>"
				,
				"#comment\n" +
				"server:\n" +
				"  port: <*>\n" +
				"something:\n" +
				"  more\n"
		);
	}

	@Ignore @Test public void testContentAssistInetAddress() throws Exception {
		//Test that InetAddress is treated as atomic w.r.t indentation

		defaultTestData();
		assertCompletion(
				"#set address of server\n" +
				"servadd<*>"
				, //=>
				"#set address of server\n" +
				"server:\n"+
				"  address: <*>"
		);

		assertCompletion(
				"#set address of server\n" +
				"server:\n"+
				"  port: 888\n" +
				"more: stuff\n" +
				"servadd<*>"
				, //=>
				"#set address of server\n" +
				"server:\n"+
				"  port: 888\n" +
				"  address: <*>\n" +
				"more: stuff\n"
		);

	}

	@Ignore @Test public void testContentAssistInsertCompletionElsewhereThatAlreadyExists() throws Exception {
		data("server.port", "java.lang.Integer", null, "Server http port");
		data("server.address", "String", "localhost", "Server host address");

		//inserting something that already exists should just move the cursor to existing node

		assertCompletion(
				"server:\n"+
				"  port:\n" +
				"    8888\n"+
				"  address: localhost\n"+
				"something: nice\n"+
				"po<*>"
				,
				"server:\n"+
				"  port:\n"+
				"    <*>8888\n" +
				"  address: localhost\n"+
				"something: nice\n"
		);

		assertCompletion(
				"server:\n"+
				"  port: 8888\n" +
				"  address: localhost\n"+
				"something: nice\n"+
				"po<*>"
				,
				"server:\n"+
				"  port: <*>8888\n" +
				"  address: localhost\n"+
				"something: nice\n"
		);

		assertCompletion(
				"server:\n"+
				"  port:\n"+
				"  address: localhost\n"+
				"something: nice\n"+
				"po<*>"
				,
				"server:\n"+
				"  port:<*>\n" +
				"  address: localhost\n"+
				"something: nice\n"
		);
	}


	@Ignore @Test public void testContentAssistPropertyWithMapType() throws Exception {
		data("foo.mapping", "java.util.Map<java.lang.String,java.lang.String>", null, "Nice little map");

		//Try in-place completion
		assertCompletion(
				"map<*>"
				,
				"foo:\n"+
				"  mapping:\n"+
				"    <*>"
		);

		//Try 'elswhere' completion
		assertCompletion(
				"foo:\n" +
				"  something:\n" +
				"more: stuff\n" +
				"map<*>"
				,
				"foo:\n" +
				"  something:\n" +
				"  mapping:\n" +
				"    <*>\n" +
				"more: stuff\n"
		);
	}

	@Ignore @Test public void testContentAssistPropertyWithArrayType() throws Exception {
		data("foo.list", "java.util.List<java.lang.String>", null, "Nice little list");

		//Try in-place completion
		assertCompletion(
				"lis<*>"
				,
				"foo:\n"+
				"  list:\n"+
				"  - <*>"
		);

		//Try 'elsewhere' completion
		assertCompletion(
				"foo:\n" +
				"  something:\n" +
				"more: stuff\n" +
				"lis<*>"
				,
				"foo:\n" +
				"  something:\n" +
				"  list:\n" +
				"  - <*>\n" +
				"more: stuff\n"
		);
	}

	@Ignore @Test public void testContentAssistPropertyWithPojoType() throws Exception {
		useProject(createPredefinedMavenProject("enums-boot-1.3.2-app"));

		//Try in-place completion
		assertCompletion(
				"foo.d<*>"
				,
				"foo:\n" +
				"  data:\n" +
				"    <*>"
		);

		//Try 'elsewhere' completion
		assertCompletion(
				"foo:\n" +
				"  something:\n" +
				"more: stuff\n" +
				"foo.d<*>"
				,
				"foo:\n" +
				"  something:\n" +
				"  data:\n" +
				"    <*>\n" +
				"more: stuff\n"
		);
	}

	@Ignore @Test public void testContentAssistPropertyWithEnumType() throws Exception {
		useProject(createPredefinedMavenProject("enums-boot-1.3.2-app"));

		//Try in-place completion
		assertCompletion(
				"foo.co<*>"
				,
				"foo:\n" +
				"  color: <*>"
		);

		//Try 'elsewhere' completion
		assertCompletion(
				"foo:\n" +
				"  something:\n" +
				"more: stuff\n" +
				"foo.co<*>"
				,
				"foo:\n" +
				"  something:\n" +
				"  color: <*>\n" +
				"more: stuff\n"
		);
	}

	@Ignore @Test public void testCompletionForExistingGlobalPropertiesAreDemoted() throws Exception {
		data("foo.bar", "java.lang.String", null, null);
		data("foo.buttar", "java.lang.String", null, null);
		data("foo.baracks", "java.lang.String", null, null);
		data("foo.zamfir", "java.lang.String", null, null);
		assertCompletions(
				"foo:\n" +
				"  bar: nice\n" +
				"  baracks: full\n" +
				"something:\n" +
				"  in: between\n"+
				"bar<*>",
				//=>
				// buttar
				"foo:\n" +
				"  bar: nice\n" +
				"  baracks: full\n" +
				"  buttar: <*>\n" +
				"something:\n" +
				"  in: between",
				// bar (already existed so becomes navigation)
				"foo:\n" +
				"  bar: <*>nice\n" +
				"  baracks: full\n" +
				"something:\n" +
				"  in: between",
				// baracks (already existed so becomes navigation)
				"foo:\n" +
				"  bar: nice\n" +
				"  baracks: <*>full\n" +
				"something:\n" +
				"  in: between"
		);
	}

	@Ignore @Test public void testCompletionForExistingBeanPropertiesAreDemoted() throws Exception {
		useProject(createPredefinedMavenProject("enums-boot-1.3.2-app"));
		assertCompletions(
				"foo:\n" +
				"  data:\n" +
				"    children:\n" +
				"      -\n" +
				"    name: foo\n" +
				"    <*>",
				///// non-existing ///
				//color-children
				"foo:\n" +
				"  data:\n" +
				"    children:\n" +
				"      -\n" +
				"    name: foo\n" +
				"    color-children:\n"+
				"      <*>",
				//funky
				"foo:\n" +
				"  data:\n" +
				"    children:\n" +
				"      -\n" +
				"    name: foo\n" +
				"    funky: <*>",
				//mapped-children
				"foo:\n" +
				"  data:\n" +
				"    children:\n" +
				"      -\n" +
				"    name: foo\n" +
				"    mapped-children:\n"+
				"      <*>",
				//nested
				"foo:\n" +
				"  data:\n" +
				"    children:\n" +
				"      -\n" +
				"    name: foo\n" +
				"    nested:\n"+
				"      <*>",
				//next
				"foo:\n" +
				"  data:\n" +
				"    children:\n" +
				"      -\n" +
				"    name: foo\n" +
				"    next: <*>",
				//tags
				"foo:\n" +
				"  data:\n" +
				"    children:\n" +
				"      -\n" +
				"    name: foo\n" +
				"    tags:\n" +
				"    - <*>",
				//wavelen
				"foo:\n" +
				"  data:\n" +
				"    children:\n" +
				"      -\n" +
				"    name: foo\n" +
				"    wavelen: <*>",
				///// existing ////
				//children
				"foo:\n" +
				"  data:\n" +
				"    children:\n" +
				"      <*>-\n" +
				"    name: foo",
				//name
				"foo:\n" +
				"  data:\n" +
				"    children:\n" +
				"      -\n" +
				"    name: <*>foo"
		);
	}

	@Test public void testNoCompletionsInsideComments() throws Exception {
		defaultTestData();

		//Ensure this test is not trivially passing because of missing test data
		assertCompletion(
				"po<*>"
				,
				"server:\n"+
				"  port: <*>"
		);

		assertNoCompletions(
				"#po<*>"
		);
	}

	@Ignore @Test public void testCompletionsFromDeeplyNestedNode() throws Exception {
		String[] names = {"foo", "nested", "bar"};
		int levels = 4;
		generateNestedProperties(levels, names, "");

		assertCompletionCount(81, // 3^4
				"<*>"
		);

		assertCompletionCount(27, // 3^3
				"#comment\n" +
				"foo:\n" +
				"  <*>"
		);

		assertCompletionCount( 9, // 3^2
				"#comment\n" +
				"foo:\n" +
				"  bar: <*>"
		);

		assertCompletionCount( 3,
				"#comment\n" +
				"foo:\n" +
				"  bar:\n"+
				"    nested:\n" +
				"      <*>"
		);

		assertCompletionCount( 9,
				"#comment\n" +
				"foo:\n" +
				"  bar:\n"+
				"    nested:\n" +
				"    <*>"
		);

		assertCompletionCount(27,
				"#comment\n" +
				"foo:\n" +
				"  bar:\n"+
				"    nested:\n" +
				"  <*>"
		);

		assertCompletionCount(81,
				"#comment\n" +
				"foo:\n" +
				"  bar:\n"+
				"    nested:\n" +
				"<*>"
		);


		assertCompletion(
				"#comment\n" +
				"foo:\n" +
				"  bar:\n"+
				"    nested:\n" +
				"      <*>"
				,
				"#comment\n" +
				"foo:\n" +
				"  bar:\n"+
				"    nested:\n" +
				"      bar: <*>"
		);

		assertCompletion(
				"#comment\n" +
				"foo:\n" +
				"  bar:\n"+
				"    nested:\n" +
				"    <*>"
				,
				"#comment\n" +
				"foo:\n" +
				"  bar:\n"+
				"    nested:\n" +
				"    bar:\n" +
				"      bar: <*>"
		);

		assertCompletion(
				"#comment\n" +
				"foo:\n" +
				"  bar:\n"+
				"    nested:\n" +
				"  <*>"
				,
				"#comment\n" +
				"foo:\n" +
				"  bar:\n"+
				"    nested:\n" +
				"    bar:\n" +
				"      bar: <*>\n" +
				"  "
		);

		assertCompletion(
				"#comment\n" +
				"foo:\n" +
				"  bar:\n"+
				"    nested:\n" +
				"<*>"
				,
				"#comment\n" +
				"foo:\n" +
				"  bar:\n"+
				"    nested:\n" +
				"bar:\n" +
				"  bar:\n" +
				"    bar:\n" +
				"      bar: <*>"
		);
	}

	@Ignore @Test public void testInsertCompletionIntoDeeplyNestedNode() throws Exception {
		String[] names = {"foo", "nested", "bar"};
		int levels = 4;
		generateNestedProperties(levels, names, "");

		assertCompletion(
				"foo:\n" +
				"  nested:\n" +
				"    bar:\n" +
				"      foo:\n" +
				"bar.foo.nested.b<*>"
				,
				"foo:\n" +
				"  nested:\n" +
				"    bar:\n" +
				"      foo:\n" +
				"bar:\n" +
				"  foo:\n" +
				"    nested:\n" +
				"      bar: <*>"
		);

		assertCompletion(
				"foo:\n" +
				"  nested:\n" +
				"    bar:\n" +
				"      foo:\n" +
				"other:\n" +
				"foo.foo.nested.b<*>"
				,
				"foo:\n" +
				"  nested:\n" +
				"    bar:\n" +
				"      foo:\n" +
				"  foo:\n" +
				"    nested:\n" +
				"      bar: <*>\n" +
				"other:\n"
		);

		assertCompletion(
				"foo:\n" +
				"  nested:\n" +
				"    bar:\n" +
				"      foo:\n" +
				"foo.foo.nested.b<*>"
				,
				"foo:\n" +
				"  nested:\n" +
				"    bar:\n" +
				"      foo:\n" +
				"  foo:\n" +
				"    nested:\n" +
				"      bar: <*>"
		);

		assertCompletion(
				"foo:\n" +
				"  nested:\n" +
				"    bar:\n" +
				"      foo:\n" +
				"other:\n" +
				"foo.nested.nested.b<*>"
				,
				"foo:\n" +
				"  nested:\n" +
				"    bar:\n" +
				"      foo:\n" +
				"    nested:\n" +
				"      bar: <*>\n"+
				"other:\n"
		);

		assertCompletion(
				"foo:\n" +
				"  nested:\n" +
				"    bar:\n" +
				"      foo:\n" +
				"foo.nested.nested.b<*>"
				,
				"foo:\n" +
				"  nested:\n" +
				"    bar:\n" +
				"      foo:\n" +
				"    nested:\n" +
				"      bar: <*>\n"
		);

		assertCompletion(
				"foo:\n" +
				"  nested:\n" +
				"    bar:\n" +
				"      foo:\n" +
				"other:\n" +
				"foo.nested.bar.b<*>"
				,
				"foo:\n" +
				"  nested:\n" +
				"    bar:\n" +
				"      foo:\n" +
				"      bar: <*>\n" +
				"other:\n"
		);

		assertCompletion(
				"foo:\n" +
				"  nested:\n" +
				"    bar:\n" +
				"      foo:\n" +
				"foo.nested.bar.b<*>"
				,
				"foo:\n" +
				"  nested:\n" +
				"    bar:\n" +
				"      foo:\n" +
				"      bar: <*>\n"
		);

	}

	@Test public void testBooleanValueCompletion() throws Exception {
		defaultTestData();
		assertCompletions(
				"liquibase:\n" +
				"  enabled: <*>",
				"liquibase:\n" +
				"  enabled: false<*>",
				"liquibase:\n" +
				"  enabled: true<*>"
		);

		assertCompletions(
				"liquibase:\n" +
				"  enabled:\n" +
				"    <*>",
				"liquibase:\n" +
				"  enabled:\n" +
				"    false<*>",
				"liquibase:\n" +
				"  enabled:\n" +
				"    true<*>"
		);

		assertCompletions(
				"liquibase:\n" +
				"  enabled: f<*>\n",
				"liquibase:\n" +
				"  enabled: false<*>\n"
		);

		assertCompletions(
				"liquibase:\n" +
				"  enabled: t<*>\n",
				"liquibase:\n" +
				"  enabled: true<*>\n"
		);

		assertCompletions(
				"liquibase:\n" +
				"  enabled:\n" +
				"    f<*>\n",
				"liquibase:\n" +
				"  enabled:\n"+
				"    false<*>\n"
		);

		assertCompletions(
				"liquibase:\n" +
				"  enabled:\n" +
				"    t<*>\n",
				"liquibase:\n" +
				"  enabled:\n"+
				"    true<*>\n"
		);

		// booleans can also be completed in upper case?
		assertCompletions(
				"liquibase:\n" +
				"  enabled:\n" +
				"    T<*>\n",
				"liquibase:\n" +
				"  enabled:\n" +
				"    TRUE<*>\n"
		);

		assertCompletions(
				"liquibase:\n" +
				"  enabled:\n" +
				"    F<*>\n",
				"liquibase:\n" +
				"  enabled:\n" +
				"    FALSE<*>\n"
		);

		//Dont suggest completion for something that's already complete. Causes odd
		// and annoying behavior, like a completion popup after you stopped typing 'true'
		assertNoCompletions(
				"liquibase:\n" +
				"  enabled:\n" +
				"    true<*>"
		);

		//one more... for special char like '-' in the name

		assertCompletions(
				"liquibase:\n" +
				"  check-change-log-location: t<*>",
				"liquibase:\n" +
				"  check-change-log-location: true<*>"
		);

		assertCompletions(
				"liquibase:\n" +
				"  enabled:<*>",
				"liquibase:\n" +
				"  enabled: false<*>",
				"liquibase:\n" +
				"  enabled: true<*>"
		);


	}

	@Test public void testEnumValueCompletion() throws Exception {
		useProject(createPredefinedMavenProject("enums-boot-1.3.2-app"));
		data("foo.color", "demo.Color", null, "A foonky colour");

		assertCompletion("foo.c<*>",
				"foo:\n" +
				"  color: <*>" //Should complete on same line because enums are 'simple' values.
		);

		assertCompletion("foo:\n  color: R<*>", "foo:\n  color: RED<*>");
		assertCompletion("foo:\n  color: G<*>", "foo:\n  color: GREEN<*>");
		assertCompletion("foo:\n  color: B<*>", "foo:\n  color: BLUE<*>");

		assertCompletion("foo:\n  color: r<*>", "foo:\n  color: red<*>");
		assertCompletion("foo:\n  color: g<*>", "foo:\n  color: green<*>");
		assertCompletion("foo:\n  color: b<*>", "foo:\n  color: blue<*>");

		assertCompletionsDisplayString("foo:\n  color: <*>",
				"red", "green", "blue"
		);
	}

	@Test public void testEnumMapValueCompletion() throws Exception {
		useProject(createPredefinedMavenProject("enums-boot-1.3.2-app"));

		assertCompletions(
				"foo:\n" +
				"  nam<*>",
				//==>
				"foo:\n" +
				"  name-colors:\n"+
				"    <*>",
				// or
				"foo:\n" +
				"  color-names:\n"+
				"    <*>"
		);
		assertCompletionsDisplayString(
				"foo:\n"+
				"  name-colors:\n" +
				"    something: <*>",
				//=>
				"red", "green", "blue"
		);
		assertCompletions(
				"foo:\n"+
				"  name-colors:\n" +
				"    something: G<*>",
				// =>
				"foo:\n"+
				"  name-colors:\n" +
				"    something: GREEN<*>"
		);
	}

	@Test public void testEnumMapValueReconciling() throws Exception {
		useProject(createPredefinedMavenProject("enums-boot-1.3.2-app"));
		data("foo.name-colors", "java.util.Map<java.lang.String,demo.Color>", null, "Map with colors in its values");

		Editor editor;

		editor = newEditor(
				"foo:\n"+
				"  name-colors:\n" +
				"    jacket: BLUE\n" +
				"    hat: RED\n" +
				"    pants: GREEN\n" +
				"    wrong: NOT_A_COLOR\n"
		);
		editor.assertProblems(
				"NOT_A_COLOR|Color"
		);

		//lowercase enums should work too
		editor = newEditor(
				"foo:\n"+
				"  name-colors:\n" +
				"    jacket: blue\n" +
				"    hat: red\n" +
				"    pants: green\n" +
				"    wrong: NOT_A_COLOR\n"
		);
		editor.assertProblems(
				"NOT_A_COLOR|Color"
		);
	}

	@Ignore @Test public void testEnumMapKeyCompletion() throws Exception {
		useProject(createPredefinedMavenProject("enums-boot-1.3.2-app"));

		data("foo.color-names", "java.util.Map<demo.Color,java.lang.String>", null, "Map with colors in its keys");
		data("foo.color-data", "java.util.Map<demo.Color,demo.ColorData>", null, "Map with colors in its keys, and pojo in values");

		//Map Enum -> String:
		assertCompletions("foo:\n  colnam<*>",
				"foo:\n" +
				"  color-names:\n" +
				"    <*>");
		assertCompletions(
				"foo:\n" +
				"  color-names:\n" +
				"    <*>",
				//=>
				"foo:\n" +
				"  color-names:\n" +
				"    blue: <*>",
				"foo:\n" +
				"  color-names:\n" +
				"    green: <*>",
				"foo:\n" +
				"  color-names:\n" +
				"    red: <*>"
		);

		assertCompletionsDisplayString(
				"foo:\n" +
				"  color-names:\n" +
				"    <*>",
				//=>
				"blue : String", "green : String", "red : String"
		);

		assertCompletions(
				"foo:\n" +
				"  color-names:\n" +
				"    B<*>",
				"foo:\n" +
				"  color-names:\n" +
				"    BLUE: <*>"
		);

		//Map Enum -> Pojo:
		assertCompletions("foo.coldat<*>",
				"foo:\n" +
				"  color-data:\n" +
				"    <*>");
		assertCompletions(
				"foo:\n" +
				"  color-data:\n" +
				"    <*>",
				// =>
				"foo:\n" +
				"  color-data:\n" +
				"    blue:\n" +
				"      <*>",
				"foo:\n" +
				"  color-data:\n" +
				"    green:\n" +
				"      <*>",
				"foo:\n" +
				"  color-data:\n" +
				"    red:\n" +
				"      <*>"
		);
		assertCompletions(
				"foo:\n" +
				"  color-data:\n" +
				"    B<*>",
				//=>
				"foo:\n" +
				"  color-data:\n" +
				"    BLUE:\n" +
				"      <*>"
		);

		assertCompletions(
				"foo:\n" +
				"  color-data:\n" +
				"    b<*>",
				//=>
				"foo:\n" +
				"  color-data:\n" +
				"    blue:\n" +
				"      <*>"
		);

		assertCompletions(
				"foo:\n" +
				"  color-data: b<*>",
				//=>
				"foo:\n" +
				"  color-data: \n" +
				"    blue:\n" +
				"      <*>"
		);

		assertCompletionsDisplayString(
				"foo:\n" +
				"  color-data:\n" +
				"    <*>",
				"red : demo.ColorData", "green : demo.ColorData", "blue : demo.ColorData"
		);

		assertCompletionsDisplayString(
				"foo:\n" +
				"  color-data: <*>\n",
				"red : demo.ColorData", "green : demo.ColorData", "blue : demo.ColorData"
		);

	}

	@Test public void testPojoReconciling() throws Exception {
		useProject(createPredefinedMavenProject("enums-boot-1.3.2-app"));

		Editor editor = newEditor(
			"foo:\n" +
			"  data:\n" +
			"    bogus: Something\n" +
			"    wavelen: 3.0\n" +
			"    wavelen: not a double\n" +
			"    wavelen:\n"+
			"      more: 3.0\n"+
			"    wavelen:\n" +
			"      - 3.0\n"
		);
		editor.assertProblems(
				"bogus|Unknown property",
				"wavelen|Duplicate",
				"wavelen|Duplicate",
				"not a double|'double'",
				"wavelen|Duplicate",
				"more: 3.0|Expecting a 'double' but got a 'Mapping' node",
				"wavelen|Duplicate",
				"- 3.0|Expecting a 'double' but got a 'Sequence' node"
		);
	}


	@Test public void testListOfAtomicCompletions() throws Exception {
		data("foo.slist", "java.util.List<java.lang.String>", null, "list of strings");
		data("foo.ulist", "java.util.List<Unknown>", null, "list of strings");
		data("foo.dlist", "java.util.List<java.lang.Double>", null, "list of doubles");
		assertCompletions("foo:\n  u<*>",
				"foo:\n" +
				"  ulist:\n" +
				"  - <*>");
		assertCompletions("foo:\n  d<*>",
				"foo:\n" +
				"  dlist:\n" +
				"  - <*>");
		assertCompletions("foo:\n  sl<*>",
				"foo:\n"+
				"  slist:\n" +
				"  - <*>");
	}

	@Test public void testEnumsInLowerCaseReconciling() throws Exception {
		useProject(createPredefinedMavenProject("enums-boot-1.3.2-app"));

		data("simple.pants.size", "demo.ClothingSize", null, "The simple pant's size");

		Editor editor = newEditor(
				"simple:\n" +
				"  pants:\n"+
				"    size: NOT_A_SIZE\n"+
				"    size: EXTRA_SMALL\n"+
				"    size: extra-small\n"+
				"    size: small\n"+
				"    size: SMALL\n"
		);
		editor.assertProblems(
				"size|Duplicate",
				"NOT_A_SIZE|ClothingSize",
				"size|Duplicate",
				"size|Duplicate",
				"size|Duplicate",
				"size|Duplicate"
		);

		editor = newEditor(
				"foo:\n" +
				"  color-names:\n"+
				"    red: Rood\n"+
				"    green: Groen\n"+
				"    blue: Blauw\n" +
				"    not-a-color: Wrong\n" +
				"    blue.bad: Blauw\n" +
				"    blue:\n" +
				"      bad: Blauw"
		);
		editor.assertProblems(
				"blue|Duplicate",
				"not-a-color|Color",
				"blue.bad|Color",
				"blue|Duplicate",
				"bad: Blauw|Expecting a 'String' but got a 'Mapping'"
		);

		editor = newEditor(
				"foo:\n" +
				"  color-data:\n"+
				"    red:\n" +
				"      next: green\n" +
				"      next: not a color\n" +
				"      bogus: green\n" +
				"      name: Rood\n"
		);
		editor.assertProblems(
				"next|Duplicate",
				"next|Duplicate",
				"not a color|Color",
				"bogus|Unknown property"
		);
	}

	@Ignore @Test public void testEnumsInLowerCaseContentAssist() throws Exception {
		IJavaProject p = createPredefinedMavenProject("enums-boot-1.3.2-app");
		useProject(p);
		assertNotNull(p.getClasspath().findType("demo.ClothingSize"));

		data("simple.pants.size", "demo.ClothingSize", null, "The simple pant's size");

		assertCompletions(
				"simple:\n" +
				"  pants:\n"+
				"    size: S<*>",
				"simple:\n" +
				"  pants:\n"+
				"    size: SMALL<*>",
				"simple:\n" +
				"  pants:\n"+
				"    size: EXTRA_SMALL<*>"
		);

		assertCompletions(
				"simple:\n" +
				"  pants:\n"+
				"    size: s<*>",
				"simple:\n" +
				"  pants:\n"+
				"    size: small<*>",
				"simple:\n" +
				"  pants:\n"+
				"    size: extra-small<*>"
		);

		assertCompletions(
				"simple:\n" +
				"  pants:\n"+
				"    size: ex<*>",
				// =>
				"simple:\n" +
				"  pants:\n"+
				"    size: extra-large<*>",
				// or
				"simple:\n" +
				"  pants:\n"+
				"    size: extra-small<*>"
		);

		assertCompletions(
				"simple:\n" +
				"  pants:\n"+
				"    size: EX<*>",
				// =>
				"simple:\n" +
				"  pants:\n"+
				"    size: EXTRA_LARGE<*>",
				// or
				"simple:\n" +
				"  pants:\n"+
				"    size: EXTRA_SMALL<*>"
		);

		assertCompletionsDisplayString("foo:\n  color: <*>", "red", "green", "blue");

		assertCompletions("foo:\n  color-data: B<*>", "foo:\n  color-data: \n    BLUE:\n      <*>");
		assertCompletions("foo:\n  color-data: b<*>", "foo:\n  color-data: \n    blue:\n      <*>");
		assertCompletions("foo:\n  color-data: <*>",
				"foo:\n  color-data: \n    blue:\n      <*>",
				"foo:\n  color-data: \n    green:\n      <*>",
				"foo:\n  color-data: \n    red:\n      <*>"
		);

		assertCompletions(
				"foo:\n"+
				"  color-data:\n"+
				"    red: na<*>",
				"foo:\n"+
				"  color-data:\n"+
				"    red: \n"+
				"      name: <*>"
		);
	}

	@Ignore @Test public void testPojoInListCompletion() throws Exception {
		useProject(createPredefinedMavenProject("enums-boot-1.3.2-app"));

		assertCompletion(
				"foo:\n" +
				"  color-data:\n" +
				"    red: chi<*>"
				,// =>
				"foo:\n" +
				"  color-data:\n" +
				"    red: \n" +
				"      children:\n" +
				"      - <*>"
		);

		assertCompletions(
				"foo:\n" +
				"  color-data:\n" +
				"    red:\n" +
				"      children:\n" +
				"        - nex<*>",
				// =>
				"foo:\n" +
				"  color-data:\n" +
				"    red:\n" +
				"      children:\n" +
				"        - next: <*>"
		);

		assertCompletions(
				"foo:\n" +
				"  color-data:\n" +
				"    red:\n" +
				"      children:\n" +
				"        - nes<*>",
				// =>
				"foo:\n" +
				"  color-data:\n" +
				"    red:\n" +
				"      children:\n" +
				"        - nested:\n"+
				"            <*>"
		);

		assertCompletions(
				"foo:\n" +
				"  color-data:\n" +
				"    red:\n" +
				"      children:\n" +
				"      - nex<*>",
				// =>
				"foo:\n" +
				"  color-data:\n" +
				"    red:\n" +
				"      children:\n" +
				"      - next: <*>"
		);

		assertCompletions(
				"foo:\n" +
				"  color-data:\n" +
				"    red:\n" +
				"      children:\n" +
				"      - nes<*>",
				// =>
				"foo:\n" +
				"  color-data:\n" +
				"    red:\n" +
				"      children:\n" +
				"      - nested:\n"+
				"          <*>"
		);

		assertCompletions(
				"foo:\n" +
				"  color-data:\n" +
				"    red:\n" +
				"      children:\n" +
				"        - next: RED\n" +
				"          wav<*>",
				// =>
				"foo:\n" +
				"  color-data:\n" +
				"    red:\n" +
				"      children:\n" +
				"        - next: RED\n" +
				"          wavelen: <*>"
		);

		assertCompletions(
				"foo:\n" +
				"  color-data:\n" +
				"    red:\n" +
				"      children:\n" +
				"      - next: RED\n" +
				"        wav<*>",
				// =>
				"foo:\n" +
				"  color-data:\n" +
				"    red:\n" +
				"      children:\n" +
				"      - next: RED\n" +
				"        wavelen: <*>"
		);
	}

	@Ignore @Test public void test_STS4111_NoEmptyLinesGapBeforeInsertedCompletion() throws Exception {
		data("spring.application.name", "java.lang.String", null, "The name of the application");
		data("spring.application.index", "java.lang.Integer", true, "App instance index");
		data("spring.considerable.fun", "java.lang.Boolean", true, "Whether the spring fun is considerable");

		assertCompletions(
				"spring:\n" +
				"  application:\n" +
				"    index: 12\n" +
				"\n" +
				"server:\n" +
				"  port: 8888\n" +
				"appname<*>"
				, //=>
				"spring:\n" +
				"  application:\n" +
				"    index: 12\n" +
				"    name: <*>\n" +
				"\n" +
				"server:\n" +
				"  port: 8888"

		);

		//Also test that:
		// - Fully commented lines also count as gaps
		// - Gaps of more than one line are also handled correctly
		assertCompletions(
				"# spring stuff\n" +
				"spring:\n" +
				"  application:\n" +
				"    index: 12\n" +
				"\n" +
				"#server stuff\n" +
				"server:\n" +
				"  port: 8888\n" +
				"cfun<*>"
				, //=>
				"# spring stuff\n" +
				"spring:\n" +
				"  application:\n" +
				"    index: 12\n" +
				"  considerable:\n" +
				"    fun: <*>\n" +
				"\n" +
				"#server stuff\n" +
				"server:\n" +
				"  port: 8888"

		);

	}

	@Test public void testDocumentSeparator() throws Exception {
		defaultTestData();

		assertCompletion(
				"flyway:\n" +
				"  encoding: utf8\n" +
				"---\n" +
				"flyena<*>",
				// =>
				"flyway:\n" +
				"  encoding: utf8\n" +
				"---\n" +
				"flyway:\n" +
				"  enabled: <*>"
		);
	}

	@Test public void testMultiProfileYamlReconcile() throws Exception {
		Editor editor;
		//See https://issuetracker.springsource.com/browse/STS-4144
		defaultTestData();

		//Narrowly focussed test-case for easier debugging
		editor = newEditor(
				"spring:\n" +
				"  profiles: seven\n"
		);
		editor.assertProblems(/*NONE*/);

		//More complete test
		editor = newEditor(
				"spring:\n" +
				"  profiles: seven\n" +
				"server:\n" +
				"  port: 7777\n" +
				"---\n" +
				"spring:\n" +
				"  profiles: eight\n" +
				"server:\n" +
				"  port: 8888\n"+
				"bogus: bad"
		);
		editor.assertProblems(
				"bogus|Unknown property"
		);
	}

	@Test public void testReconcileArrayOfPrimitiveType() throws Exception {
		data("my.array", "int[]", null, "A primitive array");
		data("my.boxarray", "int[]", null, "An array of boxed primitives");
		data("my.list", "java.util.List<java.lang.Integer>", null, "A list of boxed types");

		Editor editor = newEditor(
				"my:\n" +
				"  array:\n" +
				"    - 777\n" +
				"    - bad\n" +
				"    - 888"
		);
		editor.assertProblems(
				"bad|Expecting a 'int'"
		);

		editor = newEditor(
				"my:\n" +
				"  boxarray:\n" +
				"    - 777\n" +
				"    - bad\n" +
				"    - 888"
		);
		editor.assertProblems(
				"bad|Expecting a 'int'"
		);

		editor = newEditor(
				"my:\n" +
				"  list:\n" +
				"    - 777\n" +
				"    - bad\n" +
				"    - 888"
		);
		editor.assertProblems(
				"bad|Expecting a 'int'"
		);
	}

	@Ignore @Test public void test_STS4231() throws Exception {
		//Should the 'predefined' project need to be recreated... use the commented code below:
//		BootProjectTestHarness projectHarness = new BootProjectTestHarness(ResourcesPlugin.getWorkspace());
//		IJavaProject project = projectHarness.createBootProject("boot-1.3.0-app-sts-4231",
//				bootVersionAtLeast("1.3.0"),
//				withStarters("web", "cloud-config-server")
//		);

		//For more robust test use predefined project which is not so much a moving target:
		IJavaProject project = createPredefinedMavenProject("boot-1.3.0-app-sts-4231");
		useProject(project);

		assertCompletionsDisplayString(
				"info:\n" +
				"  component: Config Server\n" +
				"spring:\n" +
				"  application:\n" +
				"    name: configserver\n" +
				"  jmx:\n" +
				"    default-domain: cloud.config.server\n" +
				"  cloud:\n" +
				"    config:\n" +
				"      server:\n" +
				"        git:\n" +
				"          uri: https://github.com/spring-cloud-samples/config-repo\n" +
				"          repos:\n" +
				"            my-repo:\n" +
				"              <*>\n",
				// ==>
				"name : String",
				"pattern : String[]"
		);

		assertCompletion(
				"info:\n" +
				"  component: Config Server\n" +
				"spring:\n" +
				"  application:\n" +
				"    name: configserver\n" +
				"  jmx:\n" +
				"    default-domain: cloud.config.server\n" +
				"  cloud:\n" +
				"    config:\n" +
				"      server:\n" +
				"        git:\n" +
				"          uri: https://github.com/spring-cloud-samples/config-repo\n" +
				"          repos:\n" +
				"            my-repo:\n" +
				"              p<*>\n",
				// ==>
				"info:\n" +
				"  component: Config Server\n" +
				"spring:\n" +
				"  application:\n" +
				"    name: configserver\n" +
				"  jmx:\n" +
				"    default-domain: cloud.config.server\n" +
				"  cloud:\n" +
				"    config:\n" +
				"      server:\n" +
				"        git:\n" +
				"          uri: https://github.com/spring-cloud-samples/config-repo\n" +
				"          repos:\n" +
				"            my-repo:\n" +
				"              pattern:\n" +
				"              - <*>\n"
		);

	}

	@Test public void test_STS_4254_MapStringObjectReconciling() throws Exception {
		Editor editor;
		data("info", "java.util.Map<java.lang.String,java.lang.Object>", null, "Info for the actuator's info endpoint.");

		editor = newEditor(
				"info: not-a-map\n"
		);
		editor.assertProblems(
				"not-a-map|Expecting a 'Map<String, Object>'"
		);

		editor= newEditor(
				"info:\n" +
				"  build: \n" +
				"    artifact: foo-bar\n"
		);
		editor.assertProblems(/*NONE*/);

		editor= newEditor(
				"info:\n" +
				"  more: \n" +
				"    deeply:\n" +
				"      nested: foo-bar\n"
		);
		editor.assertProblems(/*NONE*/);

		editor= newEditor(
				"booger: Bad\n" +
				"info:\n" +
				"  akey: avalue\n"
		);
		editor.assertProblems(
				"booger|Unknown property"
		);
	}

	@Test public void test_STS_4254_MapStringStringReconciling() throws Exception {
		Editor editor;
		data("info", "java.util.Map<java.lang.String,java.lang.String>", null, "Info for the actuator's info endpoint.");

		editor= newEditor(
				"info:\n" +
				"  build: \n" +
				"    artifact: foo-bar\n"
		);
		editor.assertProblems(/*NONE*/);

		editor= newEditor(
				"info:\n" +
				"  more: \n" +
				"    deeply:\n" +
				"      nested: foo-bar\n"
		);
		editor.assertProblems(/*NONE*/);

		editor= newEditor(
				"booger: Bad\n" +
				"info:\n" +
				"  akey: avalue\n"
		);
		editor.assertProblems(
				"booger|Unknown property"
		);

	}

	@Test public void test_STS_4254_MapStringIntegerReconciling() throws Exception {
		Editor editor;
		data("info", "java.util.Map<java.lang.String,java.lang.Integer>", null, "Info for the actuator's info endpoint.");

		editor= newEditor(
				"info:\n" +
				"  build: \n" +
				"    foo-bar\n"
		);
		editor.assertProblems(
				"foo-bar|Expecting a 'int'"
		);

		editor= newEditor(
				"info:\n" +
				"  build: 123\n"
		);
		editor.assertProblems(/*NONE*/);

		editor= newEditor(
				"info:\n" +
				"  build: \n" +
				"    number: 123\n"
		);
		editor.assertProblems(/*NONE*/);

		editor= newEditor(
				"info:\n" +
				"  build: \n" +
				"    artifact: abc\n"
		);
		editor.assertProblems(
				"abc|Expecting a 'int'"
		);

		//A more complex example for good measure
		editor= newEditor(
				"info:\n" +
				"  some: \n" +
				"    nested: foo\n" +
				"    and: bar\n" +
				"  or: 444\n" +
				"  also: bad\n"
		);
		editor.assertProblems(
				"foo|Expecting a 'int'",
				"bar|Expecting a 'int'",
				"bad|Expecting a 'int'"
		);
	}

	/*
	 * TODO: Remove editor.setText(contents) after the call to deprecate(...) once property index listener mechanism is in place
	 */
	@Test public void testDeprecatedReconcileProperty() throws Exception {
		data("error.path", "java.lang.String", null, "Path of the error controller.");

		String contents = "# a comment\n"+
				"error:\n"+
				"  path: foo\n";

		Editor editor = newEditor(
				contents
		);

		deprecate("error.path", "server.error.path", null);
		editor.setText(contents);
		editor.assertProblems(
				"path|'error.path' is Deprecated: Use 'server.error.path'"
				//no other problems
		);

		deprecate("error.path", "server.error.path", "This is old.");
		editor.setText(contents);
		editor.assertProblems(
				"path|'error.path' is Deprecated: Use 'server.error.path' instead. Reason: This is old."
				//no other problems
		);

		deprecate("error.path", null, "This is old.");
		editor.setText(contents);
		editor.assertProblems(
				"path|'error.path' is Deprecated: This is old."
				//no other problems
		);

		deprecate("error.path", null, null);
		editor.setText(contents);
		editor.assertProblems(
				"path|'error.path' is Deprecated!"
				//no other problems
		);
	}

	@Ignore @Test public void testDeprecatedPropertyCompletion() throws Exception {
		data("error.path", "java.lang.String", null, "Path of the error controller.");
		data("server.error.path", "java.lang.String", null, "Path of the error controller.");
		deprecate("error.path", "server.error.path", "This is old.");
		assertCompletionsDisplayString(
				"errorpa<*>"
				, // =>
				"server.error.path : String", // should be first because it is not deprecated, even though it is not as good a pattern match
				"error.path : String"
		);
		assertStyledCompletions("error.pa<*>",
				StyledStringMatcher.plainFont("server.error.path : String"),
				StyledStringMatcher.strikeout("error.path")
		);
	}

	@Ignore @Test public void testDeprecatedPropertyHoverInfo() throws Exception {
		data("error.path", "java.lang.String", null, "Path of the error controller.");
		Editor editor = newEditor(
				"# a comment\n"+
				"error:\n" +
				"  path: foo\n"
		);

		deprecate("error.path", "server.error.path", null);
		editor.assertHoverContains("path", "<s>error.path</s> -&gt; server.error.path");
		editor.assertHoverContains("path", "<b>Deprecated!</b>");

		deprecate("error.path", "server.error.path", "This is old.");
		editor.assertHoverContains("path", "<s>error.path</s> -&gt; server.error.path");
		editor.assertHoverContains("path", "<b>Deprecated: </b>This is old");

		deprecate("error.path", null, "This is old.");
		editor.assertHoverContains("path", "<b>Deprecated: </b>This is old");

		deprecate("error.path", null, null);
		editor.assertHoverContains("path", "<b>Deprecated!</b>");
	}

	@Ignore @Test public void testDeprecatedBeanPropertyHoverInfo() throws Exception {
		IJavaProject jp = createPredefinedMavenProject("tricky-getters-boot-1.3.1-app");
		useProject(jp);
		data("foo", "demo.Deprecater", null, "A bean with deprecated property.");
		Editor editor = newEditor(
				"# a comment\n"+
				"foo:\n" +
				"  name: foo\n"
		);

		editor.assertHoverContains("name", "<b>Deprecated!</b>");
	}

	@Test public void testDeprecatedBeanPropertyReconcile() throws Exception {
		IJavaProject jp = createPredefinedMavenProject("tricky-getters-boot-1.3.1-app");
		useProject(jp);
		data("foo", "demo.Deprecater", null, "A Bean with deprecated properties");

		Editor editor = newEditor(
				"# comment\n" +
				"foo:\n" +
				"  name: Old faithfull\n" +
				"  new-name: New and fancy\n" +
				"  alt-name: alternate\n"
		);
		editor.assertProblems(
				"name|Property 'name' of type 'demo.Deprecater' is Deprecated!",
				"alt-name|Deprecated"
		);

		editor = newEditor(
				"# comment\n" +
				"foo:\n" +
				"  alt-name: alternate\n"
		);
		//check that message also contains reason and replacement infos.
		editor.assertProblems(
				"alt-name|Use 'something.else' instead"
		);
		editor.assertProblems(
				"alt-name|No good anymore"
		);


	}

	@Ignore @Test public void testDeprecatedBeanPropertyCompletions() throws Exception {
		IJavaProject jp = createPredefinedMavenProject("tricky-getters-boot-1.3.1-app");
		useProject(jp);
		data("foo", "demo.Deprecater", null, "A Bean with deprecated properties");

		assertStyledCompletions(
				"foo:\n" +
				"  nam<*>"
				, // =>
				StyledStringMatcher.plainFont("new-name : String"),
				StyledStringMatcher.strikeout("name"),
				StyledStringMatcher.strikeout("alt-name")
		);
	}

	@Ignore @Test public void testDeprecatedPropertyQuickfixSimple() throws Exception {
		//A simple case for starters. The path edits aren't too complicated since there's
		//just the one property in the file and only the last part of the 'path' changes.
		//So this is a simple 'in-place' edit.

		data("my.old-name", "java.lang.String", null, "Old and deprecated name");
		deprecate("my.old-name", "my.new-name", null);

		{
			Editor editor = newEditor(
					"# a comment\n"+
					"my:\n" +
					"  old-name: foo\n"
			);

			Diagnostic problem = editor.assertProblem("old-name");
			CompletionItem fix = editor.assertFirstQuickfix(problem, "Change to 'my.new-name'");
			editor.apply(fix);
			editor.assertText(
					"# a comment\n"+
					"my:\n"+
					"  new-name: foo\n"
			);
		}

		{
			Editor editor = newEditor(
					"# a comment\n"+
					"my:\n" +
					"  old-name: foo\n" +
					"your: stuff"
			);

			Diagnostic problem = editor.assertProblem("old-name");
			CompletionItem fix = editor.assertFirstQuickfix(problem, "Change to 'my.new-name'");
			editor.apply(fix);
			editor.assertText(
					"# a comment\n"+
					"my:\n"+
					"  new-name: foo\n"+
					"your: stuff"
			);
		}

		{	// Don't move prop if it doesn't need to be moved
			Editor editor = newEditor(
					"# a comment\n"+
					"my:\n" +
					"  old-name: foo\n" +
					"  other: bar\n"
			);

			Diagnostic problem = editor.assertProblem("old-name");
			CompletionItem fix = editor.assertFirstQuickfix(problem, "Change to 'my.new-name'");
			editor.apply(fix);
			editor.assertText(
					"# a comment\n"+
					"my:\n"+
					"  new-name: foo\n" +
					"  other: bar\n"
			);
		}
	}

	@Ignore @Test public void testDeprecatedPropertyQuickfixMovingValue() throws Exception {
		data("my.old-name", "java.lang.String", null, "Old and deprecated name");
		deprecate("my.old-name", "your.new-name", null);

		data("my.stuff", "java.lang.String", null, "Old and deprecated name");
		deprecate("my.stuff", "my.for-sale.stuff", null);

		data("my.long.path.with.many.pieces", "java.lang.String", null, "Old");
		deprecate("my.long.path.with.many.pieces", "my.path.with.many.pieces", null);

		{
			Editor editor = newEditor(
					"# a comment\n"+
					"my:\n" +
					"  long:\n"+
					"    path:\n"+
					"      with:\n"+
					"        many:\n"+
					"          pieces: foo\n"
			);

			Diagnostic problem = editor.assertProblem("pieces");
			CompletionItem fix = editor.assertFirstQuickfix(problem, "Change to 'my.path.with.many.pieces'");
			editor.apply(fix);
			editor.assertText(
					"# a comment\n"+
					"my:\n" +
					"  path:\n"+
					"    with:\n"+
					"      many:\n"+
					"        pieces: foo"
			);
		}

		{
			Editor editor = newEditor(
					"# a comment\n"+
					"my:\n" +
					"  long:\n"+
					"    path:\n"+
					"      with:\n"+
					"        many:\n"+
					"          cannot: remove this\n"+
					"          pieces: foo\n"
			);

			Diagnostic problem = editor.assertProblem("pieces");
			CompletionItem fix = editor.assertFirstQuickfix(problem, "Change to 'my.path.with.many.pieces'");
			editor.apply(fix);
			editor.assertText(
					"# a comment\n"+
					"my:\n" +
					"  long:\n"+
					"    path:\n"+
					"      with:\n"+
					"        many:\n"+
					"          cannot: remove this\n"+
					"  path:\n"+
					"    with:\n"+
					"      many:\n"+
					"        pieces: foo"
			);
		}

		{
			Editor editor = newEditor(
					"# a comment\n"+
					"my:\n" +
					"  stuff: foo\n"
			);

			Diagnostic problem = editor.assertProblem("stuff");
			CompletionItem fix = editor.assertFirstQuickfix(problem, "Change to 'my.for-sale.stuff'");
			editor.apply(fix);
			editor.assertText(
					"# a comment\n"+
					"my:\n" +
					"  for-sale:\n"+
					"    stuff: foo"
			);
		}

		{
			Editor editor = newEditor(
					"# a comment\n"+
					"my:\n" +
					"  old-name: foo\n"
			);

			Diagnostic problem = editor.assertProblem("old-name");
			CompletionItem fix = editor.assertFirstQuickfix(problem, "Change to 'your.new-name'");
			editor.apply(fix);
			editor.assertText(
					"# a comment\n"+
					"your:\n"+
					"  new-name: foo"
			);
		}

		{
			Editor editor = newEditor(
					"# a comment\n"+
					"my:\n" +
					"  old-name: foo\n" +
					"your:\n" +
					"  goodies: nice"
			);

			Diagnostic problem = editor.assertProblem("old-name");
			CompletionItem fix = editor.assertFirstQuickfix(problem, "Change to 'your.new-name'");
			editor.apply(fix);
			editor.assertText(
					"# a comment\n"+
					"your:\n"+
					"  goodies: nice\n"+
					"  new-name: foo"
			);
		}

		{
			Editor editor = newEditor(
					"# a comment\n"+
					"your:\n" +
					"  goodies: nice\n" +
					"my:\n" +
					"  other: stuff\n" +
					"  old-name: foo\n"
			);

			Diagnostic problem = editor.assertProblem("old-name");
			CompletionItem fix = editor.assertFirstQuickfix(problem, "Change to 'your.new-name'");
			editor.apply(fix);
			editor.assertText(
					"# a comment\n"+
					"your:\n"+
					"  goodies: nice\n"+
					"  new-name: foo\n" +
					"my:\n" +
					"  other: stuff"
			);
		}
	}

	@Ignore @Test public void testDeprecatedPropertyQuickfixMovingIndentedValue() throws Exception {
		data("my.old-name", "java.lang.String", null, "Old and deprecated name");
		deprecate("my.old-name", "your.new-name", null); //same indent level

		data("my.stuff", "java.lang.String", null, "Old and deprecated name");
		deprecate("my.stuff", "my.long.path.with.many.pieces", null); // deeper indent level

		data("my.long.path.with.many.pieces", "java.lang.String", null, "Old");
		deprecate("my.long.path.with.many.pieces", "short.path", null); //shallower level

		{
			Editor editor = newEditor(
					"# a comment\n"+
					"my:\n" +
					"  long:\n"+
					"    path:\n"+
					"      with:\n"+
					"        many:\n"+
					"          pieces: >\n"+
					"            foo spread over\n"+
					"            several lines\n" +
					"            of text\n"+
					"short:\n"+
					"  stuff: goes here\n"
			);
			Diagnostic problem = editor.assertProblem("pieces");
			CompletionItem fix = editor.assertFirstQuickfix(problem, "Change to 'short.path'");
			editor.apply(fix);
			editor.assertText(
					"# a comment\n"+
					"short:\n"+
					"  stuff: goes here\n" +
					"  path: >\n" +
					"    foo spread over\n"+
					"    several lines\n" +
					"    of text\n"
			);
		}

		{
			Editor editor = newEditor(
					"# a comment\n"+
					"my:\n" +
					"  long:\n"+
					"    bits: go here\n"+
					"  stuff: >\n" +
					"    foo spread over\n"+
					"      several lines\n" +
					"     of text\n"
			);
			Diagnostic problem = editor.assertProblem("stuff");
			CompletionItem fix = editor.assertFirstQuickfix(problem, "Change to 'my.long.path.with.many.pieces'");
			editor.apply(fix);
			editor.assertText(
					"# a comment\n"+
					"my:\n" +
					"  long:\n"+
					"    bits: go here\n"+
					"    path:\n" +
					"      with:\n" +
					"        many:\n"+
					"          pieces: >\n" +
					"            foo spread over\n"+
					"              several lines\n" +
					"             of text"
			);
		}
		{
			Editor editor = newEditor(
					"# a comment\n"+
					"my:\n" +
					"  long:\n"+
					"    path:\n"+
					"      with:\n"+
					"        many:\n"+
					"          pieces:\n"+
					"# some\n" +
					"          - foo spread over\n"+
					"              #confusing\n" +
					"          - several lines\n" +
					"            #comments\n" +
					"          - of text\n"+
					"short:\n"+
					"  stuff: goes here\n"
			);
			Diagnostic problem = editor.assertProblem("pieces");
			CompletionItem fix = editor.assertFirstQuickfix(problem, "Change to 'short.path'");
			editor.apply(fix);
			editor.assertText(
					"# a comment\n"+
					"short:\n"+
					"  stuff: goes here\n" +
					"  path:\n" +
					"  # some\n" +
					"  - foo spread over\n"+
					"      #confusing\n" +
					"  - several lines\n" +
					"    #comments\n" +
					"  - of text\n"
			);
		}
	}

	@Test public void testReconcileDuplicateProperties() throws Exception {
		defaultTestData();
		Editor editor = newEditor(
				"spring:\n" +
				"  profiles: cloudfoundry\n" +
				"spring:  \n" +
				"  application:\n" +
				"    name: eureka"
		);
		editor.assertProblems(
				"spring|Duplicate",
				"spring|Duplicate"
		);
	}

	@Test public void testReconcileDuplicatePropertiesNested() throws Exception {
		data("foo.person.name", "String", null, "Name of person");
		data("foo.person.family", "String", null, "Family name of person");
		Editor editor = newEditor(
				"foo:\n" +
				"  person:\n" +
				"    name: Hohohoh\n" +
				"  person:\n" +
				"    family:\n"
		);
		editor.assertProblems(
				"person|Duplicate",
				"person|Duplicate"
		);
	}

	@Test public void testReconcileDuplicatePropertiesInBean() throws Exception {
		useProject(createPredefinedMavenProject("enums-boot-1.3.2-app"));
		data("some.color", "demo.ColorData", null, "Some info about a color.");
		Editor editor = newEditor(
				"some:\n" +
				"  color:\n" +
				"    name: RED\n" +
				"    name: GREEN\n"
		);
		editor.assertProblems(
				"name|Duplicate",
				"name|Duplicate"
		);
	}

	@Test public void testCharSetCompletions() throws Exception {
		data("foobar.encoding", "java.nio.charset.Charset", null, "The charset-encoding to use for foobars");

		assertCompletions(
				"foobar:\n" +
				"  enco<*>"
				, // ==>
				"foobar:\n" +
				"  encoding: <*>"
		);

		assertCompletionWithLabel(
				"foobar:\n" +
				"  encoding: UT<*>"
				,
				"UTF-8"
				,
				"foobar:\n" +
				"  encoding: UTF-8<*>"
		);
	}

	@Test public void testLocaleCompletions() throws Exception {
		data("foobar.locale", "java.util.Locale", null, "The locale for foobars");

		assertCompletions(
				"foobar:\n" +
				"  loca<*>"
				, // ==>
				"foobar:\n" +
				"  locale: <*>"
		);

		assertCompletionWithLabel(
				"foobar:\n" +
				"  locale: en<*>"
				,
				"en_CA"
				,
				"foobar:\n" +
				"  locale: en_CA<*>"
		);
	}

	@Test public void testMimeTypeCompletions() throws Exception {
		data("foobar.mime", "org.springframework.util.MimeType", null, "The mimetype for foobars");

		assertCompletions(
				"foobar:\n" +
				"  mi<*>"
				, // ==>
				"foobar:\n" +
				"  mime: <*>"
		);

		assertCompletionWithLabel(
				"foobar:\n" +
				"  mime: json<*>"
				,
				"application/json; charset=utf-8"
				,
				"foobar:\n" +
				"  mime: application/json; charset=utf-8<*>"
		);
	}

	@Test public void testPropertyValueHintCompletions() throws Exception {
		//Test that 'value hints' work when property name is associated with 'value' hints.
		// via boot metadata.
		useProject(createPredefinedMavenProject("empty-boot-1.3.0-app"));

		assertCompletionsDisplayString(
				"spring:\n" +
				"  http:\n" +
				"    converters:\n" +
				"      preferred-json-mapper: <*>\n"
				, //=>
				"gson",
				"jackson"
		);
	}

	@Test public void testPropertyListHintCompletions() throws Exception {
		useProject(createPredefinedMavenProject("empty-boot-1.3.0-app"));

		assertCompletion(
				"management:\n" +
				"  health:\n" +
				"    status:\n" +
				"      ord<*>"
				, //=>
				"management:\n" +
				"  health:\n" +
				"    status:\n" +
				"      order:\n"+
				"      - <*>"
		);

		assertCompletionsDisplayString(
				"management:\n" +
				"  health:\n" +
				"    status:\n" +
				"      order:\n" +
				"      - <*>"
				, //=>
				"DOWN",
				"OUT_OF_SERVICE",
				"UNKNOWN",
				"UP"
		);
	}

	@Test public void testPropertyMapValueCompletions() throws Exception {
		useProject(createPredefinedMavenProject("empty-boot-1.3.0-app"));

		assertCompletionsDisplayString(
				"logging:\n" +
				"  level:\n" +
				"    some.package: <*>"
				, // =>
				"trace",
				"debug",
				"info",
				"warn",
				"error",
				"fatal",
				"off"
		);
	}

	@Test public void testPropertyMapKeyCompletions() throws Exception {
		useProject(createPredefinedMavenProject("empty-boot-1.3.0-app"));
		assertCompletionWithLabel(
				"logging:\n" +
				"  level:\n" +
				"    <*>"
				, // =>
				"root"
				,
				"logging:\n" +
				"  level:\n" +
				"    root: <*>"
		);
		assertCompletionDetails(
				"logging:\n" +
				"  level:\n" +
				"    <*>"
				, // =>
				"root",
				"String",
				null
		);
	}

	@Test public void testEscapeStringValueStartingWithStar() throws Exception {
		useProject(createPredefinedMavenProject("empty-boot-1.3.0-app"));
		assertCompletions(
				"endpoints:\n"+
				"  cors:\n"+
				"    allowed-headers: \n" +
				"      - <*>"
				, // =>
				"endpoints:\n"+
				"  cors:\n"+
				"    allowed-headers: \n" +
				"      - '*'<*>"
		);
	}

	@Test public void testEscapeStringValueWithAQuote() throws Exception {
		data("foo.quote", "java.lang.String", null, "Character to used to surround quotes");
		valueHints("foo.quote", "\"", "'", "`");

		assertCompletions(
				"foo:\n" +
				"  quote: <*>"
				, // =>
				"foo:\n" +
				"  quote: '\"'<*>"
				,
				"foo:\n" +
				"  quote: ''''<*>"
				,
				"foo:\n" +
				"  quote: '`'<*>"
		);
	}

	@Ignore @Test public void testEscapeStringKeyWithAQuote() throws Exception {
		data("foo.quote", "java.util.Map<java.lang.String,java.lang.String>", null, "Name of quote characters");
		keyHints("foo.quote", "\"", "'", "`");

		assertCompletions(
				"foo:\n" +
				"  quote: <*>"
				, // =>
				"foo:\n" +
				"  quote: \n"+
				"    '\"': <*>"
				,
				"foo:\n" +
				"  quote: \n"+
				"    '''': <*>"
				,
				"foo:\n" +
				"  quote: \n"+
				"    '`': <*>"
		);
	}

	@Test public void testLoggerNameCompletion() throws Exception {
		CachingValueProvider.TIMEOUT = Duration.ofSeconds(20); // the provider can't be reliably tested if its not allowed to
											// fetch all its values (even though in 'production' you
											// wouldn't want it to block the UI thread for this long.
		useProject(createPredefinedMavenProject("empty-boot-1.3.0-app"));
		//Finds a package:
		assertCompletionWithLabel(
				"logging:\n" +
				"  level:\n" +
				"    boot.auto<*>"
				, //-----------------
				"org.springframework.boot.autoconfigure"
				, // =>
				"logging:\n" +
				"  level:\n" +
				"    org.springframework.boot.autoconfigure: <*>"
		);
		assertCompletionDetails(
				"logging:\n" +
				"  level:\n" +
				"    boot.auto<*>"
				, //-----------------
				"org.springframework.boot.autoconfigure", "String", null);

		//Finds a type:
		assertCompletionWithLabel(
				"logging:\n" +
				"  level:\n" +
				"    MesgSource<*>"
				, //-----------------
				"org.springframework.boot.autoconfigure.MessageSourceAutoConfiguration"
				, // =>
				"logging:\n" +
				"  level:\n" +
				"    org.springframework.boot.autoconfigure.MessageSourceAutoConfiguration: <*>"
		);
		assertCompletionDetails(
				"logging:\n" +
				"  level:\n" +
				"    MesgSource<*>"
				, //-----------------
				"org.springframework.boot.autoconfigure.MessageSourceAutoConfiguration",
				"String",
				null
		);
	}

	@Test public void testSimpleResourceCompletion() throws Exception {
		CachingValueProvider.TIMEOUT = Duration.ofSeconds(20);

		useProject(createPredefinedMavenProject("empty-boot-1.3.0-app"));

		data("my.nice.resource", "org.springframework.core.io.Resource", null, "A very nice resource.");

		assertCompletion(
				"my:\n" +
				"  nice:\n" +
				"    <*>\n"
				,// =>
				"my:\n" +
				"  nice:\n" +
				"    resource: <*>\n"
		);

		assertCompletionsDisplayString(
				"my:\n" +
				"  nice:\n" +
				"    resource: <*>\n"
				, // =>
				"classpath:",
				"classpath*:",
				"file:",
				"http://",
				"https://"
		);

		assertCompletionsDisplayString(
				"my:\n" +
				"  nice:\n" +
				"    resource:\n" +
				"      <*>\n"
				, // =>
				"classpath:",
				"classpath*:",
				"file:",
				"http://",
				"https://"
		);

	}

	/**
	 * TODO: Delete this tempotary test.
	 * This test is just one piece copied from another test to do more focussed debugging. If you find this test
	 * in the git repo, then it was committed by accident. So feel free to delete it.
	 */
	@Test public void testClasspathResourceCompletionTemp() throws Exception {
		CachingValueProvider.TIMEOUT = Duration.ofSeconds(20);

		useProject(createPredefinedMavenProject("empty-boot-1.3.0-app"));

		data("my.nice.resource", "org.springframework.core.io.Resource", null, "A very nice resource.");
		data("my.nice.list", "java.util.List<org.springframework.core.io.Resource>", null, "A nice list of resources.");

		assertCompletionWithLabel(
				"my:\n" +
				"  nice:\n" +
				"    list:\n"+
				"    - classpath:<*>\n"
				,// ==========
				"classpath:application.yml"
				, // =>
				"my:\n" +
				"  nice:\n" +
				"    list:\n"+
				"    - classpath:application.yml<*>\n"
		);

	}

	@Test public void testClasspathResourceCompletion() throws Exception {
		CachingValueProvider.TIMEOUT = Duration.ofSeconds(20);

		useProject(createPredefinedMavenProject("empty-boot-1.3.0-app"));

		data("my.nice.resource", "org.springframework.core.io.Resource", null, "A very nice resource.");
		data("my.nice.list", "java.util.List<org.springframework.core.io.Resource>", null, "A nice list of resources.");

		//Test 'simple key context'

		assertCompletionsDisplayString(
				"my:\n" +
				"  nice:\n" +
				"    resource: classpath:app<*>\n"
				,// =>
				"classpath:application.properties",
				"classpath:application.yml"
		);

		//Test 'list item' context:

		assertCompletionsDisplayString(
				"my:\n" +
				"  nice:\n" +
				"    list:\n"+
				"    - classpath:app<*>\n"
				,// =>
				"classpath:application.properties",
				"classpath:application.yml"
		);

		assertCompletionWithLabel(
				"my:\n" +
				"  nice:\n" +
				"    list:\n"+
				"    - classpath:app<*>\n"
				,// ==========
				"classpath:application.yml"
				, // =>
				"my:\n" +
				"  nice:\n" +
				"    list:\n"+
				"    - classpath:application.yml<*>\n"
		);

		assertCompletionWithLabel(
				"my:\n" +
				"  nice:\n" +
				"    list:\n"+
				"    - classpath:<*>\n"
				,// ==========
				"classpath:application.yml"
				, // =>
				"my:\n" +
				"  nice:\n" +
				"    list:\n"+
				"    - classpath:application.yml<*>\n"
		);

		//Test 'raw node' context

		assertCompletionsDisplayString(
				"my:\n" +
				"  nice:\n" +
				"    resource:\n"+
				"      classpath:app<*>\n"
				,// =>
				"classpath:application.properties",
				"classpath:application.yml"
		);

		assertCompletionWithLabel(
				"my:\n" +
				"  nice:\n" +
				"    resource:\n"+
				"      classpath:app<*>\n"
				,//===============
				"classpath:application.properties"
				,// =>
				"my:\n" +
				"  nice:\n" +
				"    resource:\n"+
				"      classpath:application.properties<*>\n"
		);

		assertCompletionWithLabel(
				"my:\n" +
				"  nice:\n" +
				"    resource:\n"+
				"      classpath:<*>\n"
				,//===============
				"classpath:application.properties"
				,// =>
				"my:\n" +
				"  nice:\n" +
				"    resource:\n"+
				"      classpath:application.properties<*>\n"
		);

		// do we find resources in sub-folders too?
		assertCompletionWithLabel(
				"my:\n" +
				"  nice:\n" +
				"    resource:\n"+
				"      classpath:word<*>\n"
				,//===============
				"classpath:stuff/wordlist.txt"
				,// =>
				"my:\n" +
				"  nice:\n" +
				"    resource:\n"+
				"      classpath:stuff/wordlist.txt<*>\n"
		);
	}

	@Test public void testCompletionsInContextWithDuplicateKey() throws Exception {
		//See: https://www.pivotaltracker.com/story/show/135708013
		defaultTestData();

		assertCompletions(
				"spring:\n" +
				"  application:\n" +
				"    name: my-app\n" +
				"spring:\n" +
				"  activemq:\n" +
				"    broker-u<*>"
				, // ==>
				"spring:\n" +
				"  application:\n" +
				"    name: my-app\n" +
				"spring:\n" +
				"  activemq:\n" +
				"    broker-url: <*>"
		);
	}

	@Test public void testClassReferenceCompletion() throws Exception {
		CachingValueProvider.TIMEOUT = Duration.ofSeconds(20);

		useProject(createPredefinedMavenProject("empty-boot-1.3.0-with-mongo"));

		assertCompletion(
				"spring:\n" +
				"  data:\n" +
				"    mongodb:\n" +
				"      field-na<*>"
				, // =>
				"spring:\n" +
				"  data:\n" +
				"    mongodb:\n" +
				"      field-naming-strategy: <*>"
		);

		assertCompletionsDisplayString(
			"spring:\n" +
			"  data:\n" +
			"    mongodb:\n" +
			"      field-naming-strategy: <*>"
			, // =>
			"org.springframework.data.mapping.model.CamelCaseAbbreviatingFieldNamingStrategy",
			"org.springframework.data.mapping.model.CamelCaseSplittingFieldNamingStrategy",
			"org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy",
			"org.springframework.data.mapping.model.SnakeCaseFieldNamingStrategy"
		);

		assertCompletionWithLabel(
			"spring:\n" +
			"  data:\n" +
			"    mongodb:\n" +
			"      field-naming-strategy: <*>"
			, //=====
			"org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy"
			, //=>
			"spring:\n" +
			"  data:\n" +
			"    mongodb:\n" +
			"      field-naming-strategy: org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy<*>"
		);

		//Test what happens when 'target' type isn't on the classpath:
		useProject(createPredefinedMavenProject("empty-boot-1.3.0-app"));
		assertCompletionsDisplayString(
			"spring:\n" +
			"  data:\n" +
			"    mongodb:\n" +
			"      field-naming-strategy: <*>"
			// =>
			/*NONE*/
		);
	}

	@Ignore @Test public void testClassReferenceInValueLink() throws Exception {
		Editor editor;
		useProject(createPredefinedMavenProject("empty-boot-1.3.0-with-mongo"));

		editor = newEditor(
			"spring:\n" +
			"  data:\n" +
			"    mongodb:\n" +
			"      field-naming-strategy: org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy\n"
		);
		editor.assertLinkTargets("org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy", "org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy");

		editor = newEditor(
			"spring:\n" +
			"  data:\n" +
			"    mongodb:\n" +
			"      field-naming-strategy:\n" +
			"        org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy\n"
		);
		editor.assertLinkTargets("org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy", "org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy");

		//Linking should also work for types that aren't valid based on the constraints
		editor = newEditor(
				"spring:\n" +
				"  data:\n" +
				"    mongodb:\n" +
				"      field-naming-strategy: java.lang.String\n" +
				"#more stuff"
		);
		editor.assertLinkTargets("java.lang.String", "java.lang.String");
	}

	@Test public void test_STS_3335_reconcile_list_nested_in_Map_of_String() throws Exception {
		Editor editor;
		useProject(createPredefinedMavenProject("boot-1.3.3-sts-4335"));

		editor = newEditor(
				"test-map:\n" +
				"  test-list-object:\n" +
				"    color-list:\n" +
				"      - not-a-color\n"+
				"      - RED\n" +
				"      - GREEN\n"
		);
		editor.assertProblems(
				"not-a-color|Expecting a 'com.wellsfargo.lendingplatform.web.config.Color"
		);

		editor = newEditor(
				"test-map:\n" +
				"  test-list-object:\n" +
				"    string-list:\n" +
				"      - abc\n" +
				"      - def\n"
		);
		editor.assertProblems(/*NONE*/);

	}


	@Test public void test_STS_3335_completions_list_nested_in_Map_of_String() throws Exception {
		useProject(createPredefinedMavenProject("boot-1.3.3-sts-4335"));

		assertCompletions(
				"test-map:\n" +
				"  some-string-key:\n" +
				"    col<*>"
				, // =>
				"test-map:\n" +
				"  some-string-key:\n" +
				"    color-list:\n" +
				"    - <*>"
		);

		assertCompletionsDisplayString(
				"test-map:\n" +
				"  some-string-key:\n" +
				"    color-list:\n" +
				"    - <*>"
				, // =>
				"red", "green", "blue"
		);
	}

	@Test public void testHandleAsResourceContentAssist() throws Exception {
		//"name": "my.terms-and-conditions",
		//        "providers": [
		//                      {
		//                          "name": "handle-as",
		//                          "parameters": {
		//                              "target": "org.springframework.core.io.Resource"
		//                          }
		//                      }
		//                  ]
		data("my.terms-and-conditions", "java.lang.String", null, "Terms and Conditions text file")
		.provider("handle-as", "target", "org.springframework.core.io.Resource");

		assertCompletionsDisplayString(
				"my:\n" +
				"  terms-and-conditions: <*>"
				, // =>
				"classpath:",
				"classpath*:",
				"file:",
				"http://",
				"https://"
		);
	}

	@Test public void testBootBug5905() throws Exception {
		useProject(createPredefinedMavenProject("boot-1.3.3-app-with-resource-prop"));

		//Check the metadata reflects the 'handle-as':
		PropertyInfo metadata = getIndexProvider().getIndex(null).get("my.welcome.path");
		assertEquals("org.springframework.core.io.Resource", metadata.getType());

		//Check the content assist based on it works too:
		assertCompletionsDisplayString(
				"my:\n"+
				"  welcome:\n" +
				"    path: <*>"
				, // =>
				"classpath:",
				"classpath*:",
				"file:",
				"http://",
				"https://"
		);
	}

	@Test public void testEnumJavaDocShownInValueContentAssist() throws Exception {
		useProject(createPredefinedMavenProject("enums-boot-1.3.2-app"));
		data("my.background", "demo.Color", null, "Color to use as default background.");

		assertCompletionDetails(
				"my:\n" +
				"  background: <*>"
				, // ==========
				"red"
				, // ==>
				"demo.Color[BLUE, GREEN, RED]",
				"Hot and delicious"
		);
	}

	@Ignore @Test public void testEnumJavaDocShownInValueHover() throws Exception {
		useProject(createPredefinedMavenProject("enums-boot-1.3.2-app"));
		data("my.background", "demo.Color", null, "Color to use as default background.");

		Editor editor;

		editor = newEditor(
				"my:\n" +
				"  background: red"
		);
		editor.assertHoverContains("red", "Hot and delicious");

		editor = newEditor(
				"my:\n" +
				"  background: RED"
		);
		editor.assertHoverContains("RED", "Hot and delicious");
	}

	@Ignore @Test public void testHyperLinkEnumValue() throws Exception {
		Editor editor;
		useProject(createPredefinedMavenProject("enums-boot-1.3.2-app"));
		data("my.background", "demo.Color", null, "Color to use as default background.");

		editor = newEditor(
				"my:\n" +
				"  background: RED"
		);
		editor.assertLinkTargets("RED", "demo.Color.RED");

		editor = newEditor(
				"my:\n" +
				"  background: red"
		);
		editor.assertLinkTargets("red", "demo.Color.RED");
	}

	@Ignore @Test public void testHyperLinkEnumValueInMapKey() throws Exception {
		Editor editor;
		useProject(createPredefinedMavenProject("enums-boot-1.3.2-app"));
		data("my.color.map", "java.util.Map<demo.Color,java.lang.String>", null, "Pretty names for the colors.");

		editor = newEditor(
				"my:\n" +
				"  color:\n" +
				"    map:\n" +
				"      RED: Rood\n" +
				"      green: Groen\n"
		);
		editor.assertLinkTargets("RED", "demo.Color.RED");
		editor.assertLinkTargets("green", "demo.Color.GREEN");

		editor = newEditor(
			"spring:\n" +
			"  jackson:\n" +
			"    serialization:\n" +
			"      INDENT_OUTPUT: true"
		);
		editor.assertLinkTargets("INDENT_OUTPUT",
				"com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT"
		);

		editor = newEditor(
			"spring:\n" +
			"  jackson:\n" +
			"    serialization:\n" +
			"      indent-output: true"
		);
		editor.assertLinkTargets("indent-output",
				"com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT"
		);
	}

	@Test public void PT_137299017_extra_space_wth_bean_property_value_completion() throws Exception {
		useProject(createPredefinedMavenProject("enums-boot-1.3.2-app"));
		data("color", "demo.ColorData", null, "colorful stuff");

		assertCompletions(
				"color:\n" +
				"  next:<*>"
				,  //==>
				"color:\n" +
				"  next: blue<*>"
				, // ==
				"color:\n" +
				"  next: green<*>"
				, // ==
				"color:\n" +
				"  next: red<*>"
		);
	}

	@Test public void PT_137299017_extra_space_wth_index_property_value_completion() throws Exception {
		defaultTestData();
		assertCompletions(
				"flyway:\n"+
				"  enabled:<*>"
				, // ==>
				"flyway:\n"+
				"  enabled: false<*>"
				, // ==
				"flyway:\n"+
				"  enabled: true<*>"
		);
	}

	@Test
	public void testIgnoreTypeErrorsForValuesContainingMavenResourcesPlaceholders_workaround() throws Exception {
		//See: https://www.pivotaltracker.com/story/show/150005676
		defaultTestData();
		Editor editor = newEditor(
				"server:\n" +
				"  port: \"@application-port@\"\n" +
				"bogus: bad" //token error to ensure reconciler is really working
		);
		editor.assertProblems("bogus|Unknown property");
	}

	@Test @Ignore
	public void IGNORED_testIgnoreTypeErrorsForValuesContainingMavenResourcesPlaceholders_direct() throws Exception {
		//See: https://www.pivotaltracker.com/story/show/150005676
		//Not implemented, this test fails. The choice not to implement this was deliberate!
		defaultTestData();
		Editor editor = newEditor(
				"server:\n" +
				"  port: @application-port@\n" +
				"bogus: bad" //token error to ensure reconciler is really working
		);
		editor.assertProblems("bogus|Unknown property");
	}

	///////////////// cruft ////////////////////////////////////////////////////////

	private void generateNestedProperties(int levels, String[] names, String prefix) {
		if (levels==0) {
			data(prefix, "java.lang.String", null, "Property "+prefix);
		} else if (levels > 0) {
			for (int i = 0; i < names.length; i++) {
				generateNestedProperties(levels-1, names, join(prefix, names[i]));
			}
		}
	}

	private String join(String prefix, String string) {
		if (StringUtil.hasText(prefix)) {
			return prefix +"." + string;
		}
		return string;
	}

	@Override
	protected SimpleLanguageServer newLanguageServer() {
		ComposableLanguageServer<?> server = BootLanguageServer.create(
				s -> new BootLanguageServerParams(
						javaProjectFinder, 
						ProjectObserver.NULL, 
						md.getIndexProvider(),
						typeUtilProvider,
						RunningAppProvider.NULL,
						SpringLiveHoverWatchdog.DEFAULT_INTERVAL
				)
		);
		server.setMaxCompletionsNumber(-1);
		return server.getServer();
	}

	@Override
	protected String getFileExtension() {
		return ".yml";
	}

	@Override
	protected LanguageId getLanguageId() {
		return LanguageId.BOOT_PROPERTIES_YAML;
	}
	
}
