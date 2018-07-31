package org.springframework.tooling.ls.eclipse.commons;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.inlined.LineHeaderAnnotation;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

public class BootHeadlineAnnotation extends LineHeaderAnnotation {

	public BootHeadlineAnnotation(Position position, ISourceViewer viewer) {
		super(position, viewer);
	}

//	@Override
//	public void draw(GC gc, StyledText textWidget, int offset, int length, Color color, int x, int y) {
//		initGC(textWidget, color, gc);
//
//		FontMetrics fontMetrics = gc.getFontMetrics();
//		int height = fontMetrics.getHeight();
//
//		Image bootImage = LanguageServerCommonsActivator.getInstance().getImageRegistry().get(LanguageServerCommonsActivator.BOOT_ICON_2X_KEY);
//		Rectangle bootImgBounds = bootImage.getBounds();
//		int width = (int) Math.round(bootImgBounds.width / (double) bootImgBounds.height * height);
//
//		gc.drawImage(bootImage, bootImgBounds.x, bootImgBounds.y, bootImgBounds.width, bootImgBounds.height, x, y, width, height);
//
//		int textX = x + width + 4;
//		int textY = y;
//
//		gc.drawText("CODE LENS goes here!", textX, textY);
//	}
//
//	/**
//	 * Initialize GC with given color and styled text background color and font.
//	 *
//	 * @param textWidget the text widget
//	 * @param color      the color
//	 * @param gc         the gc to initialize
//	 */
//	private void initGC(StyledText textWidget, Color color, GC gc) {
//		gc.setForeground(color);
//		gc.setBackground(textWidget.getBackground());
//		gc.setFont(textWidget.getFont());
//	}


}
