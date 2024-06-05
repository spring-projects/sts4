export const userPrompt = `Create the Spring Java application with the following project information

Main Spring project: [Spring Project Name]
Root Package name: [Package Name]
Build tool: [Build Tool]
Java version: [Java Version]
Spring boot version: [Spring Boot Version]

Ensure the solution meets the following additional criteria

1. Include import statements in all code files
2. Generate getter and setters in entity classes
3. Provide the necessary Spring Boot Starter dependencies in the pom.xml file 
4. When generating markdown for the 'application.properties' file in Spring Boot, please ensure that the markdown 'info' field is set to 'properties' for that specific section.
`