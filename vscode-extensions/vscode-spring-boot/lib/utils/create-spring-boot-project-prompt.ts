export const projectCreationPrompt = `
Follow the instructions below to ensure your solution meets the criteria:

### Project Setup

1. **Spring Boot Version**: Create a Java source code application using Spring Boot framework version 3.0. Avoid using Spring Initializr for project initialization.

2. **Spring Boot 3.0 Release Notes and Best Practices**:
    - Refer to the [Spring Boot 3.0 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Release-Notes) for detailed information on new features, improvements, and any breaking changes.
    - Adhere to best practices outlined in the release notes to ensure optimal performance, security, and maintainability.

Use the following placeholders provided in the user message for the solution:
3  **Project Information**:
   - **Main Spring Project Name**: [Spring Project Name]
   - **Application Description**: [Description]
   - **Package Name**: [Package Name]
   - **Build Tool**: [Build Tool]
   - **Java Version**: [Java Version]

Ensure that your solution meets the following criteria:

### Dependencies and Build Configuration

5. **Dependency Management**: Include build tool dependency management configuration for the project. Create a pom.xml file in the root of your project.
Include necessary Maven configurations such as groupId, artifactId, version, and dependencies.
6. **Spring Boot Starters**: Include necessary Spring Boot Starter dependencies, such as "spring-boot-starter-jpa."
7. **Testing Dependencies**: Utilize the Maven artifactId "spring-boot-starter-test" for JUnit-related functionality. Avoid direct addition of a JUnit dependency.

### Project Structure and Code Generation

8. **Package Structure**: Place all code, including test code, in the same Java package. Follow the "package per feature" convention.
9. **Entity Definition**: If no entity or domain object is specified in the description, use a Person entity with properties "name" and "phone number."
10. **Entity Code Generation**: Generate constructors, property getters, and setters for the entity. For example, "getName" and "setName" for the name property.
11. **JSON Manipulation**: Utilize classes from the Java Jackson library for JSON manipulation
11. Include import statements in all code files. Spring Boot 3 introduces a significant update requiring the transition from 'javax.' imports to 'jakarta.' imports.
12. Ensure that all imports using 'javax.' are now replaced with 'jakarta.' imports. Focus on updating classes and interfaces under 'javax.' to 'jakarta.' and converting 'javax.persistence'
imports to 'jakarta.persistence' for proper Jakarta EE compatibility. Additionally, set the Hibernate versionto a minimum of Hibernate ORM 5.6.x, supporting Jakarta Persistence. The default configuration in Spring Boot 3
should use Hibernate 6 and Flyway 9. Please generate code accordingly.
13. Create interface for the service class

### Architectural Layers and Testing

14. **Architectural Layers**: Include architectural layers (Controller, Service, Repository) as appropriate.
15. **Unit Tests**: Include unit tests for each architectural layer (Controller, Service, Repository) as appropriate.
16. **Integration Test**: Include an integration test if there are multiple architectural layers.

### Additional Considerations

17. **Logging Configuration**:
    - Configure logging for the application using Spring Boot's logging facilities.
    - Ensure that logs include sufficient information for debugging and monitoring.
18. **Database Configuration**:
    - Configure the application to connect to an H2 in-memory database.
    - Provide appropriate configuration for database connection properties.
19. **API Documentation**:
    - Generate documentation for API endpoints, request, and response objects.
20. **Profiles**:
    - Implement different profiles (e.g., development, production) for the application.
    - Configure profile-specific properties and behaviors.
21. **GitHub Reference**: Ensure the sample code resembles that available in the GitHub repositories of the organization located at [https://github.com/spring-guides](https://github.com/spring-guides).
22. **Spring Security (If Applicable)**: If the application uses Spring Security, include the following dependency artifact IDs: "spring-boot-starter-security" and "spring-security-test."
23. **Markdown Code Blocks**: When generating markdown for code blocks in the final response, include an appropriate value for the "info" field.
24. **Tutorial Links**: Provide links to tutorials for learning more about the generated application. Prefer tutorials from the domains [https://spring.io](https://spring.io) and [https://www.baeldung.com/](https://www.baeldung.com/), but include others if they are popular. Provide a maximum of 5 links.
`;