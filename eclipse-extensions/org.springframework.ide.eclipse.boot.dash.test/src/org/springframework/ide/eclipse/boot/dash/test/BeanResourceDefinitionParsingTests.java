/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.springframework.ide.eclipse.boot.test.BootProjectTestHarness.bootVersionAtLeast;
import static org.springframework.ide.eclipse.boot.test.BootProjectTestHarness.setPackage;
import static org.springframework.ide.eclipse.boot.test.BootProjectTestHarness.withStarters;
import static org.springsource.ide.eclipse.commons.tests.util.StsTestCase.createFile;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.eclipse.beans.ui.live.model.SpringResource;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.test.BootProjectTestHarness;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

public class BeanResourceDefinitionParsingTests {

	private BootProjectTestHarness projects;
	private TestBootDashModelContext context;

	@Before
	public void setup() throws Exception {
		StsTestUtil.deleteAllProjects();
		this.context = new TestBootDashModelContext(ResourcesPlugin.getWorkspace(),
				DebugPlugin.getDefault().getLaunchManager());
		this.projects = new BootProjectTestHarness(context.getWorkspace());
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void classPathResource() throws Exception {
		String resourceDefinition = "class path resource [org/springframework/boot/actuate/autoconfigure/audit/AuditAutoConfiguration.class]";
		String expectedPath = "org/springframework/boot/actuate/autoconfigure/audit/AuditAutoConfiguration.class";
		String expectedFQType = "org.springframework.boot.actuate.autoconfigure.audit.AuditAutoConfiguration";
		SpringResource parser = new SpringResource(resourceDefinition, null);
		String actualPath = parser.getResourcePath();
		String actualClassName = parser.getClassName();
		assertEquals(expectedPath, actualPath);
		assertEquals(expectedFQType, actualClassName);
	}

	@Test
	public void classPathResourceWithProject() throws Exception {
		// Tests parsing of type from a "class path resource" bean definition that is NOT in the project source, with a given project
		// The purpose of this test is to ensure that passing a project to the parser does not affect
		// parsing of type from "class path resource"
		IProject project = createBootProject("test-bean-resource-file",
				"src/main/java/com/example/demo/HelloController.java", "package com.example.demo;\n" + //
						"\n" + //
						"import org.springframework.web.bind.annotation.RequestMapping;\n" + //
						"import org.springframework.web.bind.annotation.RestController;\n" + //
						"\n" + //
						"@RestController\n" + //
						"public class HelloController {\n" + //
						"\n" + //
						"	@RequestMapping(\"/hello\")\n" + //
						"	public String hello() {\n" + //
						"		return \"Hello, World!\";\n" + //
						"	}\n" + //
						"\n" + //
						"}\n");

		// A resource definition that is NOT in the project source.
		String resourceDefinition = "class path resource [org/springframework/boot/actuate/autoconfigure/audit/AuditAutoConfiguration.class]";
		String expectedPath = "org/springframework/boot/actuate/autoconfigure/audit/AuditAutoConfiguration.class";
		String expectedFQType = "org.springframework.boot.actuate.autoconfigure.audit.AuditAutoConfiguration";

		SpringResource parser = new SpringResource(resourceDefinition, project);

		String actualPath = parser.getResourcePath();
		String actualClassName = parser.getClassName();
		assertEquals(expectedPath, actualPath);
		assertEquals(expectedFQType, actualClassName);
	}

	@Test
	public void classPathResourceInnerClass() throws Exception {
		String resourceDefinition = "class path resource [org/springframework/boot/actuate/autoconfigure/metrics/MetricsAutoConfiguration$MeterBindersConfiguration.class]";
		String expectedPath = "org/springframework/boot/actuate/autoconfigure/metrics/MetricsAutoConfiguration$MeterBindersConfiguration.class";
		// Expected should be in JDT form using '.' to allow Inner Class navigation
		String expectedFQType = "org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration.MeterBindersConfiguration";
		SpringResource parser = new SpringResource(resourceDefinition, null);
		String actualPath = parser.getResourcePath();
		String actualClassName = parser.getClassName();
		assertEquals(expectedPath, actualPath);
		assertEquals(expectedFQType, actualClassName);
	}

	@Test
	public void resourceDefinitionTypeOnly() throws Exception {
		String resourceDefinition = "com/example/demo/HelloController.class";
		String expectedPath = "com/example/demo/HelloController.class";
		String expectedFQType = "com.example.demo.HelloController";
		SpringResource parser = new SpringResource(resourceDefinition, null);
		String actualPath = parser.getResourcePath();
		String actualClassName = parser.getClassName();
		assertEquals(expectedPath, actualPath);
		assertEquals(expectedFQType, actualClassName);
	}

	@Test
	public void resourceDefinitionTypeOnlyInnerClass() throws Exception {
		String resourceDefinition = "com/example/demo/HelloController$InnerClass.class";
		String expectedPath = "com/example/demo/HelloController$InnerClass.class";
		String expectedFQType = "com.example.demo.HelloController.InnerClass";
		SpringResource parser = new SpringResource(resourceDefinition, null);
		String actualPath = parser.getResourcePath();
		String actualClassName = parser.getClassName();
		assertEquals(expectedPath, actualPath);
		assertEquals(expectedFQType, actualClassName);
	}

	@Test
	public void beanDefinitionIn() throws Exception {
		String resourceDefinition = "BeanDefinition defined in org.springframework.security.oauth2.config.annotation.web.configuration.OAuth2ClientConfiguration";
		String expectedPath = "org/springframework/security/oauth2/config/annotation/web/configuration/OAuth2ClientConfiguration.class";
		String expectedFQType = "org.springframework.security.oauth2.config.annotation.web.configuration.OAuth2ClientConfiguration";
		SpringResource parser = new SpringResource(resourceDefinition, null);
		String actualPath = parser.getResourcePath();
		String actualClassName = parser.getClassName();
		assertEquals(expectedPath, actualPath);
		assertEquals(expectedFQType, actualClassName);
	}

	@Test
	public void fileDefinition1() throws Exception {
		// To test this, we need to create a real project with an actual class file
		IProject project = createBootProject("test-bean-resource-file",
				"src/main/java/com/example/demo/HelloController.java", "package com.example.demo;\n" + //
						"\n" + //
						"import org.springframework.web.bind.annotation.RequestMapping;\n" + //
						"import org.springframework.web.bind.annotation.RestController;\n" + //
						"\n" + //
						"@RestController\n" + //
						"public class HelloController {\n" + //
						"\n" + //
						"	@RequestMapping(\"/hello\")\n" + //
						"	public String hello() {\n" + //
						"		return \"Hello, World!\";\n" + //
						"	}\n" + //
						"\n" + //
						"}\n");

		// To test a resource definition with a path to a bean type in a project, we need the absolute path
		// of that type file, as that is what we'd get from "real" actuator info.
		// However, we can't hardcode the absolute project path that would contain that type ahead of time as this test is run on different environments,
		// so we have to construct a resource definition to test with the project path as shown below.
		String resourceDefinition = "file [" + project.getLocation() + "/target/classes/com/example/demo/HelloController.class]";

		String expectedFQType = "com.example.demo.HelloController";
		SpringResource parser = new SpringResource(resourceDefinition, project);
		String actualClassName = parser.getClassName();
		assertEquals(expectedFQType, actualClassName);
	}

	@Test
	public void fileDefinitionInnerClassBean() throws Exception {
		// To test this, we need to create a real project with an actual class file
		IProject project = createBootProject("test-bean-resource-file",
				"src/main/java/com/example/demo/HelloController.java", "package com.example.demo;\n" + //
						"\n" + //
						"import org.springframework.web.bind.annotation.RequestMapping;\n" + //
						"import org.springframework.web.bind.annotation.RestController;\n" + //
						"\n" + //
						"@RestController\n" + //
						"public class HelloController {\n" + //
						"\n" + //
						"	@RequestMapping(\"/hello\")\n" + //
						"	public String hello() {\n" + //
						"		return \"Hello, World!\";\n" + //
						"	}\n" + //
						"    @Component\n" + //
						"    static public class InnerClass {\n" + //
						"\n" + //
						"        /**\n" + //
						"         * \n" + //
						"         */\n" + //
						"        public InnerClass() {\n" + //
						"            super();\n" + //
						"        }\n" + //
						"\n" + //
						"    }\n" + //
						"\n" + //
						"    @Autowired private InnerClass innerclass;" + //
						"\n" + //
						"}\n");//

		// To test a resource definition with a path to a bean type in a project, we need the absolute path
		// of that type file, as that is what we'd get from "real" actuator info.
		// However, we can't hardcode the absolute project path that would contain that type ahead of time as this test is run on different environments,
		// so we have to construct a resource definition to test with the project path as shown below.
		String resourceDefinition = project.getLocation() + "/target/classes/com/example/demo/HelloController$InnerClass.class";

		String expectedFQType = "com.example.demo.HelloController.InnerClass";
		SpringResource parser = new SpringResource(resourceDefinition, project);
		String actualClassName = parser.getClassName();
		assertEquals(expectedFQType, actualClassName);
	}

	private IProject createBootProject(String projectName, String typePath, String clsContent) throws Exception {

		IProject project = projects.createBootWebProject(projectName, 		bootVersionAtLeast("1.3.0"), //required for us to be able to determine the actuator port
				withStarters("web", "actuator"), //required to actually *have* an actuator
				setPackage("com.example.demo"));

		createFile(project, typePath, clsContent);
		return project;
	}
}
