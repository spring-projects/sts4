/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.maven.pom;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.internal.merge.DocumentMerger;
import org.eclipse.compare.internal.merge.DocumentMerger.Diff;
import org.eclipse.compare.internal.merge.DocumentMerger.IDocumentMergerInput;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.Bundle;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

@SuppressWarnings("restriction")
public class PomMergeViewer extends TextMergeViewer {

	public PomMergeViewer(Composite parent, int style, CompareConfiguration configuration) {

		super(parent, style, configuration);
		if (Boolean.TRUE.equals(configuration.getProperty(PomPlugin.POM_STRUCTURE_ADDITIONS_COMPARE_SETTING))) {
			DocumentMerger original = getDocumentMerger();
			PomDocumentMerger pomMerger = new PomDocumentMerger(getInputFromMerger(original), configuration);
			setDocumentMerger(pomMerger);
		}
	}

	@Override
	public String getTitle() {
		return "Maven POM Compare";
	}

	@Override
	protected void configureTextViewer(TextViewer textViewer) {
		if (textViewer instanceof SourceViewer) {
			Bundle bundle = Platform.getBundle("org.eclipse.m2e.editor.xml");
			if (bundle != null) {
				try {
					Class<?> clazz = bundle.loadClass("org.eclipse.m2e.editor.xml.PomStructuredTextViewConfiguration");
					SourceViewerConfiguration config = (SourceViewerConfiguration) clazz.newInstance();
					((SourceViewer)textViewer).configure(config);
					return;
				} catch (ClassNotFoundException e) {
					Log.warn(e);
				} catch (InstantiationException e) {
					Log.warn(e);
				} catch (IllegalAccessException e) {
					Log.warn(e);
				}
			}
		}
		super.configureTextViewer(textViewer);
	}

	@Override
	protected SourceViewer createSourceViewer(Composite parent, int textOrientation) {
		Bundle bundle = Platform.getBundle("org.eclipse.m2e.editor.xml");
		if (bundle != null) {
			try {
				Class<?> clazz = bundle.loadClass("org.eclipse.wst.sse.ui.internal.StructuredTextViewer");
				Constructor<?> constructor = clazz.getDeclaredConstructor(Composite.class, IVerticalRuler.class, IOverviewRuler.class, boolean.class, int.class);
				return (SourceViewer) constructor.newInstance(parent, null, null, false, textOrientation | SWT.H_SCROLL | SWT.V_SCROLL);
			} catch (ClassNotFoundException e) {
				Log.warn(e);
			} catch (InstantiationException e) {
				Log.warn(e);
			} catch (IllegalAccessException e) {
				Log.warn(e);
			} catch (NoSuchMethodException e) {
				Log.warn(e);
			} catch (SecurityException e) {
				Log.warn(e);
			} catch (IllegalArgumentException e) {
				Log.warn(e);
			} catch (InvocationTargetException e) {
				Log.warn(e);
			}
		}
		return super.createSourceViewer(parent, textOrientation);
	}
	
	private IDocumentMergerInput getInputFromMerger(DocumentMerger merger) {
		try {
			Field field = DocumentMerger.class.getDeclaredField("fInput");
			field.setAccessible(true);
			return (IDocumentMergerInput) field.get(merger);
		} catch (Exception e) {
			throw ExceptionUtil.unchecked(e);
		}
	}
	
	private DocumentMerger getDocumentMerger() {
		try {
			Field field = TextMergeViewer.class.getDeclaredField("fMerger");
			field.setAccessible(true);
			return (DocumentMerger) field.get(this);
		} catch (Exception e) {
			throw ExceptionUtil.unchecked(e);
		}
	}
	
	private void setDocumentMerger(DocumentMerger docMerger) {
		try {
			Field field = TextMergeViewer.class.getDeclaredField("fMerger");
			field.setAccessible(true);
			field.set(this, docMerger);
		} catch (Exception e) {
			throw ExceptionUtil.unchecked(e);
		}
	}

	@Override
	protected void copy(boolean leftToRight) {
		if (Boolean.TRUE.equals(getCompareConfiguration().getProperty(PomPlugin.POM_STRUCTURE_ADDITIONS_COMPARE_SETTING))) {
			try {
				Method copyDiffMethod = TextMergeViewer.class.getDeclaredMethod("copy", Diff.class, boolean.class);
				copyDiffMethod.setAccessible(true);
				
				for (Iterator<Diff> it = ((PomDocumentMerger)getDocumentMerger()).changesIterator(); it.hasNext();) {
					Diff d = it.next();
					copyDiffMethod.invoke(this, d, leftToRight);
				}
				
				// Execute: update(false);
				Method updateMethod = TextMergeViewer.class.getDeclaredMethod("update", boolean.class);
				updateMethod.setAccessible(true);
				updateMethod.invoke(this, false);
				
				// Execute: selectFirstDiff(true)
				Method selectFirstDiffMethod = TextMergeViewer.class.getDeclaredMethod("selectFirstDiff", boolean.class);
				selectFirstDiffMethod.setAccessible(true);				
				selectFirstDiffMethod.invoke(this, true);
			} catch (Exception e) {
				Log.log(e);
			}
		} else {
			super.copy(leftToRight);
		}
	}

	
}
