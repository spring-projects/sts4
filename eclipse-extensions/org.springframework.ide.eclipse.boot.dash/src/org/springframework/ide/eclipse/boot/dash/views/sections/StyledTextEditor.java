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

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.swt.widgets.Composite;

/**
 * Styled Text editor control. It's based on the corresponding cell editor. The
 * difference is that tries to give away focus (yield to the active shell) when
 * editing is completed or cancelled
 *
 * @author Alex Boyko
 *
 */
public abstract class StyledTextEditor extends StyledTextCellEditor {

	public StyledTextEditor(Composite parent) {
		super(parent);
	}

	public StyledTextEditor(Composite parent, IContentProposalProvider contentProposalProvider,
			KeyStroke keyStroke, char[] autoActivationCharacters) {
		super(parent, contentProposalProvider, keyStroke, autoActivationCharacters);
		setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
	}

	@Override
	public void deactivate() {
		if (text.getDisplay() != null && text.isFocusControl() && text.getDisplay().getActiveShell() != null) {
			text.getDisplay().getActiveShell().forceFocus();
		}
	}

	@Override
	protected void fireCancelEditor() {
		super.fireCancelEditor();
		deactivate();
	}

}
