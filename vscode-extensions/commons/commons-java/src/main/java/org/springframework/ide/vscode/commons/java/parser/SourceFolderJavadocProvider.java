package org.springframework.ide.vscode.commons.java.parser;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.ide.vscode.commons.java.IType;

public class SourceFolderJavadocProvider extends AbstractJavadocProvider {
	
	private File sourceFolder;
	
	public SourceFolderJavadocProvider(File sourceFolder) {
		super();
		this.sourceFolder = sourceFolder;
	}

	@Override
	protected URL createSourceUrl(IType type) throws MalformedURLException {
		return new File(sourceFolder, type.getFullyQualifiedName().replaceAll("\\.", "/") + ".java").toURI().toURL();
	}

}
