package org.spring.tools.boot.java.ls

import java.awt.Color

import com.github.gtache.lsp.client.LanguageClientImpl
import com.github.gtache.lsp.client.languageserver.wrapper.LanguageServerWrapper
import com.github.gtache.lsp.editor.EditorEventManager
import com.github.gtache.lsp.utils.FileUtils
import com.intellij.openapi.application.ApplicationManager
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

  override def connect(server: LanguageServer, wrapper: LanguageServerWrapper): Unit = {
    super.connect(server, wrapper)
    this.serverWrapper = wrapper
  }

  @JsonNotification("sts/highlight")
  def publishHints(params: HighlightParams): Unit = {
    println("Highlight MSG")
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
        val highlighters: ListBuffer[RangeHighlighter] = ListBuffer()
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
        }
        rangeHighlights += Tuple2(uri, highlighters)
      }
    })
  }

  @JsonNotification("sts/progress")
  def progress(progressEvent: ProgressParams): Unit = {
    println("Progress MSG")
  }

}

object StsLanguageClientImpl {
  val SPRING_BOOT_HINT_COLOR = new Color(0x32, 0xBA, 0x56)
}
