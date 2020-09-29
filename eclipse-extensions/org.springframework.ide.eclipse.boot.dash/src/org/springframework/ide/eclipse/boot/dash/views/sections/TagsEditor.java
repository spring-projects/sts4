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
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Composite;
import org.springframework.ide.eclipse.boot.dash.model.TagUtils;
import org.springsource.ide.eclipse.commons.livexp.ui.Stylers;

/**
 * Tags editor based on StyledText cell editor
 *
 * @author Alex Boyko
 *
 */
public class TagsEditor extends StyledTextEditor {

	private Stylers stylers;

	public TagsEditor(Composite parent, Stylers stylers) {
		super(parent);
		this.stylers = stylers;
	}

	public TagsEditor(Composite parent, Stylers stylers, IContentProposalProvider contentProposalProvider,
			KeyStroke keyStroke, char[] autoActivationCharacters) {
		super(parent, contentProposalProvider, keyStroke, autoActivationCharacters);
		setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		this.stylers = stylers;
	}

	@Override
	protected StyleRange[] updateStyleRanges(String text) {
		StyledString styled = TagUtils.applyTagStyles(text, stylers.tag());
		return styled.getStyleRanges();
	}


}
