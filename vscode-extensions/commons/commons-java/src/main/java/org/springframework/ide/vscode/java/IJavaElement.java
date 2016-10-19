package org.springframework.ide.vscode.java;

import org.springframework.ide.vscode.util.HtmlSnippet;

public interface IJavaElement {
	String getElementName();
	HtmlSnippet getJavaDoc();
	boolean exists();
}
