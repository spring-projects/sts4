package org.springframework.ide.vscode.util;

import io.typefox.lsapi.TextDocumentContentChangeEvent;

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
