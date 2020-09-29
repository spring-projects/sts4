/*******************************************************************************
 * Copyright (c) 2016, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.deployment;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension2;
import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlASTProvider;

/**
 * Reconciling strategy responsible for keeping track of application name
 * annotations
 *
 * @author Alex Boyko
 *
 */
public class AppNameReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {

	/**
	 * Reconciler for application name annotations
	 */
	private AppNameReconciler fReconciler;

	/**
	 * Source viewer
	 */
	private ISourceViewer fViewer;

	/**
	 * Document to perform reconciling on
	 */
	private IDocument fDocument;

	/**
	 * Reconciling cycle progress monitor
	 */
	private IProgressMonitor fProgressMonitor;

	/**
	 * Creates new instance of the reconciler
	 *
	 * @param viewer Source viewer
	 * @param parser YAML parser
	 * @param appName Application name to keep selected all the time
	 */
	public AppNameReconcilingStrategy(YamlASTProvider parser) {
		fReconciler = new AppNameReconciler(parser);
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#initialReconcile()
	 */
	public void initialReconcile() {
		reconcile(new Region(0, fDocument.getLength()));
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#reconcile(org.eclipse.jface.text.reconciler.DirtyRegion,org.eclipse.jface.text.IRegion)
	 */
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		try {
			IRegion startLineInfo= fDocument.getLineInformationOfOffset(subRegion.getOffset());
			IRegion endLineInfo= fDocument.getLineInformationOfOffset(subRegion.getOffset() + Math.max(0, subRegion.getLength() - 1));
			if (startLineInfo.getOffset() == endLineInfo.getOffset())
				subRegion= startLineInfo;
			else
				subRegion= new Region(startLineInfo.getOffset(), endLineInfo.getOffset() + Math.max(0, endLineInfo.getLength() - 1) - startLineInfo.getOffset());

		} catch (BadLocationException e) {
			subRegion= new Region(0, fDocument.getLength());
		}
		reconcile(subRegion);
	}

	private AppNameAnnotationModel getAppNameAnnotationModel() {
		IAnnotationModel model = fViewer instanceof ISourceViewerExtension2 ? ((ISourceViewerExtension2)fViewer).getVisualAnnotationModel() : fViewer.getAnnotationModel();
		if (model instanceof IAnnotationModelExtension) {
			return (AppNameAnnotationModel) ((IAnnotationModelExtension) model).getAnnotationModel(AppNameAnnotationModel.APP_NAME_MODEL_KEY);
		}
		return (AppNameAnnotationModel) model;
	}

	@Override
	public void reconcile(IRegion region) {
		fReconciler.reconcile(fDocument, getAppNameAnnotationModel(), fProgressMonitor);
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#setProgressMonitor(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public final void setProgressMonitor(IProgressMonitor monitor) {
		fProgressMonitor= monitor;
	}

	@Override
	public void setDocument(IDocument document) {
		fDocument= document;
	}

	public void install(ISourceViewer viewer) {
		fViewer = viewer;
	}

	public void uninstall() {
		fViewer = null;
	}

}
