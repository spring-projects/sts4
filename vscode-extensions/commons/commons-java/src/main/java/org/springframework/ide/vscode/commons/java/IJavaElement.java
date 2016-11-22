package org.springframework.ide.vscode.commons.java;

import org.springframework.ide.vscode.commons.javadoc.IJavadoc;

public interface IJavaElement {
	String getElementName();
	IJavadoc getJavaDoc();
	boolean exists();
}
