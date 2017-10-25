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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.time.Duration;

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
				.getAutoConfigReport(
						"{\"positiveMatches\":{\"ConditionalOnBeanConfig#hi\":[{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnBean (types: example.Hello; SearchStrategy: all) found bean 'missing'\"}]}}")
				.build();

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
		editor.assertHoverContains("@ConditionalOnBean", "Condition: OnBeanCondition\n" + "\n"
				+ "Message: @ConditionalOnBean (types: example.Hello; SearchStrategy: all) found bean 'missing'");

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
				.getAutoConfigReport(
						"{\"positiveMatches\":{\"ConditionalOnMissingBeanConfig#missing\":[{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnMissingBean (types: example.Hello; SearchStrategy: all) did not find any beans\"}]}}")
				.build();

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
		editor.assertHoverContains("@ConditionalOnMissingBean", "Condition: OnBeanCondition\n" + "\n"
				+ "Message: @ConditionalOnMissingBean (types: example.Hello; SearchStrategy: all) did not find any beans");

	}

	@Test
	public void testMultipleLiveHoverContentRealProject() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-conditionals-live-hover/").toURI());
		String docUri = "file://" + directory.getAbsolutePath() + "/src/main/java/example/MultipleConditionals.java";

		// Build a mock running boot app
		mockAppProvider.builder().isSpringBootApp(true).port("1111").processId("22022").host("cfapps.io")
				.processName("test-conditionals-live-hover")
				.getAutoConfigReport(
						"{\"positiveMatches\":{\"HelloConfig#missing\":[{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnMissingBean (types: example.Hello; SearchStrategy: all) did not find any beans\"}],\"HelloConfig2#hi\":[{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnBean (types: example.Hello; SearchStrategy: all) found bean 'missing'\"}],\"MultipleConditionals#hi\":[{\"condition\":\"OnClassCondition\",\"message\":\"@ConditionalOnClass found required class; @ConditionalOnMissingClass did not find unwanted class\"},{\"condition\":\"OnWebApplicationCondition\",\"message\":\"@ConditionalOnWebApplication (required) found StandardServletEnvironment\"},{\"condition\":\"OnJavaCondition\",\"message\":\"@ConditionalOnJava (1.8 or newer) found 1.8\"},{\"condition\":\"OnExpressionCondition\",\"message\":\"@ConditionalOnExpression (#{true}) resulted in true\"},{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnBean (types: example.Hello; SearchStrategy: all) found beans 'hi', 'missing'\"}]}}")
				.build();

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);

		editor.assertHoverContains("@ConditionalOnBean", "Condition: OnBeanCondition\n" + "\n"
				+ "Message: @ConditionalOnBean (types: example.Hello; SearchStrategy: all) found beans 'hi', 'missing'");

		editor.assertHoverContains("@ConditionalOnWebApplication", "Condition: OnWebApplicationCondition\n" + "\n"
				+ "Message: @ConditionalOnWebApplication (required) found StandardServletEnvironment");

		editor.assertHoverContains("@ConditionalOnJava(value=ConditionalOnJava.JavaVersion.EIGHT)",
				"Condition: OnJavaCondition\n" + "\n" + "Message: @ConditionalOnJava (1.8 or newer) found 1.8");

		editor.assertHoverContains("@ConditionalOnMissingClass", "Condition: OnClassCondition\n" + "\n"
				+ "Message: @ConditionalOnClass found required class; @ConditionalOnMissingClass did not find unwanted class");

		editor.assertHoverContains("@ConditionalOnExpression", "Condition: OnExpressionCondition\n" + "\n"
				+ "Message: @ConditionalOnExpression (#{true}) resulted in true");
	}

