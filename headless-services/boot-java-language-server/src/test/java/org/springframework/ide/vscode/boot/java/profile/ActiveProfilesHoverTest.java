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
package org.springframework.ide.vscode.boot.java.profile;

import java.time.Duration;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.MockRunningAppProvider;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;

public class ActiveProfilesHoverTest {

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
		harness.useProject(projects.mavenProject("empty-boot-15-web-app"));
		harness.intialize(null);
	}

	@Test
	public void testActiveProfileHover() throws Exception {
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.processId("22022")
			.processName("foo.bar.RunningApp")
			.profiles("testing-profile", "local-profile")
			.build();

		Editor editor = harness.newEditor(LanguageId.JAVA,
				"package hello;\n" +
				"\n" +
				"import org.springframework.context.annotation.Configuration;\n" +
				"import org.springframework.context.annotation.Profile;\n" +
				"\n" +
				"@Configuration\n" +
				"@Profile({\"local-profile\", \"inactive\", \"testing-profile\"})\n" +
				"public class LocalConfig {\n" +
				"}"
		);
		editor.assertHoverContains("@Profile", "testing-profile");
		editor.assertHoverContains("@Profile", "local-profile");
		editor.assertHoverContains("@Profile", "foo.bar.RunningApp");
		editor.assertHoverContains("@Profile", "22022");

		//TODO:
//		editor.assertHighlights(
//				"@Profile", "local-profile", "testing-profile"
//		);
	}


	@Test
	public void testActiveProfileHover_Unknown() throws Exception {
		//Sometimes its not possible to determine active profiles for an app (e.g. no actuator dependency).
		//Make sure we show something sensible

		mockAppProvider.builder()
			.isSpringBootApp(true)
			.processId("22022")
			.processName("foo.bar.RunningApp")
			.profilesUnknown()
			.build();

		Editor editor = harness.newEditor(LanguageId.JAVA,
				"package hello;\n" +
				"\n" +
				"import org.springframework.context.annotation.Configuration;\n" +
				"import org.springframework.context.annotation.Profile;\n" +
				"\n" +
				"@Configuration\n" +
				"@Profile(\"local\")\n" +
				"public class LocalConfig {\n" +
				"\n" +
				"}"
		);
		editor.assertHoverContains("@Profile", "Process [PID=22022, name=`foo.bar.RunningApp`] : _Unknown_");
	}

	@Test
	public void testNoRunningApps() throws Exception {
		Editor editor = harness.newEditor(LanguageId.JAVA,
				"package hello;\n" +
				"\n" +
				"import org.springframework.context.annotation.Configuration;\n" +
				"import org.springframework.context.annotation.Profile;\n" +
				"\n" +
				"@Configuration\n" +
				"@Profile(\"local\")\n" +
				"public class LocalConfig {\n" +
				"\n" +
				"}"
		);
		editor.assertNoHover("@Profile");
	}
}
