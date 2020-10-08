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

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.vscode.boot.validation.generations.SpringProjectIndex;
import org.springframework.ide.vscode.boot.validation.generations.SpringProjectsClient;
import org.springframework.ide.vscode.boot.validation.generations.json.Generation;
import org.springframework.ide.vscode.boot.validation.generations.json.Generations;
import org.springframework.ide.vscode.boot.validation.generations.json.GenerationsEmbedded;
import org.springframework.ide.vscode.boot.validation.generations.json.JsonHalParser;
import org.springframework.ide.vscode.boot.validation.generations.json.Link;
import org.springframework.ide.vscode.boot.validation.generations.json.SpringProject;
import org.springframework.ide.vscode.boot.validation.generations.json.SpringProjects;
import org.springframework.ide.vscode.boot.validation.generations.json.SpringProjectsEmbedded;

public class ProjectGenerationsValidationTest {

	@Before
	public void setup() throws Exception {

	}

	@Test
	public void testProjectsInfoFromSpringIo() throws Exception {
		String url = "https://spring.io/api/projects";
		SpringProjectsClient client = new SpringProjectsClient(url);
		SpringProjectIndex projectIndex = new SpringProjectIndex(client);

		SpringProjects springProjects = projectIndex.getProjects();
		assertNotNull(springProjects);
		assertTrue(!springProjects.getProjects().isEmpty());

		SpringProject project = projectIndex.getProject("spring-boot");
		assertNotNull(project);
		assertEquals("Spring Boot", project.getName());
		assertEquals("spring-boot", project.getSlug());

		project = projectIndex.getProject("spring-integration");
		assertNotNull(project);
		assertEquals("Spring Integration", project.getName());
		assertEquals("spring-integration", project.getSlug());
	}

	@Test
	public void testGenerationsFromSpringIo() throws Exception {
		String url = "https://spring.io/api/projects";
		SpringProjectsClient client = new SpringProjectsClient(url);
		SpringProjectIndex projectIndex = new SpringProjectIndex(client);

		SpringProject project = projectIndex.getProject("spring-boot");
		assertNotNull(project);
		Link generationsUrl = project.get_links().getGenerations();
		assertNotNull(generationsUrl);
		assertEquals("https://spring.io/api/projects/spring-boot/generations", generationsUrl.getHref());

		Generations generations = projectIndex.getGenerations(project);

		// NOTE: at the moment Generations are not available from spring.io API. Enable
		// when they are
//		assertNotNull(generations);
	}

	@Test
	public void testGenerationsFromSample() throws Exception {
		SpringProjectsClient client = getMockClient();

		SpringProjectIndex projectIndex = new SpringProjectIndex(client);

		SpringProject project = projectIndex.getProject("spring-boot");
		assertNotNull(project);

		Generations generations = projectIndex.getGenerations(project);
		assertNotNull(generations);

		List<Generation> genList = generations.getGenerations();

		assertNotNull(genList);
		assertTrue(genList.size() > 0);

		Generation generation = genList.get(0);
		assertEquals("2.1.x", generation.getName());
		assertEquals("2019-01-01", generation.getInitialReleaseDate());
		assertEquals("2020-01-01", generation.getOssSupportEndDate());
		assertEquals("2021-01-01", generation.getCommercialSupportEndDate());
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
}
