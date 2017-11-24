# Atom package for Spring Boot projects

[![macOS Build Status](https://travis-ci.org/spring-projects/atom-boot-java.svg?branch=master)](https://travis-ci.org/spring-projects/atom-boot-java) [![Windows Build Status](https://ci.appveyor.com/api/projects/status/1jvknxt9jhykgrxo?svg=true)](https://ci.appveyor.com/project/spring-projects/atom-boot-java/branch/master) [![Dependency Status](https://david-dm.org/spring-projects/atom-boot-java.svg)](https://david-dm.org/spring-projects/atom-boot-java)

Atom package and Language Server providing support for working with Spring Boot apps in Java.

# Usage:

The extension will automatically activate when you edit files with the following
name patterns:

 - `*.java` => activates support for Java files

# Functionality

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
* `RequestMapping`: show information about the request mapping for running apps

### Configuration
You can enable/disable this feature via workspace preferences by adding the following:
```
  "boot-java":
    "boot-hints":
      on: false

```

## Smart code completions
Additional code completions for Spring-specific annotations

![Smart code completion for boot properties][screenshot-code-completion]

### Examples
* `@Value`: code completion for Spring Boot property keys
* `@Scope`: code completion for standard scope names

# Releases:

Released versions of this package can be installed directly from the Atom package installer.

There are also development snapshots available with the latest fixes and improvements as a `.tgz` file 
that can be donwloaded from 
[here](http://dist.springsource.com/snapshot/STS4/nightly-distributions.html). To install it:
1. Unpack into a folder `<package>` (this folder should have `package.json` file)
2. Execute `apm link .` from that folder
3. Execute `Window Reload` (Package -> Command Palette -> Toggle then search for it) command in the opened Atom instance or open an Atom instance

[screenshot-live-hovers]: https://raw.githubusercontent.com/spring-projects/sts4/112106b8bfdcebc33bb7923dda496f7a91fa93d8/atom-extensions/atom-boot-java/readme-imgs/screenshot-live-hovers.png
[screenshot-code-completion]: https://github.com/spring-projects/sts4/blob/112106b8bfdcebc33bb7923dda496f7a91fa93d8/atom-extensions/atom-boot-java/readme-imgs/screenshot-code-completion.png
