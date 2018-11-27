/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.commons;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension5;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageClientImpl;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.springframework.tooling.jdt.ls.commons.Logger;
import org.springframework.tooling.jdt.ls.commons.classpath.ReusableClasspathListenerHandler;
import org.springframework.tooling.jdt.ls.commons.javadoc.JavadocResponse;
import org.springframework.tooling.jdt.ls.commons.javadoc.JavadocUtils;
import org.springframework.tooling.ls.eclipse.commons.javadoc.JavaDoc2MarkdownConverter;
import org.springframework.tooling.ls.eclipse.commons.preferences.PreferenceConstants;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

@SuppressWarnings("restriction")
public class STS4LanguageClientImpl extends LanguageClientImpl implements STS4LanguageClient {

	private static ReusableClasspathListenerHandler classpathService = new ReusableClasspathListenerHandler (
			Logger.forEclipsePlugin(LanguageServerCommonsActivator::getInstance),
			new LSP4ECommandExecutor(),
			() -> new ProjectSorter()
	);

	private static final String ANNOTION_TYPE_ID = "org.springframework.tooling.bootinfo";

	private static final String ALT_ANNOTATION_DRAWING_STRATEGY_ID = "boot.hint.strategy";
	private static final String ALT_ANNOTATION_TYPE_ID = "org.springframework.tooling.bootinfoCodeLens";

	/**
	 * Latest highlight request params. It is sufficient to only remember the last request per uri, because
	 * each new request is expected to replace the previous highlights.
	 */
	static final Map<String, HighlightParams> currentHighlights = new ConcurrentHashMap<>();

	/**
	 * Current markers... indexed per document uri, needed sp we to be removed upon next update.
	 */
	private static Map<String, Annotation[]> currentAnnotations = new ConcurrentHashMap<>();

	private static final IDrawingStrategy BOOT_RANGE_HIGHLIGHT_DRAWING_STRATEGY = new IDrawingStrategy() {

		@Override
		public void draw(Annotation annotation, GC gc, StyledText textWidget, int offset, int length, Color color) {

			if (gc == null) {
				textWidget.redrawRange(offset, length, true);
			} else {
				int oldAlpha = gc.getAlpha();
				Font oldFont = gc.getFont();

				Point left= textWidget.getLocationAtOffset(offset);
				Point right = textWidget.getLocationAtOffset(offset + length);
				gc.setFont(textWidget.getFont());
				int fontHeight = gc.getFontMetrics().getHeight();
				Rectangle r = new Rectangle(left.x, left.y + textWidget.getLineHeight(offset) - fontHeight, right.x - left.x, fontHeight);
				gc.setAlpha(0x40);
				gc.setBackground(color);
				gc.fillRectangle(r);

				gc.setAlpha(oldAlpha);
				gc.setFont(oldFont);
			}
		}

	};

	static class UpdateHighlights extends UIJob {

		private String target;

		UpdateHighlights(String target) {
			super("Update highlights");
			this.target = target;
			setSystem(true);
			schedule();
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			Utils.getActiveEditors().forEach(this::updateSourceViewer);
			return Status.OK_STATUS;
		}

		protected void updateSourceViewer(IEditorPart editor) {
			ITextViewer viewer = editor.getAdapter(ITextViewer.class);
			if (viewer instanceof ISourceViewer) {
				ISourceViewer sourceViewer = (ISourceViewer) viewer;
				IAnnotationModel annotationModel = sourceViewer.getAnnotationModel();
				if (annotationModel != null) {
					IDocument doc = sourceViewer.getDocument();
					if (doc != null && sourceViewer != null) {
						if (target != null) {
							HighlightParams highlightParams = currentHighlights.get(target);
							if (Utils.isProperDocumentIdFor(doc, highlightParams.getDoc())) {
								updateHighlightAnnotations(editor, sourceViewer, annotationModel, target);
							}
						} else {
							URI uri = Utils.findDocUri(doc);
							if (uri != null) {
								updateHighlightAnnotations(editor, sourceViewer, annotationModel, uri.toString());
							}
						}
					}
				}
			}
		}

	};

