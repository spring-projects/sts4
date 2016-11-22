package org.springframework.ide.vscode.commons.javadoc;

import org.springframework.ide.vscode.commons.util.Renderable;

public interface IJavadoc {
	
	String raw();
	
	Renderable getRenderable();
	
}
