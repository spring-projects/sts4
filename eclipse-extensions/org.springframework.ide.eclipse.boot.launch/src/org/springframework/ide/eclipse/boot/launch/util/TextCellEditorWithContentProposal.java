/*******************************************************************************
 * Copyright (c) 2008 Software Competence Center Hagenberg (SCCH) GmbH
 * Copyright (c) 2008 Mario Winterer
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.launch.util;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposalListener2;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;

public class TextCellEditorWithContentProposal extends TextCellEditor {

	private ContentProposalAdapter contentProposalAdapter;
	private boolean popupOpen = false; // true, iff popup is currently open

	public TextCellEditorWithContentProposal(Composite parent, IContentProposalProvider contentProposalProvider,
			KeyStroke keyStroke, char[] autoActivationCharacters) {
		super(parent);

		enableContentProposal(contentProposalProvider, keyStroke, autoActivationCharacters);
	}

	public TextCellEditorWithContentProposal setProposalAcceptanceStyle(int acceptance) {
		contentProposalAdapter.setProposalAcceptanceStyle(acceptance);
		return this;
	}

	private void enableContentProposal(IContentProposalProvider contentProposalProvider, KeyStroke keyStroke,
			char[] autoActivationCharacters) {
		contentProposalAdapter = new ContentProposalAdapter(text, new TextContentAdapter(),
				contentProposalProvider, keyStroke, autoActivationCharacters);

		// Listen for popup open/close events to be able to handle focus events correctly
		contentProposalAdapter.addContentProposalListener(new IContentProposalListener2() {

			public void proposalPopupClosed(ContentProposalAdapter adapter) {
				popupOpen = false;
			}

			public void proposalPopupOpened(ContentProposalAdapter adapter) {
				popupOpen = true;
			}
		});
	}

	/**
	 * Return the {@link ContentProposalAdapter} of this cell editor.
	 *
	 * @return the {@link ContentProposalAdapter}
	 */
	public ContentProposalAdapter getContentProposalAdapter() {
		return contentProposalAdapter;
	}

	protected void focusLost() {
		if (!popupOpen) {
			// Focus lost deactivates the cell editor.
			// This must not happen if focus lost was caused by activating
			// the completion proposal popup.
			super.focusLost();
		}
	}

	protected boolean dependsOnExternalFocusListener() {
		// Always return false;
		// Otherwise, the ColumnViewerEditor will install an additional focus listener
		// that cancels cell editing on focus lost, even if focus gets lost due to
		// activation of the completion proposal popup. See also bug 58777.
		return false;
	}
}