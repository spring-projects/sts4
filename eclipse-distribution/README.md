# eclipse distribution
STS 4 provides a ready to use distribution of Eclipse, equipped with all the necessary extensions and plugins that are useful to develop Spring Boot apps.
The necessary product and build definitions are included here.

## run the snapshot build
To run the distribution builds locally:

`mvn -Pe412 -Psnapshot clean package`

- `-Pe412` defines the eclipse platform version

This will build the full distributions for Windows, macOS, and Linux. The final products can be found in `eclipse-distribution/org.springframework.boot.ide.product.e412/target/products/org.springframework.boot.ide.branding.sts4/`

This consumes the language servers from the nightly update site as defined here: https://github.com/spring-projects/sts4/blob/master/eclipse-distribution/pom.xml#L81

