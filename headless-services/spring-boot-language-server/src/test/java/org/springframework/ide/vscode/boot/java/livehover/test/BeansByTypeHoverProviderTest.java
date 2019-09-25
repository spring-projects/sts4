/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
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
import org.springframework.ide.vscode.project.harness.SpringProcessLiveDataBuilder;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@BootLanguageServerTest
@Import(HoverTestConf.class)
public class BeansByTypeHoverProviderTest {

	private ProjectsHarness projects = ProjectsHarness.INSTANCE;
	@Autowired private BootLanguageServerHarness harness;
	@Autowired private SpringProcessLiveDataProvider liveDataProvider;

	@Before
	public void setup() throws Exception {
		MavenJavaProject jp =  projects.mavenProject("empty-boot-15-web-app");
		harness.useProject(jp);
		harness.intialize(null);
	}
	
	@After
	public void tearDown() throws Exception {
		liveDataProvider.remove("processkey");
	}

	@Test
	public void typeButNotABean() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("scannedRandomClass")
						.type("com.example.ScannedRandomClass")
						.build()
				)
				.add(LiveBean.builder()
						.id("randomOtherBean")
						.type("randomOtherBeanType")
						.dependencies("scannedRandomClass")
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
				"import java.io.Serializable;\n" +
				"\n" +
				"public class ClassNoBean implements Serializable {\n" +
				"\n" +
				"	public String apply(String t) {\n" +
				"		return t.toUpperCase();\n" +
				"	}\n" +
				"\n" +
				"}\n" +
				""
		);
		editor.assertHighlights();
		editor.assertNoHover("ClassNoBean");
	}

	@Test
	public void typeWithGeneralBean() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("scannedRandomClass")
						.type("com.example.ScannedRandomClass")
						.build()
				)
				.add(LiveBean.builder()
						.id("randomOtherBean")
						.type("randomOtherBeanType")
						.dependencies("scannedRandomClass")
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
				"import java.io.Serializable;\n" +
				"\n" +
				"public class ScannedRandomClass implements Serializable {\n" +
				"\n" +
				"	public String apply(String t) {\n" +
				"		return t.toUpperCase();\n" +
				"	}\n" +
				"\n" +
				"}\n" +
				""
		);
		editor.assertHighlights("ScannedRandomClass");
		editor.assertTrimmedHover("ScannedRandomClass",
				"**&#8594; `randomOtherBeanType`**\n" +
				"- Bean: `randomOtherBean`  \n" +
				"  Type: `randomOtherBeanType`\n" +
				"  \n" +
				"Bean id: `scannedRandomClass`  \n" +
				"Process [PID=111, name=`the-app`]"
		);
	}
	
	@Test
	public void beanWithNonStandardId() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("random")
						.type("com.example.ScannedRandomClass")
						.build()
				)
				.add(LiveBean.builder()
						.id("randomOtherBean")
						.type("randomOtherBeanType")
						.dependencies("random")
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
				"import java.io.Serializable;\n" +
				"\n" +
				"public class ScannedRandomClass implements Serializable {\n" +
				"\n" +
				"	public String apply(String t) {\n" +
				"		return t.toUpperCase();\n" +
				"	}\n" +
				"\n" +
				"}\n" +
				""
		);
		editor.assertHighlights("ScannedRandomClass");
		editor.assertTrimmedHover("ScannedRandomClass",
				"**&#8594; `randomOtherBeanType`**\n" +
				"- Bean: `randomOtherBean`  \n" +
				"  Type: `randomOtherBeanType`\n" +
				"  \n" +
				"Bean id: `random`  \n" +
				"Process [PID=111, name=`the-app`]"
		);
	}
	
	@Test
	public void beansWithNonStandardIdMoreThanOneOfSameType() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("random")
						.type("com.example.ScannedRandomClass")
						.build()
				)
				.add(LiveBean.builder()
						.id("anotherRandom")
						.type("com.example.ScannedRandomClass")
						.build()
				)
				.add(LiveBean.builder()
						.id("randomOtherBean")
						.type("randomOtherBeanType")
						.dependencies("random")
						.build()
				)
				.add(LiveBean.builder()
						.id("irrelevantBean")
						.type("com.example.IrrelevantBean")
						.dependencies("anotherRandom")
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
				"import java.io.Serializable;\n" +
				"\n" +
				"public class ScannedRandomClass implements Serializable {\n" +
				"\n" +
				"	public String apply(String t) {\n" +
				"		return t.toUpperCase();\n" +
				"	}\n" +
				"\n" +
				"}\n" +
				""
		);
		editor.assertHighlights();
		editor.assertNoHover("ScannedRandomClass");
	}

	@Test
	public void scannedAndInjectedFunction() throws Exception {
		LiveBeansModel beans = LiveBeansModel.builder()
				.add(LiveBean.builder()
						.id("scannedFunctionClass")
						.type("com.example.ScannedFunctionClass")
						.build()
				)
				.add(LiveBean.builder()
						.id("org.springframework.cloud.function.context.config.ContextFunctionCatalogAutoConfiguration")
						.type("org.springframework.cloud.function.context.config.ContextFunctionCatalogAutoConfiguration")
						.dependencies("scannedFunctionClass")
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
				"import java.util.function.Function;\n" +
				"\n" +
				"public class ScannedFunctionClass implements Function<String, String> {\n" +
				"\n" +
				"	@Override\n" +
				"	public String apply(String t) {\n" +
				"		return t.toUpperCase();\n" +
				"	}\n" +
				"\n" +
				"}\n" +
				""
		);
		editor.assertHighlights("ScannedFunctionClass");
		editor.assertTrimmedHover("ScannedFunctionClass",
				"**&#8594; 1 bean**\n" +
				"- Bean: `org.springframework.cloud.function.context.config.ContextFunctionCatalogAutoConfiguration`  \n" +
				"  Type: `org.springframework.cloud.function.context.config.ContextFunctionCatalogAutoConfiguration`\n" +
				"  \n" +
				"Bean id: `scannedFunctionClass`  \n" +
				"Process [PID=111, name=`the-app`]"

		);
	}

	@Test
	public void generalBeanLiveHoverAvoidOverlapWithAnnotation() throws Exception {
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


}
