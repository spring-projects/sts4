package org.springframework.ide.vscode.yaml;

import java.nio.file.Path;

import org.springframework.ide.vscode.boot.properties.metadata.PropertyInfo;
import org.springframework.ide.vscode.boot.properties.metadata.SpringPropertiesIndexManager;
import org.springframework.ide.vscode.boot.properties.metadata.SpringPropertyIndexProvider;
import org.springframework.ide.vscode.boot.properties.metadata.ValueProviderRegistry;
import org.springframework.ide.vscode.boot.properties.util.FuzzyMap;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.IDocument;
import org.springframework.ide.vscode.java.IJavaProject;

public class DefaultSpringPropertyIndexProvider implements SpringPropertyIndexProvider {

	private JavaProjectFinder javaProjectFinder = JavaProjectFinder.DEFAULT;
	private SpringPropertiesIndexManager indexManager = new SpringPropertiesIndexManager(ValueProviderRegistry.getDefault());
	
	@Override
	public FuzzyMap<PropertyInfo> getIndex(IDocument doc) {
		IJavaProject jp = javaProjectFinder.find(doc);
		if (jp!=null) {
			Path projectFolder = jp.getPath();
			if (projectFolder!=null) {
				return indexManager.get(projectFolder);
			}
		}
		return null;
	}

}
