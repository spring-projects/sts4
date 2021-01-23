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
package org.springframework.ide.vscode.boot.validation.generations;

import java.util.List;

import org.springframework.ide.vscode.boot.validation.generations.json.Generations;
import org.springframework.ide.vscode.boot.validation.generations.json.GenerationsEmbedded;
import org.springframework.ide.vscode.boot.validation.generations.json.JsonHalParser;
import org.springframework.ide.vscode.boot.validation.generations.json.SpringProject;
import org.springframework.ide.vscode.boot.validation.generations.json.SpringProjects;
import org.springframework.ide.vscode.boot.validation.generations.json.SpringProjectsEmbedded;


/**
 * Spring-boot sample json. Used for testing or providing a hardcoded fall-back to spring.io, when
 * the latter is not available.
 *  
 */
public class SampleProjectsProvider implements SpringProjectsProvider {

	private SpringProjects projects;

	@Override
	public SpringProject getProject(String projectSlug) throws Exception {
		if (this.projects == null) {
			JsonHalParser parser = new JsonHalParser();
			this.projects = parser.getEmbedded(SPRING_PROJECTS_JSON_SAMPLE, SpringProjectsEmbedded.class);
		}
		List<SpringProject> projectList = this.projects.getProjects();
		for (SpringProject springProject : projectList) {
			if (springProject.getSlug().equals(projectSlug)) {
				return springProject;
			}
		}
		return null;
	}

	@Override
	public Generations getGenerations(String projectSlug) throws Exception {
		SpringProject project = getProject(projectSlug);
		if (project != null && project.getSlug().equals("spring-boot")) {
			JsonHalParser parser = new JsonHalParser();
			return parser.getEmbedded(SPRING_BOOT_PROJECT_GENERATIONS, GenerationsEmbedded.class);
		}
		return null;
	}

	public static final String SPRING_BOOT_PROJECT_GENERATIONS = "{\n" + "  \"_embedded\" : {\n"
			+ "    \"generations\" : [ {\n" + "      \"name\" : \"1.3.x\",\n"
			+ "      \"initialReleaseDate\" : \"2019-01-01\",\n" + "      \"ossSupportEndDate\" : \"2020-01-01\",\n"
			+ "      \"commercialSupportEndDate\" : \"2021-01-01\",\n" + "      \"_links\" : {\n"
			+ "        \"self\" : {\n"
			+ "          \"href\" : \"https://spring.io/api/projects/spring-boot/generations/1.3.x\"\n" + "        },\n"
			+ "        \"project\" : {\n" + "          \"href\" : \"https://spring.io/api/projects/spring-boot\"\n"
			+ "        }\n" + "      }\n" + "    }, {\n" + "      \"name\" : \"2.2.x\",\n"
			+ "      \"initialReleaseDate\" : \"2020-01-01\",\n" + "      \"ossSupportEndDate\" : \"2021-01-01\",\n"
			+ "      \"commercialSupportEndDate\" : \"2022-01-01\",\n" + "      \"_links\" : {\n"
			+ "        \"self\" : {\n"
			+ "          \"href\" : \"https://spring.io/api/projects/spring-boot/generations/2.2.x\"\n" + "        },\n"
			+ "        \"project\" : {\n" + "          \"href\" : \"https://spring.io/api/projects/spring-boot\"\n"
			+ "        }\n" + "      }\n" + "    } ]\n" + "  },\n" + "  \"_links\" : {\n" + "    \"project\" : {\n"
			+ "      \"href\" : \"https://spring.io/api/projects/spring-boot\"\n" + "    }\n" + "  }\n" + "}";

	public static final String SPRING_PROJECTS_JSON_SAMPLE = "{\n" + "  \"_embedded\" : {\n"
			+ "    \"projects\" : [ {\n" + "      \"name\" : \"Spring Boot\",\n" + "      \"slug\" : \"spring-boot\",\n"
			+ "      \"repositoryUrl\" : \"https://github.com/spring-projects/spring-boot\",\n"
			+ "      \"status\" : \"ACTIVE\",\n" + "      \"_links\" : {\n" + "        \"self\" : {\n"
			+ "          \"href\" : \"https://spring.io/api/projects/spring-boot\"\n" + "        },\n"
			+ "        \"releases\" : {\n"
			+ "          \"href\" : \"https://spring.io/api/projects/spring-boot/releases\"\n" + "        },\n"
			+ "        \"generations\" : {\n"
			+ "          \"href\" : \"https://spring.io/api/projects/spring-boot/generations\"\n" + "        }\n"
			+ "      }\n" + "    }, {\n" + "      \"name\" : \"Spring Data\",\n" + "      \"slug\" : \"spring-data\",\n"
			+ "      \"repositoryUrl\" : \"https://github.com/spring-projects/spring-data\",\n"
			+ "      \"status\" : \"ACTIVE\",\n" + "      \"_links\" : {\n" + "        \"self\" : {\n"
			+ "          \"href\" : \"https://spring.io/api/projects/spring-data\"\n" + "        },\n"
			+ "        \"releases\" : {\n"
			+ "          \"href\" : \"https://spring.io/api/projects/spring-data/releases\"\n" + "        },\n"
			+ "        \"generations\" : {\n"
			+ "          \"href\" : \"https://spring.io/api/projects/spring-data/generations\"\n" + "        }\n"
			+ "      }\n" + "    }, {\n" + "      \"name\" : \"Spring Data Elasticsearch\",\n"
			+ "      \"slug\" : \"spring-data-elasticsearch\",\n"
			+ "      \"repositoryUrl\" : \"https://github.com/spring-projects/spring-data-elasticsearch\",\n"
			+ "      \"status\" : \"ACTIVE\",\n" + "      \"_links\" : {\n" + "        \"self\" : {\n"
			+ "          \"href\" : \"https://spring.io/api/projects/spring-data-elasticsearch\"\n" + "        },\n"
			+ "        \"releases\" : {\n"
			+ "          \"href\" : \"https://spring.io/api/projects/spring-data-elasticsearch/releases\"\n"
			+ "        },\n" + "        \"generations\" : {\n"
			+ "          \"href\" : \"https://spring.io/api/projects/spring-data-elasticsearch/generations\"\n"
			+ "        },\n" + "        \"parent\" : {\n"
			+ "          \"href\" : \"https://spring.io/api/projects/spring-data\"\n" + "        }\n" + "      }\n"
			+ "    } ]\n" + "  }\n" + "}";

}
