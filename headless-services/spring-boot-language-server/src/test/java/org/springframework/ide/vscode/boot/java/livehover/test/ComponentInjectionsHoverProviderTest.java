/*******************************************************************************
 * Copyright (c) 2017, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.livehover.test;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.boot.bootiful.HoverTestConf;
import org.springframework.ide.vscode.boot.java.livehover.v2.LiveBean;
import org.springframework.ide.vscode.boot.java.livehover.v2.LiveBeansModel;
import org.springframework.ide.vscode.boot.java.livehover.v2.SpringProcessLiveData;
import org.springframework.ide.vscode.boot.java.livehover.v2.SpringProcessLiveDataProvider;
import org.springframework.ide.vscode.commons.maven.java.MavenJavaProject;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness.CustomizableProjectContent;
import org.springframework.ide.vscode.project.harness.ProjectsHarness.ProjectCustomizer;
import org.springframework.ide.vscode.project.harness.SpringProcessLiveDataBuilder;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@BootLanguageServerTest
@Import(HoverTestConf.class)
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

	private ProjectsHarness projects = ProjectsHarness.INSTANCE;
	@Autowired private BootLanguageServerHarness harness;
	@Autowired private SpringProcessLiveDataProvider liveDataProvider;

	@Before
	public void setup() throws Exception {
		MavenJavaProject jp =  projects.mavenProject("empty-boot-15-web-app", EXTRA_TYPES);
		assertTrue(jp.getIndex().findType("com.example.Foo").exists());
		harness.useProject(jp);
		harness.intialize(null);
	}
	
	@After
	public void tearDown() throws Exception {
		liveDataProvider.remove("processkey");
		liveDataProvider.remove("processkey1");
		liveDataProvider.remove("processkey2");
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

		
		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
			.processID("111")
			.processName("the-app")
			.beans(beans)
			.build();
		liveDataProvider.add("processkey", liveData);

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
				"Bean id: `fooImplementation`  \n" +
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
		
		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
			.processID("111")
			.processName("the-app")
			.beans(beans)
			.build();
		liveDataProvider.add("processkey", liveData);

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
				"**&#8594; `MyController`**\n" +
				"- Bean: `myController`  \n" +
				"  Type: `com.example.MyController`\n" +
				"  \n" +
				"Bean id: `fooImplementation`  \n" +
				"Process [PID=111, name=`the-app`]\n"
		);
	}

	@Test
	public void componentWithOneCGILibInjection() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("fooImplementation")
						.type("com.example.FooImplementation$$EnhancerBySpringCGLIB$$Blah")
						.build()
				)
				.add(LiveBean.builder()
						.id("myController")
						.type("com.example.MyController$$EnhancerBySpringCGLIB$$Blah")
						.dependencies("fooImplementation")
						.build()
				)
				.add(LiveBean.builder()
						.id("irrelevantBean")
						.type("com.example.IrrelevantBean$$EnhancerBySpringCGLIB$$Blah")
						.dependencies("myController")
						.build()
				)
				.build();
		
		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
			.processID("111")
			.processName("the-app")
			.beans(beans)
			.build();
		liveDataProvider.add("processkey", liveData);

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
				"**&#8594; `MyController`**\n" +
				"- Bean: `myController`  \n" +
				"  Type: `com.example.MyController`\n" +
				"  \n" +
				"Bean id: `fooImplementation`  \n" +
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

		
		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
			.processID("111")
			.processName("the-app")
			.beans(beans)
			.build();
		liveDataProvider.add("processkey", liveData);

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
				"**&#8594; `MyController` `OtherBean`**\n" +
				"- Bean: `myController`  \n" +
				"  Type: `com.example.MyController`\n" +
				"- Bean: `otherBean`  \n" +
				"  Type: `com.example.OtherBean`\n" +
				"  \n" +
				"Bean id: `fooImplementation`  \n" +
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
			SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
						.processID("100"+i)
						.processName("app-instance-"+i)
						.beans(beans)
						.build();
			liveDataProvider.add("processkey" + i, liveData);
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
				"**&#8594; `MyController` `OtherBean`**\n" +
				"- Bean: `myController`  \n" +
				"  Type: `com.example.MyController`\n" +
				"- Bean: `otherBean`  \n" +
				"  Type: `com.example.OtherBean`\n" +
				"  \n" +
				"Bean id: `fooImplementation`  \n" +
				"Process [PID=1001, name=`app-instance-1`]" +
				"  \n  \n" +
				"**&#8594; `MyController` `OtherBean`**\n" +
				"- Bean: `myController`  \n" +
				"  Type: `com.example.MyController`\n" +
				"- Bean: `otherBean`  \n" +
				"  Type: `com.example.OtherBean`\n" +
				"  \n" +
				"Bean id: `fooImplementation`  \n" +
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

		
		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
			.processID("111")
			.processName("the-app")
			.beans(beans)
			.build();
		liveDataProvider.add("processkey", liveData);

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
				"**&#8594; `MyController`**\n" +
				"- Bean: `myController`  \n" +
				"  Type: `com.example.MyController`\n" +
				"  \n" +
				"Bean id: `fooImplementation`  \n" +
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
		
		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
			.processID("111")
			.processName("the-app")
			.beans(beans)
			.build();
		liveDataProvider.add("processkey", liveData);

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
				"**&#8594; `OtherBean`**\n" +
				"- Bean: `otherBean`  \n" +
				"  Type: `com.example.OtherBean`\n" +
				"  \n" +
				"Bean id: `alternateFooImplementation`  \n" +
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

		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
			.processID("111")
			.processName("unrelated-app")
			.beans(beans)
			.build();
		liveDataProvider.add("processkey", liveData);

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
		
		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
			.processID("111")
			.processName("the-app")
			.beans(beans)
			.build();
		liveDataProvider.add("processkey", liveData);

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
		editor.assertHighlights("@Component", "AutowiredClass", "depA", "depB");
		editor.assertTrimmedHover("@Component",
				"**&#8592; `DependencyA` `DependencyB`**\n" +
				"- Bean: `dependencyA`  \n" +
				"  Type: `com.example.DependencyA`  \n" +
				"  Resource: `com/example/DependencyA.class`\n" +
				"- Bean: `dependencyB`  \n" +
				"  Type: `com.example.DependencyB`  \n" +
				"  Resource: `com/example/DependencyB.class`\n" +
				"  \n" +
				"Bean id: `autowiredClass`  \n" +
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
		
		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
			.processID("111")
			.processName("the-app")
			.beans(beans)
			.build();
		liveDataProvider.add("processkey", liveData);

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
				"**&#8592; `DependencyA` `DependencyB`**\n" +
				"- Bean: `dependencyA`  \n" +
				"  Type: `com.example.DependencyA`\n" +
				"- Bean: `dependencyB`  \n" +
				"  Type: `com.example.DependencyB`\n" +
				"  \n" +
				"Bean id: `autowiredClass`  \n" +
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
		
		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
			.processID("111")
			.processName("the-app")
			.beans(beans)
			.build();
		liveDataProvider.add("processkey", liveData);

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
				"Bean id: `demoApplication`  \n" +
				"Process [PID=111, name=`the-app`]"
		);
	}

	@Test
	public void componentFromStaticInnerClass() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("com.example.DemoApplication$InnerClass")
						.type("com.example.DemoApplication$InnerClass")
						.build()
				)
				.add(LiveBean.builder()
						.id("demoApplication.InnerClass")
						.type("com.example.DemoApplication$InnerClass")
						.build()
				)
				.build();
		
		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
			.processID("111")
			.processName("the-app")
			.beans(beans)
			.build();
		liveDataProvider.add("processkey", liveData);

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
				"Bean id: `demoApplication.InnerClass`  \n" +
				"Process [PID=111, name=`the-app`]"
		);
	}

	@Test
	public void componentFromNestedClass() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("com.example.Example$Inner")
						.type("com.example.Example$Inner")
						.build()
				)
				.add(LiveBean.builder()
						.id("example.Inner")
						.type("com.example.Example$Inner")
						.build()
				)
				.add(LiveBean.builder()
						.id("example")
						.type("com.example.Example")
						.build()
				)
				.build();
		
		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
			.processID("111")
			.processName("the-app")
			.beans(beans)
			.build();
		liveDataProvider.add("processkey", liveData);

		Editor editor = harness.newEditor(LanguageId.JAVA,
				"package com.example;\n" +
				"\n" +
				"import org.springframework.beans.factory.annotation.Autowired;\n" +
				"import org.springframework.stereotype.Component;\n" +
				"\n" +
				"@Component\n" +
				"public class Example {	\n" +
				"	@Component\n" +
				"	public class Inner {\n" +
				"		@Autowired\n" +
				"		public Inner() {\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
		);
		editor.assertHighlights("@Component", "@Component");
		editor.assertHoverContains("@Component", 1,
				"Bean id: `example`  \n" +
				"Process [PID=111, name=`the-app`]"
		);
		editor.assertHoverContains("@Component", 2,
				"Bean id: `com.example.Example$Inner`  \n" +
				"Process [PID=111, name=`the-app`]"
		);
	}

	@Test
	public void componentFromStaticInnerInnerClass() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("com.example.DemoApplication$InnerClass$InnerInnerClass")
						.type("com.example.DemoApplication$InnerClass$InnerInnerClass")
						.build()
				)
				.add(LiveBean.builder()
						.id("demoApplication.InnerClass.InnerInnerClass")
						.type("com.example.DemoApplication$InnerClass$InnerInnerClass")
						.build()
				)
				.build();
		
		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
			.processID("111")
			.processName("the-app")
			.beans(beans)
			.build();
		liveDataProvider.add("processkey", liveData);

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
				"Bean id: `demoApplication.InnerClass.InnerInnerClass`  \n" +
				"Process [PID=111, name=`the-app`]"
		);
	}
}
