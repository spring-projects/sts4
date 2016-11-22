package org.springframework.ide.vscode.commons.javadoc;

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
	public String plainText() {
		throw new UnsupportedOperationException("Not yet implemnted");
	}

	@Override
	public String html() {
		throw new UnsupportedOperationException("Not yet implemnted");
	}

	@Override
	public String markdown() {
		throw new UnsupportedOperationException("Not yet implemnted");
	}

}
