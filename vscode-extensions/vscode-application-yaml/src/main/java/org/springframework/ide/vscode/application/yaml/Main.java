package org.springframework.ide.vscode.application.yaml;

import java.io.IOException;

import org.springframework.ide.vscode.application.properties.metadata.DefaultSpringPropertyIndexProvider;
import org.springframework.ide.vscode.application.properties.metadata.SpringPropertyIndexProvider;
import org.springframework.ide.vscode.application.properties.metadata.types.TypeUtil;
import org.springframework.ide.vscode.application.properties.metadata.types.TypeUtilProvider;
import org.springframework.ide.vscode.commons.languageserver.LaunguageServerApp;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.IDocument;

public class Main {

	public static void main(String[] args) throws IOException {
		LaunguageServerApp.start(() -> {
			JavaProjectFinder javaProjectFinder = JavaProjectFinder.DEFAULT;
			//TODO: proper TypeUtilProvider and IndexProvider that somehow determine classpath that should be
			// in effect for given IDocument and provide TypeUtil or SpringPropertyIndex parsed from that classpath.
			// Note that the provider is responsible for doing some kind of sensible caching so that indexes are not
			// rebuilt every time the index is being used.
			SpringPropertyIndexProvider indexProvider = new DefaultSpringPropertyIndexProvider(javaProjectFinder);
			TypeUtilProvider typeUtilProvider = (IDocument doc) -> new TypeUtil(javaProjectFinder.find(doc));
			ApplicationYamlLanguageServer server = new ApplicationYamlLanguageServer(indexProvider, typeUtilProvider, javaProjectFinder);
			return server;
		});
	}
}
