package org.springframework.ide.vscode.commons.java;

import org.springframework.ide.vscode.commons.util.HtmlSnippet;

public interface IJavaElement {
	String getElementName();
	HtmlSnippet getJavaDoc();
	boolean exists();
}
