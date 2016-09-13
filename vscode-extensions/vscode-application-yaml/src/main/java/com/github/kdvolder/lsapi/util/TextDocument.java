package com.github.kdvolder.lsapi.util;

import io.typefox.lsapi.Range;
import io.typefox.lsapi.TextDocumentContentChangeEvent;

public class TextDocument {
	
	private final String uri;
	private String text = "";
	
	public TextDocument(String uri) {
		this.uri = uri;
	}
	
	public String getUri() {
		return uri;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	public void apply(TextDocumentContentChangeEvent change) {
		Range rng = change.getRange();
		if (rng==null) {
			//full sync mode
			this.text = change.getText();
		} else {
			//incremental sync mode
			throw new IllegalStateException("Incremental sync not yet implemented");
		}
	}


}
