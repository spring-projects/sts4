/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.boot.ls.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionRelevance;
import org.springframework.tooling.boot.ls.BootLanguageServerPlugin;

public class DelegateMarkerResolution implements IMarkerResolution, IMarkerResolutionRelevance, IJavaCompletionProposal {
	
	final private IMarkerResolution delegate;
	
	final private int relevance;
	
	public DelegateMarkerResolution(IMarkerResolution res, int relevance) {
		this.delegate = res;
		this.relevance = relevance;
	}

	@Override
	public String getLabel() {
		return delegate.getLabel();
	}

	@Override
	public void run(IMarker marker) {
		delegate.run(marker);
	}

	@Override
	public void apply(IDocument document) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Point getSelection(IDocument document) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getAdditionalProposalInfo() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getDisplayString() {
		return getLabel();
	}

	@Override
	public Image getImage() {
		return BootLanguageServerPlugin.getDefault().getImageRegistry().get(BootLanguageServerPlugin.SPRING_ICON);
	}

	@Override
	public IContextInformation getContextInformation() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getRelevance() {
		return relevance;
	}

	@Override
	public int getRelevanceForResolution() {
		return relevance;
	}

}
