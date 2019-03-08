/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.languageserver.util;

import java.util.List;

import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class TextDocumentContentChange {

	private final TextDocument document;
	private final List<TextDocumentContentChangeEvent> changes;

	public TextDocumentContentChange(TextDocument doc, List<TextDocumentContentChangeEvent> changes) {
		this.document = doc;
		this.changes = changes;
	}

	public TextDocument getDocument() {
		return document;
	}

	public List<TextDocumentContentChangeEvent> getChanges() {
		return changes;
	}

}
