package org.springframework.ide.vscode.commons.javadoc;

import java.net.URL;
import java.nio.file.Paths;

import org.springframework.ide.vscode.commons.java.IType;

@FunctionalInterface
public interface SourceUrlProviderFromSourceContainer {
	
	public static final SourceUrlProviderFromSourceContainer JAR_SOURCE_URL_PROVIDER = (sourceContainerUrl, type) -> {
		StringBuilder sourceUrlStr = new StringBuilder();
		sourceUrlStr.append("jar:");
		sourceUrlStr.append(sourceContainerUrl);
		sourceUrlStr.append("!");
		sourceUrlStr.append('/');
		sourceUrlStr.append(type.getFullyQualifiedName().replaceAll("\\.", "/"));
		sourceUrlStr.append(".java");
		return new URL(sourceUrlStr.toString());

	};
	
	public static final SourceUrlProviderFromSourceContainer SOURCE_FOLDER_URL_SUPPLIER = (sourceContainerUrl, type) -> {
		return Paths.get(sourceContainerUrl.toURI()).resolve(type.getFullyQualifiedName().replaceAll("\\.", "/") + ".java").toUri().toURL();
	};
	
	URL sourceUrl(URL sourceContainerUrl, IType type) throws Exception;

}
