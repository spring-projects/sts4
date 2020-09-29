/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.ui;

import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

/**
 * @author Steffen Pingel
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class UnderlinedHyperlink extends ImageHyperlink {

	private final MouseTrackListener mouseTrackListener = new MouseTrackListener() {

		public void mouseEnter(MouseEvent e) {
			setUnderlined(true);
			setForeground(JFaceColors.getActiveHyperlinkText(getDisplay()));
		}

		public void mouseExit(MouseEvent e) {
			setUnderlined(false);
			setForeground(JFaceColors.getHyperlinkText(getDisplay()));
		}

		public void mouseHover(MouseEvent e) {
		}
	};

	private boolean active;

	public UnderlinedHyperlink(Composite parent, int style) {
		super(parent, style);
		addMouseTrackListener(mouseTrackListener);
		setForeground(JFaceColors.getHyperlinkText(getDisplay()));
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public void setEnabled(boolean enabled) {
		if (enabled) {
			setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
		}
		else {
			setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		}
		super.setEnabled(enabled);
	}

}
