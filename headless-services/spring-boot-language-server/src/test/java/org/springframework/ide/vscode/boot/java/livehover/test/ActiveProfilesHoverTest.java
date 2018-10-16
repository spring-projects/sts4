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
package org.springframework.ide.vscode.boot.java.livehover.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.MockRunningAppProvider;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.ide.vscode.boot.bootiful.HoverTestConf;

@RunWith(SpringRunner.class)
@BootLanguageServerTest
@Import(HoverTestConf.class)
public class ActiveProfilesHoverTest {

	private ProjectsHarness projects = ProjectsHarness.INSTANCE;

	@Autowired
	private BootLanguageServerHarness harness;

	@Autowired
	private MockRunningAppProvider mockAppProvider;

	@Before
	public void setup() throws Exception {
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

		String[] hoverSites = {
				"@Profile", "local-profile", "testing-profile"
		};
		editor.assertHighlights(
				hoverSites
		);
		for (String hoverOver : hoverSites) {
			editor.assertHoverContains(hoverOver, "testing-profile");
			editor.assertHoverContains(hoverOver, "local-profile");
			editor.assertHoverContains(hoverOver, "foo.bar.RunningApp");
			editor.assertHoverContains(hoverOver, "22022");
		}
	}

	@Test
	public void testActiveProfileHover_Unknown() throws Exception {
		//Sometimes its not possible to determine active profiles for an app (e.g. no actuator dependency).
		//Make sure we show something sensible
		harness.useProject(projects.mavenProject("no-actuator-boot-15-web-app"));

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
		editor.assertHoverContains("@Profile", "Consider adding `spring-boot-actuator` as a dependency");
		editor.assertHighlights(/*NONE*/);
	}

	@Test
	public void testActiveProfileHoverMixedKnownAndUnknown() throws Exception {
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.processId("22022")
			.processName("foo.bar.NoActuatorApp")
			.profilesUnknown()
			.build();

		mockAppProvider.builder()
			.isSpringBootApp(true)
			.processId("3456")
			.processName("foo.bar.NormalApp")
			.profiles("fancy")
			.build();

		Editor editor = harness.newEditor(LanguageId.JAVA,
				"package hello;\n" +
				"\n" +
				"import org.springframework.context.annotation.Configuration;\n" +
				"import org.springframework.context.annotation.Profile;\n" +
				"\n" +
				"@Configuration\n" +
				"@Profile({\"unknown\", \"inactive\", \"fancy\"})\n" +
				"public class LocalConfig {\n" +
				"\n" +
				"}"
		);

		editor.assertHighlights("@Profile", "fancy");
		editor.assertHoverContains("@Profile", "Unknown");
		editor.assertHoverContains("@Profile", "fancy");
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
		editor.assertHighlights(/*NONE*/);
	}
}
