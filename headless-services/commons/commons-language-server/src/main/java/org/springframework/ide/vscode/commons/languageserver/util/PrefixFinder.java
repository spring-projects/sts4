/*******************************************************************************
 * Copyright (c) 2015, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.util;

import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.text.DocumentRegion;
import org.springframework.ide.vscode.commons.util.text.IDocument;

public abstract class PrefixFinder {
	public String getPrefix(IDocument doc, int offset, int lowerBound) {
		try {
			if (doc == null || offset > doc.getLength())
				return null;
			int prefixStart = offset;
			while (prefixStart > lowerBound && isPrefixChar(doc.getChar(prefixStart-1))) {
				prefixStart--;
			}
			return doc.get(prefixStart, offset-prefixStart);
		} catch (BadLocationException e) {
			return null;
		}
	}

	public String getPrefix(IDocument doc, int offset) {
		return getPrefix(doc, offset, 0);
	}
	protected abstract boolean isPrefixChar(char c);

	public DocumentRegion getPrefixRegion(IDocument doc, int offset) {
	   String prefix = getPrefix(doc, offset);
	   return prefix != null ? new DocumentRegion(doc, offset - prefix.length(), offset) : null;
	}
}