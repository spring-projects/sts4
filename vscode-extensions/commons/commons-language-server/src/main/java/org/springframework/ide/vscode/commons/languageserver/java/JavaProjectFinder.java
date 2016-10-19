package org.springframework.ide.vscode.commons.languageserver.java;

import org.springframework.ide.vscode.commons.languageserver.util.IDocument;
import org.springframework.ide.vscode.java.IJavaProject;

@FunctionalInterface
public interface JavaProjectFinder {
	JavaProjectFinder DEFAULT = new DefaultJavaProjectFinder("classpath.txt");
	IJavaProject find(IDocument doc);
}
