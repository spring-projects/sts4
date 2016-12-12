package org.springframework.ide.vscode.commons.languageserver.java;

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.text.IDocument;

@FunctionalInterface
public interface JavaProjectFinder {
	IJavaProject find(IDocument doc);
}
