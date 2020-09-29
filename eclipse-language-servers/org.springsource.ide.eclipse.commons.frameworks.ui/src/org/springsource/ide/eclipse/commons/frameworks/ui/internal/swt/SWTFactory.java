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
package org.springsource.ide.eclipse.commons.frameworks.ui.internal.swt;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.contentassist.ContentProposalAdapter;


/**
 * @author Nieraj Singh
 */
public class SWTFactory {

	private SWTFactory() {
		// Util class
	}

	public static Button createRadialButton(Composite parent, String label,
			boolean initialSelection) {
		if (parent == null || label == null) {
			return null;
		}
		Button radio = new Button(parent, SWT.RADIO);
		radio.setText(label);

		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER)
				.applyTo(radio);
		radio.setSelection(initialSelection);
		return radio;
	}
}
