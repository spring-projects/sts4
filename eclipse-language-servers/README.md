# Eclipse plugins for the language server

The purpose of these Eclipse plugin projects is to embed and integrate the language server into the Eclipse IDE (by using the LSP4E integration for Eclipse).

## development environment setup

In order to develop and run those projects from your Eclipse IDE, you should:

- go to the directory `headless-services` and run the maven build via `mvn -DskipTests=true clean install`. This will build and install the language servers as maven artifacts.
- go to the directory `eclipse-language-servers` and run the maven build via `mvn -Pe411 clean package`. This will built the Eclipse plugins for the language servers and include the previously built language server artifacts in the Eclipse plugin projects.

Once you imported the Eclipse plugin projects into your workspace, you can just run a runtime workbench with those language servers installed without doing anything else.
