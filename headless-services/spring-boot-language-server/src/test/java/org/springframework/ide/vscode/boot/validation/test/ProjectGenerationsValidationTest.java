/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.validation.test;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.boot.bootiful.HoverTestConf;
import org.springframework.ide.vscode.boot.validation.generations.SpringProjectsCache;
import org.springframework.ide.vscode.boot.validation.generations.SpringProjectsValidations;
import org.springframework.ide.vscode.boot.validation.generations.SpringProjectsClient;
import org.springframework.ide.vscode.boot.validation.generations.json.Generation;
import org.springframework.ide.vscode.boot.validation.generations.json.Generations;
import org.springframework.ide.vscode.boot.validation.generations.json.GenerationsEmbedded;
import org.springframework.ide.vscode.boot.validation.generations.json.JsonHalParser;
import org.springframework.ide.vscode.boot.validation.generations.json.Link;
import org.springframework.ide.vscode.boot.validation.generations.json.SpringProject;
import org.springframework.ide.vscode.boot.validation.generations.json.SpringProjects;
import org.springframework.ide.vscode.boot.validation.generations.json.SpringProjectsEmbedded;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@BootLanguageServerTest
@Import(HoverTestConf.class)
public class ProjectGenerationsValidationTest {
	
	@Autowired private BootLanguageServerHarness harness;

	private ProjectsHarness projects = ProjectsHarness.INSTANCE;


	@Before
	public void setup() throws Exception {
		harness.useProject(projects.mavenProject("empty-boot-1.3.0-app"));
		harness.intialize(null);
	}
	
	@Test
	public void testMajMinVersionParsing() throws Exception {
		String version = SpringProjectUtil.getMajMinVersion("spring-boot-starter-batch-2.3.4.RELEASE");
		assertEquals("2.3", version);
		
		version = SpringProjectUtil.getMajMinVersion("spring-boot-starter-batch-2.4.0-M4");
		assertEquals("2.4", version);

		version = SpringProjectUtil.getMajMinVersion("spring-boot-4.4.0-RC2");
		assertEquals("4.4", version);
		
		version = SpringProjectUtil.getMajMinVersion("spring-integration-70.811.0.RELEASE");
		assertEquals("70.811", version);

		version = SpringProjectUtil.getMajMinVersion("another-java-");
		assertNull(version);

		version = SpringProjectUtil.getMajMinVersion("spring-core-5.f.2");
		assertNull(version);

		version = SpringProjectUtil.getMajMinVersion("springcore.f.b");
		assertNull(version);
	}
	
	@Test
	public void testVersionParsing() throws Exception {
		String version = SpringProjectUtil.getVersion("spring-boot-starter-batch-2.3.0.RELEASE");
		assertEquals("2.3.0", version);
		
		version = SpringProjectUtil.getVersion("spring-boot-starter-batch-2.4.0-M4");
		assertEquals("2.4.0-M4", version);

		version = SpringProjectUtil.getVersion("spring-boot-4.4.0-RC2");
		assertEquals("4.4.0-RC2", version);
		
		version = SpringProjectUtil.getVersion("spring-integration-70.811.0.RELEASE");
		assertEquals("70.811.0", version);

		version = SpringProjectUtil.getVersion("another-java-");
		assertNull(version);

		version = SpringProjectUtil.getVersion("spring-core-5.f.2");
		assertNull(version);

		version = SpringProjectUtil.getVersion("springcore.f.b");
		assertNull(version);
	}
	
	@Test
	public void testProjectSlugParsing() throws Exception {
		String slug = SpringProjectUtil.getProjectSlug("spring-boot-starter-batch-2.3.4.RELEASE");
		assertEquals("spring-boot-starter-batch", slug);
		
		slug = SpringProjectUtil.getProjectSlug("spring-2.4.0-M4");
		assertEquals("spring", slug);

		slug = SpringProjectUtil.getProjectSlug("-4.4.0-RC2");
		assertNull(slug);
	}
	
	@Test
	public void testVersionAndLibsFromActualProject() throws Exception {
		IJavaProject jp = projects.mavenProject("empty-boot-1.3.0-app");
		assertTrue(SpringProjectUtil.isBootProject(jp));
		
		List<File> springLibs = SpringProjectUtil.getLibrariesOnClasspath(jp, "spring");
		assertNotNull(springLibs);
		assertTrue(springLibs.size() > 1);
		
		File file = getLib(springLibs, "spring-boot");
		assertNotNull(file);
		assertTrue(file.exists());
		
		String version = SpringProjectUtil.getMajMinVersion(file.getName());
		assertEquals("1.3", version);
		
		String slug = SpringProjectUtil.getProjectSlug(file.getName());
		assertEquals("spring-boot", slug);
	}

