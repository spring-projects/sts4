/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.sections;

import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.IControlContentAdapter2;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

/**
 * Content Adapter for StyledText widget
 *
 * @author Alex Boyko
 *
 */
public class StyledTextContentAdapter implements IControlContentAdapter, IControlContentAdapter2 {

	@Override
	public String getControlContents(Control control) {
		StyledText styledText = (StyledText) control;
		return styledText.getText();
	}

	@Override
	public void setControlContents(Control control, String text,
			int cursorPosition) {
		StyledText styledText = (StyledText) control;
		styledText.setText(text);
		styledText.setSelection(cursorPosition, cursorPosition);
	}

	@Override
	public void insertControlContents(Control control, String text,
			int cursorPosition) {
		StyledText styledText = (StyledText) control;
		Point selection = styledText.getSelection();
		styledText.insert(text);
		// Insert will leave the cursor at the end of the inserted text. If this
		// is not what we wanted, reset the selection.
		if (cursorPosition < text.length()) {
			styledText.setSelection(selection.x + cursorPosition,
					selection.x + cursorPosition);
		}
	}

	@Override
	public int getCursorPosition(Control control) {
		StyledText styledText = (StyledText) control;
		return styledText.getCaretOffset();
	}

	@Override
	public Rectangle getInsertionBounds(Control control) {
		StyledText styledText = (StyledText) control;
		return styledText.getCaret().getBounds();
	}

	@Override
	public void setCursorPosition(Control control, int position) {
		StyledText styledText = (StyledText) control;
		styledText.setSelection(new Point(position, position));
	}

	@Override
	public Point getSelection(Control control) {
		StyledText styledText = (StyledText) control;
		return styledText.getSelection();
	}

	@Override
	public void setSelection(Control control, Point range) {
		StyledText styledText = (StyledText) control;
		styledText.setSelection(range);
	}

}
