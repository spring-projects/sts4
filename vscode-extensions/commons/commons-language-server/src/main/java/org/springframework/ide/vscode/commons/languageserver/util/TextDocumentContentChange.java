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
