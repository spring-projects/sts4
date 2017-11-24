# VS Code Language Server for Spring Boot Application Properties

VSCode extension and Language Server providing support for working with Spring Boot 
`application.properties` and `application.yml` files.

# Usage:

The extension will automatically activate when you edit files with the following
name patterns:

 - `application*.java` => activates support for Spring Boot properties in `.properties`format.
 - `application*.yml` =>  activates support for Spring Boot properties in `.yml` format.

You can also define your own patterns and map them to the language-ids
`spring-boot-properties` or `spring-boot-properties-yaml` by defining `files.associations`
in workspace settings.  See [vscode documentation](https://code.visualstudio.com/Docs/languages/overview#_adding-a-file-extension-to-a-language) for details.

# Functionality

This extension analyzes your project's classpath and parses and indexes any [Spring Boot
Properties Metadata](https://docs.spring.io/spring-boot/docs/current/reference/html/configuration-metadata.html) it finds. Both Maven and Gradle projects are supported.

The data in the index is used to provide validation, code completions and information
hovers while editing Spring Boot Properties in either `.properties` or `.yml` format.

## Validation

![application-yaml-validation][yaml-validation]

![application-properties-validation][yaml-validation]

## Code Completions

![application-yaml-completions][yaml-validation]

![application-properties-completions][yaml-validation]

## Information Hovers

![application-yaml-hovers][yaml-validation]

## Issues and Feature Requests

Please report bugs, issues and feature requests on the [Github STS4 issue tracker](https://github.com/spring-projects/sts4/issues). 

# Releases:

Released versions of this extension can be installed directly from the vscode marketplace.

There are also development snapshots available with the latest fixes and improvements as a `.vsix` file 
that can be donwloaded from 
[here](http://dist.springsource.com/snapshot/STS4/nightly-distributions.html). To install it
open vscode, press `CTRL-SHIFT-P` and search for VSIX, then select `Extension: Install from VSIX`

[screenshot-code-completion]: https://github.com/spring-projects/sts4/raw/ff066560158e50cd9c7e4a9ff0b84d47c84c9d14/vscode-extensions/vscode-boot-java/readme-imgs/screenshot-code-completion.png
[screenshot-live-apps-quick-access]: https://github.com/spring-projects/sts4/raw/ff066560158e50cd9c7e4a9ff0b84d47c84c9d14/vscode-extensions/vscode-boot-java/readme-imgs/screenshot-live-apps-quick-access.png
[screenshot-live-hovers]: https://github.com/spring-projects/sts4/raw/ff066560158e50cd9c7e4a9ff0b84d47c84c9d14/vscode-extensions/vscode-boot-java/readme-imgs/screenshot-live-hovers.png
[screenshot-navigation]: https://github.com/spring-projects/sts4/raw/ff066560158e50cd9c7e4a9ff0b84d47c84c9d14/vscode-extensions/vscode-boot-java/readme-imgs/screenshot-navigation.png
