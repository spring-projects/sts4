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
				.build();
	}

	@Test
	public void testNoLiveHoverNoRunningApp() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-conditionals-live-hover/").toURI());
		String docUri = "file://" +directory.getAbsolutePath() + "/src/main/java/example/ConditionalOnMissingBeanConfig.java";

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
		String docUri = "file://" +directory.getAbsolutePath() + "/src/main/java/example/ConditionalOnBeanConfig.java";


		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.containsLanguageServerProcessPropery(false)
			.port("1111")
			.processId("22022")
			.host("cfapps.io")
			.processName("test-conditionals-live-hover")
			.getAutoConfigReport("{\"positiveMatches\":{\"ConditionalOnBeanConfig#hi\":[{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnBean (types: example.Hello; SearchStrategy: all) found bean 'missing'\"}]}}")
			.build();

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
		editor.assertHoverContains("@ConditionalOnBean", "Condition: OnBeanCondition\n" +
				"\n" +
				"Message: @ConditionalOnBean (types: example.Hello; SearchStrategy: all) found bean 'missing'");

	}

	@Test
	public void testLiveHoverConditionalOnMissingBean() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-conditionals-live-hover/").toURI());
		String docUri = "file://" + directory.getAbsolutePath() + "/src/main/java/example/ConditionalOnMissingBeanConfig.java";

		// Build a mock running boot app
		mockAppProvider.builder().isSpringBootApp(true).containsLanguageServerProcessPropery(false).port("1111")
				.processId("22022").host("cfapps.io").processName("test-conditionals-live-hover")
				.getAutoConfigReport(
						"{\"positiveMatches\":{\"ConditionalOnMissingBeanConfig#missing\":[{\"condition\":\"OnBeanCondition\",\"message\":\"@ConditionalOnMissingBean (types: example.Hello; SearchStrategy: all) did not find any beans\"}]}}")
				.build();

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
		editor.assertHoverContains("@ConditionalOnMissingBean", "Condition: OnBeanCondition\n" + "\n"
				+ "Message: @ConditionalOnMissingBean (types: example.Hello; SearchStrategy: all) did not find any beans");

	}
}
