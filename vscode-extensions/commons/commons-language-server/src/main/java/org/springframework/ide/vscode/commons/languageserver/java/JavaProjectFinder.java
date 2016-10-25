package org.springframework.ide.vscode.commons.languageserver.java;

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.util.IDocument;

@FunctionalInterface
public interface JavaProjectFinder {
	JavaProjectFinder DEFAULT = new DefaultJavaProjectFinder();
	IJavaProject find(IDocument doc);
}
