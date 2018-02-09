/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
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

import java.nio.file.Path;
import java.nio.file.Paths;
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

public class BeanInjectedIntoHoverProviderTest {

	private static final ProjectCustomizer FOO_INTERFACE = (CustomizableProjectContent p) -> {
		p.createType("hello.Foo",
				"package hello;\n" +
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
		assertTrue(jp.getClasspath().findType("hello.Foo").exists());
		harness.useProject(jp);
		harness.intialize(null);
	}

	@Test
	public void beanWithNoInjections() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("myFoo")
						.type("hello.FooImplementation")
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
		editor.assertHighlights("@Bean");
		editor.assertTrimmedHover("@Bean",
				"**Injection report for Bean [id: myFoo]**\n" +
				"\n" +
				"Process [PID=111, name=`the-app`]:\n" +
				"\n" +
				"Bean [id: myFoo, type: `hello.FooImplementation`] exists but is **Not injected anywhere**\n"
		);
	}

	@Test
	public void explicitIdCases() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("beanId")
						.type("hello.FooImplementation")
						.build()
				)
				.add(LiveBean.builder()
						.id("irrelevantBean") //wrong id
						.type("hello.Foo") //right type (but should be ignored!)
						.build()
				)
				.build();
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.processId("111")
			.processName("the-app")
			.beans(beans)
			.build();

		String[] beanAnnotations = {
				"@Bean(value=\"beanId\")",
				"@Bean(value=\"beanId\", destroyMethod=\"cleanup\")",
				"@Bean(value= {\"beanId\", \"alias\"}, destroyMethod=\"cleanup\")",
				"@Bean(name=\"beanId\")",
				"@Bean(name=\"beanId\", destroyMethod=\"cleanup\")",
				"@Bean(name= {\"beanId\", \"alias\"}, destroyMethod=\"cleanup\")",

				"@Bean(\"beanId\")",
				"@Bean({\"beanId\", \"alias\"})",
		};
		for (String beanAnnotation : beanAnnotations) {
			Editor editor = harness.newEditor(
					"package hello;\n" +
					"\n" +
					"import org.springframework.context.annotation.Bean;\n" +
					"import org.springframework.context.annotation.Configuration;\n" +
					"import org.springframework.context.annotation.Profile;\n" +
					"\n" +
					"@Configuration\n" +
					"public class LocalConfig {\n" +
					"	\n" +
					"	"+beanAnnotation+"\n" +
					"	Foo otherFoo() {\n" +
					"		return new FooImplementation();\n" +
					"	}\n" +
					"}"
			);
			editor.assertHighlights("@Bean");
			editor.assertTrimmedHover("@Bean",
					"**Injection report for Bean [id: beanId]**\n" +
					"\n" +
					"Process [PID=111, name=`the-app`]:\n" +
					"\n" +
					"Bean [id: beanId, type: `hello.FooImplementation`] exists but is **Not injected anywhere**\n"
			);
		}
	}

	@Test
	public void beanWithOneInjection() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("fooImplementation")
						.type("hello.FooImplementation")
						.build()
				)
				.add(LiveBean.builder()
						.id("myController")
						.type("hello.MyController")
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
				"package hello;\n" +
				"\n" +
				"import org.springframework.context.annotation.Bean;\n" +
				"import org.springframework.context.annotation.Configuration;\n" +
				"import org.springframework.context.annotation.Profile;\n" +
				"\n" +
				"@Configuration\n" +
				"public class LocalConfig {\n" +
				"	\n" +
				"	@Bean(\"fooImplementation\")\n" +
				"	Foo someFoo() {\n" +
				"		return new FooImplementation();\n" +
				"	}\n" +
				"}"
		);
		editor.assertHighlights("@Bean");
		editor.assertTrimmedHover("@Bean",
				"**Injection report for Bean [id: fooImplementation]**\n" +
				"\n" +
				"Process [PID=111, name=`the-app`]:\n" +
				"\n" +
				"Bean [id: fooImplementation, type: `hello.FooImplementation`] injected into:\n" +
				"\n" +
				"- Bean: myController  \n" +
				"  Type: `hello.MyController`\n"
		);
	}

	@Test
	public void beanFromInnerClassWithOneInjection() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("fooImplementation")
						.type("hello.FooImplementation")
						.build()
				)
				.add(LiveBean.builder()
						.id("myController")
						.type("hello.MyController")
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
				"package hello;\n" +
				"\n" +
				"import org.springframework.context.annotation.Bean;\n" +
				"import org.springframework.context.annotation.Configuration;\n" +
				"import org.springframework.context.annotation.Profile;\n" +
				"\n" +
				"public class OuterClass {\n" +
				"	\n" +
				"	@Configuration\n" +
				"	public static class InnerClass {\n" +
				"		\n" +
				"		@Bean(\"fooImplementation\")\n" +
				"		Foo someFoo() {\n" +
				"			return new FooImplementation();\n" +
				"		}\n"	 +
				"	}\n"	 +
				"}"
		);
		editor.assertHighlights("@Bean");
		editor.assertTrimmedHover("@Bean",
				"**Injection report for Bean [id: fooImplementation]**\n" +
				"\n" +
				"Process [PID=111, name=`the-app`]:\n" +
				"\n" +
				"Bean [id: fooImplementation, type: `hello.FooImplementation`] injected into:\n" +
				"\n" +
				"- Bean: myController  \n" +
				"  Type: `hello.MyController`\n"
		);
	}

	@Test
	public void beanWithFileResource() throws Exception {
		Path of = harness.getOutputFolder();
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("fooImplementation")
						.type("hello.FooImplementation")
						.fileResource("should/not/matter")
						.build()
				)
				.add(LiveBean.builder()
						.id("myController")
						.type("hello.MyController")
						.fileResource(of+"/hello/MyController.class")
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
				"package hello;\n" +
				"\n" +
				"import org.springframework.context.annotation.Bean;\n" +
				"import org.springframework.context.annotation.Configuration;\n" +
				"import org.springframework.context.annotation.Profile;\n" +
				"\n" +
				"@Configuration\n" +
				"public class LocalConfig {\n" +
				"	\n" +
				"	@Bean(\"fooImplementation\")\n" +
				"	Foo someFoo() {\n" +
				"		return new FooImplementation();\n" +
				"	}\n" +
				"}"
		);
		editor.assertHighlights("@Bean");
		editor.assertTrimmedHover("@Bean",
				"**Injection report for Bean [id: fooImplementation]**\n" +
				"\n" +
				"Process [PID=111, name=`the-app`]:\n" +
				"\n" +
				"Bean [id: fooImplementation, type: `hello.FooImplementation`] injected into:\n" +
				"\n" +
				"- Bean: myController  \n" +
				"  Type: `hello.MyController`  \n" +
				"  Resource: `" + Paths.get("hello/MyController.class") + "`"
		);
	}

	@Test
	public void beanWithClasspathResource() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("fooImplementation")
						.type("hello.FooImplementation")
						.fileResource("should/not/matter")
						.build()
				)
				.add(LiveBean.builder()
						.id("myController")
						.type("hello.MyController")
						.classpathResource("hello/MyController.class")
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
				"package hello;\n" +
				"\n" +
				"import org.springframework.context.annotation.Bean;\n" +
				"import org.springframework.context.annotation.Configuration;\n" +
				"import org.springframework.context.annotation.Profile;\n" +
				"\n" +
				"@Configuration\n" +
				"public class LocalConfig {\n" +
				"	\n" +
				"	@Bean(\"fooImplementation\")\n" +
				"	Foo someFoo() {\n" +
				"		return new FooImplementation();\n" +
				"	}\n" +
				"}"
		);
		editor.assertHighlights("@Bean");
		editor.assertTrimmedHover("@Bean",
				"**Injection report for Bean [id: fooImplementation]**\n" +
				"\n" +
				"Process [PID=111, name=`the-app`]:\n" +
				"\n" +
				"Bean [id: fooImplementation, type: `hello.FooImplementation`] injected into:\n" +
				"\n" +
				"- Bean: myController  \n" +
				"  Type: `hello.MyController`  \n" +
				"  Resource: `hello/MyController.class`"
		);
	}

	@Test
	public void beanWithMultipleInjections() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("fooImplementation")
						.type("hello.FooImplementation")
						.build()
				)
				.add(LiveBean.builder()
						.id("myController")
						.type("hello.MyController")
						.dependencies("fooImplementation")
						.build()
				)
				.add(LiveBean.builder()
						.id("otherBean")
						.type("hello.OtherBean")
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
				"package hello;\n" +
				"\n" +
				"import org.springframework.context.annotation.Bean;\n" +
				"import org.springframework.context.annotation.Configuration;\n" +
				"import org.springframework.context.annotation.Profile;\n" +
				"\n" +
				"@Configuration\n" +
				"public class LocalConfig {\n" +
				"	\n" +
				"	@Bean(\"fooImplementation\")\n" +
				"	Foo someFoo() {\n" +
				"		return new FooImplementation();\n" +
				"	}\n" +
				"}"
		);
		editor.assertHighlights("@Bean");
		editor.assertTrimmedHover("@Bean",
				"**Injection report for Bean [id: fooImplementation]**\n" +
				"\n" +
				"Process [PID=111, name=`the-app`]:\n" +
				"\n" +
				"Bean [id: fooImplementation, type: `hello.FooImplementation`] injected into:\n" +
				"\n" +
				"- Bean: myController  \n" +
				"  Type: `hello.MyController`\n" +
				"- Bean: otherBean  \n" +
				"  Type: `hello.OtherBean`\n"
		);
	}

	@Test
	public void noHoversWhenRunningAppDoesntHaveTheBean() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("whateverBean")
						.type("com.example.UnrelatedBeanType")
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
				"package hello;\n" +
				"\n" +
				"import org.springframework.context.annotation.Bean;\n" +
				"import org.springframework.context.annotation.Configuration;\n" +
				"import org.springframework.context.annotation.Profile;\n" +
				"\n" +
				"@Configuration\n" +
				"public class LocalConfig {\n" +
				"	\n" +
				"	@Bean(\"fooImplementation\")\n" +
				"	Foo someFoo() {\n" +
				"		return new FooImplementation();\n" +
				"	}\n" +
				"}"
		);
		editor.assertHighlights(/*NONE*/);
		editor.assertNoHover("@Bean");
	}

	@Test
	public void noHoversWhenNoRunningApps() throws Exception {
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
				"	@Bean(\"fooImplementation\")\n" +
				"	Foo someFoo() {\n" +
				"		return new FooImplementation();\n" +
				"	}\n" +
				"}"
		);
		editor.assertHighlights(/*NONE*/);
		editor.assertNoHover("@Bean");
	}
}
