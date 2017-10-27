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

public class InjectedIntoProviderTest {

	private static final ProjectCustomizer FOO_INTERFACE = (CustomizableProjectContent p) -> {
		p.createType("com.examle.Foo",
				"package com.example;\n" +
				"\n" +
				"public interface Foo {\n" +
				"	void doSomeFoo();\n" +
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

	@Test public void componentWithNoInjections() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("fooImplementation")
						.type("com.example.FooImplementation")
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
				"import org.springframework.stereotype.Component;\n" +
				"\n" +
				"@Component\n" +
				"public class FooImplementation implements Foo {\n" +
				"\n" +
				"	@Override\n" +
				"	public void doSomeFoo() {\n" +
				"		System.out.println(\"Foo do do do!\");\n" +
				"	}\n" +
				"}\n"
		);
		editor.assertHighlights("@Component");
		editor.assertTrimmedHover("@Component",
				"**Injection report for Bean [id: fooImplementation, type: `com.example.FooImplementation`]**\n" +
				"\n" +
				"Process [PID=111, name=`the-app`]:\n" +
				"\n" +
				"Bean [id: fooImplementation, type: `com.example.FooImplementation`] exists but is **Not injected anywhere**\n"
		);
	}

	@Test public void componentWithOneInjection() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("fooImplementation")
						.type("com.example.FooImplementation")
						.build()
				)
				.add(LiveBean.builder()
						.id("myController")
						.type("com.example.MyController")
						.dependencies("fooImplementation")
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
				"import org.springframework.stereotype.Component;\n" +
				"\n" +
				"@Component\n" +
				"public class FooImplementation implements Foo {\n" +
				"\n" +
				"	@Override\n" +
				"	public void doSomeFoo() {\n" +
				"		System.out.println(\"Foo do do do!\");\n" +
				"	}\n" +
				"}\n"
		);
		editor.assertHighlights("@Component");
		editor.assertTrimmedHover("@Component",
				"**Injection report for Bean [id: fooImplementation, type: `com.example.FooImplementation`]**\n" +
				"\n" +
				"Process [PID=111, name=`the-app`]:\n" +
				"\n" +
				"Bean [id: fooImplementation, type: `com.example.FooImplementation`] injected into:\n" +
				"\n" +
				"- Bean [id: myController, type: `com.example.MyController`]\n"
		);
	}

	@Test public void componentWithMultipleInjections() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("fooImplementation")
						.type("com.example.FooImplementation")
						.build()
				)
				.add(LiveBean.builder()
						.id("myController")
						.type("com.example.MyController")
						.dependencies("fooImplementation")
						.build()
				)
				.add(LiveBean.builder()
						.id("otherBean")
						.type("com.example.OtherBean")
						.dependencies("fooImplementation")
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
				"import org.springframework.stereotype.Component;\n" +
				"\n" +
				"@Component\n" +
				"public class FooImplementation implements Foo {\n" +
				"\n" +
				"	@Override\n" +
				"	public void doSomeFoo() {\n" +
				"		System.out.println(\"Foo do do do!\");\n" +
				"	}\n" +
				"}\n"
		);
		editor.assertHighlights("@Component");
		editor.assertTrimmedHover("@Component",
				"**Injection report for Bean [id: fooImplementation, type: `com.example.FooImplementation`]**\n" +
				"\n" +
				"Process [PID=111, name=`the-app`]:\n" +
				"\n" +
				"Bean [id: fooImplementation, type: `com.example.FooImplementation`] injected into:\n" +
				"\n" +
				"- Bean [id: myController, type: `com.example.MyController`]\n" +
				"- Bean [id: otherBean, type: `com.example.OtherBean`]\n"
		);
	}

	@Test public void componentWithMultipleInjectionsAndMultipleProcesses() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("fooImplementation")
						.type("com.example.FooImplementation")
						.build()
				)
				.add(LiveBean.builder()
						.id("myController")
						.type("com.example.MyController")
						.dependencies("fooImplementation")
						.build()
				)
				.add(LiveBean.builder()
						.id("otherBean")
						.type("com.example.OtherBean")
						.dependencies("fooImplementation")
						.build()
				)
				.build();
		for (int i = 1; i <= 2; i++) {
			mockAppProvider.builder()
			.isSpringBootApp(true)
			.processId("100"+i)
			.processName("app-instance-"+i)
			.beans(beans)
			.build();
		}

		Editor editor = harness.newEditor(LanguageId.JAVA,
				"package com.example;\n" +
				"\n" +
				"import org.springframework.stereotype.Component;\n" +
				"\n" +
				"@Component\n" +
				"public class FooImplementation implements Foo {\n" +
				"\n" +
				"	@Override\n" +
				"	public void doSomeFoo() {\n" +
				"		System.out.println(\"Foo do do do!\");\n" +
				"	}\n" +
				"}\n"
		);
		editor.assertHighlights("@Component");
		editor.assertTrimmedHover("@Component",
				"**Injection report for Bean [id: fooImplementation, type: `com.example.FooImplementation`]**\n" +
				"\n" +
				"Process [PID=1001, name=`app-instance-1`]:\n" +
				"\n" +
				"Bean [id: fooImplementation, type: `com.example.FooImplementation`] injected into:\n" +
				"\n" +
				"- Bean [id: myController, type: `com.example.MyController`]\n" +
				"- Bean [id: otherBean, type: `com.example.OtherBean`]\n" +
				"\n" +
				"Process [PID=1002, name=`app-instance-2`]:\n" +
				"\n" +
				"Bean [id: fooImplementation, type: `com.example.FooImplementation`] injected into:\n" +
				"\n" +
				"- Bean [id: myController, type: `com.example.MyController`]\n" +
				"- Bean [id: otherBean, type: `com.example.OtherBean`]\n"
		);
	}

	@Test public void onlyShowInfoForRelevantBeanId() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("fooImplementation")
						.type("com.example.FooImplementation")
						.build()
				)
				.add(LiveBean.builder()
						.id("alternateFooImplementation")
						.type("com.example.FooImplementation")
						.build()
				)
				.add(LiveBean.builder()
						.id("myController")
						.type("com.example.MyController")
						.dependencies("fooImplementation")
						.build()
				)
				.add(LiveBean.builder()
						.id("otherBean")
						.type("com.example.OtherBean")
						.dependencies("alternateFooImplementation")
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
				"import org.springframework.stereotype.Component;\n" +
				"\n" +
				"@Component\n" +
				"public class FooImplementation implements Foo {\n" +
				"\n" +
				"	@Override\n" +
				"	public void doSomeFoo() {\n" +
				"		System.out.println(\"Foo do do do!\");\n" +
				"	}\n" +
				"}\n"
		);
		editor.assertHighlights("@Component");
		editor.assertHoverExactText("@Component",
				"**Injection report for Bean [id: fooImplementation, type: `com.example.FooImplementation`]**\n" +
				"\n" +
				"Process [PID=111, name=`the-app`]:\n" +
				"\n" +
				"Bean [id: fooImplementation, type: `com.example.FooImplementation`] injected into:\n" +
				"\n" +
				"- Bean [id: myController, type: `com.example.MyController`]"
		);
	}

	@Test public void explicitComponentId() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("fooImplementation")
						.type("com.example.FooImplementation")
						.build()
				)
				.add(LiveBean.builder()
						.id("alternateFooImplementation")
						.type("com.example.FooImplementation")
						.build()
				)
				.add(LiveBean.builder()
						.id("myController")
						.type("com.example.MyController")
						.dependencies("fooImplementation")
						.build()
				)
				.add(LiveBean.builder()
						.id("otherBean")
						.type("com.example.OtherBean")
						.dependencies("alternateFooImplementation")
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
				"import org.springframework.stereotype.Component;\n" +
				"\n" +
				"@Component(\"alternateFooImplementation\")\n" +
				"public class FooImplementation implements Foo {\n" +
				"\n" +
				"	@Override\n" +
				"	public void doSomeFoo() {\n" +
				"		System.out.println(\"Foo do do do!\");\n" +
				"	}\n" +
				"}\n"
		);
		editor.assertHighlights("@Component");
		editor.assertTrimmedHover("@Component",
				"**Injection report for Bean [id: alternateFooImplementation, type: `com.example.FooImplementation`]**\n" +
				"\n" +
				"Process [PID=111, name=`the-app`]:\n" +
				"\n" +
				"Bean [id: alternateFooImplementation, type: `com.example.FooImplementation`] injected into:\n" +
				"\n" +
				"- Bean [id: otherBean, type: `com.example.OtherBean`]\n"
		);
	}

	@Test public void hoHoversWhenRunningAppDoesntHaveTheComponent() throws Exception {
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
				"import org.springframework.stereotype.Component;\n" +
				"\n" +
				"@Component\n" +
				"public class FooImplementation implements Foo {\n" +
				"\n" +
				"	@Override\n" +
				"	public void doSomeFoo() {\n" +
				"		System.out.println(\"Foo do do do!\");\n" +
				"	}\n" +
				"}\n"
		);
		editor.assertHighlights(/*MONE*/);
		editor.assertNoHover("@Component");
	}

	@Test public void hoHoversWhenNoRunningApps() throws Exception {
		Editor editor = harness.newEditor(LanguageId.JAVA,
				"package com.example;\n" +
				"\n" +
				"import org.springframework.stereotype.Component;\n" +
				"\n" +
				"@Component\n" +
				"public class FooImplementation implements Foo {\n" +
				"\n" +
				"	@Override\n" +
				"	public void doSomeFoo() {\n" +
				"		System.out.println(\"Foo do do do!\");\n" +
				"	}\n" +
				"}\n"
		);
		editor.assertHighlights(/*MONE*/);
		editor.assertNoHover("@Component");
	}

}
