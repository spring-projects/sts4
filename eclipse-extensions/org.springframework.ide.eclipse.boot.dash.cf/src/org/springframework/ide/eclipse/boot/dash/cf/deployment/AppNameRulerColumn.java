/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.deployment;

import java.util.Iterator;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.AnnotationRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Application name annotations ruler control. Implementation based on
 * ProjectionRulerColumn class implementation
 *
 * @author Alex Boyko
 *
 */
public class AppNameRulerColumn extends AnnotationRulerColumn {

	/**
	 * Currently examined/hovered over annotation
	 */
	private AppNameAnnotation fCurrentAnnotation;

	/**
	 * Line number recorded on mouse down.
	 */
	private int fMouseDownLine;

	public AppNameRulerColumn(IAnnotationModel model, int width, IAnnotationAccess annotationAccess) {
		super(model, width, annotationAccess);
	}

	public AppNameRulerColumn(int width, IAnnotationAccess annotationAccess) {
		super(width, annotationAccess);
	}

	@Override
	protected void mouseClicked(int line) {
		clearCurrentAnnotation();
		if (fMouseDownLine != line)
			return;
		AppNameAnnotation annotation= findAnnotation(line, true);
		if (annotation != null) {
			AppNameAnnotationModel model= (AppNameAnnotationModel) getModel();
			model.markSelected(annotation);
		}
	}

	@Override
	protected void mouseDown(int rulerLine) {
		fMouseDownLine= rulerLine;
	}

	@Override
	protected void mouseDoubleClicked(int rulerLine) {
		if (findAnnotation(rulerLine, true) != null)
			return;

		AppNameAnnotation annotation= findAnnotation(rulerLine, false);
		if (annotation != null) {
			AppNameAnnotationModel model= (AppNameAnnotationModel) getModel();
			model.markSelected(annotation);
		}
	}

	/**
	 * Returns the app's name annotation of the column's annotation
	 * model that contains the given line.
	 *
	 * @param line the line
	 * @param exact <code>true</code> if the annotation range must match exactly
	 * @return the app name annotation containing the given line
	 */
	private AppNameAnnotation findAnnotation(int line, boolean exact) {

		AppNameAnnotation previousAnnotation= null;

		IAnnotationModel model= getModel();
		if (model != null) {
			IDocument document= getCachedTextViewer().getDocument();

			int previousDistance= Integer.MAX_VALUE;

			Iterator<?> e= model.getAnnotationIterator();
			while (e.hasNext()) {
				Object next= e.next();
				if (next instanceof AppNameAnnotation) {
					AppNameAnnotation annotation= (AppNameAnnotation) next;
					Position p= model.getPosition(annotation);
					if (p == null)
						continue;

					int distance= getDistance(annotation, p, document, line);
					if (distance == -1)
						continue;

					if (!exact) {
						if (distance < previousDistance) {
							previousAnnotation= annotation;
							previousDistance= distance;
						}
					} else if (distance == 0) {
						previousAnnotation= annotation;
					}
				}
			}
		}

		return previousAnnotation;
	}

	/**
	 * Returns the distance of the given line to the start line of the given position in the given document. The distance is
	 * <code>-1</code> when the line is not included in the given position.
	 *
	 * @param annotation the annotation
	 * @param position the position
	 * @param document the document
	 * @param line the line
	 * @return <code>-1</code> if line is not contained, a position number otherwise
	 */
	private int getDistance(AppNameAnnotation annotation, Position position, IDocument document, int line) {
		if (position.getOffset() > -1 && position.getLength() > -1) {
			try {
				int startLine= document.getLineOfOffset(position.getOffset());
				int endLine= document.getLineOfOffset(position.getOffset() + position.getLength());
				if (startLine <= line && line < endLine) {
					return line - startLine;
				}
			} catch (BadLocationException x) {
			}
		}
		return -1;
	}

	private boolean clearCurrentAnnotation() {
		if (fCurrentAnnotation != null) {
			fCurrentAnnotation.setRangeIndication(false);
			fCurrentAnnotation= null;
			return true;
		}
		return false;
	}

	@Override
	public Control createControl(CompositeRuler parentRuler, Composite parentControl) {
		Control control= super.createControl(parentRuler, parentControl);

		// set background
		Color background= getCachedTextViewer().getTextWidget().getBackground();
		control.setBackground(background);

		// install hover listener
		control.addMouseTrackListener(new MouseTrackAdapter() {
			public void mouseExit(MouseEvent e) {
				if (clearCurrentAnnotation())
					redraw();
			}
		});

		// install mouse move listener
		control.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				boolean redraw= false;
				AppNameAnnotation annotation= findAnnotation(toDocumentLineNumber(e.y), false);
				if (annotation != fCurrentAnnotation) {
					if (fCurrentAnnotation != null) {
						fCurrentAnnotation.setRangeIndication(false);
						redraw= true;
					}
					fCurrentAnnotation= annotation;
					if (fCurrentAnnotation != null) {
						fCurrentAnnotation.setRangeIndication(true);
						redraw= true;
					}
				}
				if (redraw)
					redraw();
			}
		});
		return control;
	}

	@Override
	public void setModel(IAnnotationModel model) {
		if (!(model instanceof AppNameAnnotationModel) && model instanceof IAnnotationModelExtension) {
			IAnnotationModelExtension extension= (IAnnotationModelExtension) model;
			model= extension.getAnnotationModel(AppNameAnnotationModel.APP_NAME_MODEL_KEY);
		}
		super.setModel(model);
	}

	/*
	 * @see org.eclipse.jface.text.source.AnnotationRulerColumn#isPropagatingMouseListener()
	 */
	protected boolean isPropagatingMouseListener() {
		return false;
	}

	/*
	 * @see org.eclipse.jface.text.source.AnnotationRulerColumn#hasAnnotation(int)
	 */
	protected boolean hasAnnotation(int lineNumber) {
		return findAnnotation(lineNumber, true) != null;
	}

}