	@Test
	public void testProjectsInfoFromSpringIo() throws Exception {
		String url = "https://spring.io/api/projects";
		SpringProjectsClient client = new SpringProjectsClient(url);
		SpringProjectsCache cache = new SpringProjectsCache(client, harness.getServer());

		SpringProject project = cache.getProject("spring-boot");
		assertNotNull(project);
		assertEquals("Spring Boot", project.getName());
		assertEquals("spring-boot", project.getSlug());
		Link generationsUrl = project.get_links().getGenerations();
		assertNotNull(generationsUrl);
		assertEquals("https://spring.io/api/projects/spring-boot/generations", generationsUrl.getHref());

		project = cache.getProject("spring-integration");
		assertNotNull(project);
		assertEquals("Spring Integration", project.getName());
		assertEquals("spring-integration", project.getSlug());
		generationsUrl = project.get_links().getGenerations();
		assertNotNull(generationsUrl);
		assertEquals("https://spring.io/api/projects/spring-integration/generations", generationsUrl.getHref());
	}

	@Test
	public void testGenerationsFromSample() throws Exception {
		SpringProjectsClient client = getMockClient();

		SpringProjectsCache cache = new SpringProjectsCache(client, harness.getServer());

		SpringProject project = cache.getProject("spring-boot");
		assertNotNull(project);

		Generations generations = cache.getGenerations(project);
		assertNotNull(generations);

		List<Generation> genList = generations.getGenerations();

		assertNotNull(genList);
		assertTrue(genList.size() > 0);

		Generation generation = genList.get(0);
		assertEquals("1.3.x", generation.getName());
		assertEquals("2019-01-01", generation.getInitialReleaseDate());
		assertEquals("2020-01-01", generation.getOssSupportEndDate());
		assertEquals("2021-01-01", generation.getCommercialSupportEndDate());
	}
	
	@Test
	public void testWarningsFromSample() throws Exception {
		
		SpringProjectsClient client = getMockClient();
		IJavaProject jp = projects.mavenProject("empty-boot-1.3.0-app");

		SpringProjectsCache cache = new SpringProjectsCache(client, harness.getServer());
		SpringProjectsValidations validation = new SpringProjectsValidations(cache);
		List<String> messages = validation.getVersionWarnings(jp);
		assertTrue(messages != null && messages.size()  > 0);
		String msg = messages.get(0);
		// Check that the message mentions the boot version of the project and the OSS support end date
		assertTrue(msg.contains("1.3.2") && msg.contains("OSS") &&  msg.contains("2020-01-01"));
	}

	/*
	 * 
	 * 
	 * Helper methods
	 * 
	 * 
	 * 
	 */

	protected SpringProjectsClient getMockClient() throws Exception {
		return new SpringProjectsClient(null) {

			@Override
			public SpringProjects getSpringProjects() throws Exception {
				return getProjectsFromSampleJson();
			}

			@Override
			public Generations getGenerations(String generationsUrl) throws Exception {
				return getGenerationsFromSampleJson(generationsUrl);
			}
		};
	}

	protected SpringProjects getProjectsFromSampleJson() throws Exception {
		JsonHalParser parser = new JsonHalParser();
		return parser.getEmbedded(SpringProjectsTestSamples.SPRING_PROJECTS_JSON_SAMPLE, SpringProjectsEmbedded.class);
	}

	protected Generations getGenerationsFromSampleJson(String genUrl) throws Exception {
		String json = null;
		if ("https://spring.io/api/projects/spring-boot/generations".equals(genUrl)) {
			json = SpringProjectsTestSamples.SPRING_BOOT_PROJECT_GENERATIONS;
		}

		if (json != null) {
			JsonHalParser parser = new JsonHalParser();
			return parser.getEmbedded(json, GenerationsEmbedded.class);
		}
		return null;
	}
	
	private File getLib(List<File> springLibs, String slug) {
		for (File file : springLibs) {
			String name = file.getName();
			String libSlug = SpringProjectUtil.getProjectSlug(name);
			if (slug.equals(libSlug)) {
				return file;
			}
		}
		return null;
	}
}
