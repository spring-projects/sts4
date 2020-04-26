/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package org.spring.tools.boot.java.ls

import java.awt.Color

import com.github.gtache.lsp.client.LanguageClientImpl
import com.github.gtache.lsp.client.languageserver.wrapper.LanguageServerWrapper
import com.github.gtache.lsp.utils.FileUtils
import com.intellij.codeInsight.daemon.impl.HintRenderer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.markup._
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification
import org.eclipse.lsp4j.services.LanguageServer

import scala.collection.JavaConversions
import scala.collection.mutable.ListBuffer

/**
  * @author Alex Boyko
  */
class StsLanguageClientImpl extends LanguageClientImpl {

  private var serverWrapper: LanguageServerWrapper = _
  private var rangeHighlights = scala.collection.mutable.Map[String, ListBuffer[RangeHighlighter]]()
  private var allInLays = scala.collection.mutable.Map[String, ListBuffer[Inlay[HintRenderer]]]()

  private val LOGGER: Logger = Logger.getInstance(StsLanguageClientImpl.getClass)

  override def connect(server: LanguageServer, wrapper: LanguageServerWrapper): Unit = {
    super.connect(server, wrapper)
    this.serverWrapper = wrapper
  }

  @JsonNotification("sts/highlight")
  def publishHints(params: HighlightParams): Unit = {
    LOGGER.debug("Highlight MSG")
    val uri = FileUtils.sanitizeURI(params.getDoc.getUri)
    val editorManager = this.serverWrapper.getEditorManagerFor(uri)
    if (editorManager == null || editorManager.editor == null || editorManager.editor.getDocument == null) {
      return
    }
    val doc = editorManager.editor.getDocument
    // Similar to Eclipse UI thread execution
      ApplicationManager.getApplication.invokeLater(new Runnable {
      override def run(): Unit = {
        if (rangeHighlights.get(uri).isDefined) {
          rangeHighlights.get(uri).get.foreach(h => editorManager.editor.getMarkupModel.removeHighlighter(h))
        }

        if(allInLays.get(uri).isDefined) {
          allInLays.get(uri).get.foreach(i => i.dispose())
        }

        val highlighters: ListBuffer[RangeHighlighter] = ListBuffer()
        val inLays: ListBuffer[Inlay[HintRenderer]] = ListBuffer()
        for (l <- JavaConversions.asScalaBuffer(params.getCodeLenses)) {
          val r: Range = l.getRange

          val startOffset = doc.getLineStartOffset(r.getStart.getLine) + r.getStart.getCharacter
          val endOffset = doc.getLineStartOffset(r.getEnd.getLine) + r.getEnd.getCharacter
          val attrs = new TextAttributes()
          attrs.setEffectType(EffectType.BOXED)
          attrs.setEffectColor(StsLanguageClientImpl.SPRING_BOOT_HINT_COLOR)
          attrs.setErrorStripeColor(StsLanguageClientImpl.SPRING_BOOT_HINT_COLOR)
          val highlighter = editorManager.editor.getMarkupModel.addRangeHighlighter(startOffset, endOffset, HighlighterLayer.ERROR, attrs, HighlighterTargetArea.EXACT_RANGE)
          highlighter.setGutterIconRenderer(SpringBootGutterIconRenderer.INSTANCE)
          highlighters += highlighter

          if(l.getData != null) {
            val inlineRenderer = new HintRenderer(l.getData.toString);
            inLays += editorManager.editor.getInlayModel.addInlineElement(endOffset, inlineRenderer)
          }
        }
        rangeHighlights += Tuple2(uri, highlighters)
        allInLays += Tuple2(uri, inLays)
      }
    })
  }

  @JsonNotification("sts/progress")
  def progress(progressEvent: ProgressParams): Unit = {
    LOGGER.debug("Progress MSG")
  }

}

object StsLanguageClientImpl {
  val SPRING_BOOT_HINT_COLOR = new Color(0x32, 0xBA, 0x56)
}
