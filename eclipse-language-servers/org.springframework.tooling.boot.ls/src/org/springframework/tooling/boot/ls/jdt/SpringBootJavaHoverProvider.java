/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.boot.ls.jdt;

import org.eclipse.jdt.ui.text.java.hover.IJavaEditorTextHover;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.lsp4e.operations.hover.LSBasedHover;
import org.eclipse.ui.IEditorPart;

/**
 * @author Martin Lippert
 */
@SuppressWarnings("restriction")
public class SpringBootJavaHoverProvider implements IJavaEditorTextHover, ITextHoverExtension {
	
	private LSBasedHover lsBasedHover;

	public SpringBootJavaHoverProvider() {
		lsBasedHover = new LSBasedHover();
	}

	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		return this.lsBasedHover.getHoverInfo(textViewer, hoverRegion);
	}

	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		return this.lsBasedHover.getHoverRegion(textViewer, offset);
	}

	@Override
	public void setEditor(IEditorPart editor) {
	}

	@Override
	public IInformationControlCreator getHoverControlCreator() {
		return this.lsBasedHover.getHoverControlCreator();
	}

}
