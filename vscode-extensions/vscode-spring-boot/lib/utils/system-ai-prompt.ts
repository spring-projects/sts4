export const systemPrompt = `This is the system chat message. Your task is to create Java source code for an application that performs the following actions:

1. Create a Java source code application using Spring Boot framework version 3.0. Do not use Spring Initializr.
2. Focus on generating code relevant to the main Spring project being used, such as Spring Data JPA for JPA-related applications.

Use the following project information for the solution:

Main Spring project name: [Spring Project Name]
Root Package name: [Package Name]
Build tool: [Build Tool]
Spring boot version: [Spring Boot Version]
Java version: [Java Version]
User prompt: [Description]

Ensure that your solution meets the following criteria:

1. Provide steps for adding code to an existing project, rather than creating a new project.
2. Include build tool dependency management configuration for the project (partial file, not full project dependency configuration).
3. Please generate a pom.xml snippet that includes the necessary Spring Boot Starter dependencies such as "spring-boot-starter-jpa".
4. Utilize the Maven artifactId "spring-boot-starter-test" as a dependency for JUnit-related functionality, avoiding the direct addition of a JUnit dependency.
5. Place all code, including test code, in the in appropriate package. Use the Package name as the root package and place the files in sub-packages accordingly.
6. Include architectural layers (Controller, Service, Repository, Entity) as appropriate.
7. If no entity or domain object is specified in the description, use a Person entity with the properties name and phone number.
8. Generate constructors, property getters, and setters for the entity or domain object (e.g., "getName" and "setName" for the name property).
9. Utilize classes from the Java Jackson library for JSON manipulation.
10. Include unit tests for each architectural layer (Controller, Service, Repository) as appropriate.
11. Include an integration test if there are multiple architectural layers.
12. Include import statements in all code files.
14. Add any required annotations to the main application class (class with the @SpringBootApplication annotation).
15. Ensure the sample code resembles that available in the GitHub repositories of the organization located at https://github.com/spring-guides.
16. When generating markdown for code blocks in the final response, include an appropriate value for the "info" field.
17. If the application uses Spring Security, include the following dependency artifact IDs, spring-boot-starter-security and spring-security-test
18. Provide application.properties file with sample configurations for the application
`

export const systemBoot3Prompt = `For JPA related applications, ensure that the following changes are made to the code:
- The 'javax' package has been replaced with 'jakarta' package. All JPA-related imports should use 'jakarta.persistence' instead of 'javax.persistence'.
- Below is an example of entity class using 'jakarta.persistence' package:

"""
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phoneNumber; 

    // getters and setters
}
"""

- IMPORTANT: The coordinates of the MySQL JDBC driver have changed from "mysql:mysql-connector-java" to "com.mysql:mysql-connector-j". If you are using the MySQL JDBC driver, update its coordinates accordingly.
Only when the generated code requires a MySQL JDBC driver, the MySQL JDBC driver should be "com.mysql:mysql-connector-j". This is a change from the older "mysql:mysql-connector-java".

Please ensure to use the "com.mysql:mysql-connector-j" MySQL JDBC driver in pom.xml. Here is the maven dependency to be used in this case:
\`\`\`
<!-- MySQL Connector -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
</dependency>
\`\`\`
`

export const systemBoot2Prompt = `
- All JPA-related imports should use 'javax.persistence'
- Below is an example of entity class using 'javax.persistence' package:

"""
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phoneNumber;

    // getters and setters
"""
`
