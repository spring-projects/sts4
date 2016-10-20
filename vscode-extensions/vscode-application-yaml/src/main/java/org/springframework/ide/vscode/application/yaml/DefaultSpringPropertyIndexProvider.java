package org.springframework.ide.vscode.application.yaml;

import java.nio.file.Path;

import org.springframework.ide.vscode.application.properties.metadata.PropertyInfo;
import org.springframework.ide.vscode.application.properties.metadata.SpringPropertiesIndexManager;
import org.springframework.ide.vscode.application.properties.metadata.SpringPropertyIndexProvider;
import org.springframework.ide.vscode.application.properties.metadata.ValueProviderRegistry;
import org.springframework.ide.vscode.application.properties.metadata.util.FuzzyMap;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.IDocument;

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
