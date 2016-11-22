package org.springframework.ide.vscode.commons.java.roaster;

import org.jboss.forge.roaster.model.JavaDoc;
import org.springframework.ide.vscode.commons.javadoc.IJavadoc;
import org.springframework.ide.vscode.commons.util.Renderable;

public class RoasterJavadoc implements IJavadoc {
	
	private JavaDoc<?> javadoc;

	public RoasterJavadoc(JavaDoc<?> javadoc) {
		this.javadoc = javadoc;
	}

	@Override
	public String raw() {
		return javadoc.getFullText();
	}

	@Override
	public Renderable getRenderable() {
		throw new UnsupportedOperationException("Not yet implemented");
	}


}
