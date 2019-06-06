# eclipse distribution
STS 4 provides a ready to use distribution of Eclipse, equipped with all the necessary extensions and plugins that are useful to develop Spring Boot apps.
The necessary product and build definitions are included here.

## run the snapshot build
To run the distribution builds locally:

`mvn -Pe412 -Psnapshot clean package`

- `-Pe412` defines the eclipse platform version

This consumes the language servers from the nightly update site as defined here: https://github.com/spring-projects/sts4/blob/master/eclipse-distribution/pom.xml#L81