	private static void updateHighlightAnnotations(IEditorPart editor, ISourceViewer sourceViewer,
			IAnnotationModel annotationModel, String docUri) {
		if (annotationModel instanceof IAnnotationModelExtension) {
			if (isCodeLensHighlightOn()) {
				addBootRangeHighlightSupport(editor, sourceViewer);
			}
			updateAnnotations(docUri, sourceViewer, (IAnnotationModelExtension) annotationModel);
		}
		if (sourceViewer instanceof ISourceViewerExtension5) {
			((ISourceViewerExtension5) sourceViewer).updateCodeMinings();
		}
	}

	@SuppressWarnings("unchecked")
	private static void addBootRangeHighlightSupport(IEditorPart editor, ISourceViewer sourceViewer) {
		try {
			Field f = AbstractDecoratedTextEditor.class.getDeclaredField("fSourceViewerDecorationSupport");
			f.setAccessible(true);
			SourceViewerDecorationSupport support = (SourceViewerDecorationSupport) f.get(editor);
			f = SourceViewerDecorationSupport.class.getDeclaredField("fAnnotationPainter");
			f.setAccessible(true);
			AnnotationPainter painter = (AnnotationPainter) f.get(support);

			f = AnnotationPainter.class.getDeclaredField("fAnnotationType2Color");
			f.setAccessible(true);
			Color highlightColor = LanguageServerCommonsActivator.getInstance().getBootHighlightRangeColor();
			if (highlightColor!=null && ((Map<Object, Color>)f.get(painter)).get(ALT_ANNOTATION_TYPE_ID) != highlightColor) {
				painter.setAnnotationTypeColor(ALT_ANNOTATION_TYPE_ID, highlightColor);
				painter.addDrawingStrategy(ALT_ANNOTATION_DRAWING_STRATEGY_ID, BOOT_RANGE_HIGHLIGHT_DRAWING_STRATEGY);
				painter.addAnnotationType(ALT_ANNOTATION_TYPE_ID, ALT_ANNOTATION_DRAWING_STRATEGY_ID);
			}

		} catch (Exception e) {
			LanguageServerCommonsActivator.logError(e,
					"Failed to contribute alternative range highlight annotation. Switch off highlight CodeLense under STS Language Server preferences!");
		}
	}

	private static boolean isCodeLensHighlightOn() {
		IPreferenceStore store = LanguageServerCommonsActivator.getInstance().getPreferenceStore();
		return store.getBoolean(PreferenceConstants.HIGHLIGHT_CODELENS_PREFS);
	}

	static synchronized void updateAnnotations(String target, ISourceViewer sourceViewer,
			IAnnotationModelExtension annotationModel) {
		Annotation[] toRemove = currentAnnotations.get(target);
		if (toRemove == null) {
			toRemove = new Annotation[0];
		}
		HighlightParams highlightParams = currentHighlights.get(target);
		List<CodeLens> highlights = highlightParams == null ? null : highlightParams.getCodeLenses();
		String annotationType = isCodeLensHighlightOn() ? ALT_ANNOTATION_TYPE_ID : ANNOTION_TYPE_ID;
		Map<Annotation, Position> newAnnotations = createAnnotations(sourceViewer.getDocument(), highlights, annotationType);
		annotationModel.replaceAnnotations(toRemove, newAnnotations);
		currentAnnotations.put(target, newAnnotations.keySet().toArray(new Annotation[newAnnotations.size()]));
	}

