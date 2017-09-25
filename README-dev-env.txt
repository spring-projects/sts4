Setting up dev env:
-------------------

Meta note: We do not have comprehensive or good notes yet on how to setup dev env. For now I'm just dumping some info in here that
Martin sent around in an email message as its 'the best we have' at the moment.

The language server project is this one:
https://github.com/spring-projects/sts4/tree/master/headless-services/boot-java-language-server

It is implemented in the same way as the other ones. If you start it from within Eclipse, you can pass:
-Dstandalone-startup=true
as VM option to the app and it will launch in server mode, waiting for a client to connect to it (rather that the client starting up its own language server).

The VSCode extension is here:
https://github.com/spring-projects/sts4/tree/master/vscode-extensions/vscode-boot-java

You can switch the “CONNECT_TO_LS” flag in the Main.ts file to true and the VSCode extension will use the language server that you started from Eclipse (for example in debug mode) via the option mentioned above.

That gives you a pretty nice environment to run and debug the language server.
I would recommend to install the java language server extension from RedHat into VSCode, too.


If you start to work in the Eclipse STS4 client side, I recommend to use a distribution build from here as a target platform:
http://dist.springsource.com/snapshot/STS4/nightly-distributions.html

The important project for the boot java ls integration is:
https://github.com/spring-projects/sts4/tree/master/eclipse-language-servers/org.springframework.tooling.boot.java.ls

If you want to run the Eclipse runtime workbench so that it connects to the locally started language server (see above), you can pass these arguments as VM options:
-Dboot-java-ls-host=localhost -Dboot-java-ls-port=5007
That way the Eclipse runtime workbench will connect to your separate language server process instead of starting up its own.

I hope this gives you a nice getting started experience… :-)
Please do not hesitate to reach out to me anytime.

--- more pasted from google docs:

STS4 - Dev Environment Setup


Running the STS4 Boot Java Language Server locally
Import the headless-services modules into your workspace as existing Maven projects.
Startup the boot java language server with:
Main Class: org.springframework.ide.vscode.boot.java.Main
Vmargs: -Dstandalone-startup=true
This starts up the language server and it listens on port 5007
(this is implemented in LaunguageServerApp)

Connect local STS4 Boot Java LS to VSCode
Change the setting in Main.ts:
CONNECT_TO_LS: true
This connects the VSCode extension to the locally running language server.
It also uses port 5007

Connect local STS4 Boot Java LS to Eclipse runtime workbench
Startup a runtime workbench of the Eclipse STS4 distribution
The language server plugin is: org.springframework.tooling.boot.java.ls
Add Vmargs: -Dboot-java-ls-port=5007
This lets the Eclipse language server extension connect to the locally running language server on port 5007

Additional Resources
If you want to debug into LSP4E, clone the project from https://git.eclipse.org/r/lsp4e/lsp4e and import the projects into your workspace. That is definitely the easiest way.

If you want to debug into LSP4J code, you can add the LSP4J-SDK feature to your target platform. We have usually versions from this in our builds:
http://services.typefox.io/open-source/jenkins/job/lsp4j/job/master/lastStableBuild/artifact/build/p2-repository/


There is an update site that contains our latest language servers as extensions for Eclipse:
http://dist.springsource.com/snapshot/TOOLS/sts4-language-server-integrations/nightly
This is being produced by:
https://build.spring.io/browse/IDE-CODE

CI builds of the full STS4 distribution can be downloaded from here:
http://dist.springsource.com/snapshot/STS4/nightly-distributions.html



