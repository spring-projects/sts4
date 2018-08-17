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

import java.time.Duration;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBean;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBeansModel;
import org.springframework.ide.vscode.commons.maven.java.MavenJavaProject;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.project.harness.BootJavaLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.MockRunningAppProvider;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness.CustomizableProjectContent;
import org.springframework.ide.vscode.project.harness.ProjectsHarness.ProjectCustomizer;

public class ComponentInjectionsHoverProviderTest {

	private static final ProjectCustomizer EXTRA_TYPES = (CustomizableProjectContent p) -> {
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

	private BootJavaLanguageServerHarness harness;
	private ProjectsHarness projects = ProjectsHarness.INSTANCE;

	private MockRunningAppProvider mockAppProvider;

	@Before
	public void setup() throws Exception {
		mockAppProvider = new MockRunningAppProvider();
		harness = BootJavaLanguageServerHarness.builder()
				.mockDefaults()
				.runningAppProvider(mockAppProvider.provider)
				.watchDogInterval(Duration.ofMillis(100))
				.build();

		MavenJavaProject jp =  projects.mavenProject("empty-boot-15-web-app", EXTRA_TYPES);
		assertTrue(jp.findType("com.example.Foo").exists());
		harness.useProject(jp);
		harness.intialize(null);
	}

	@Test
	public void componentWithNoInjections() throws Exception {
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
				"**Injected `fooImplementation` &rarr; _not injected anywhere_**  \n" +
				"Process [PID=111, name=`the-app`]"
		);
	}