	private static Map<Annotation, Position> createAnnotations(IDocument doc, List<CodeLens> highlights, String annotationType) {
		ImmutableMap.Builder<Annotation, Position> annotations = ImmutableMap.builder();
		if (highlights==null) {
			highlights = ImmutableList.of();
		}
		highlights.stream().map(CodeLens::getRange).distinct().forEach(rng -> {
			try {
				int start = LSPEclipseUtils.toOffset(rng.getStart(), doc);
				int end = LSPEclipseUtils.toOffset(rng.getEnd(), doc);
				Position e_rng = new Position(start, Math.max(0, end-start));
				annotations.put(new Annotation(annotationType, false, null), e_rng);
			} catch (BadLocationException e) {
				//ignore invalid highlights
			}
		});
		return annotations.build();
	}

	@Override
	public synchronized void highlight(HighlightParams highlights) {
		String target = highlights.getDoc().getUri();
		if (target!=null) {
			currentHighlights.put(target, highlights);
			new UpdateHighlights(target);
		}
	}

	@Override
	public void progress(ProgressParams progressEvent) {
		String status = progressEvent.getStatusMsg() != null ? progressEvent.getStatusMsg() : "";
		showStatusMessage(status);
	}

	private void showStatusMessage(final String status) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				IStatusLineManager statusLineManager = getStatusLineManager();
				if (statusLineManager != null) {
					statusLineManager.setMessage(status);
				}
			}
		});
	}

	private IStatusLineManager getStatusLineManager() {
		try {
			return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorSite().getActionBars().getStatusLineManager();
		}
		catch (NullPointerException e) {
			return null;
		}
	}

	@Override
	public CompletableFuture<Object> addClasspathListener(ClasspathListenerParams params) {
		return CompletableFuture.completedFuture(classpathService.addClasspathListener(params.getCallbackCommandId()));
	}

	@Override
	public CompletableFuture<Object> removeClasspathListener(ClasspathListenerParams params) {
		return CompletableFuture.completedFuture(classpathService.removeClasspathListener(params.getCallbackCommandId()));
	}

	@Override
	public CompletableFuture<JavadocResponse> javadoc(JavadocParams params) {
		JavadocResponse response = new JavadocResponse();
		try {
			String content = JavadocUtils.javadoc(JavaDoc2MarkdownConverter::getMarkdownContentReader,
					URI.create(params.getProjectUri()), params.getBindingKey());
			response.setContent(content);
		} catch (Exception e) {
			LanguageServerCommonsActivator.logError(e, "Failed getting javadoc for " + params.toString());
		}
		return CompletableFuture.completedFuture(response);
	}

	@Override
	public CompletableFuture<Object> moveCursor(CursorMovement cursorMovement) {
		System.err.println("moveCursor request received: "+cursorMovement);
		Utils.getActiveEditors().forEach(_editor -> {
			try {
				if (_editor instanceof AbstractTextEditor) {
					AbstractTextEditor editor = (AbstractTextEditor) _editor;
					IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
					if (doc!=null) {
						URI uri = Utils.findDocUri(doc);
						if (cursorMovement.getUri().equals(uri.toString())) {
							new UIJob("Move cursor") {
								{
									setSystem(true);
								}
								@Override
								public IStatus runInUIThread(IProgressMonitor arg0) {
									try {
										org.eclipse.lsp4j.Position pos = cursorMovement.getPosition();
										//Careful, it seems like the computation of offset only works correctly
										// when called from UIJob. Otherwise it is likely to be using stale data
										// not yet accounting for the most recent edits that may have been applied
										// to the document.
										int offset = LSPEclipseUtils.toOffset(pos, doc);
										editor.getSelectionProvider().setSelection(new TextSelection(offset, 0));
									} catch (Exception e) {
										LanguageServerCommonsActivator.logError(e, "sts/moveCursor failed");
									}
									return Status.OK_STATUS;
								}
							}.schedule();
						}
					}
				}
			} catch (Exception e) {
				LanguageServerCommonsActivator.logError(e, "sts/moveCursor failed");
			}
		});
		return CompletableFuture.completedFuture("ok");
	}

}
