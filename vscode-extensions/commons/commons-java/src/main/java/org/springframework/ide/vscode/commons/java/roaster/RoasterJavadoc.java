package org.springframework.ide.vscode.commons.java.roaster;

import org.jboss.forge.roaster.model.JavaDoc;
import org.springframework.ide.vscode.commons.javadoc.IJavadoc;

public class RoasterJavadoc implements IJavadoc {
	
	private JavaDoc<?> javadoc;

	public RoasterJavadoc(JavaDoc<?> javadoc) {
		this.javadoc = javadoc;
	}

	@Override
	public String raw() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public String plainText() {
		return javadoc.getFullText();
	}

	@Override
	public String html() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public String markdown() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

}
