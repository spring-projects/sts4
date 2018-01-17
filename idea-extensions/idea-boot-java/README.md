# Spring Boot Java Support for IntelliJ IDEA (_Experimental_)
The support is based on IntelliJ LSP client plugin https://github.com/gtache/intellij-lsp

## Dev Instructions
1. Clone and import the project into IntelliJ. ()Project is Scala based hence accept whatever IntelliJ suggests about having Scala)
2. Build `boot-java-language-server` maven project from STS4 (https://github.com/spring-projects/sts4/tree/master/headless-services/boot-java-language-server)
3. Copy the built `jar` file into this project `/resources/server` folder and name it `language-server.jar`
4. Install `LSP Support` plugin into IntelliJ as explained here: https://www.jetbrains.com/help/idea/installing-updating-and-uninstalling-repository-plugins.html
5. For some reason it's not enough for the dev env, hence also download the same plugin from https://www.jetbrains.com/help/idea/installing-updating-and-uninstalling-repository-plugins.html
6. Unzip the downloaded file and copy the folder into IntelliJ `pligins` folder
7. Select `idea-boot-java` project in IntelliJ go to `File -> Project Structure...` and then `Platform Settings -> SDKs`
8. Select `IntelliJ IDEA` in the middle pane. Find `+` in the bottom left corner of the right-most pane and click on it (list of jars within IntelliJ SDK)
9. Find all `Jar`s under the `IntelliJ/plugins/LSP` folder, select all of them and press `Open` in the dialog. This will ensure that your target platform has necessary LSP jars
10. Click on `Run -> Run...`, create new `Plugin` launch config. Select `Use classpath of module` to be `idea-boot-java`, select `JRE` to the `IntelliJ IDEA` SDK from step 8
11. Run the configuration. This should start IntelliJ runtime workbench where `idea-boot-java` LS plugin is available