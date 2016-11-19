package org.springframework.ide.vscode.commons.javadoc;

import java.net.URL;

import org.springframework.ide.vscode.commons.javadoc.internal.JavadocContents;


public interface HtmlJavadocIndex {
	
	public static final HtmlJavadocIndex DEFAULT = new DefaultHtmlJavadocIndex();
	
	JavadocContents getHtmlJavadoc(URL url);

}
