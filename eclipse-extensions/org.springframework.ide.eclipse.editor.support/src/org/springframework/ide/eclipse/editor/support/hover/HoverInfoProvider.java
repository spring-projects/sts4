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
package org.springframework.ide.eclipse.editor.support.hover;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

/**
 * A simple interface to implement for a component that provides HoverInfo based on
 * editor contents.
 *
 * @author Kris De Volder
 */
public interface HoverInfoProvider {
	HoverInfo getHoverInfo(IDocument doc, IRegion r);
	IRegion getHoverRegion(IDocument document, int offset);
}
