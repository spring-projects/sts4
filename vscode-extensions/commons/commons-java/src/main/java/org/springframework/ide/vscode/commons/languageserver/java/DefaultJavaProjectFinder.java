package org.springframework.ide.vscode.commons.languageserver.java;

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.text.IDocument;

public class DefaultJavaProjectFinder implements JavaProjectFinder {

	private final IJavaProjectFinderStrategy[] strategies;
	
	public DefaultJavaProjectFinder(IJavaProjectFinderStrategy[] strategies) {
		this.strategies = strategies;
	}

	@Override
	public IJavaProject find(IDocument d) {
		for (IJavaProjectFinderStrategy strategy : strategies) {
			try {
				IJavaProject project = strategy.find(d);
				if (project != null) {
					return project;
				}
			} catch (Exception e) {
				Log.log(e);
			}
		}
		return null;
	}
	
}
