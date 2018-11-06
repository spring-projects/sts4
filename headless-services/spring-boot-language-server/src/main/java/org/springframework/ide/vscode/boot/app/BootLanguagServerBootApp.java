/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.ide.vscode.boot.java.links.DefaultJavaElementLocationProvider;
import org.springframework.ide.vscode.boot.java.links.JavaElementLocationProvider;
import org.springframework.ide.vscode.boot.java.links.JavaDocumentUriProvider;
import org.springframework.ide.vscode.boot.java.links.JdtJavaDocumentUriProvider;
import org.springframework.ide.vscode.boot.java.links.SourceLinkFactory;
import org.springframework.ide.vscode.boot.java.links.SourceLinks;
import org.springframework.ide.vscode.boot.java.utils.CompilationUnitCache;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.LogRedirect;

@SpringBootApplication
public class BootLanguagServerBootApp {
	private static final String SERVER_NAME = "boot-language-server";

	public static void main(String[] args) throws Exception {
		LogRedirect.bootRedirectToFile(SERVER_NAME); //TODO: use boot (or logback realy) to configure logging instead.
		SpringApplication.run(BootLanguagServerBootApp.class, args);
	}

	@Bean public String serverName() {
		return SERVER_NAME;
	}

	@ConditionalOnMissingClass("org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness")
	@Bean BootLanguageServerParams serverParams(SimpleLanguageServer server) {
		return BootLanguageServerParams.createDefault(server);
	}

	@ConditionalOnMissingClass("org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness")
	@Bean SourceLinks sourceLinks(CompilationUnitCache cuCache) {
		return SourceLinkFactory.createSourceLinks(cuCache);
	}

	@Bean CompilationUnitCache cuCache(BootLanguageServerParams params, SimpleTextDocumentService documents) {
		return new CompilationUnitCache(params.projectFinder, documents, params.projectObserver);
	}

	@Bean JavaDocumentUriProvider javaDocumentUriProvider() {
		return new JdtJavaDocumentUriProvider();
	}

	@Bean JavaElementLocationProvider javaElementLocationProvider(CompilationUnitCache cuCache, JavaDocumentUriProvider javaDocUriProvider) {
		return new DefaultJavaElementLocationProvider(cuCache, javaDocUriProvider);
	}

}
