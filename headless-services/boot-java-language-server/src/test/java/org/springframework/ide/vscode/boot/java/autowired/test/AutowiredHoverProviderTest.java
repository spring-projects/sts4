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
package org.springframework.ide.vscode.boot.java.autowired.test;

import static org.junit.Assert.assertTrue;

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
import org.springframework.ide.vscode.project.harness.ProjectsHarness.CustomizableProjectContent;
import org.springframework.ide.vscode.project.harness.ProjectsHarness.ProjectCustomizer;

/**
 * @author Martin Lippert
 */
public class AutowiredHoverProviderTest {

	private static final ProjectCustomizer FOO_INTERFACE = (CustomizableProjectContent p) -> {
		p.createType("com.examle.Foo",
				"package com.example;\n" +
				"\n" +
				"public interface Foo {\n" +
				"	void doSomeFoo();\n" +
				"}\n"
		);

		p.createType("com.examle.DependencyA",
				"package com.example;\n" +
				"\n" +
				"public class DependencyA {\n" +
				"}\n"
		);

		p.createType("com.examle.DependencyB",
				"package com.example;\n" +
				"\n" +
				"public class DependencyB {\n" +
				"}\n"
		);

	};

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

		MavenJavaProject jp =  projects.mavenProject("empty-boot-15-web-app", FOO_INTERFACE);
		assertTrue(jp.getClasspath().findType("com.example.Foo").exists());
		harness.useProject(projects.mavenProject("empty-boot-15-web-app"));
		harness.intialize(null);
	}

	@Test
	public void componentWithAutomaticallyWiredConstructorInjections() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("autowiredClass")
						.type("com.example.AutowiredClass")
						.dependencies("dependencyA", "dependencyB")
						.build()
				)
				.add(LiveBean.builder()
						.id("dependencyA")
						.type("com.example.DependencyA")
						.build()
				)
				.add(LiveBean.builder()
						.id("dependencyB")
						.type("com.example.DependencyB")
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
				"import org.springframework.beans.factory.annotation.Autowired;\n" +
				"import org.springframework.stereotype.Component;\n" +
				"\n" +
				"@Component\n" +
				"public class AutowiredClass {\n" +
				"\n" +
				"   @Autowired\n" +
				"	public AutowiredClass(DependencyA depA, DependencyB depB) {\n" +
				"	}\n" +
				"}\n"
		);

		editor.assertHighlights("@Component", "@Autowired");
		editor.assertTrimmedHover("@Autowired",
				"**Injection report for Bean [id: autowiredClass, type: `com.example.AutowiredClass`]**\n" +
				"\n" +
				"Process [PID=111, name=`the-app`]:\n" +
				"\n" +
				"Bean [id: autowiredClass, type: `com.example.AutowiredClass`] got autowired with:\n" +
				"\n" +
				"- Bean: dependencyA  \n" +
				"  Type: `com.example.DependencyA`\n" +
				"- Bean: dependencyB  \n" +
				"  Type: `com.example.DependencyB`\n"
		);
	}

	@Test
	public void noHoversWhenNoRunningApps() throws Exception {
		Editor editor = harness.newEditor(LanguageId.JAVA,
				"package com.example;\n" +
				"\n" +
				"import org.springframework.beans.factory.annotation.Autowired;\n" +
				"import org.springframework.stereotype.Component;\n" +
				"\n" +
				"@Component\n" +
				"public class AutowiredClass {\n" +
				"\n" +
				"   @Autowired\n" +
				"	public AutowiredClass(DependencyA depA, DependencyB depB) {\n" +
				"	}\n" +
				"}\n"
		);

		editor.assertHighlights(/*MONE*/);
		editor.assertNoHover("@Autowired");
	}

	@Test
	public void noHoversWhenRunningAppDoesntHaveTheComponent() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("whateverBean")
						.type("com.example.UnrelatedComponent")
						.build()
				)
				.add(LiveBean.builder()
						.id("myController")
						.type("com.example.UnrelatedComponent")
						.dependencies("whateverBean")
						.build()
				)
				.build();
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.processId("111")
			.processName("unrelated-app")
			.beans(beans)
			.build();

		Editor editor = harness.newEditor(LanguageId.JAVA,
				"package com.example;\n" +
				"\n" +
				"import org.springframework.beans.factory.annotation.Autowired;\n" +
				"import org.springframework.stereotype.Component;\n" +
				"\n" +
				"@Component\n" +
				"public class AutowiredClass {\n" +
				"\n" +
				"   @Autowired\n" +
				"	public AutowiredClass(DependencyA depA, DependencyB depB) {\n" +
				"	}\n" +
				"}\n"
		);

		editor.assertHighlights(/*MONE*/);
		editor.assertNoHover("@Autowired");
	}

	@Test
	public void noHoversWhenRunningAppDoesntHaveDependenciesForTheAutowiring() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("autowiredClass")
						.type("com.example.AutowiredClass")
						.build()
				)
				.add(LiveBean.builder()
						.id("dependencyA")
						.type("com.example.DependencyA")
						.build()
				)
				.add(LiveBean.builder()
						.id("dependencyB")
						.type("com.example.DependencyB")
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
				"import org.springframework.beans.factory.annotation.Autowired;\n" +
				"import org.springframework.stereotype.Component;\n" +
				"\n" +
				"@Component\n" +
				"public class AutowiredClass {\n" +
				"\n" +
				"   @Autowired\n" +
				"	public AutowiredClass(DependencyA depA, DependencyB depB) {\n" +
				"	}\n" +
				"}\n"
		);

		editor.assertHighlights("@Component");
		editor.assertNoHover("@Autowired");
	}

	@Test public void bug_152553935() throws Exception {
		//https://www.pivotaltracker.com/story/show/152553935
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("defaultFoo")
						.type("com.example.FooImplementation")
						.dependencies("defaultFoo")
						.dependencies("otherBean")
						.build()
				)
				.add(LiveBean.builder()
						.id("otherBean")
						.type("com.example.DependencyA")
						.build()
				)
				.build();
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.processId("111")
			.processName("the-app")
			.beans(beans)
			.build();


		Editor editor = harness.newEditor(
				"package com.example;\n" +
				"\n" +
				"import org.springframework.beans.factory.annotation.Autowired;\n" +
				"import org.springframework.scheduling.TaskScheduler;\n" +
				"import org.springframework.stereotype.Component;\n" +
				"\n" +
				"@Component(\"defaultFoo\")\n" +
				"public class FooImplementation implements Foo {\n" +
				"	\n" +
				"	private TaskScheduler scheduler;\n" +
				"	\n" +
				"	@Autowired Foo self;\n" +
				"		\n" +
				"	@Override\n" +
				"	public void doSomeFoo() {\n" +
				"		scheduler.scheduleWithFixedDelay(() -> {\n" +
				"			System.out.println(\"Doo Done done!\");\n" +
				"		}, 1000);\n" +
				"		System.out.println(\"Foo do do do do!\");\n" +
				"	}\n" +
				"\n" +
				"	@Autowired\n" +
				"	public void setScheduler(TaskScheduler scheduler) {\n" +
				"		this.scheduler = scheduler;\n" +
				"	}\n" +
				"\n" +
				"}"
		);
		editor.assertHighlights("@Component", "@Autowired", "@Autowired");
		for (int i = 1; i <= 2; i++) {
			editor.assertHoverContains("@Autowired", 1,
					"Bean [id: defaultFoo, type: `com.example.FooImplementation`] got autowired with:\n" +
					"\n" +
					"- Bean: otherBean  \n" +
					"  Type: `com.example.DependencyA`");
		}
	}

}
