# VS Code Language Server for Spring Boot Application Properties

VSCode extension and Language Server providing support for working with Spring Boot apps in Java.

# Usage:

The extension will automatically activate when you edit files with the following
name patterns:

 - `*.java` => activates support for Java files

# Functionality

## Navigating the source code - Go to symbol in file/workspace
Easy navigation to Spring-specific elements of your source code.

![Go to Symbol in workspace][screenshot-navigation]

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

![accessing running apps quickly][screenshot-live-apps-quick-access]

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

![live data from running apps as hover on source code][screenshot-live-hovers]

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
* `@RequestMapping`

## Smart code completions
Additional code completions for Spring-specific annotations

![Smart code completion for boot properties][screenshot-code-completion]

### Examples
* `@Value`: code completion for Spring Boot property keys
* `@Scope`: code completion for standard scope names

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
