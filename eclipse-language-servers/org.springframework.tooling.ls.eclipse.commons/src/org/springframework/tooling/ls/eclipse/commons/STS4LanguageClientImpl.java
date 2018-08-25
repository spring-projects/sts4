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
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension5;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageClientImpl;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
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

	public STS4LanguageClientImpl() {
	}

	private static final String ANNOTION_TYPE_ID = "org.springframework.tooling.bootinfo";

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
			Utils.getActiveSourceViewers().forEach(this::updateSourceViewer);
			return Status.OK_STATUS;
		}

		protected void updateSourceViewer(ISourceViewer sourceViewer) {
			IAnnotationModel annotationModel = sourceViewer.getAnnotationModel();
			if (annotationModel != null) {
				IDocument doc = sourceViewer.getDocument();
				if (doc != null && sourceViewer != null) {
					if (target != null) {
						HighlightParams highlightParams = currentHighlights.get(target);
						if (Utils.isProperDocumentIdFor(doc, highlightParams.getDoc())) {
							if (annotationModel instanceof IAnnotationModelExtension) {
								updateAnnotations(target, sourceViewer, (IAnnotationModelExtension) annotationModel);
							}
							if (sourceViewer instanceof ISourceViewerExtension5) {
								((ISourceViewerExtension5) sourceViewer).updateCodeMinings();
							}
						}
					} else {
						URI uri = Utils.findDocUri(doc);
						if (uri != null) {
							if (annotationModel instanceof IAnnotationModelExtension) {
								updateAnnotations(uri.toString(), sourceViewer, (IAnnotationModelExtension) annotationModel);
							}
							if (sourceViewer instanceof ISourceViewerExtension5) {
								((ISourceViewerExtension5) sourceViewer).updateCodeMinings();
							}
						}
					}
				}
			}
		}
	};

	/**
	 * Latest highlight request params. It is sufficient to only remember the last request per uri, because
	 * each new request is expected to replace the previous highlights.
	 */
	static final Map<String, HighlightParams> currentHighlights = new ConcurrentHashMap<>();

	/**
	 * Current markers... indexed per document uri, needed sp we to be removed upon next update.
	 */
	private static Map<String, Annotation[]> currentAnnotations = new ConcurrentHashMap<>();

	static synchronized void updateAnnotations(String target, ISourceViewer sourceViewer,
			IAnnotationModelExtension annotationModel) {
		Annotation[] toRemove = currentAnnotations.get(target);
		if (toRemove == null) {
			toRemove = new Annotation[0];
		}
		HighlightParams highlightParams = currentHighlights.get(target);
		IPreferenceStore store = LanguageServerCommonsActivator.getInstance().getPreferenceStore();
		List<CodeLens> highlights = store.getBoolean(PreferenceConstants.HIGHLIGHT_CODELENS_PREFS) || highlightParams == null ? null : highlightParams.getCodeLenses();
		Map<Annotation, Position> newAnnotations = createAnnotations(sourceViewer.getDocument(), highlights);
		annotationModel.replaceAnnotations(toRemove, newAnnotations);
		currentAnnotations.put(target, newAnnotations.keySet().toArray(new Annotation[newAnnotations.size()]));
	}

	private static Map<Annotation, Position> createAnnotations(IDocument doc, List<CodeLens> highlights) {
		ImmutableMap.Builder<Annotation, Position> annotations = ImmutableMap.builder();
		if (highlights==null) {
			highlights = ImmutableList.of();
		}
		highlights.stream().map(CodeLens::getRange).forEach(rng -> {
			try {
				int start = LSPEclipseUtils.toOffset(rng.getStart(), doc);
				int end = LSPEclipseUtils.toOffset(rng.getEnd(), doc);
				Position e_rng = new Position(start, Math.max(0, end-start));
				annotations.put(new Annotation(ANNOTION_TYPE_ID, false, null), e_rng);
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

}
