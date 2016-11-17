package org.springframework.ide.vscode.commons.javadoc;

import java.net.URL;

import org.springframework.ide.vscode.commons.java.IType;

@FunctionalInterface
public interface SourceUrlProvider {

	URL sourceUrl(IType type) throws Exception;
	
}
