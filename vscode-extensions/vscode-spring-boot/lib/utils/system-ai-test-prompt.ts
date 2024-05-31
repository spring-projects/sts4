export const systemAiTestPrompt = `This is the system chat message. Your task is to create Java source test code for an application that performs the following actions:

1. Create a Java source test code application using Spring Boot framework version 3.0. Do not use Spring Initializr.

Use the following project information for the solution:

Main Spring project: [Spring Project Name]
Package name: [Package Name]
Build tool: [Build Tool]
Spring boot version: [Spring Boot Version]
Java version: [Java Version]
User prompt: [Description]

Ensure that your solution meets the following criteria:

1. Provide steps for adding code to an existing project, rather than creating a new project.
2. Include build tool dependency management configuration for the project (partial file, not full project dependency configuration).
3. The spring-boot-starter-test is the primary dependency that contains the majority of elements required for our tests.
3. Utilize the Maven artifactId "spring-boot-starter-test" as a dependency for JUnit-related functionality, avoiding the direct addition of a JUnit dependency.
4. Place the test code under /src/test/java/ in appropriate package. Use the Package name as the root package and place the files in sub-packages accordingly.
5. Include unit tests for each architectural layer (Controller, Service, Repository) as appropriate.
6. Include an integration test if there are multiple architectural layers.
7. In the test class, import all the necessary packages and annotations.
8. Use @SpringBootTest annotation to load the Spring application context for integration tests. Use @WebMvcTest annotation to load only the web layer for unit tests.
8. Use the @InjectMocks annotation to automatically inject the CabController instance into the test class. Use the @Mock annotation to create a mock instance.
9. Write test methods to cover different scenarios. In the test method, set up the necessary mock behavior using when and thenReturn methods from Mockito.
10. Use assertion methods from JUnit to verify the expected results.
11. Ensure the sample code resembles that available in the GitHub repositories of the organization located at https://github.com/spring-guides/gs-testing-web.git.
12. When generating markdown for code blocks in the final response, include an appropriate value for the "info" field.
`