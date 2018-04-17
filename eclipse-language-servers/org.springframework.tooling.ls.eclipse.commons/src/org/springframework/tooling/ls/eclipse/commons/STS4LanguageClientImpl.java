/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.commons;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageClientImpl;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4e.LanguageServiceAccessor.LSPDocumentInfo;
import org.eclipse.lsp4j.Range;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.springframework.tooling.jdt.ls.commons.classpath.ReusableClasspathListenerHandler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;


@SuppressWarnings("restriction")
public class STS4LanguageClientImpl extends LanguageClientImpl implements STS4LanguageClient {

	private static ReusableClasspathListenerHandler classpathService = new ReusableClasspathListenerHandler(new LSP4ECommandExecutor());


	public STS4LanguageClientImpl() {
		System.out.println("Instantiatin STS4LanguageClientImpl");
	}

	private static final String ANNOTION_TYPE_ID = "org.springframework.tooling.bootinfo";

	class UpdateHighlights extends UIJob {

		private String target;

		UpdateHighlights(String target) {
			super("Update highlights");
			this.target = target;
			setSystem(true);
			schedule();
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			IWorkbenchWindow ww = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (ww!=null) {
				IWorkbenchPage page = ww.getActivePage();
				if (page!=null) {
					IEditorPart editorPart = page.getActiveEditor();
					if (editorPart instanceof AbstractTextEditor) {
						try {
							Method m = AbstractTextEditor.class.getDeclaredMethod("getSourceViewer");
							m.setAccessible(true);
							ISourceViewer sourceViewer = (ISourceViewer) m.invoke(editorPart);
							if (sourceViewer!=null) {
								IAnnotationModel annotationModel = sourceViewer.getAnnotationModel();
								if (annotationModel!=null) {
									IDocument doc = sourceViewer.getDocument();
									if (sourceViewer!=null) {
										if (doc!=null && annotationModel instanceof IAnnotationModelExtension) {
											updateAnnotations(target, doc, (IAnnotationModelExtension) annotationModel);
										}
									}
								}
							}
						} catch (Exception e) {
							//ignore reflection errors
						}
					}
				}
			}
			return Status.OK_STATUS;
		}
	};

	/**
	 * Latest highlight request params. It is sufficient to only remember the last request per uri, because
	 * each new request is expected to replace the previous highlights.
	 */
	private Map<String,  List<Range>> currentHighlights = new HashMap<>();

	/**
	 * Current markers... indexed per document uri, needed sp we to be removed upon next update.
	 */
	private Map<String, Annotation[]> currentAnnotations = new HashMap<>();

	private synchronized void updateAnnotations(String target, IDocument doc, IAnnotationModelExtension annotationModel) {
		if (target!=null) {
			Collection<LSPDocumentInfo> infos = LanguageServiceAccessor.getLSPDocumentInfosFor(doc, (x) -> true);
			for (LSPDocumentInfo docInfo : infos) {
				URI uri = docInfo.getFileUri();
				if (uri!=null && uri.toString().equals(target)) {
					Annotation[] toRemove = currentAnnotations.get(target);
					if (toRemove==null) {
						toRemove = new Annotation[0];
					}
					Map<Annotation, Position> newAnnotations = createAnnotations(doc, currentHighlights.get(target));
					annotationModel.replaceAnnotations(toRemove, newAnnotations);
					currentAnnotations.put(target, newAnnotations.keySet().toArray(new Annotation[newAnnotations.size()]));
				}
			}
		}
	}

	private Map<Annotation, Position> createAnnotations(IDocument doc, List<Range> highlights) {
		ImmutableMap.Builder<Annotation, Position> annotations = ImmutableMap.builder();
		if (highlights==null) {
			highlights = ImmutableList.of();
		}
		for (Range rng : highlights) {
			try {
				int start = LSPEclipseUtils.toOffset(rng.getStart(), doc);
				int end = LSPEclipseUtils.toOffset(rng.getEnd(), doc);
				Position e_rng = new Position(start, Math.max(0, end-start));
				annotations.put(new Annotation(ANNOTION_TYPE_ID, false, null), e_rng);
			} catch (BadLocationException e) {
				//ignore invalid highlights
			}
		}
		return annotations.build();
	}

	@Override
	public synchronized void highlight(HighlightParams highlights) {
		String target = highlights.getDoc().getUri();
		if (target!=null) {
			currentHighlights.put(target, highlights.getRanges());
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

}
