package org.springframework.ide.vscode.commons.javadoc;

import org.springframework.ide.vscode.commons.util.Renderable;

public class RawJavadoc implements IJavadoc {
	
	private String rawContent;
	
	public RawJavadoc(String rawContent) {
		this.rawContent = rawContent;
	}

	@Override
	public String raw() {
		return rawContent;
	}

	@Override
	public Renderable getRenderable() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

}
