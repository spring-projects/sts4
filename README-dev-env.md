# STS4 - Dev Environment Setup

## Running the STS4 Boot Java Language Server locally

Import the headless-services modules into your workspace as existing Maven projects.
Startup the boot java language server with:
Main Class: org.springframework.ide.vscode.boot.java.Main
Vmargs: -Dstandalone-startup=true
This starts up the language server and it listens on port 5007
(this is implemented in LaunguageServerApp)

@ Connect local STS4 Boot Java LS to VSCode

You should run the extension from vscode as a 'runtime workbench' with some small changes.

Start by opening the extension for the server you want to work on in vscode:

```
$ cd vscode-extensions/vscode-concourse
$ code .
```

Open `Main.ts` and change:
```
CONNECT_TO_LS: true
```

Save, then launch by pressing `F5`.

This connects the VSCode extension to the locally running language server.
It also uses port 5007

## Connect local STS4 Boot Java LS to Eclipse runtime workbench

Startup Eclipse or STS (3). I.e something that support for RCP / plugin development.

Define a recent nightly STS 4 build as your target platform.

Import the projects under `eclipse-language-servers` into your workspace.

Startup a runtime workbench of the Eclipse STS4 distribution
The language server plugin is: org.springframework.tooling.boot.java.ls
Add Vmargs: -Dboot-java-ls-port=5007
This lets the Eclipse language server extension connect to the locally running language server on port 5007

## Additional Resources:

- Generic editor source code (used by lsp4e as a dependency): https://github.com/eclipse/eclipse.platform.text
  Not sure which version of this you should checkout, it probably depends on your version of Eclipse as this is part
  of the eclipse platform. 
- LSP4E source code: clone gir repo and import: https://git.eclipse.org/r/lsp4e/lsp4e and import the projects into your workspace.
- LSP4J source code: Add the LSP4J-SDK feature to your target platform. We have usually versions from this in our builds:
         http://services.typefox.io/open-source/jenkins/job/lsp4j/job/master/lastStableBuild/artifact/build/p2-repository/

- There is an update site that contains our latest language servers as extensions for Eclipse:
  https://dist.springsource.com/snapshot/TOOLS/sts4-language-server-integrations/nightly
  This is being produced by:
  https://build.spring.io/browse/IDE-CODE

- CI builds of the full STS4 distribution can be downloaded from here:
  https://dist.springsource.com/snapshot/STS4/nightly-distributions.html



