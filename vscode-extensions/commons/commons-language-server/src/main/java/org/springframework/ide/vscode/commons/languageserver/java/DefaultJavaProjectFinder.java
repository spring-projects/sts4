package org.springframework.ide.vscode.commons.languageserver.java;

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.util.IDocument;

public class DefaultJavaProjectFinder implements JavaProjectFinder {

	private final IJavaProjectFinderStrategy[] STRATEGIES = new IJavaProjectFinderStrategy[] {
		new JavaProjectWithClasspathFileFinderStrategy(),
		new MavenProjectFinderStrategy()
	};

	@Override
	public IJavaProject find(IDocument d) {
		for (IJavaProjectFinderStrategy strategy : STRATEGIES) {
			try {
				return strategy.find(d);
			} catch (Throwable t) {
				// Log perhaps?
			}
		}
		return null;
	}
}
