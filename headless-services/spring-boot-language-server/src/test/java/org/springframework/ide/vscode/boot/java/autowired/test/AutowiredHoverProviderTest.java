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
package org.springframework.ide.vscode.boot.java.autowired.test;

import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

/**
 * @author Martin Lippert
 */
@RunWith(SpringRunner.class)
@BootLanguageServerTest
@Import(HoverTestConf.class)
public class AutowiredHoverProviderTest {

	private static final String FOO_IMPL_CONTENTS = "package com.example;\n" +
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
	"}";

	private static final ProjectCustomizer FOO_INTERFACE = (CustomizableProjectContent p) -> {
		p.createType("com.examle.Foo",
				"package com.example;\n" +
				"\n" +
				"public interface Foo {\n" +
				"	void doSomeFoo();\n" +
				"}\n"
		);

		p.createType("com.examle.IDependency",
				"package com.example;\n" +
				"\n" +
				"public interface IDependency {\n" +
				"}\n"
		);

		p.createType("com.examle.DependencyA",
				"package com.example;\n" +
				"\n" +
				"public class DependencyA implements IDependency {\n" +
				"}\n"
		);

		p.createType("com.examle.DependencyB",
				"package com.example;\n" +
				"\n" +
				"public class DependencyB implements IDependency {\n" +
				"}\n"
		);

		p.createType("com.example.RuntimeBeanFactory",
			Stream.of(
				"package com.example;",
				"public interface RuntimeBeanFactory {",
				"void createRuntimeBean(String info);",
				"}"
			).collect(Collectors.joining("\n"))
		);

		p.createType("com.example.SomeComponent",
				Stream.of("package com.example;",
					"",
					"import org.springframework.context.annotation.Bean;",
					"",
//					"@Component",
					"public class SomeComponent {",
					"",
					"@Bean",
					"public RuntimeBeanFactory getBeanFactory() {",
					"\treturn new RuntimeBeanFactory() {",
					"\t\tpublic void createRuntimeBean(String info){}",
					"\t};",
					"}",
					"",
					"}"
				).collect(Collectors.joining("\n"))
		);

		p.createType("com.example.FooImplementation", FOO_IMPL_CONTENTS);
	};

	@Autowired private BootLanguageServerHarness harness;
	@Autowired private SpringProcessLiveDataProvider liveDataProvider;
	
	private ProjectsHarness projects = ProjectsHarness.INSTANCE;

	@Before
	public void setup() throws Exception {
		MavenJavaProject jp =  projects.mavenProject("empty-boot-15-web-app", FOO_INTERFACE);
		assertTrue(jp.getIndex().findType("com.example.Foo").exists());
		harness.useProject(jp);
		harness.intialize(null);
	}
	
	@After
	public void tearDown() throws Exception {
		liveDataProvider.remove("processkey");
	}

