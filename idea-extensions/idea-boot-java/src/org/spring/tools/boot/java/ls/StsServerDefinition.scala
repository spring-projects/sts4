package org.spring.tools.boot.java.ls

import com.github.gtache.lsp.client.LanguageClientImpl
import com.github.gtache.lsp.client.languageserver.serverdefinition.{CommandServerDefinition, RawCommandServerDefinition, UserConfigurableServerDefinitionObject}

/**
  * @author Alex Boyko
  */
class StsServerDefinition(override val ext: String, override val command: Array[String]) extends RawCommandServerDefinition(ext, command) {

  override def createLanguageClient: LanguageClientImpl = new StsLanguageClientImpl

}
