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

import java.nio.file.Path;
import java.nio.file.Paths;

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
public class BeanInjectedIntoHoverProviderTest {

	private static final ProjectCustomizer FOO_INTERFACE = (CustomizableProjectContent p) -> {
		p.createType("hello.Foo",
				"package hello;\n" +
				"\n" +
				"public interface Foo {\n" +
				"	void doSomeFoo();\n" +
				"}\n"
		);

		p.createType("hello.IDependency",
				"package hello;\n" +
				"\n" +
				"public interface IDependency {\n" +
				"}\n"
		);

		p.createType("hello.DependencyA",
				"package hello;\n" +
				"\n" +
				"public class DependencyA implements IDependency {\n" +
				"}\n"
		);

		p.createType("hello.DependencyB",
				"package hello;\n" +
				"\n" +
				"public class DependencyB implements IDependency {\n" +
				"}\n"
		);

	};

	private ProjectsHarness projects = ProjectsHarness.INSTANCE;

	@Autowired private BootLanguageServerHarness harness;
	@Autowired private SpringProcessLiveDataProvider liveDataProvider;

	@Before
	public void setup() throws Exception {
		MavenJavaProject jp =  projects.mavenProject("empty-boot-15-web-app", FOO_INTERFACE);
		assertTrue(jp.getIndex().findType("hello.Foo").exists());
		harness.useProject(jp);
		harness.intialize(null);
	}
	
