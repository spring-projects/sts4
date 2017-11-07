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
package org.springframework.ide.vscode.boot.java.conditionals.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.time.Duration;

import org.eclipse.lsp4j.Hover;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServer;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.MockRunningAppProvider;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;

public class ConditionalsLiveHoverTest {

	private LanguageServerHarness<BootJavaLanguageServer> harness;
	private MockRunningAppProvider mockAppProvider;

	@Before
	public void setup() throws Exception {

		mockAppProvider = new MockRunningAppProvider();
		harness = BootLanguageServerHarness.builder()
				.runningAppProvider(mockAppProvider.provider)
				.watchDogInterval(Duration.ofMillis(100))
				.build();
	}

	@Test
	public void testNoLiveHoverNoRunningApp() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-conditionals-live-hover/").toURI());
		String docUri = "file://" + directory.getAbsolutePath()
				+ "/src/main/java/example/ConditionalOnMissingBeanConfig.java";

		harness.intialize(directory);

		assertTrue("Expected no mock running boot apps, but found: " + mockAppProvider.mockedApps,
				mockAppProvider.mockedApps.isEmpty());

		Editor editorWithMethodLiveHover = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
		editorWithMethodLiveHover.assertNoHover("@ConditionalOnMissingBean");
	}

	@Test
	public void testLiveHoverConditionalOnBean() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-conditionals-live-hover/").toURI());
		String docUri = "file://" + directory.getAbsolutePath() + "/src/main/java/example/ConditionalOnBeanConfig.java";

		// Build a mock running boot app
		mockAppProvider.builder().isSpringBootApp(true).port("1111").processId("22022").host("cfapps.io")
				.processName("test-conditionals-live-hover")
				.positiveMatchesJsonForLiveConditionals(
						"{\"positiveMatches\":{\"ConditionalOnBeanConfig#hi\":[{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnBean (types: example.Hello; SearchStrategy: all) found bean 'missing'\"}]}}")
				.build();

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
		editor.assertHoverContains("@ConditionalOnBean",
				"@ConditionalOnBean (types: example.Hello; SearchStrategy: all) found bean 'missing'\n" +
				"\n" +
				"Process [PID=22022, name=`test-conditionals-live-hover`]");
	}

	@Test
	public void testLiveHoverConditionalOnMissingBean() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-conditionals-live-hover/").toURI());
		String docUri = "file://" + directory.getAbsolutePath()
				+ "/src/main/java/example/ConditionalOnMissingBeanConfig.java";

		// Build a mock running boot app
		mockAppProvider.builder().isSpringBootApp(true).port("1111").processId("22022").host("cfapps.io")
				.processName("test-conditionals-live-hover")
				.positiveMatchesJsonForLiveConditionals(
						"{\"positiveMatches\":{\"ConditionalOnMissingBeanConfig#missing\":[{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnMissingBean (types: example.Hello; SearchStrategy: all) did not find any beans\"}]}}")
				.build();

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
		editor.assertHoverContains("@ConditionalOnMissingBean", "@ConditionalOnMissingBean (types: example.Hello; SearchStrategy: all) did not find any beans\n"+
				"\n" +
				"Process [PID=22022, name=`test-conditionals-live-hover`]");

	}

	@Test
	public void testMultipleLiveHoverContentRealProject() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-conditionals-live-hover/").toURI());
		String docUri = "file://" + directory.getAbsolutePath() + "/src/main/java/example/MultipleConditionals.java";

		// Build a mock running boot app
		mockAppProvider.builder().isSpringBootApp(true).port("1111").processId("22022").host("cfapps.io")
				.processName("test-conditionals-live-hover")
				.positiveMatchesJsonForLiveConditionals(
						"{\"positiveMatches\":{\"HelloConfig#missing\":[{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnMissingBean (types: example.Hello; SearchStrategy: all) did not find any beans\"}],\"HelloConfig2#hi\":[{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnBean (types: example.Hello; SearchStrategy: all) found bean 'missing'\"}],\"MultipleConditionals#hi\":[{\"condition\":\"OnClassCondition\",\"message\":\"@ConditionalOnClass found required class; @ConditionalOnMissingClass did not find unwanted class\"},{\"condition\":\"OnWebApplicationCondition\",\"message\":\"@ConditionalOnWebApplication (required) found StandardServletEnvironment\"},{\"condition\":\"OnJavaCondition\",\"message\":\"@ConditionalOnJava (1.8 or newer) found 1.8\"},{\"condition\":\"OnExpressionCondition\",\"message\":\"@ConditionalOnExpression (#{true}) resulted in true\"},{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnBean (types: example.Hello; SearchStrategy: all) found beans 'hi', 'missing'\"}]}}")
				.build();

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);

		editor.assertHoverContains("@ConditionalOnBean", "@ConditionalOnBean (types: example.Hello; SearchStrategy: all) found beans 'hi', 'missing'\n"	+
				"\n" +
				"Process [PID=22022, name=`test-conditionals-live-hover`]");

		editor.assertHoverContains("@ConditionalOnWebApplication", "@ConditionalOnWebApplication (required) found StandardServletEnvironment\n"+
				"\n" +
				"Process [PID=22022, name=`test-conditionals-live-hover`]");

		editor.assertHoverContains("@ConditionalOnJava(value=ConditionalOnJava.JavaVersion.EIGHT)",
				"@ConditionalOnJava (1.8 or newer) found 1.8\n" +
						"\n" +
						"Process [PID=22022, name=`test-conditionals-live-hover`]");

		editor.assertHoverContains("@ConditionalOnMissingClass", "@ConditionalOnClass found required class; @ConditionalOnMissingClass did not find unwanted class\n"	+
				"\n" +
				"Process [PID=22022, name=`test-conditionals-live-hover`]");

		editor.assertHoverContains("@ConditionalOnExpression", "@ConditionalOnExpression (#{true}) resulted in true\n"	+
				"\n" +
				"Process [PID=22022, name=`test-conditionals-live-hover`]");
	}

	@Test
	public void testMultipleAppInstances() throws Exception {

		// Test that live hover shows information for multiple app instances
		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-conditionals-live-hover/").toURI());
		String docUri = "file://" + directory.getAbsolutePath()
				+ "/src/main/java/example/ConditionalOnMissingBeanConfig.java";

		// Build a mock running boot app
		mockAppProvider.builder().isSpringBootApp(true).port("1000").processId("70000").host("cfapps.io")
				.processName("test-conditionals-live-hover")
				.positiveMatchesJsonForLiveConditionals(
						"{\"positiveMatches\":{\"ConditionalOnMissingBeanConfig#missing\":[{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnMissingBean (types: example.Hello; SearchStrategy: all) did not find any beans\"}]}}")
				.build();

		mockAppProvider.builder().isSpringBootApp(true).port("1001").processId("80000").host("cfapps.io")
				.processName("test-conditionals-live-hover")
				.positiveMatchesJsonForLiveConditionals(
						"{\"positiveMatches\":{\"ConditionalOnMissingBeanConfig#missing\":[{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnMissingBean (types: example.Hello; SearchStrategy: all) did not find any beans\"}]}}")
				.build();

		mockAppProvider.builder().isSpringBootApp(true).port("1002").processId("90000").host("cfapps.io")
				.processName("test-conditionals-live-hover")
				.positiveMatchesJsonForLiveConditionals(
						"{\"positiveMatches\":{\"ConditionalOnMissingBeanConfig#missing\":[{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnMissingBean (types: example.Hello; SearchStrategy: all) did not find any beans\"}]}}")
				.build();

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);


		editor.assertHoverContains("@ConditionalOnMissingBean", "@ConditionalOnMissingBean (types: example.Hello; SearchStrategy: all) did not find any beans\n" +
				"\n" +
				"Process [PID=70000, name=`test-conditionals-live-hover`]\n" +
				"\n" +
				"---\n" +
				"\n" +
				"@ConditionalOnMissingBean (types: example.Hello; SearchStrategy: all) did not find any beans\n" +
              	"\n" +
              	"Process [PID=80000, name=`test-conditionals-live-hover`]\n" +
				"\n" +
				"---\n" +
				"\n" +
				"@ConditionalOnMissingBean (types: example.Hello; SearchStrategy: all) did not find any beans\n" +
              	"\n" +
				"Process [PID=90000, name=`test-conditionals-live-hover`]");

	}

	@Test
	public void testMultipleConditionalsSameMethod() throws Exception {

		// Tests something like this:
//		@Bean
//		@ConditionalOnBean
//		@ConditionalOnWebApplication
//		@ConditionalOnJava(value=ConditionalOnJava.JavaVersion.EIGHT)
//		@ConditionalOnMissingClass
//		@ConditionalOnExpression
//		public Hello hi() {
//			return null;
//		}

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-conditionals-live-hover/").toURI());
		String docUri = "file://" + directory.getAbsolutePath() + "/src/main/java/example/MultipleConditionals.java";

		// Build a mock running boot app
		mockAppProvider.builder().isSpringBootApp(true).port("1000").processId("70000").host("cfapps.io")
				.processName("test-conditionals-live-hover")
				.positiveMatchesJsonForLiveConditionals(
						"{\"positiveMatches\":{\"HelloConfig#missing\":[{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnMissingBean (types: example.Hello; SearchStrategy: all) did not find any beans\"}],\"HelloConfig2#hi\":[{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnBean (types: example.Hello; SearchStrategy: all) found bean 'missing'\"}],\"MultipleConditionals#hi\":[{\"condition\":\"OnClassCondition\",\"message\":\"@ConditionalOnClass found required class; @ConditionalOnMissingClass did not find unwanted class\"},{\"condition\":\"OnWebApplicationCondition\",\"message\":\"@ConditionalOnWebApplication (required) found StandardServletEnvironment\"},{\"condition\":\"OnJavaCondition\",\"message\":\"@ConditionalOnJava (1.8 or newer) found 1.8\"},{\"condition\":\"OnExpressionCondition\",\"message\":\"@ConditionalOnExpression (#{true}) resulted in true\"},{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnBean (types: example.Hello; SearchStrategy: all) found beans 'hi', 'missing'\"}]}}")
				.build();

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);

		// IMPORTANT: test EXACT text to ensure that multiple conditionals on the same method do not show
		// up while
		// hovering over only one of the conditional annotations
		editor.assertHoverExactText("@ConditionalOnBean",
				"@ConditionalOnBean (types: example.Hello; SearchStrategy: all) found beans 'hi', 'missing'\n" + "\n"
						+ "Process [PID=70000, name=`test-conditionals-live-hover`]");

		editor.assertHoverExactText("@ConditionalOnWebApplication",
				"@ConditionalOnWebApplication (required) found StandardServletEnvironment\n" + "\n"
						+ "Process [PID=70000, name=`test-conditionals-live-hover`]");

		editor.assertHoverExactText("@ConditionalOnJava(value=ConditionalOnJava.JavaVersion.EIGHT)",
				"@ConditionalOnJava (1.8 or newer) found 1.8\n" + "\n" + "Process [PID=70000, name=`test-conditionals-live-hover`]");

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
		String docUri = "file://" + directory.getAbsolutePath()
				+ "/src/main/java/example/MultipleConditionalsPT152535713.java";

		// Build a mock running boot app
		mockAppProvider.builder().isSpringBootApp(true).port("1000").processId("70000").host("cfapps.io")
				.processName("test-conditionals-live-hover")
				.positiveMatchesJsonForLiveConditionals(
						"{\"positiveMatches\":{\"MultipleConditionalsPT152535713#hi\":[{\"condition\":\"OnWebApplicationCondition\",\"message\":\"@ConditionalOnWebApplication (required) found StandardServletEnvironment\"},{\"condition\":\"OnJavaCondition\",\"message\":\"@ConditionalOnJava (1.8 or newer) found 1.8\"}]}}")
				.build();

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);

		editor.assertHoverExactText("@ConditionalOnWebApplication",
				"@ConditionalOnWebApplication (required) found StandardServletEnvironment\n" + "\n"
						+ "Process [PID=70000, name=`test-conditionals-live-hover`]");

		editor.assertHoverExactText("@ConditionalOnJava(value=ConditionalOnJava.JavaVersion.EIGHT)",
				"@ConditionalOnJava (1.8 or newer) found 1.8\n" + "\n" + "Process [PID=70000, name=`test-conditionals-live-hover`]");

		// Test that the hovers dont have extra information of the other conditionals:
		Hover hover = editor.getHover("@ConditionalOnWebApplication");
		String hoverContent = editor.hoverString(hover);
		assertFalse(hoverContent.contains("@ConditionalOnJava"));

		hover = editor.getHover("@ConditionalOnJava(value=ConditionalOnJava.JavaVersion.EIGHT)");
		hoverContent = editor.hoverString(hover);
		assertFalse(hoverContent.contains("@ConditionalOnWebApplication"));
	}

	@Test
	public void testHighlights() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-conditionals-live-hover/").toURI());
		String docUri = "file://" + directory.getAbsolutePath() + "/src/main/java/example/MultipleConditionals.java";

		// Build a mock running boot app
		mockAppProvider.builder().isSpringBootApp(true).port("1111").processId("22022").host("cfapps.io")
				.processName("test-conditionals-live-hover")
				.positiveMatchesJsonForLiveConditionals(
						"{\"positiveMatches\":{\"HelloConfig#missing\":[{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnMissingBean (types: example.Hello; SearchStrategy: all) did not find any beans\"}],\"HelloConfig2#hi\":[{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnBean (types: example.Hello; SearchStrategy: all) found bean 'missing'\"}],\"MultipleConditionals#hi\":[{\"condition\":\"OnClassCondition\",\"message\":\"@ConditionalOnClass found required class; @ConditionalOnMissingClass did not find unwanted class\"},{\"condition\":\"OnWebApplicationCondition\",\"message\":\"@ConditionalOnWebApplication (required) found StandardServletEnvironment\"},{\"condition\":\"OnJavaCondition\",\"message\":\"@ConditionalOnJava (1.8 or newer) found 1.8\"},{\"condition\":\"OnExpressionCondition\",\"message\":\"@ConditionalOnExpression (#{true}) resulted in true\"},{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnBean (types: example.Hello; SearchStrategy: all) found beans 'hi', 'missing'\"}]}}")
				.build();

		harness.intialize(directory);


		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);

		editor.assertHighlights("@ConditionalOnBean", "@ConditionalOnWebApplication",
				"@ConditionalOnJava(value=ConditionalOnJava.JavaVersion.EIGHT)", "@ConditionalOnMissingClass",
				"@ConditionalOnExpression");

	}
}
