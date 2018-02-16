# VS Code Language Server for Spring Boot Application Properties

VSCode extension and Language Server providing support for working with Spring Boot 
`application.properties`, `application.yml` and `.java` files.

# Usage:

The extension will automatically activate when you edit files with the following
name patterns:

 - `application*.properties` => activates support for Spring Boot properties in `.properties`format.
 - `application*.yml` =>  activates support for Spring Boot properties in `.yml` format.
 - `*.java` =>  activates Spring Boot specific support editing `.java` files.

You can also define your own patterns and map them to the language-ids
`spring-boot-properties` or `spring-boot-properties-yaml` by defining `files.associations`
in workspace settings.  See [vscode documentation](https://code.visualstudio.com/Docs/languages/overview#_adding-a-file-extension-to-a-language) for details.

# Functionality for `.java`

## Navigating the source code - Go to symbol in file/workspace
Easy navigation to Spring-specific elements of your source code.

![Go to Symbol in workspace][java-navigation]

### Shortcuts
* Mac: Cmd-Shift-O (symbols in file), Cmd-T (symbols in workspace)
* Linux/Windows: Ctrl-Shift-O (symbols in file), Ctrl-T (symbols in workspace)

### Examples
* `@/` shows all defined request mappings (mapped path, request method, source location)
* `@+` shows all defined beans (bean name, bean type, source location)
* `@>` shows all functions (prototype implementation)
* `@` shows all Spring annotations in the code

## Quick-access for running apps
Easy navigation to the provided request mappings of running apps.

![accessing running apps quickly][java-live-apps-quick-access]

### Shortcuts
* Mac: Cmd-Shift-O (symbols in file), Cmd-T (symbols in workspace)
* Linux/Windows: Ctrl-Shift-O (symbols in file), Ctrl-T (symbols in workspace)

### Examples
* `//` shows all request mappings of all running Spring Boot apps and opens a browser for the selected endpoint

## Live application information hovers
Show information from running Spring Boot apps on your machine in the source code. This allows you to run the Spring Boot app locally on your machine and visualizes information from those running apps in your source code.

### Visualization
Once the tooling detects a running Spring Boot app on your local machine, it automatically shows hints in the source code where data from the running app can be inspected. Then hovering over that area (with the mouse pointer), the data from the running app shows up.

If there are multiple instances of the app running on your machine, the live data from all those instances will show up in the hover information.

![live data from running apps as hover on source code][java-live-hovers]

### Examples
* `@Profile`: shows information about the active profiles on the running apps
* `@Component`, `@Bean`, `@Autowired`: shows detailed information about the beans and their wiring from the live app
* `@ContidionalOn...`: shows information about the conditions and their evaluation at runtime

### Configuration
You can enable/disable this feature via workspace or user preferences, 
using the key: `boot-java.boot-hints.on`.

## Code templates
Write Spring code with templates, available via regular code completion.

### Examples
* `@GetMapping`
* `@PostMapping`
* `@PutMapping`

## Smart code completions
Additional code completions for Spring-specific annotations

![Smart code completion for boot properties][java-code-completion]

### Examples
* `@Value`: code completion for Spring Boot property keys
* `@Scope`: code completion for standard scope names

# Functionality for `.properties` and `.yml`

This extension analyzes your project's classpath and parses and indexes any [Spring Boot
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

There are also development snapshots available with the latest fixes and improvements as a `.vsix` file 
that can be donwloaded from 
[here](http://dist.springsource.com/snapshot/STS4/nightly-distributions.html). To install it
open vscode, press `CTRL-SHIFT-P` and search for VSIX, then select `Extension: Install from VSIX`

[yaml-completion]: https://github.com/spring-projects/sts4/raw/5360ae4fabf9245da58f5897c54e9a14786d0622/vscode-extensions/vscode-boot-properties/readme-imgs/yaml-completion.png
[properties-completion]: https://github.com/spring-projects/sts4/raw/5360ae4fabf9245da58f5897c54e9a14786d0622/vscode-extensions/vscode-boot-properties/readme-imgs/properties-completion.png
[yaml-validation]: https://github.com/spring-projects/sts4/raw/5360ae4fabf9245da58f5897c54e9a14786d0622/vscode-extensions/vscode-boot-properties/readme-imgs/yaml-validation.png
[properties-validation]: https://github.com/spring-projects/sts4/raw/5360ae4fabf9245da58f5897c54e9a14786d0622/vscode-extensions/vscode-boot-properties/readme-imgs/properties-validation.png
[yaml-hovers]: https://github.com/spring-projects/sts4/raw/1d731ed1ad5c8defcca4e4abb3cf5f2d89daba43/vscode-extensions/vscode-boot-properties/readme-imgs/yaml-hover.png
[java-code-completion]: https://github.com/spring-projects/sts4/raw/facac2003191bc29bf79049aa02a091457ffbe47/vscode-extensions/vscode-spring-boot/readme-imgs/java-code-completion.png
[java-live-apps-quick-access]: https://github.com/spring-projects/sts4/raw/facac2003191bc29bf79049aa02a091457ffbe47/vscode-extensions/vscode-spring-boot/readme-imgs/java-live-apps-quick-access.png
[java-live-hovers]: https://github.com/spring-projects/sts4/raw/facac2003191bc29bf79049aa02a091457ffbe47/vscode-extensions/vscode-spring-boot/readme-imgs/java-live-hovers.png
[java-navigation]: https://github.com/spring-projects/sts4/raw/facac2003191bc29bf79049aa02a091457ffbe47/vscode-extensions/vscode-spring-boot/readme-imgs/java-navigation.png
