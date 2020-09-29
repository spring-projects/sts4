/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.ui;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Control;

/**
 * @author Kris De Volder
 */
public class UIConstants {

	/**
	 * Used as hint for the width of a field label. This makes the UI look nicer
	 * as input boxes for fields tend to line up nicely.
	 * <p>
	 * Deprecated: use fieldLabelWidthHint() method instead to obtain a width hint that
	 * adapts to dialog font dimensions.
	 */
	@Deprecated
	public static final int FIELD_LABEL_WIDTH_HINT = 80;

	public static final int FIELD_TEXT_AREA_WIDTH = 250;

	public static final int DIALOG_WIDTH_HINT = 310;

	public static final int SCROLLABLE_LIST_HEIGTH = 200;

	/**
	 * Computes a 'FIELD_LABEL_WIDTH_HINT' that scales based on the font metrics of
	 * the 'DialogFont'. (See bug STS-3899)
	 */
	public static final int fieldLabelWidthHint(Control control, int numChars) {
		Font dialogFont = JFaceResources.getDialogFont();
		GC gc = new GC(control);
		try {
			gc.setFont(dialogFont);
			return gc.getFontMetrics().getAverageCharWidth()*numChars;
		} finally {
			gc.dispose();
		}
	}

	public static int fieldLabelWidthHint(Control control) {
		return fieldLabelWidthHint(control, 15);
	}

}
