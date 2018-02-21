/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.util.text;

import org.springframework.ide.vscode.commons.util.Assert;
import org.springframework.ide.vscode.commons.util.BadLocationException;

public class DocumentUtil {

	//TODO: this stuff belongs in IDocument and its implementation, not here. This class should be removed.

	/**
	 * Fetch text between two offsets. Doesn't throw BadLocationException.
	 * If either one or both of the offsets points outside the
	 * document then they will be adjusted to point the appropriate boundary to
	 * retrieve the text just upto the end or beginning of the document instead.
	 */
	public static String textBetween(IDocument doc, int start, int end) {
		Assert.isLegal(start<=end);
		if (start>=doc.getLength()) {
			return "";
		}
		if (start<0) {
			start = 0;
		}
		if (end>doc.getLength()) {
			end = doc.getLength();
		}
		if (end<start) {
			end = start;
		}
		try {
			return doc.get(start, end-start);
		} catch (BadLocationException e) {
			//unless the code above is wrong... this is supposed to be impossible!
			throw new IllegalStateException("Bug!", e);
		}
	}

}
