# VS Code Language Server for Spring Boot Application Properties

VSCode extension and Language Server providing support for editing `application.properties` 
and `application.yml` files containing Spring Boot configuration properties.

## Installation:

Currently this plugin is not distributed via vscode marketplace. You can only install
it from a .vsix file. You can download it here:

   TODO: add link

To install it:

- open vscode. 
- press `CTRL-SHIFT-P` and search for VSIX
- select `Extension: Install from VSIX`

## Usage:

The extension will automatically activate when you edit files with the following
name patterns:

 - `application*.properties` => activates support for .properties file format.
 - `application*.yml` => activates support for .yml file format.

# Developer notes

## Bulding and Running

This project consists of three pieces:

 - a vscode-extension which is a language-server client implemented in TypeScript.
 - commons-vscode: a local npm module with some utilities implemented in TypeScript.
 - a language server implemented in Java.

To build all these pieces you normally only need to run:

   npm install

**However, the first time you build** it might fail trying to
find the `commons-vscode` module on npm central. Once we publish a stable 
version of that module on npm central that will no longer be a problem. 
Until that time, you can work around this by doing a one time manual 
run of the `preinstall` script prior to running `npm install`:

    ./scripts/preinstall.sh
    npm install

Now you can open the client-app in vscode. From the root of this project.

    code .

To launch the language server in a vscode runtime, press F5.

## Debugging

To debug the language server, open `lib/Main.ts` and edit to set the
`DEBUG` option to `true`. When you laucnh the app next by pressing
`F5` it will launch with debug options being passed to the JVM.

You can then connect a 'Remote Java' Eclipse debugger on port 8000.

## Packaging as a vscode extension

First make sure the stuff is all built locally:

     ./scripts/preinstall.sh  # only needed if this is the first build.
     npm install

Then package it:

     npm run vsce-package

This produces a `.vsix` file which you can install directly into vscode.