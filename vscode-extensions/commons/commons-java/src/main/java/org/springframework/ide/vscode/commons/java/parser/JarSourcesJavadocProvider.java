package org.springframework.ide.vscode.commons.java.parser;

import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.ide.vscode.commons.java.IType;

import com.google.common.base.Supplier;

public class JarSourcesJavadocProvider extends AbstractJavadocProvider {
	
	private Supplier<URL> sourcesJarUrl;
	
	public JarSourcesJavadocProvider(Supplier<URL> sourcesJarUrl) {
		super();
		this.sourcesJarUrl = sourcesJarUrl;
	}

	@Override
	protected URL createSourceUrl(IType type) throws MalformedURLException {
		StringBuilder sourceUrlStr = new StringBuilder();
		sourceUrlStr.append("jar:");
		sourceUrlStr.append(sourcesJarUrl.get());
		sourceUrlStr.append("!");
		sourceUrlStr.append('/');
		sourceUrlStr.append(type.getFullyQualifiedName().replaceAll("\\.", "/"));
		sourceUrlStr.append(".java");
		return new URL(sourceUrlStr.toString());
	}

}
