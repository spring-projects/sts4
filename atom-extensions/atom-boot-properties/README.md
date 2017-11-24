# VS Code Language Server for Spring Boot Application Properties

Atom package and Language Server providing support for working with Spring Boot 
`application.properties` and `application.yml` files.

# Usage:

The package will automatically activate when you edit files with the following
name patterns:

 - `application.properties` => activates support for Spring Boot properties in `.properties`format.
 - `application.yml` =>  activates support for Spring Boot properties in `.yml` format.

For all other files select grammar to be `Spring-Boot-Properties` for `properties` file format or `Spring-Boot-Properties-YAML` for 'YAML' file format

# Functionality

This package analyzes your project's classpath and parses and indexes any [Spring Boot
Properties Metadata](https://docs.spring.io/spring-boot/docs/current/reference/html/configuration-metadata.html) it finds. Both Maven and Gradle projects are supported.

The data in the index is used to provide validation, code completions and information
hovers while editing Spring Boot Properties in either `.properties` or `.yml` format.

## Validation

![application-yaml-validation][yaml-validation]
![application-properties-validation][properties-validation]

## Code Completions

![application-yaml-completions][yaml-completion]

![application-properties-completions][properties-completion]

## Information Hovers

![application-yaml-hovers][yaml-hovers]

## Issues and Feature Requests

Please report bugs, issues and feature requests on the [Github STS4 issue tracker](https://github.com/spring-projects/sts4/issues). 

# Releases:

Released versions of this extension can be installed directly from the vscode marketplace.

There are also development snapshots available with the latest fixes and improvements as a `.tgz` file 
that can be donwloaded from 
[here](http://dist.springsource.com/snapshot/STS4/nightly-distributions.html). To install it:
1. Unpack into a folder `<package>` (this folder should have `package.json` file)
2. Execute `apm link .` from that folder
3. Execute `Window Reload` (Package -> Command Palette -> Toggle then search for it) command in the opened Atom instance or open an Atom instance

[yaml-completion]: https://github.com/spring-projects/sts4/blob/714c6bbb519f714ebba9f9fc5968ac2e619159f8/atom-extensions/atom-boot-properties/readme-imgs/yaml-completion.png
[properties-completion]: https://github.com/spring-projects/sts4/blob/714c6bbb519f714ebba9f9fc5968ac2e619159f8/atom-extensions/atom-boot-properties/readme-imgs/properties-completion.png
[yaml-validation]: https://github.com/spring-projects/sts4/blob/714c6bbb519f714ebba9f9fc5968ac2e619159f8/atom-extensions/atom-boot-properties/readme-imgs/yaml-validation.png
[properties-validation]: https://github.com/spring-projects/sts4/blob/714c6bbb519f714ebba9f9fc5968ac2e619159f8/atom-extensions/atom-boot-properties/readme-imgs/properties-validation.png
[yaml-hovers]: https://github.com/spring-projects/sts4/blob/714c6bbb519f714ebba9f9fc5968ac2e619159f8/atom-extensions/atom-boot-properties/readme-imgs/yaml-hovers.png
