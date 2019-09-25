/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.conditionals.test;

import static org.junit.Assert.assertFalse;

import java.io.File;

import org.eclipse.lsp4j.Hover;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.boot.bootiful.HoverTestConf;
import org.springframework.ide.vscode.boot.java.livehover.v2.SpringProcessLiveData;
import org.springframework.ide.vscode.boot.java.livehover.v2.SpringProcessLiveDataProvider;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.ide.vscode.project.harness.SpringProcessLiveDataBuilder;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@BootLanguageServerTest
@Import(HoverTestConf.class)
public class ConditionalsLiveHoverTest {

	@Autowired private BootLanguageServerHarness harness;
	@Autowired private SpringProcessLiveDataProvider liveDataProvider;

	@Before
	public void setup() throws Exception {
		harness.useProject(ProjectsHarness.INSTANCE.mavenProject("test-conditionals-live-hover"));
	}
	
	@After
	public void tearDown() throws Exception {
		liveDataProvider.remove("processkey");
		liveDataProvider.remove("processkey1");
		liveDataProvider.remove("processkey2");
		liveDataProvider.remove("processkey3");
	}

	@Test
	public void testNoLiveHoverNoRunningApp() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-conditionals-live-hover/").toURI());
		String docUri = directory.toPath().resolve("src/main/java/example/ConditionalOnMissingBeanConfig.java").toUri()
				.toString();

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
		editor.assertNoHover("@ConditionalOnMissingBean");
	}

	@Test
	public void testLiveHoverConditionalOnBean() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-conditionals-live-hover/").toURI());
		String docUri = directory.toPath().resolve("src/main/java/example/ConditionalOnBeanConfig.java").toUri()
				.toString();

		// Build a mock running boot app
		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
				.port("1111")
				.processID("22022")
				.host("cfapps.io")
				.processName("test-conditionals-live-hover")
				.liveConditionalsJson(
						"{\"positiveMatches\":{\"ConditionalOnBeanConfig#hi\":[{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnBean (types: example.Hello; SearchStrategy: all) found bean 'missing'\"}]}}")
				.build();
		liveDataProvider.add("processkey", liveData);

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
		editor.assertHoverContains("@ConditionalOnBean",
				"@ConditionalOnBean (types: example.Hello; SearchStrategy: all) found bean 'missing'\n" + "\n"
						+ "Process [PID=22022, name=`test-conditionals-live-hover`]");
	}

	@Test
	public void testLiveHoverConditionalOnMissingBean() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-conditionals-live-hover/").toURI());
		String docUri = directory.toPath().resolve("src/main/java/example/ConditionalOnMissingBeanConfig.java").toUri()
				.toString();

		// Build a mock running boot app
		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
				.port("1111")
				.processID("22022")
				.host("cfapps.io")
				.processName("test-conditionals-live-hover")
				.liveConditionalsJson(
						"{\"positiveMatches\":{\"ConditionalOnMissingBeanConfig#missing\":[{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnMissingBean (types: example.Hello; SearchStrategy: all) did not find any beans\"}]}}")
				.build();
		liveDataProvider.add("proesskey", liveData);

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
		editor.assertHoverContains("@ConditionalOnMissingBean",
				"@ConditionalOnMissingBean (types: example.Hello; SearchStrategy: all) did not find any beans\n" + "\n"
						+ "Process [PID=22022, name=`test-conditionals-live-hover`]");

	}

	@Test
	public void testMultipleLiveHoverContentRealProject() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-conditionals-live-hover/").toURI());
		String docUri = directory.toPath().resolve("src/main/java/example/MultipleConditionals.java").toUri()
				.toString();

		// Build a mock running boot app
		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
				.port("1111")
				.processID("22022")
				.host("cfapps.io")
				.processName("test-conditionals-live-hover")
				.liveConditionalsJson(
						"{\"positiveMatches\":{\"HelloConfig#missing\":[{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnMissingBean (types: example.Hello; SearchStrategy: all) did not find any beans\"}],\"HelloConfig2#hi\":[{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnBean (types: example.Hello; SearchStrategy: all) found bean 'missing'\"}],\"MultipleConditionals#hi\":[{\"condition\":\"OnClassCondition\",\"message\":\"@ConditionalOnClass found required class; @ConditionalOnMissingClass did not find unwanted class\"},{\"condition\":\"OnWebApplicationCondition\",\"message\":\"@ConditionalOnWebApplication (required) found StandardServletEnvironment\"},{\"condition\":\"OnJavaCondition\",\"message\":\"@ConditionalOnJava (1.8 or newer) found 1.8\"},{\"condition\":\"OnExpressionCondition\",\"message\":\"@ConditionalOnExpression (#{true}) resulted in true\"},{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnBean (types: example.Hello; SearchStrategy: all) found beans 'hi', 'missing'\"}]}}")
				.build();
		liveDataProvider.add("proesskey", liveData);

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);

		editor.assertHoverContains("@ConditionalOnBean",
				"@ConditionalOnBean (types: example.Hello; SearchStrategy: all) found beans 'hi', 'missing'\n" + "\n"
						+ "Process [PID=22022, name=`test-conditionals-live-hover`]");

		editor.assertHoverContains("@ConditionalOnWebApplication",
				"@ConditionalOnWebApplication (required) found StandardServletEnvironment\n" + "\n"
						+ "Process [PID=22022, name=`test-conditionals-live-hover`]");

		editor.assertHoverContains("@ConditionalOnJava(value=ConditionalOnJava.JavaVersion.EIGHT)",
				"@ConditionalOnJava (1.8 or newer) found 1.8\n" + "\n"
						+ "Process [PID=22022, name=`test-conditionals-live-hover`]");

		editor.assertHoverContains("@ConditionalOnMissingClass",
				"@ConditionalOnClass found required class; @ConditionalOnMissingClass did not find unwanted class\n"
						+ "\n" + "Process [PID=22022, name=`test-conditionals-live-hover`]");

		editor.assertHoverContains("@ConditionalOnExpression", "@ConditionalOnExpression (#{true}) resulted in true\n"
				+ "\n" + "Process [PID=22022, name=`test-conditionals-live-hover`]");
	}

	@Test
	public void testMultipleAppInstances() throws Exception {

		// Test that live hover shows information for multiple app instances
		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-conditionals-live-hover/").toURI());
		String docUri = directory.toPath().resolve("src/main/java/example/ConditionalOnMissingBeanConfig.java").toUri()
				.toString();

		// Build a mock running boot app
		SpringProcessLiveData liveData1 = new SpringProcessLiveDataBuilder()
				.port("1000")
				.processID("70000")
				.host("cfapps.io")
				.processName("test-conditionals-live-hover")
				.liveConditionalsJson(
						"{\"positiveMatches\":{\"ConditionalOnMissingBeanConfig#missing\":[{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnMissingBean (types: example.Hello; SearchStrategy: all) did not find any beans\"}]}}")
				.build();
		liveDataProvider.add("processkey1", liveData1);

		SpringProcessLiveData liveData2 = new SpringProcessLiveDataBuilder()
				.port("1001")
				.processID("80000")
				.host("cfapps.io")
				.processName("test-conditionals-live-hover")
				.liveConditionalsJson(
						"{\"positiveMatches\":{\"ConditionalOnMissingBeanConfig#missing\":[{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnMissingBean (types: example.Hello; SearchStrategy: all) did not find any beans\"}]}}")
				.build();
		liveDataProvider.add("processkey2", liveData2);

		SpringProcessLiveData liveData3 = new SpringProcessLiveDataBuilder()
				.port("1002")
				.processID("90000")
				.host("cfapps.io")
				.processName("test-conditionals-live-hover")
				.liveConditionalsJson(
						"{\"positiveMatches\":{\"ConditionalOnMissingBeanConfig#missing\":[{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnMissingBean (types: example.Hello; SearchStrategy: all) did not find any beans\"}]}}")
				.build();
		liveDataProvider.add("processkey3", liveData3);

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);

		editor.assertHoverContains("@ConditionalOnMissingBean",
				"@ConditionalOnMissingBean (types: example.Hello; SearchStrategy: all) did not find any beans\n" + "\n"
						+ "Process [PID=70000, name=`test-conditionals-live-hover`]\n" + "\n"
						+ "@ConditionalOnMissingBean (types: example.Hello; SearchStrategy: all) did not find any beans\n"
						+ "\n" + "Process [PID=80000, name=`test-conditionals-live-hover`]\n" + "\n"
						+ "@ConditionalOnMissingBean (types: example.Hello; SearchStrategy: all) did not find any beans\n"
						+ "\n" + "Process [PID=90000, name=`test-conditionals-live-hover`]");

	}

	@Test
	public void testMultipleConditionalsSameMethod() throws Exception {

		// Tests something like this:
		// @Bean
		// @ConditionalOnBean
		// @ConditionalOnWebApplication
		// @ConditionalOnJava(value=ConditionalOnJava.JavaVersion.EIGHT)
		// @ConditionalOnMissingClass
		// @ConditionalOnExpression
		// public Hello hi() {
		// return null;
		// }

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-conditionals-live-hover/").toURI());
		String docUri = directory.toPath().resolve("src/main/java/example/MultipleConditionals.java").toUri()
				.toString();

		// Build a mock running boot app
		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
				.port("1000")
				.processID("70000")
				.host("cfapps.io")
				.processName("test-conditionals-live-hover")
				.liveConditionalsJson(
						"{\"positiveMatches\":{\"HelloConfig#missing\":[{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnMissingBean (types: example.Hello; SearchStrategy: all) did not find any beans\"}],\"HelloConfig2#hi\":[{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnBean (types: example.Hello; SearchStrategy: all) found bean 'missing'\"}],\"MultipleConditionals#hi\":[{\"condition\":\"OnClassCondition\",\"message\":\"@ConditionalOnClass found required class; @ConditionalOnMissingClass did not find unwanted class\"},{\"condition\":\"OnWebApplicationCondition\",\"message\":\"@ConditionalOnWebApplication (required) found StandardServletEnvironment\"},{\"condition\":\"OnJavaCondition\",\"message\":\"@ConditionalOnJava (1.8 or newer) found 1.8\"},{\"condition\":\"OnExpressionCondition\",\"message\":\"@ConditionalOnExpression (#{true}) resulted in true\"},{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnBean (types: example.Hello; SearchStrategy: all) found beans 'hi', 'missing'\"}]}}")
				.build();
		liveDataProvider.add("processkey", liveData);

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);

		// IMPORTANT: test EXACT text to ensure that multiple conditionals on the same
		// method do not show
		// up while
		// hovering over only one of the conditional annotations
		editor.assertHoverExactText("@ConditionalOnBean",
				"@ConditionalOnBean (types: example.Hello; SearchStrategy: all) found beans 'hi', 'missing'\n" + "\n"
						+ "Process [PID=70000, name=`test-conditionals-live-hover`]");

		editor.assertHoverExactText("@ConditionalOnWebApplication",
				"@ConditionalOnWebApplication (required) found StandardServletEnvironment\n" + "\n"
						+ "Process [PID=70000, name=`test-conditionals-live-hover`]");

		editor.assertHoverExactText("@ConditionalOnJava(value=ConditionalOnJava.JavaVersion.EIGHT)",
				"@ConditionalOnJava (1.8 or newer) found 1.8\n" + "\n"
						+ "Process [PID=70000, name=`test-conditionals-live-hover`]");

		editor.assertHoverExactText("@ConditionalOnMissingClass",
				"@ConditionalOnClass found required class; @ConditionalOnMissingClass did not find unwanted class\n"
						+ "\n" + "Process [PID=70000, name=`test-conditionals-live-hover`]");

		editor.assertHoverExactText("@ConditionalOnExpression", "@ConditionalOnExpression (#{true}) resulted in true\n"
				+ "\n" + "Process [PID=70000, name=`test-conditionals-live-hover`]");
	}

	@Test
	public void PT152535713testMultipleLiveHoverHints() throws Exception {

		// Tests fix for PT152535713. Ensure that in a method with multiple
		// conditionals,
		// hovering over any one conditional annotation only shows content for that
		// conditional
		// and not any of the other ones
		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-conditionals-live-hover/").toURI());
		String docUri = directory.toPath().resolve("src/main/java/example/MultipleConditionalsPT152535713.java").toUri()
				.toString();

		// Build a mock running boot app
		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
				.port("1000")
				.processID("70000")
				.host("cfapps.io")
				.processName("test-conditionals-live-hover")
				.liveConditionalsJson(
						"{\"positiveMatches\":{\"MultipleConditionalsPT152535713#hi\":[{\"condition\":\"OnWebApplicationCondition\",\"message\":\"@ConditionalOnWebApplication (required) found StandardServletEnvironment\"},{\"condition\":\"OnJavaCondition\",\"message\":\"@ConditionalOnJava (1.8 or newer) found 1.8\"}]}}")
				.build();
		liveDataProvider.add("processkey", liveData);

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);

		editor.assertHoverExactText("@ConditionalOnWebApplication",
				"@ConditionalOnWebApplication (required) found StandardServletEnvironment\n" + "\n"
						+ "Process [PID=70000, name=`test-conditionals-live-hover`]");

		editor.assertHoverExactText("@ConditionalOnJava(value=ConditionalOnJava.JavaVersion.EIGHT)",
				"@ConditionalOnJava (1.8 or newer) found 1.8\n" + "\n"
						+ "Process [PID=70000, name=`test-conditionals-live-hover`]");

		// Test that the hovers dont have extra information of the other conditionals:
		Hover hover = editor.getHover("@ConditionalOnWebApplication");
		String hoverContent = editor.hoverString(hover);
		assertFalse(hoverContent.contains("@ConditionalOnJava"));

		hover = editor.getHover("@ConditionalOnJava(value=ConditionalOnJava.JavaVersion.EIGHT)");
		hoverContent = editor.hoverString(hover);
		assertFalse(hoverContent.contains("@ConditionalOnWebApplication"));
	}

	@Test
	public void testHighlightsMethodConditionals() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-conditionals-live-hover/").toURI());
		String docUri = directory.toPath().resolve("src/main/java/example/MultipleConditionals.java").toUri()
				.toString();

		// Build a mock running boot app
		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
				.port("1111")
				.processID("22022")
				.host("cfapps.io")
				.processName("test-conditionals-live-hover")
				.liveConditionalsJson(
						"{\"positiveMatches\":{\"HelloConfig#missing\":[{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnMissingBean (types: example.Hello; SearchStrategy: all) did not find any beans\"}],\"HelloConfig2#hi\":[{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnBean (types: example.Hello; SearchStrategy: all) found bean 'missing'\"}],\"MultipleConditionals#hi\":[{\"condition\":\"OnClassCondition\",\"message\":\"@ConditionalOnClass found required class; @ConditionalOnMissingClass did not find unwanted class\"},{\"condition\":\"OnWebApplicationCondition\",\"message\":\"@ConditionalOnWebApplication (required) found StandardServletEnvironment\"},{\"condition\":\"OnJavaCondition\",\"message\":\"@ConditionalOnJava (1.8 or newer) found 1.8\"},{\"condition\":\"OnExpressionCondition\",\"message\":\"@ConditionalOnExpression (#{true}) resulted in true\"},{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnBean (types: example.Hello; SearchStrategy: all) found beans 'hi', 'missing'\"}]}}")
				.build();
		liveDataProvider.add("processkey", liveData);

		harness.intialize(directory);

		String content = "package example;\n" + "\n"
				+ "import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;\n"
				+ "import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;\n"
				+ "import org.springframework.boot.autoconfigure.condition.ConditionalOnJava;\n"
				+ "import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;\n"
				+ "import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;\n"
				+ "import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;\n"
				+ "import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;\n"
				+ "import org.springframework.context.annotation.Bean;\n"
				+ "import org.springframework.context.annotation.Configuration;\n" + "\n" + "@Configuration\n"
				+ "public class MultipleConditionals {\n" + "\n" + "	@Bean\n" + "	@ConditionalOnBean\n"
				+ "	@ConditionalOnWebApplication\n" + "	@ConditionalOnJava(value=ConditionalOnJava.JavaVersion.EIGHT)\n"
				+ "	@ConditionalOnMissingClass\n" + "	@ConditionalOnExpression\n" + "	public Hello hi() {\n"
				+ "		return null;\n" + "	}\n" + "}";

		Editor editor = harness.newEditor(LanguageId.JAVA, content, docUri);

		editor.assertHighlights("@ConditionalOnBean", "@ConditionalOnWebApplication",
				"@ConditionalOnJava(value=ConditionalOnJava.JavaVersion.EIGHT)", "@ConditionalOnMissingClass",
				"@ConditionalOnExpression");

	}

	@Test
	public void testHighlightsTypeConditionals() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-conditionals-live-hover/").toURI());
		String docUri = directory.toPath().resolve("src/main/java/example/MultipleConditionals.java").toUri()
				.toString();

		// Build a mock running boot app
		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
				.port("1111")
				.processID("22022")
				.host("cfapps.io")
				.processName("test-conditionals-live-hover")
				.liveConditionalsJson("{\"negativeMatches\": {\n" + "        \"MyConditionalComponent\": {\n"
						+ "            \"notMatched\": [\n" + "                {\n"
						+ "                    \"condition\": \"OnClassCondition\",\n"
						+ "                    \"message\": \"@ConditionalOnClass did not find required class 'java.lang.String2'\"\n"
						+ "                }\n" + "            ],\n" + "            \"matched\": []\n" + "        }\n"
						+ "}\n" + "}")
				.build();
		liveDataProvider.add("processkey", liveData);

		harness.intialize(directory);

		String content = "package com.example;\n" + "\n"
				+ "import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;\n"
				+ "import org.springframework.stereotype.Component;\n" + "\n" + "@Component\n"
				+ "@ConditionalOnClass(name=\"java.lang.String2\")\n" + "public class MyConditionalComponent {\n" + "}";

		Editor editor = harness.newEditor(LanguageId.JAVA, content, docUri);

		editor.assertHighlights("@ConditionalOnClass(name=\"java.lang.String2\")");

	}

	@Test
	public void testNegativeMatches() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-conditionals-live-hover/").toURI());
		String docUri = directory.toPath().resolve("src/main/java/example/MultipleConditionals.java").toUri()
				.toString();

		// Build a mock running boot app
		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
				.port("1111")
				.processID("67950")
				.host("cfapps.io")
				.processName("test-conditionals-live-hover")
				.liveConditionalsJson("{\"negativeMatches\": {\n" + "        \"MyConditionalComponent\": {\n"
						+ "            \"notMatched\": [\n" + "                {\n"
						+ "                    \"condition\": \"OnClassCondition\",\n"
						+ "                    \"message\": \"@ConditionalOnClass did not find required class 'java.lang.String2'\"\n"
						+ "                }\n" + "            ],\n" + "            \"matched\": []\n" + "        }\n"
						+ "}\n" + "}")
				.build();
		liveDataProvider.add("processkey", liveData);

		harness.intialize(directory);

		String content = "package com.example;\n" + "\n"
				+ "import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;\n"
				+ "import org.springframework.stereotype.Component;\n" + "\n" + "@Component\n"
				+ "@ConditionalOnClass(name=\"java.lang.String2\")\n" + "public class MyConditionalComponent {\n" + "}";

		Editor editor = harness.newEditor(LanguageId.JAVA, content, docUri);

		editor.assertHoverContains("@ConditionalOnClass(name=\"java.lang.String2\")",
				"@ConditionalOnClass did not find required class 'java.lang.String2'\n" + "\n"
						+ "Process [PID=67950, name=`test-conditionals-live-hover`]");

	}
}
