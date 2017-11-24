# Atom package for Spring Boot Application Properties

[![macOS Build Status](https://travis-ci.org/spring-projects/atom-boot-properties.svg?branch=master)](https://travis-ci.org/spring-projects/atom-boot-properties) [![Windows Build Status](https://ci.appveyor.com/api/projects/status/1jvknxt9jhykgrxo?svg=true)](https://ci.appveyor.com/project/spring-projects/atom-boot-properties/branch/master) [![Dependency Status](https://david-dm.org/spring-projects/atom-boot-properties.svg)](https://david-dm.org/spring-projects/atom-boot-properties)

Atom package and Language Server providing support for editing `application.properties` 
and `application.yml` files containing Spring Boot configuration properties.

## Installation:

Install it from Atom `Install Packages`, search for `boot-properties` 

## Usage:

The extension will automatically activate when you edit files with the following
name patterns:

 - `application.properties` => activates support for .properties file format.
 - `application.yml` => activates support for .yml file format.
 
For all other files select grammar to be `Spring-Boot-Properties` for `properties` file format or `Spring-Boot-Properties-YAML` for 'YAML' file format

# Developer notes

## Bulding and Running

To build all these pieces you normally only need to run:

    npm install

Now you can link it to Atom:

    apm link .

Open Atom or execute `Refresh Window` in the opened instance (Packages -> Command Palette -> Toggle then search for the command).

## Debugging

**Client Side Debugging**: Open Atom's `Developer Tools` view - View -> Developer -> Toggle Developer Tools

**Server Side Debugging**:  Change `launchVmArgs(version)` implementation in `lib/main.js` to be:
```
    launchVmArgs(version) {
        return Promise.resolve([
            '-Xdebug',
            '-agentlib:jdwp=transport=dt_socket,server=y,address=7999,suspend=n',
            '-Dorg.slf4j.simpleLogger.logFile=boot-properties.log',
            '-Dorg.slf4j.simpleLogger.defaultLogLevel=debug',
        ]);
    }

```
(Then setup the remote debugger from your IDE on the same port `7999`)

