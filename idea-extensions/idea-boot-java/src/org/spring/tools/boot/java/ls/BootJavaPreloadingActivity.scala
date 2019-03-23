package org.spring.tools.boot.java.ls

import java.io.{BufferedReader, File, InputStreamReader}
import java.lang.ProcessBuilder
import java.net.URI
import java.nio.file.{Files, Paths}

import com.github.gtache.lsp.client.languageserver.serverdefinition.{LanguageServerDefinition, RawCommandServerDefinition}
import com.intellij.openapi.application.PreloadingActivity
import com.intellij.openapi.progress.ProgressIndicator

class BootJavaPreloadingActivity extends PreloadingActivity {

  override def preload(indicator: ProgressIndicator): Unit = {
    val javaHome: String = /*sys.env.get("java.home").get*/ System.getProperty("java.home")
    val javaVersion: String = /*sys.env.get("java.version").get*/ System.getProperty("java.version")

//    val v = sys.process.Process(Seq("java", "--version")).!!

    if (javaHome == null) {
      // TODO throw error?
      return
    }

    val javaPath = Paths.get(javaHome)

    val javaExec = javaPath.resolve(Paths.get("bin", if(isWindows()) "java.exe" else "java"))

    if (!Files.exists(javaExec)) {
      // TODO throw error?
      return
    }

    // search for the server jar near to jar file or class folder
    val root = new File(getClass().getResource("/").toURI.getPath)
    var classpath = new File(root.getParent, "/server/language-server.jar").getPath()

    if (javaVersion.startsWith("1.8")) {
        var toolsJar = javaPath.resolve(Paths.get("lib", "tools.jar"))
        if (Files.exists(toolsJar)) {
          classpath += ":" + toolsJar
        } else {
          toolsJar = javaPath.resolve(Paths.get("..", "lib", "tools.jar"))
          if (Files.exists(toolsJar)) {
            classpath += ":" + toolsJar
          } else {
            // TODO: tools.jar doesn't exist
          }
        }
    } else if (javaVersion.startsWith("9.")) {
      // all good - Java 9 has tools.jar on the classpath
    } else {
      // Error - not Java 8 or 9
    }

    LanguageServerDefinition.register(
      new StsServerDefinition("java", Array(
        javaExec.toString(),
        "-cp",
        classpath,
        "org.springframework.boot.loader.JarLauncher"
      )))
  }

  def isWindows(): Boolean = {
    return System.getProperty("os.name").toLowerCase().startsWith("win")
  }
}