	@Test
	public void componentWithOneInjection() throws Exception {
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
				"**Injected `fooImplementation` &rarr; `myController`**\n" +
				"- Bean: `myController`  \n" +
				"  Type: `com.example.MyController`\n" +
				"  \n" +
				"Process [PID=111, name=`the-app`]\n"
		);
	}

	@Test
	public void componentWithMultipleInjections() throws Exception {
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
				"**Injected `fooImplementation` &rarr; `myController` `otherBean`**\n" +
				"- Bean: `myController`  \n" +
				"  Type: `com.example.MyController`\n" +
				"- Bean: `otherBean`  \n" +
				"  Type: `com.example.OtherBean`\n" +
				"  \n" +
				"Process [PID=111, name=`the-app`]"
		);
	}

	@Test
	public void componentWithMultipleInjectionsAndMultipleProcesses() throws Exception {
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
				"**Injected `fooImplementation` &rarr; `myController` `otherBean`**\n" +
				"- Bean: `myController`  \n" +
				"  Type: `com.example.MyController`\n" +
				"- Bean: `otherBean`  \n" +
				"  Type: `com.example.OtherBean`\n" +
				"  \n" +
				"Process [PID=1001, name=`app-instance-1`]" +
				"  \n  \n" +
				"**Injected `fooImplementation` &rarr; `myController` `otherBean`**\n" +
				"- Bean: `myController`  \n" +
				"  Type: `com.example.MyController`\n" +
				"- Bean: `otherBean`  \n" +
				"  Type: `com.example.OtherBean`\n" +
				"  \n" +
 				"Process [PID=1002, name=`app-instance-2`]"
		);
	}

	@Test
	public void onlyShowInfoForRelevantBeanId() throws Exception {
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
				"**Injected `fooImplementation` &rarr; `myController`**\n" +
				"- Bean: `myController`  \n" +
				"  Type: `com.example.MyController`\n" +
				"  \n" +
				"Process [PID=111, name=`the-app`]"
		);
	}

	@Test
	public void explicitComponentId() throws Exception {
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
				"**Injected `alternateFooImplementation` &rarr; `otherBean`**\n" +
				"- Bean: `otherBean`  \n" +
				"  Type: `com.example.OtherBean`\n" +
				"  \n" +
				"Process [PID=111, name=`the-app`]"
		);
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
		editor.assertHighlights(/*NONE*/);
		editor.assertNoHover("@Component");
	}

	@Test
	public void noHoversWhenNoRunningApps() throws Exception {
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
		editor.assertHighlights(/*NONE*/);
		editor.assertNoHover("@Component");
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
						.fileResource(harness.getOutputFolder() + "/com/example/DependencyA.class")
						.build()
				)
				.add(LiveBean.builder()
						.id("dependencyB")
						.type("com.example.DependencyB")
						.classpathResource("com/example/DependencyB.class")
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
				"public class AutowiredClass {\n" +
				"\n" +
				"	public AutowiredClass(DependencyA depA, DependencyB depB) {\n" +
				"	}\n" +
				"}\n"
		);
		editor.assertHighlights("@Component", "AutowiredClass");
		editor.assertTrimmedHover("@Component",
				"**Injected `autowiredClass` &rarr; _not injected anywhere_**  \n" +
				"**Autowired `autowiredClass` &larr; `dependencyA` `dependencyB`**\n" +
				"- Bean: `dependencyA`  \n" +
				"  Type: `com.example.DependencyA`  \n" +
				"  Resource: `com/example/DependencyA.class`\n" +
				"- Bean: `dependencyB`  \n" +
				"  Type: `com.example.DependencyB`  \n" +
				"  Resource: `com/example/DependencyB.class`\n" +
				"  \n" +
				"Process [PID=111, name=`the-app`]\n"
		);
	}

	@Test
	public void componentWithAutowiredConstructorNoAdditionalHovers() throws Exception {
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
		editor.assertTrimmedHover("@Component",
				"**Injected `autowiredClass` &rarr; _not injected anywhere_**  \n" +
				"**Autowired `autowiredClass` &larr; `dependencyA` `dependencyB`**\n" +
				"- Bean: `dependencyA`  \n" +
				"  Type: `com.example.DependencyA`\n" +
				"- Bean: `dependencyB`  \n" +
				"  Type: `com.example.DependencyB`\n" +
				"  \n" +
				"Process [PID=111, name=`the-app`]\n"
		);
	}

	@Test
	public void bug_153072942_SpringBootApplication__withCGLib_is_a_Component() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("demoApplication")
						.type("com.example.DemoApplication$$EnhancerBySpringCGLIB$$f378241f")
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
				"import org.springframework.boot.SpringApplication;\n" +
				"import org.springframework.boot.autoconfigure.SpringBootApplication;\n" +
				"import org.springframework.context.annotation.Bean;\n" +
				"import org.springframework.web.client.RestTemplate;\n" +
				"\n" +
				"@SpringBootApplication\n" +
				"public class DemoApplication {\n" +
				"	\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		SpringApplication.run(DemoApplication.class, args);\n" +
				"	}\n" +
				"}\n"
		);
		editor.assertHighlights("@SpringBootApplication");
		editor.assertHoverContains("@SpringBootApplication",
				"**Injected `demoApplication` &rarr; _not injected anywhere_**  \n" +
				"Process [PID=111, name=`the-app`]"
		);
	}

	@Test
	public void componentFromInnerClass() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("com.example.DemoApplication$InnerClass")
						.type("com.example.DemoApplication$InnerClass")
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
				"import org.springframework.boot.SpringApplication;\n" +
				"import org.springframework.boot.autoconfigure.SpringBootApplication;\n" +
				"import org.springframework.context.annotation.Bean;\n" +
				"import org.springframework.web.client.RestTemplate;\n" +
				"\n" +
				"public class DemoApplication {\n" +
				"	\n" +
				"	@SpringBootApplication\n" +
				"	public static class InnerClass {\n" +
				"	\n" +
				"		public static void main(String[] args) {\n" +
				"			SpringApplication.run(DemoApplication.InnerClass.class, args);\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
		);
		editor.assertHighlights("@SpringBootApplication");
		editor.assertHoverContains("@SpringBootApplication",
				"**Injected `com.example.DemoApplication$InnerClass` &rarr; _not injected anywhere_**  \n" +
				"Process [PID=111, name=`the-app`]"
		);
	}

	@Test
	public void componentFromInnerInnerClass() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("com.example.DemoApplication$InnerClass$InnerInnerClass")
						.type("com.example.DemoApplication$InnerClass$InnerInnerClass")
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
				"import org.springframework.boot.SpringApplication;\n" +
				"import org.springframework.boot.autoconfigure.SpringBootApplication;\n" +
				"import org.springframework.context.annotation.Bean;\n" +
				"import org.springframework.web.client.RestTemplate;\n" +
				"\n" +
				"public class DemoApplication {\n" +
				"	\n" +
				"	public static class InnerClass {\n" +
				"		\n" +
				"		@SpringBootApplication\n" +
				"		public static class InnerInnerClass {\n" +
				"		\n" +
				"			public static void main(String[] args) {\n" +
				"				SpringApplication.run(DemoApplication.InnerClass.InnerInnerClass.class, args);\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
		);
		editor.assertHighlights("@SpringBootApplication");
		editor.assertHoverContains("@SpringBootApplication",
				"**Injected `com.example.DemoApplication$InnerClass$InnerInnerClass` &rarr; _not injected anywhere_**  \n" +
				"Process [PID=111, name=`the-app`]"
		);
	}
}