	@After
	public void tearDown() throws Exception {
		liveDataProvider.remove("processkey");
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
		
		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
			.processID("111")
			.processName("the-app")
			.beans(beans)
			.build();
		liveDataProvider.add("processkey", liveData);

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
				"Bean id: `myFoo`  \n" +
				"Process [PID=111, name=`the-app`]"
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

		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
			.processID("111")
			.processName("the-app")
			.beans(beans)
			.build();
		liveDataProvider.add("processkey", liveData);

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
					"	"+beanAnnotation+"\n" +
					"	Foo otherFoo() {\n" +
					"		return new FooImplementation();\n" +
					"	}\n" +
					"}"
			);
			editor.assertHighlights("@Bean");
			editor.assertTrimmedHover("@Bean",
					"Bean id: `beanId`  \n" +
					"Process [PID=111, name=`the-app`]"
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

		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
			.processID("111")
			.processName("the-app")
			.beans(beans)
			.build();
		liveDataProvider.add("processkey", liveData);

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
				"**&#8594; `MyController`**\n" +
				"- Bean: `myController`  \n" +
				"  Type: `hello.MyController`\n" +
				"  \n" +
				"Bean id: `fooImplementation`  \n" +
				"Process [PID=111, name=`the-app`]"
		);
	}

	@Test
	public void beanWithOneInjectionAndWiring() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("fooImplementation")
						.type("hello.FooImplementation")
						.dependencies("message")
						.build()
				)
				.add(LiveBean.builder()
						.id("myController")
						.type("hello.MyController")
						.dependencies("fooImplementation")
						.build()
				)
				.add(LiveBean.builder()
						.id("message")
						.type("java.lang.String")
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
				"	Foo someFoo(String msg) {\n" +
				"		return new FooImplementation();\n" +
				"	}\n" +
				"}"
		);
		// !!! 2 highlights over @Bean. 1 for injected beans CodeLens, 1 for wired beans CodeLens
		editor.assertHighlights("@Bean", "@Bean", "msg");
		editor.assertTrimmedHover("@Bean",
				"**&#8594; `MyController`**\n" +
				"- Bean: `myController`  \n" +
				"  Type: `hello.MyController`\n" +
				"  \n" +
				"**&#8592; `String`**\n" +
				"- Bean: `message`  \n" +
				"  Type: `java.lang.String`\n" +
				"  \n" +
				"Bean id: `fooImplementation`  \n" +
				"Process [PID=111, name=`the-app`]"
		);
		editor.assertTrimmedHover("msg",
				"**&#8592; `String`**\n" +
				"- Bean: `message`  \n" +
				"  Type: `java.lang.String`\n" +
				"  \n" +
				"Bean id: `fooImplementation`  \n" +
				"Process [PID=111, name=`the-app`]"
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

		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
			.processID("111")
			.processName("the-app")
			.beans(beans)
			.build();
		liveDataProvider.add("processkey", liveData);

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
				"**&#8594; `MyController`**\n" +
				"- Bean: `myController`  \n" +
				"  Type: `hello.MyController`\n" +
				"  \n" +
				"Bean id: `fooImplementation`  \n" +
				"Process [PID=111, name=`the-app`]"
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
		
		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
			.processID("111")
			.processName("the-app")
			.beans(beans)
			.build();
		liveDataProvider.add("processkey", liveData);

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
				"**&#8594; `MyController`**\n" +
				"- Bean: `myController`  \n" +
				"  Type: `hello.MyController`  \n" +
				"  Resource: `" + Paths.get("hello/MyController.class") + "`\n" +
				"  \n" +
				"Bean id: `fooImplementation`  \n" +
				"Process [PID=111, name=`the-app`]"
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

		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
			.processID("111")
			.processName("the-app")
			.beans(beans)
			.build();
		liveDataProvider.add("processkey", liveData);

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
				"**&#8594; `MyController`**\n" +
				"- Bean: `myController`  \n" +
				"  Type: `hello.MyController`  \n" +
				"  Resource: `hello/MyController.class`\n" +
				"  \n" +
				"Bean id: `fooImplementation`  \n" +
				"Process [PID=111, name=`the-app`]"
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
		
		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
			.processID("111")
			.processName("the-app")
			.beans(beans)
			.build();
		liveDataProvider.add(("processkey"), liveData);

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
				"**&#8594; `MyController` `OtherBean`**\n" +
				"- Bean: `myController`  \n" +
				"  Type: `hello.MyController`\n" +
				"- Bean: `otherBean`  \n" +
				"  Type: `hello.OtherBean`\n" +
				"  \n" +
				"Bean id: `fooImplementation`  \n" +
				"Process [PID=111, name=`the-app`]"
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

		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
			.processID("111")
			.processName("unrelated-app")
			.beans(beans)
			.build();
		liveDataProvider.add("processkey", liveData);

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

	@Test
	public void beanWithOneWiring() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("fooImplementation")
						.type("hello.FooImplementation")
						.dependencies("depA")
						.build()
				)
				.add(LiveBean.builder()
						.id("depA")
						.type("hello.DependencyA")
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
				"	Foo someFoo(DependencyA depA) {\n" +
				"		return new FooImplementation();\n" +
				"	}\n" +
				"}"
		);
		editor.assertHighlights("@Bean", "depA");
		editor.assertTrimmedHover("@Bean",
				"**&#8592; `DependencyA`**\n" +
				"- Bean: `depA`  \n" +
				"  Type: `hello.DependencyA`\n" +
				"  \n" +
				"Bean id: `fooImplementation`  \n" +
				"Process [PID=111, name=`the-app`]"
		);

		editor.assertTrimmedHover("depA",
				"**&#8592; `DependencyA`**\n" +
				"- Bean: `depA`  \n" +
				"  Type: `hello.DependencyA`\n" +
				"  \n" +
				"Bean id: `fooImplementation`  \n" +
				"Process [PID=111, name=`the-app`]"
		);

	}

	@Test
	public void beanWithMultipleWirings() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("fooImplementation")
						.type("hello.FooImplementation")
						.dependencies("depA", "depB")
						.build()
				)
				.add(LiveBean.builder()
						.id("depA")
						.type("hello.DependencyA")
						.build()
				)
				.add(LiveBean.builder()
						.id("depB")
						.type("hello.DependencyB")
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
				"	Foo someFoo(DependencyA depA, DependencyB depB) {\n" +
				"		return new FooImplementation();\n" +
				"	}\n" +
				"}"
		);
		editor.assertHighlights("@Bean", "depA", "depB");
		editor.assertTrimmedHover("@Bean",
				"**&#8592; `DependencyA` `DependencyB`**\n" +
				"- Bean: `depA`  \n" +
				"  Type: `hello.DependencyA`\n" +
				"- Bean: `depB`  \n" +
				"  Type: `hello.DependencyB`\n" +
				"  \n" +
				"Bean id: `fooImplementation`  \n" +
				"Process [PID=111, name=`the-app`]"
		);

		editor.assertTrimmedHover("depA",
				"**&#8592; `DependencyA`**\n" +
				"- Bean: `depA`  \n" +
				"  Type: `hello.DependencyA`\n" +
				"  \n" +
				"Bean id: `fooImplementation`  \n" +
				"Process [PID=111, name=`the-app`]"
		);

		editor.assertTrimmedHover("depB",
				"**&#8592; `DependencyB`**\n" +
				"- Bean: `depB`  \n" +
				"  Type: `hello.DependencyB`\n" +
				"  \n" +
				"Bean id: `fooImplementation`  \n" +
				"Process [PID=111, name=`the-app`]"
		);

	}

	@Test
	public void beanWithCollectionWiring() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("fooImplementation")
						.type("hello.FooImplementation")
						.dependencies("depA", "depB")
						.build()
				)
				.add(LiveBean.builder()
						.id("depA")
						.type("hello.DependencyA")
						.build()
				)
				.add(LiveBean.builder()
						.id("depB")
						.type("hello.DependencyB")
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
				"	Foo someFoo(IDependency[] deps) {\n" +
				"		return new FooImplementation();\n" +
				"	}\n" +
				"}"
		);
		editor.assertHighlights("@Bean", "deps");
		editor.assertTrimmedHover("@Bean",
				"**&#8592; `DependencyA` `DependencyB`**\n" +
				"- Bean: `depA`  \n" +
				"  Type: `hello.DependencyA`\n" +
				"- Bean: `depB`  \n" +
				"  Type: `hello.DependencyB`\n" +
				"  \n" +
				"Bean id: `fooImplementation`  \n" +
				"Process [PID=111, name=`the-app`]"
		);

		editor.assertTrimmedHover("deps",
				"**&#8592; `DependencyA` `DependencyB`**\n" +
				"- Bean: `depA`  \n" +
				"  Type: `hello.DependencyA`\n" +
				"- Bean: `depB`  \n" +
				"  Type: `hello.DependencyB`\n" +
				"  \n" +
				"Bean id: `fooImplementation`  \n" +
				"Process [PID=111, name=`the-app`]"
		);
}

	@Test
	public void beanWithQualifierWiring() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("fooImplementation")
						.type("hello.FooImplementation")
						.dependencies("depA", "depB")
						.build()
				)
				.add(LiveBean.builder()
						.id("depB")
						.type("hello.DependencyB")
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
				"	Foo someFoo(@Qualifier(\"depB\") IDependency[] deps) {\n" +
				"		return new FooImplementation();\n" +
				"	}\n" +
				"}"
		);
		editor.assertHighlights("@Bean", "deps");
		editor.assertTrimmedHover("@Bean",
				"**&#8592; `DependencyB`**\n" +
				"- Bean: `depB`  \n" +
				"  Type: `hello.DependencyB`\n" +
				"  \n" +
				"Bean id: `fooImplementation`  \n" +
				"Process [PID=111, name=`the-app`]"
		);

		editor.assertTrimmedHover("deps",
				"**&#8592; `DependencyB`**\n" +
				"- Bean: `depB`  \n" +
				"  Type: `hello.DependencyB`\n" +
				"  \n" +
				"Bean id: `fooImplementation`  \n" +
				"Process [PID=111, name=`the-app`]"
		);

	}
}
