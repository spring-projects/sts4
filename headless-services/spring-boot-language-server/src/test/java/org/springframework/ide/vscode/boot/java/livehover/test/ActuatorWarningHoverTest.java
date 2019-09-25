/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.livehover.test;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.boot.bootiful.HoverTestConf;
import org.springframework.ide.vscode.boot.java.livehover.v2.SpringProcessLiveData;
import org.springframework.ide.vscode.boot.java.livehover.v2.SpringProcessLiveDataProvider;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.ide.vscode.project.harness.SpringProcessLiveDataBuilder;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@BootLanguageServerTest
@Import(HoverTestConf.class)
public class ActuatorWarningHoverTest {

	private static final String ACTUATOR_PROJECT = "empty-boot-15-web-app";
	private static final String NO_ACTUATOR_PROJECT = "no-actuator-boot-15-web-app";
	private ProjectsHarness projects = ProjectsHarness.INSTANCE;

	@Autowired private BootLanguageServerHarness harness;
	@Autowired private SpringProcessLiveDataProvider liveDataProvider;
	
	@After
	public void tearDown() throws Exception {
		liveDataProvider.remove("processkey");
	}

	@Test public void showWarningIf_NoActuator_and_RunningApp() throws Exception {
		
		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
			.processID("22022")
			.processName("foo.bar.RunningApp")
			.activeProfiles((String[]) null)
			.build();
		liveDataProvider.add("processkey", liveData);

		//No actuator on classpath:
		String projectName = NO_ACTUATOR_PROJECT;
		IJavaProject project = projects.mavenProject(projectName);
		harness.useProject(project);
		harness.intialize(null);

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

		editor.assertHighlights(/*NONE*/);
		editor.assertHoverContains("@Profile", "No live hover information");
		editor.assertHoverContains("@Profile", "Consider adding `spring-boot-actuator` as a dependency to your project `"+projectName+"`");
	}

	@Test public void noWarningIf_NoRunningApps() throws Exception {

		//No running app:
		// actaully... no code needed to set that up. mockAppBuilder is 'empty' by default.

		//No actuator on classpath:
		String projectName = NO_ACTUATOR_PROJECT;
		IJavaProject project = projects.mavenProject(projectName);
		harness.useProject(project);
		harness.intialize(null);

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

		editor.assertHighlights(/*NONE*/);
		editor.assertNoHover("@Profile");
	}

	@Test public void noWarningIf_ActuatorOnClasspath() throws Exception {
		//Has running app:
		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
			.processID("22022")
			.processName("foo.bar.RunningApp")
			.activeProfiles((String[]) null)
			.build();
		liveDataProvider.add("processkey", liveData);

		//Actuator on classpath:
		String projectName = ACTUATOR_PROJECT;
		IJavaProject project = projects.mavenProject(projectName);
		harness.useProject(project);
		harness.intialize(null);

		Editor editor = harness.newEditor(LanguageId.JAVA,
				"package hello;\n" +
				"\n" +
				"import org.springframework.context.annotation.Bean;\n" +
				"import org.springframework.context.annotation.Configuration;\n" +
				"import org.springframework.context.annotation.Profile;\n" +
				"\n" +
				"@Configuration\n" +
				"public class LocalConfig {\n" +
				"	\n" +
				"	@Bean\n" +
				"	Foo myFoo() {\n" +
				"		return new FooImplementation();\n" +
				"	}\n" +
				"}"
		);
		editor.assertHighlights(/*NONE*/);
		editor.assertNoHover("@Bean");
	}

	@Test public void warningHoverHasPreciseLocation() throws Exception {
		// It will be less annoying if limit the area the hover responds to, to just inside the
		// annotation name rather than the whole range of the ast node.

		//Has running app:
		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
			.processID("22022")
			.processName("foo.bar.RunningApp")
			.activeProfiles((String[]) null)
			.build();
		liveDataProvider.add("processkey", liveData);

		//No actuator on classpath:
		String projectName = NO_ACTUATOR_PROJECT;
		IJavaProject project = projects.mavenProject(projectName);
		harness.useProject(project);
		harness.intialize(null);

		Editor editor = harness.newEditor(LanguageId.JAVA,
				"package hello;\n" +
				"\n" +
				"import org.springframework.context.annotation.Bean;\n" +
				"import org.springframework.context.annotation.Configuration;\n" +
				"import org.springframework.context.annotation.Profile;\n" +
				"\n" +
				"@Configuration\n" +
				"public class LocalConfig {\n" +
				"	\n" +
				"	@Bean(\"the-bean-name\")\n" +
				"	Foo myFoo() {\n" +
				"		return new FooImplementation();\n" +
				"	}\n" +
				"}"
		);
		editor.assertHighlights(/*NONE*/);
		editor.assertHoverContains("@Bean", "No live hover information");
		editor.assertNoHover("the-bean-name");
	}

}