	@Test
	public void javaxInjectAnnotationHover() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("autowiredClass")
						.type("com.example.AutowiredClass")
						.dependencies("dependencyA")
						.build()
				)
				.add(LiveBean.builder()
						.id("dependencyA")
						.type("com.example.DependencyA")
						.fileResource(harness.getOutputFolder() + Paths.get("/com/example/DependencyA.class").toString())
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
				"import javax.inject.Inject;\n" +
				"import org.springframework.stereotype.Component;\n" +
				"\n" +
				"@Component\n" +
				"public class AutowiredClass {\n" +
				"\n" +
				"   @Inject\n" +
				"	private DependencyA depA;\n" +
				"}\n"
		);

		editor.assertHighlights("@Component", "@Inject");
		editor.assertTrimmedHover("@Inject",
				"**&#8592; `DependencyA`**\n" +
				"- Bean: `dependencyA`  \n" +
				"  Type: `com.example.DependencyA`  \n" +
				"  Resource: `" + Paths.get("com/example/DependencyA.class") + "`\n" +
				"  \n" +
				"Bean id: `autowiredClass`  \n" +
				"Process [PID=111, name=`the-app`]"
		);
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
						.fileResource(harness.getOutputFolder() + Paths.get("/com/example/DependencyA.class").toString())
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
				"**&#8592; `DependencyA` `DependencyB`**\n" +
				"- Bean: `dependencyA`  \n" +
				"  Type: `com.example.DependencyA`  \n" +
				"  Resource: `" + Paths.get("com/example/DependencyA.class") + "`\n" +
				"- Bean: `dependencyB`  \n" +
				"  Type: `com.example.DependencyB`  \n" +
				"  Resource: `com/example/DependencyB.class`\n" +
				"  \n" +
				"Bean id: `autowiredClass`  \n" +
				"Process [PID=111, name=`the-app`]\n"

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

		editor.assertHighlights(/*NONE*/);
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

		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
				.processID("111")
				.processName("unrelated-app")
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

		editor.assertHighlights("@Component");
		editor.assertNoHover("@Autowired");
	}

	@Test public void bug_152553935() throws Exception {
		//https://www.pivotaltracker.com/story/show/152553935
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("defaultFoo")
						.type("com.example.FooImplementation")
						.dependencies("superBean", "scheduler")
						.build()
				)
				.add(LiveBean.builder()
						.id("superBean")
						.type("com.example.FooImplementation")
						.build()
				)
				.add(LiveBean.builder()
						.id("scheduler")
						.type("org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler")
						.build()
				)
				.build();

		SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
				.processID("111")
				.processName("the-app")
				.beans(beans)
				.build();
		liveDataProvider.add("processkey", liveData);
		

		Editor editor = harness.newEditor(LanguageId.JAVA, FOO_IMPL_CONTENTS);
		editor.assertHighlights("@Component", "@Autowired", "@Autowired");
		editor.assertHoverContains("@Autowired", 1,
				"**&#8592; `FooImplementation`**\n" +
				"- Bean: `superBean`  \n" +
				"  Type: `com.example.FooImplementation`");
		editor.assertHoverContains("@Autowired", 2,
				"**&#8592; `ThreadPoolTaskScheduler`**\n" +
				"- Bean: `scheduler`  \n" +
				"  Type: `org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler`");
	}

	@Test public void bug_152621242_autowired_constructor_on_a_controller() throws Exception {
		//https://www.pivotaltracker.com/story/show/152621242
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("myController")
						.type("com.example.MyController")
						.dependencies("restTemplate")
						.build()
				)
				.add(LiveBean.builder()
						.id("restTemplate")
						.type("org.springframework.web.client.RestTemplate")
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
				"import org.springframework.stereotype.Controller;\n" +
				"import org.springframework.web.client.RestTemplate;\n" +
				"\n" +
				"@Controller\n" +
				"public class MyController {\n" +
				"\n" +
				"	private RestTemplate restClient;\n" +
				"\n" +
				"	@Autowired\n" +
				"	public MyController(RestTemplate restClient) {\n" +
				"		this.restClient = restClient;\n" +
				"	}\n" +
				"	\n" +
				"}"
		);
		editor.assertHighlights("@Controller", "@Autowired");
		editor.assertHoverContains("@Autowired",
				"**&#8592; `RestTemplate`**\n" +
				"- Bean: `restTemplate`  \n" +
				"  Type: `org.springframework.web.client.RestTemplate`"
		);
		editor.assertTrimmedHover("@Controller",
				"**&#8592; `RestTemplate`**\n" +
				"- Bean: `restTemplate`  \n" +
				"  Type: `org.springframework.web.client.RestTemplate`\n" +
				"  \n" +
				"Bean id: `myController`  \n" +
				"Process [PID=111, name=`the-app`]\n"
		);
	}

	@Test
	public void implicitAutowiringSingleConstructor() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("someComponent")
						.type("com.example.SomeComponent")
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
				"import org.springframework.stereotype.Component;\n" +
				"\n" +
				"@Component\n" +
				"public class SomeComponent {\n" +
				"\n" +
				"   private DepedencyA depA;\n" +
				"   private DepedencyB depB;\n" +
				"\n" +
				"	public SomeComponent(DependencyA depA, DependencyB depB) {\n" +
				"		this.depA = depA;\n" +
				"		this.depB = depB;\n" +
				"	}\n" +
				"}\n"
		);

		editor.assertHighlights("@Component", "SomeComponent", "depA", "depB");

		editor.assertTrimmedHover("SomeComponent", 2,
				"**&#8592; `DependencyA` `DependencyB`**\n" +
				"- Bean: `dependencyA`  \n" +
				"  Type: `com.example.DependencyA`\n" +
				"- Bean: `dependencyB`  \n" +
				"  Type: `com.example.DependencyB`\n" +
				"  \n" +
				"Bean id: `someComponent`  \n" +
				"Process [PID=111, name=`the-app`]\n"
		);

		editor.assertTrimmedHover("depA", 2,
				"**&#8592; `DependencyA`**\n" +
				"- Bean: `dependencyA`  \n" +
				"  Type: `com.example.DependencyA`\n" +
				"  \n" +
				"Bean id: `someComponent`  \n" +
				"Process [PID=111, name=`the-app`]\n"
		);

		editor.assertTrimmedHover("depB", 2,
				"**&#8592; `DependencyB`**\n" +
				"- Bean: `dependencyB`  \n" +
				"  Type: `com.example.DependencyB`\n" +
				"  \n" +
				"Bean id: `someComponent`  \n" +
				"Process [PID=111, name=`the-app`]\n"
		);

	}

	@Test
	public void noImplicitAutowiringForConstructorFromNonBean() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("someOtherComponent")
						.type("com.example.SomeOtherComponent")
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
				"public class SomeComponent {\n" +
				"\n" +
				"   private DepedencyA depA;\n" +
				"   private DepedencyB depB;\n" +
				"\n" +
				"	public SomeComponent(DependencyA depA, DependencyB depB) {\n" +
				"		this.depA = depA;\n" +
				"		this.depB = depB;\n" +
				"	}\n" +
				"}\n"
		);

		editor.assertHighlights();

		for (int i = 1; i < 2; i++) {
			editor.assertNoHover("SomeComponent", i);
		}
	}

	@Test
	public void noImplicitAutowiringForMultipleConstructors() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("someComponent")
						.type("com.example.SomeComponent")
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
				"import org.springframework.stereotype.Component;\n" +
				"\n" +
				"@Component\n" +
				"public class SomeComponent {\n" +
				"\n" +
				"   private DepedencyA depA;\n" +
				"   private DepedencyB depB;\n" +
				"\n" +
				"	public SomeComponent() {\n" +
				"	}\n" +
				"\n" +
				"	public SomeComponent(DependencyA depA, DependencyB depB) {\n" +
				"		this.depA = depA;\n" +
				"		this.depB = depB;\n" +
				"	}\n" +
				"}\n"
		);

		editor.assertHighlights("@Component");
		for (int i = 1; i < 3; i++) {
			editor.assertNoHover("SomeComponent", i);
		}
	}

	@Test
	public void unableToMatchWiredBean() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("someComponent")
						.type("com.example.SomeComponent")
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
				"public class SomeComponent {\n" +
				"\n" +
				"   @Autowired\n" +
				"   private IDependency dep;\n" +
				"\n" +
				"	public SomeComponent() {\n" +
				"	}\n" +
				"\n" +
				"}\n"
		);

		editor.assertHighlights("@Component", "@Autowired");
		editor.assertTrimmedHover("@Autowired", 1,
				"**&#8592; UNKNOWN**\n" +
				"- (Cannot find precise information for the bean)\n" +
				"  \n" +
				"Bean id: `someComponent`  \n" +
				"Process [PID=111, name=`the-app`]\n"
		);
	}

	@Test
	public void qualifierParameterWiredBean() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("someComponent")
						.type("com.example.SomeComponent")
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
				"import org.springframework.beans.factory.annotation.Qualifier;\n" +
				"import org.springframework.stereotype.Component;\n" +
				"\n" +
				"@Component\n" +
				"public class SomeComponent {\n" +
				"\n" +
				"   private IDependency a;\n" +
				"   private IDependency b;\n" +
				"\n" +
				"	public SomeComponent(@Qualifier(\"dependencyA\") IDependency depA, @Qualifier(\"dependencyB\") IDependency depB) {\n" +
				"       this.a = a;\n" +
				"       this.b = b;\n" +
				"	}\n" +
				"\n" +
				"}\n"
		);

		editor.assertHighlights("@Component", "SomeComponent", "depA", "depB");
		editor.assertTrimmedHover("SomeComponent", 2,
				"**&#8592; `DependencyA` `DependencyB`**\n" +
				"- Bean: `dependencyA`  \n" +
				"  Type: `com.example.DependencyA`\n" +
				"- Bean: `dependencyB`  \n" +
				"  Type: `com.example.DependencyB`\n" +
				"  \n" +
				"Bean id: `someComponent`  \n" +
				"Process [PID=111, name=`the-app`]\n"
		);

		editor.assertTrimmedHover("depA",
				"**&#8592; `DependencyA`**\n" +
				"- Bean: `dependencyA`  \n" +
				"  Type: `com.example.DependencyA`\n" +
				"  \n" +
				"Bean id: `someComponent`  \n" +
				"Process [PID=111, name=`the-app`]\n"
		);

		editor.assertTrimmedHover("depB",
				"**&#8592; `DependencyB`**\n" +
				"- Bean: `dependencyB`  \n" +
				"  Type: `com.example.DependencyB`\n" +
				"  \n" +
				"Bean id: `someComponent`  \n" +
				"Process [PID=111, name=`the-app`]\n"
		);

	}

	@Test
	public void qualifierFieldWiredBean() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("someComponent")
						.type("com.example.SomeComponent")
						.dependencies("dependencyA")
						.build()
				)
				.add(LiveBean.builder()
						.id("dependencyA")
						.type("com.example.DependencyA")
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
				"import org.springframework.beans.factory.annotation.Qualifier;\n" +
				"import org.springframework.stereotype.Component;\n" +
				"\n" +
				"@Component\n" +
				"public class SomeComponent {\n" +
				"\n" +
				"   @Autowired\n" +
				"   @Qualifier(\"dependencyA\")\n" +
				"   private IDependency a;\n" +
				"\n" +
				"	public SomeComponent() {\n" +
				"	}\n" +
				"\n" +
				"}\n"
		);

		editor.assertHighlights("@Component", "@Autowired");
		editor.assertTrimmedHover("@Autowired", 1,
				"**&#8592; `DependencyA`**\n" +
				"- Bean: `dependencyA`  \n" +
				"  Type: `com.example.DependencyA`\n" +
				"  \n" +
				"Bean id: `someComponent`  \n" +
				"Process [PID=111, name=`the-app`]\n"
		);
	}

	@Test
	public void qualifierNotFoundWiredBean() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("someComponent")
						.type("com.example.SomeComponent")
						.dependencies("dependencyA")
						.build()
				)
				.add(LiveBean.builder()
						.id("dependencyA")
						.type("com.example.DependencyA")
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
				"import org.springframework.beans.factory.annotation.Qualifier;\n" +
				"import org.springframework.stereotype.Component;\n" +
				"\n" +
				"@Component\n" +
				"public class SomeComponent {\n" +
				"\n" +
				"   @Autowired\n" +
				"   @Qualifier(\"X\")\n" +
				"   private IDependency a;\n" +
				"\n" +
				"	public SomeComponent() {\n" +
				"	}\n" +
				"\n" +
				"}\n"
		);

		editor.assertHighlights("@Component");
		editor.assertNoHover("@Autowired");
	}

	@Test
	public void qualifierWrongTypeWiredBean() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("someComponent")
						.type("com.example.SomeComponent")
						.dependencies("dependency")
						.build()
				)
				.add(LiveBean.builder()
						.id("dependency")
						.type("com.example.DependencyA")
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
				"import org.springframework.beans.factory.annotation.Qualifier;\n" +
				"import org.springframework.stereotype.Component;\n" +
				"\n" +
				"@Component\n" +
				"public class SomeComponent {\n" +
				"\n" +
				"   @Autowired\n" +
				"   @Qualifier(\"dependency\")\n" +
				"   private DependencyB a;\n" +
				"\n" +
				"	public SomeComponent() {\n" +
				"	}\n" +
				"\n" +
				"}\n"
		);

		editor.assertHighlights("@Component");
		editor.assertNoHover("@Autowired");
	}

	@Test
	public void collectionWiredBean() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("someComponent")
						.type("com.example.SomeComponent")
						.dependencies("dependencyA")
						.build()
				)
				.add(LiveBean.builder()
						.id("dependencyA")
						.type("com.example.DependencyA")
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
				"import java.util.List;\n" +
				"import org.springframework.beans.factory.annotation.Autowired;\n" +
				"import org.springframework.stereotype.Component;\n" +
				"\n" +
				"@Component\n" +
				"public class SomeComponent {\n" +
				"\n" +
				"   @Autowired\n" +
				"   private List<DependencyA> a;\n" +
				"\n" +
				"	public SomeComponent() {\n" +
				"	}\n" +
				"\n" +
				"}\n"
		);

		editor.assertHighlights("@Component", "@Autowired");
		editor.assertTrimmedHover("@Autowired", 1,
				"**&#8592; `DependencyA`**\n" +
				"- Bean: `dependencyA`  \n" +
				"  Type: `com.example.DependencyA`\n" +
				"  \n" +
				"Bean id: `someComponent`  \n" +
				"Process [PID=111, name=`the-app`]\n"
		);
	}

	@Test
	public void collectionWithQualifierWiredBean() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("someComponent")
						.type("com.example.SomeComponent")
						.dependencies("dependencyA")
						.build()
				)
				.add(LiveBean.builder()
						.id("dependencyA")
						.type("com.example.DependencyA")
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
				"import java.util.List;\n" +
				"import org.springframework.beans.factory.annotation.Autowired;\n" +
				"import org.springframework.beans.factory.annotation.Qualifier;\n" +
				"import org.springframework.stereotype.Component;\n" +
				"\n" +
				"@Component\n" +
				"public class SomeComponent {\n" +
				"\n" +
				"   @Autowired\n" +
				"   @Qualifier(\"dependencyA\")\n" +
				"   private List<DependencyA> a;\n" +
				"\n" +
				"	public SomeComponent() {\n" +
				"	}\n" +
				"\n" +
				"}\n"
		);

		editor.assertHighlights("@Component", "@Autowired");
		editor.assertTrimmedHover("@Autowired", 1,
				"**&#8592; `DependencyA`**\n" +
				"- Bean: `dependencyA`  \n" +
				"  Type: `com.example.DependencyA`\n" +
				"  \n" +
				"Bean id: `someComponent`  \n" +
				"Process [PID=111, name=`the-app`]\n"
		);
	}

	@Test
	public void collectionOfCollectionsWiredBeanNotFound() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("someComponent")
						.type("com.example.SomeComponent")
						.dependencies("dependencyA")
						.build()
				)
				.add(LiveBean.builder()
						.id("dependencyA")
						.type("com.example.DependencyA")
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
				"import java.util.List;\n" +
				"import org.springframework.beans.factory.annotation.Autowired;\n" +
				"import org.springframework.beans.factory.annotation.Qualifier;\n" +
				"import org.springframework.stereotype.Component;\n" +
				"\n" +
				"@Component\n" +
				"public class SomeComponent {\n" +
				"\n" +
				"   @Autowired\n" +
				"   @Qualifier(\"dependencyA\")\n" +
				"   private List<List<DependencyA>> a;\n" +
				"\n" +
				"	public SomeComponent() {\n" +
				"	}\n" +
				"\n" +
				"}\n"
		);

		editor.assertHighlights("@Component");
		editor.assertNoHover("@Autowired");
	}

	@Test
	public void collectionRawWithQualifierWiredBean() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("someComponent")
						.type("com.example.SomeComponent")
						.dependencies("dependencyA")
						.build()
				)
				.add(LiveBean.builder()
						.id("dependencyA")
						.type("com.example.DependencyA")
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
				"import java.util.List;\n" +
				"import org.springframework.beans.factory.annotation.Autowired;\n" +
				"import org.springframework.beans.factory.annotation.Qualifier;\n" +
				"import org.springframework.stereotype.Component;\n" +
				"\n" +
				"@Component\n" +
				"public class SomeComponent {\n" +
				"\n" +
				"   @Autowired\n" +
				"   @Qualifier(\"dependencyA\")\n" +
				"   private List a;\n" +
				"\n" +
				"	public SomeComponent() {\n" +
				"	}\n" +
				"\n" +
				"}\n"
		);

		editor.assertHighlights("@Component");
		editor.assertNoHover("@Autowired");
	}

	@Test
	public void arrayWithQualifierWiredBean() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("someComponent")
						.type("com.example.SomeComponent")
						.dependencies("dependencyA")
						.build()
				)
				.add(LiveBean.builder()
						.id("dependencyA")
						.type("com.example.DependencyA")
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
				"import org.springframework.beans.factory.annotation.Qualifier;\n" +
				"import org.springframework.stereotype.Component;\n" +
				"\n" +
				"@Component\n" +
				"public class SomeComponent {\n" +
				"\n" +
				"   @Autowired\n" +
				"   @Qualifier(\"dependencyA\")\n" +
				"   private DependencyA[] a;\n" +
				"\n" +
				"	public SomeComponent() {\n" +
				"	}\n" +
				"\n" +
				"}\n"
		);

		editor.assertHighlights("@Component", "@Autowired");
		editor.assertTrimmedHover("@Autowired", 1,
				"**&#8592; `DependencyA`**\n" +
				"- Bean: `dependencyA`  \n" +
				"  Type: `com.example.DependencyA`\n" +
				"  \n" +
				"Bean id: `someComponent`  \n" +
				"Process [PID=111, name=`the-app`]\n"
		);
	}

	@Test
	public void arrayWiredBean() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("someComponent")
						.type("com.example.SomeComponent")
						.dependencies("dependencyA")
						.build()
				)
				.add(LiveBean.builder()
						.id("dependencyA")
						.type("com.example.DependencyA")
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
				"public class SomeComponent {\n" +
				"\n" +
				"   @Autowired\n" +
				"   private IDependency[] a;\n" +
				"\n" +
				"	public SomeComponent() {\n" +
				"	}\n" +
				"\n" +
				"}\n"
		);

		editor.assertHighlights("@Component", "@Autowired");
		editor.assertTrimmedHover("@Autowired", 1,
				"**&#8592; `DependencyA`**\n" +
				"- Bean: `dependencyA`  \n" +
				"  Type: `com.example.DependencyA`\n" +
				"  \n" +
				"Bean id: `someComponent`  \n" +
				"Process [PID=111, name=`the-app`]\n"
		);
	}

	@Test
	public void arrayWiredBeans() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("someComponent")
						.type("com.example.SomeComponent")
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
				"public class SomeComponent {\n" +
				"\n" +
				"   @Autowired\n" +
				"   private IDependency[] a;\n" +
				"\n" +
				"	public SomeComponent() {\n" +
				"	}\n" +
				"\n" +
				"}\n"
		);

		editor.assertHighlights("@Component", "@Autowired");
		editor.assertTrimmedHover("@Autowired", 1,
				"**&#8592; `DependencyA` `DependencyB`**\n" +
				"- Bean: `dependencyA`  \n" +
				"  Type: `com.example.DependencyA`\n" +
				"- Bean: `dependencyB`  \n" +
				"  Type: `com.example.DependencyB`\n" +
				"  \n" +
				"Bean id: `someComponent`  \n" +
				"Process [PID=111, name=`the-app`]\n"
		);
	}

	@Test
	public void multiDimensionalArrayWiredBean() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("someComponent")
						.type("com.example.SomeComponent")
						.dependencies("dependencyA")
						.build()
				)
				.add(LiveBean.builder()
						.id("dependencyA")
						.type("com.example.DependencyA")
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
				"public class SomeComponent {\n" +
				"\n" +
				"   @Autowired\n" +
				"   private IDependency[][] a;\n" +
				"\n" +
				"	public SomeComponent() {\n" +
				"	}\n" +
				"\n" +
				"}\n"
		);

		editor.assertHighlights("@Component");
		editor.assertNoHover("@Autowired");
	}

	@Test
	public void anonymousInnerClassBeanWiring() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("anotherComponent")
						.type("com.example.AnotherComponent")
						.dependencies("anonymousBeanFactory")
						.build()
				)
				.add(LiveBean.builder()
						.id("anonymousBeanFactory")
						.type("com.example.SomeComponent$1")
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
				"public class AnotherComponent {\n" +
				"\n" +
				"   @Autowired\n" +
				"   RuntimeBeanFactory beanFactory;\n" +
				"}\n"
		);

		editor.assertHighlights("@Component", "@Autowired");
		editor.assertTrimmedHover("@Autowired", 1,
				"**&#8592; `SomeComponent$1`**\n" +
				"- Bean: `anonymousBeanFactory`  \n" +
				"  Type: `com.example.SomeComponent$1`\n" +
				"  \n" +
				"Bean id: `anotherComponent`  \n" +
				"Process [PID=111, name=`the-app`]\n"
		);
	}
}
