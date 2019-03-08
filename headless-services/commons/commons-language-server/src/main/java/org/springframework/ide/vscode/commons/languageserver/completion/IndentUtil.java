/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.completion;

import org.springframework.ide.vscode.commons.util.Assert;
import org.springframework.ide.vscode.commons.util.text.DocumentRegion;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.util.text.IRegion;

/**
 * Helper methods to manipulate indentation levels in yaml content.
 *
 * @author Kris De Volder
 */
public class IndentUtil {

	/**
	 * Some functions introduce line separators and this may depend on the context (i.e. default line separator
	 * for the current document).
	 */
	public final String NEWLINE;

	public IndentUtil(String newline) {
		this.NEWLINE = newline;
		Assert.isNotNull(NEWLINE);
	}


	public IndentUtil(IDocument doc) {
		this(doc.getDefaultLineDelimiter());
	}

	public String applyIndentation(String text, String indentStr) {
		return text.replaceAll("\\n", "\n"+indentStr);
	}


	public String getReferenceIndent(int offset, IDocument doc) {
		//Apply indentfix, this is magic vscode seems to apply to edits returned by language server. So our harness has to
		// mimick that behavior. See https://github.com/Microsoft/language-server-protocol/issues/83
		IRegion referenceLine = doc.getLineInformationOfOffset(offset);
		DocumentRegion queryPrefix = new DocumentRegion(doc, referenceLine.getOffset(), offset);
		return queryPrefix.leadingWhitespace().toString();
	}


	public String covertTabsToSpace(String snippet) {
		return snippet.replaceAll("\\t", "    ");
	}
}