//	@Test
//	public void testMultipleLiveHoverHints() throws Exception {
//
//		File directory = new File(
//				ProjectsHarness.class.getResource("/test-projects/test-conditionals-live-hover/").toURI());
//		String docUri = "file://" + directory.getAbsolutePath() + "/src/main/java/example/MultipleConditionals.java";
//
//		// Build a mock running boot app
//		mockAppProvider.builder().isSpringBootApp(true).port("1111").processId("22022").host("cfapps.io")
//				.processName("test-conditionals-live-hover")
//				.getAutoConfigReport(
//						"{\"positiveMatches\":{\"HelloConfig#missing\":[{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnMissingBean (types: example.Hello; SearchStrategy: all) did not find any beans\"}],\"HelloConfig2#hi\":[{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnBean (types: example.Hello; SearchStrategy: all) found bean 'missing'\"}],\"MultipleConditionals#hi\":[{\"condition\":\"OnClassCondition\",\"message\":\"@ConditionalOnClass found required class; @ConditionalOnMissingClass did not find unwanted class\"},{\"condition\":\"OnWebApplicationCondition\",\"message\":\"@ConditionalOnWebApplication (required) found StandardServletEnvironment\"},{\"condition\":\"OnJavaCondition\",\"message\":\"@ConditionalOnJava (1.8 or newer) found 1.8\"},{\"condition\":\"OnExpressionCondition\",\"message\":\"@ConditionalOnExpression (#{true}) resulted in true\"},{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnBean (types: example.Hello; SearchStrategy: all) found beans 'hi', 'missing'\"}]}}")
//				.build();
//
//		harness.intialize(directory);
//
//		Editor editor = harness.newEditor(LanguageId.JAVA, "package example;\n" +
//				"\n" +
//				"import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;\n" +
//				"import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;\n" +
//				"import org.springframework.boot.autoconfigure.condition.ConditionalOnJava;\n" +
//				"import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;\n" +
//				"import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;\n" +
//				"import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;\n" +
//				"import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;\n" +
//				"import org.springframework.context.annotation.Bean;\n" +
//				"import org.springframework.context.annotation.Configuration;\n" +
//				"\n" +
//				"@Configuration\n" +
//				"public class MultipleConditionals {\n" +
//				"\n" +
//				"	@Bean\n" +
//				"	@ConditionalOnBean\n" +
//				"	@ConditionalOnWebApplication\n" +
//				"	@ConditionalOnJava(value=ConditionalOnJava.JavaVersion.EIGHT)\n" +
//				"	@ConditionalOnMissingClass\n" +
//				"	@ConditionalOnExpression\n" +
//				"	public Hello hi() {\n" +
//				"		return null;\n" +
//				"	}\n" +
//				"	\n" +
//				"	@Bean\n" +
//				"	@ConditionalOnMissingBean\n" +
//				"	@ConditionalOnNotWebApplication\n" +
//				"	public Hello missing() {\n" +
//				"		return null;\n" +
//				"	}\n" +
//				"}");
//
////		editor.assertHoverContains("@ConditionalOnBean", "Condition: OnBeanCondition\n" + "\n"
////				+ "Message: @ConditionalOnBean (types: example.Hello; SearchStrategy: all) found beans 'hi', 'missing'");
////
////		editor.assertHoverContains("@ConditionalOnWebApplication", "Condition: OnWebApplicationCondition\n" + "\n"
////				+ "Message: @ConditionalOnWebApplication (required) found StandardServletEnvironment");
////
////		editor.assertHoverContains("@ConditionalOnJava(value=ConditionalOnJava.JavaVersion.EIGHT)",
////				"Condition: OnJavaCondition\n" + "\n" + "Message: @ConditionalOnJava (1.8 or newer) found 1.8");
////
////		editor.assertHoverContains("@ConditionalOnMissingClass", "Condition: OnClassCondition\n" + "\n"
////				+ "Message: @ConditionalOnClass found required class; @ConditionalOnMissingClass did not find unwanted class");
////
////		editor.assertHoverContains("@ConditionalOnExpression", "Condition: OnExpressionCondition\n" + "\n"
////				+ "Message: @ConditionalOnExpression (#{true}) resulted in true");
//
//		editor.assertHighlights("@ConditionalOnBean", "@ConditionalOnWebApplication",
//				"@ConditionalOnJava(value=ConditionalOnJava.JavaVersion.EIGHT)", "@ConditionalOnMissingClass",
//				"@ConditionalOnExpression");
//
//	}
}
