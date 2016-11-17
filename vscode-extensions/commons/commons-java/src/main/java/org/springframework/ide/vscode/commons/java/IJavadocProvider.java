package org.springframework.ide.vscode.commons.java;

import org.springframework.ide.vscode.commons.javadoc.IJavadoc;

public interface IJavadocProvider {
	
	IJavadoc getJavadoc(IType type);
	
	IJavadoc getJavadoc(IField field);
	
	IJavadoc getJavadoc(IMethod method);

	IJavadoc getJavadoc(IAnnotation method);
}
