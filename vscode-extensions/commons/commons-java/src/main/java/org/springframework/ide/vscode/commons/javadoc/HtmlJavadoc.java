package org.springframework.ide.vscode.commons.javadoc;

import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.Renderables;

public class HtmlJavadoc implements IJavadoc {
	
	private String html;
	
	public HtmlJavadoc(String html) {
		this.html = html;
	}

	@Override
	public String raw() {
		throw new UnsupportedOperationException("Not yet implemnted");
	}

	@Override
	public Renderable getRenderable() {
		return Renderables.htmlBlob(html);
	}


}
