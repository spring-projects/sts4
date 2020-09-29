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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IViewerCreator;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.genericeditor.ExtensionBasedTextViewerConfiguration;
import org.eclipse.ui.part.FileEditorInput;
import org.springframework.ide.eclipse.boot.launch.properties.EmbeddedEditor;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

/**
 * Create compare and merge viewer for YAML content
 *
 * @author Alex Boyko
 *
 */
@SuppressWarnings("restriction")
public class YamlMergeViewerCreator implements IViewerCreator {

	@Override
	public Viewer createViewer(Composite parent, CompareConfiguration config) {
		return new TextMergeViewer(parent, SWT.NONE, config) {

			@Override
			protected SourceViewer createSourceViewer(Composite parent, int textOrientation) {
//				try {
//					IPreferenceStore preferenceStore = JavaPlugin.getDefault().getCombinedPreferenceStore();
//					EmbeddedEditor editor = new EmbeddedEditor(e -> new ExtensionBasedTextViewerConfiguration(e, preferenceStore), preferenceStore, false, false);
//					editor.init(null, new FileEditorInput((IFile) config.getProperty("manifest")));
//					editor.createControl(parent);
//					ProjectionViewer viewer = editor.getViewer();
//					viewer.getControl().addDisposeListener(e -> editor.dispose());
//					return viewer;
//				} catch (CoreException e) {
//					Log.log(e);
//				}
//				return null;
				return new SourceViewer(parent, null, null, true, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
			}

			@Override
			protected void configureTextViewer(TextViewer textViewer) {
				/*
				 * Nothing to do
				 */
			}

		};
	}

}
