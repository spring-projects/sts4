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
package org.springsource.ide.eclipse.commons.livexp.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class ViewStyler {

	private int background = SWT.COLOR_LIST_BACKGROUND;

	public ViewStyler() {
	}

	public void applyBackground(Control[] controls) {
		if (controls == null || controls.length == 0) {
			return;
		}
		for (Control control : controls) {
			if (control instanceof Composite) {
				control.setBackground(getColour(background));
			}
		}
	}

	protected Color getColour(int colour) {
		return Display.getDefault().getSystemColor(colour);
	}

}
