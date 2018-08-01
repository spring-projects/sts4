/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.commons;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.inlined.LineContentAnnotation;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Boot icon inlined annotation
 *
 * @author Alex Boyko
 *
 */
public class BootInlineAnnotation extends LineContentAnnotation {

	private static final int SPACING = 2;

	public BootInlineAnnotation(Position pos, ISourceViewer viewer) {
		super(pos, viewer);
	}

	@Override
	protected int drawAndComputeWidth(GC gc, StyledText textWidget, int offset, int length, Color color, int x, int y) {
		FontMetrics fontMetrics = gc.getFontMetrics();
		int height = fontMetrics.getHeight();

		Image bootImage = LanguageServerCommonsActivator.getInstance().getImageRegistry().get(LanguageServerCommonsActivator.BOOT_KEY);
		Rectangle bootImgBounds = bootImage.getBounds();
		int width = (int) Math.round(bootImgBounds.width / (double) bootImgBounds.height * height);

		Rectangle backgroundRect = new Rectangle(x, y, width + SPACING, fontMetrics.getHeight());
		gc.setBackground(textWidget.getBackground());
		gc.fillRectangle(backgroundRect);

		gc.drawImage(bootImage, bootImgBounds.x, bootImgBounds.y, bootImgBounds.width, bootImgBounds.height, x, y, width, height);

		return backgroundRect.width;
	}

}