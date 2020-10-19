package org.springframework.ide.vscode.boot.validation.test;

public class SpringProjectsTestSamples {
	
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
