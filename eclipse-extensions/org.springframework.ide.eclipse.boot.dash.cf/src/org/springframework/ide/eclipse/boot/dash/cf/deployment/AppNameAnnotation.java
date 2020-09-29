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

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationPresentation;
import org.eclipse.jface.text.source.ImageUtilities;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;

/**
 * Annotation for application name. Can be selected and unselected. Based on JDT
 * projection annotation
 *
 * @author Alex Boyko
 *
 */
public class AppNameAnnotation extends Annotation implements IAnnotationPresentation {
	/**
	 * The type of CF application name annotations.
	 */
	public static final String TYPE = "cf.app.name"; //$NON-NLS-1$

	private static final int COLOR= SWT.COLOR_GRAY;

	/** The state of this annotation */
	private boolean fIsSelected= false;
	/** Indicates whether this annotation should be painted as range */
	private boolean fIsRangeIndication= false;

	/**
	 * Creates a new annotation. When <code>isSelected</code>
	 * is <code>true</code> the annotation is initially selected.
	 *
	 * @param isSelected <code>true</code> if the annotation should initially be selected, <code>false</code> otherwise
	 */
	public AppNameAnnotation(String text) {
		super(TYPE, false, text);
	}

	/**
	 * Creates a new annotation.
	 *
	 * @param text the app name
	 * @param selected selected or unselected
	 */
	public AppNameAnnotation(String text, boolean selected) {
		this(text);
		fIsSelected = selected;
	}

	/**
	 * Enables and disables the range indication for this annotation.
	 *
	 * @param rangeIndication the enable state for the range indication
	 */
	public void setRangeIndication(boolean rangeIndication) {
		fIsRangeIndication= rangeIndication;
	}

	private void drawRangeIndication(GC gc, Canvas canvas, Rectangle r) {
		final int MARGIN= 3;

		/* cap the height - at least on GTK, large numbers are converted to
		 * negatives at some point */
		int height= Math.min(r.y + r.height - MARGIN, canvas.getSize().y);

		gc.setForeground(canvas.getDisplay().getSystemColor(COLOR));
		gc.setLineWidth(0); // NOTE: 0 means width is 1 but with optimized performance
		gc.drawLine(r.x + 4, r.y + 12, r.x + 4, height);
		gc.drawLine(r.x + 4, height, r.x + r.width - MARGIN, height);
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationPresentation#paint(org.eclipse.swt.graphics.GC, org.eclipse.swt.widgets.Canvas, org.eclipse.swt.graphics.Rectangle)
	 */
	public void paint(GC gc, Canvas canvas, Rectangle rectangle) {
		Image image= getImage();
		if (image != null) {
			ImageUtilities.drawImage(image, gc, canvas, rectangle, SWT.CENTER, SWT.TOP);
			if (fIsRangeIndication) {
				FontMetrics fontMetrics= gc.getFontMetrics();
				int delta= (fontMetrics.getHeight() - image.getBounds().height)/2;
				rectangle.y += delta;
				rectangle.height -= delta;
				drawRangeIndication(gc, canvas, rectangle);
			}
		}
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationPresentation#getLayer()
	 */
	public int getLayer() {
		return IAnnotationPresentation.DEFAULT_LAYER;
	}

	private Image getImage() {
		return isSelected() ? BootDashActivator.getDefault().getImageRegistry().get(BootDashActivator.CHECK_ICON)
				: BootDashActivator.getDefault().getImageRegistry().get(BootDashActivator.CHECK_GREYSCALE_ICON);
	}

	/**
	 * Returns the state of this annotation.
	 *
	 * @return <code>true</code> if collapsed
	 */
	public boolean isSelected() {
		return fIsSelected;
	}

	/**
	 * Marks this annotation as being selected.
	 */
	public void markSelected() {
		fIsSelected= true;
	}

	/**
	 * Marks this annotation as being unselected.
	 */
	public void markUnselected() {
		fIsSelected= false;
	}

}
