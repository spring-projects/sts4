/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.livehover.test;

import java.time.Duration;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBean;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBeansModel;
import org.springframework.ide.vscode.commons.maven.java.MavenJavaProject;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.MockRunningAppProvider;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;

public class FunctionInjectionsHoverProviderTest {

	private BootLanguageServerHarness harness;
	private ProjectsHarness projects = ProjectsHarness.INSTANCE;

	private MockRunningAppProvider mockAppProvider;

	@Before
	public void setup() throws Exception {
		mockAppProvider = new MockRunningAppProvider();
		harness = BootLanguageServerHarness.builder()
				.mockDefaults()
				.runningAppProvider(mockAppProvider.provider)
				.watchDogInterval(Duration.ofMillis(100))
				.build();

		MavenJavaProject jp =  projects.mavenProject("empty-boot-15-web-app");
		harness.useProject(jp);
		harness.intialize(null);
	}

	@Test
	public void componentWithNoInjections() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("scannedFunctionClass")
						.type("com.example.ScannedFunctionClass")
						.build()
				)
				.add(LiveBean.builder()
						.id("org.springframework.cloud.function.context.config.ContextFunctionCatalogAutoConfiguration")
						.type("org.springframework.cloud.function.context.config.ContextFunctionCatalogAutoConfiguration")
						.dependencies("scannedFunctionClass")
						.build()
				)
				.add(LiveBean.builder()
						.id("irrelevantBean")
						.type("com.example.IrrelevantBean")
						.dependencies("myController")
						.build()
				)
				.build();
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.processId("111")
			.processName("the-app")
			.beans(beans)
			.build();

		Editor editor = harness.newEditor(LanguageId.JAVA,
				"package com.example;\n" +
				"\n" +
				"import java.util.function.Function;\n" +
				"\n" +
				"public class ScannedFunctionClass implements Function<String, String> {\n" +
				"\n" +
				"	@Override\n" +
				"	public String apply(String t) {\n" +
				"		return t.toUpperCase();\n" +
				"	}\n" +
				"\n" +
				"}\n" +
				""
		);
		editor.assertHighlights("ScannedFunctionClass");
		editor.assertTrimmedHover("ScannedFunctionClass",
				"**Injection report for Bean [id: scannedFunctionClass, type: `com.example.ScannedFunctionClass`]**\n" +
				"\n" +
				"Process [PID=111, name=`the-app`]:\n" +
				"\n" +
				"Bean [id: scannedFunctionClass, type: `com.example.ScannedFunctionClass`] injected into:\n" +
				"\n" +
				"- Bean: org.springframework.cloud.function.context.config.ContextFunctionCatalogAutoConfiguration  \n" +
				"  Type: `org.springframework.cloud.function.context.config.ContextFunctionCatalogAutoConfiguration`"
		);
	}

}
