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

package org.springframework.ide.vscode.commons.languageserver.util;

import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class TextDocumentContentChange {

	private final TextDocument document;
	private final TextDocumentContentChangeEvent change;

	public TextDocumentContentChange(TextDocument doc, TextDocumentContentChangeEvent change) {
		this.document = doc;
		this.change = change;
	}

	public TextDocument getDocument() {
		return document;
	}

	public TextDocumentContentChangeEvent getChange() {
		return change;
	}

}
