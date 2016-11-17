package org.springframework.ide.vscode.commons.java;

public interface IJavaElement {
	String getElementName();
	IJavadoc getJavaDoc();
	boolean exists();
}
