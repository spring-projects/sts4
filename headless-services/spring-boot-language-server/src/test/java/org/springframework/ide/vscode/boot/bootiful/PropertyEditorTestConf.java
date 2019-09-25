/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.bootiful;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.app.BootLanguageServerParams;
import org.springframework.ide.vscode.boot.editor.harness.PropertyIndexHarness;
import org.springframework.ide.vscode.boot.java.links.JavaDocumentUriProvider;
import org.springframework.ide.vscode.boot.java.links.SourceLinkFactory;
import org.springframework.ide.vscode.boot.java.links.SourceLinks;
import org.springframework.ide.vscode.boot.java.utils.CompilationUnitCache;
import org.springframework.ide.vscode.boot.java.utils.SymbolCache;
import org.springframework.ide.vscode.boot.java.utils.SymbolCacheVoid;
import org.springframework.ide.vscode.boot.metadata.ValueProviderRegistry;
import org.springframework.ide.vscode.boot.metadata.types.TypeUtil;
import org.springframework.ide.vscode.boot.metadata.types.TypeUtilProvider;
import org.springframework.ide.vscode.boot.test.DefinitionLinkAsserts;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;

@Configuration
@Import(AdHocPropertyHarnessTestConf.class)
public class PropertyEditorTestConf {

	@Bean SymbolCache symbolCache() {
		return new SymbolCacheVoid();
	}

	@Bean PropertyIndexHarness indexHarness(ValueProviderRegistry valueProviders) {
		return new PropertyIndexHarness(valueProviders);
	}

	@Bean BootLanguageServerHarness harness(
			SimpleLanguageServer server,
			BootLanguageServerParams serverParams,
			PropertyIndexHarness indexHarness,
			JavaProjectFinder projectFinder,
			LanguageId defaultLanguageId,
			@Qualifier("defaultFileExtension") String defaultFileExtension
	) throws Exception {
		return new BootLanguageServerHarness(server, serverParams, indexHarness, projectFinder, defaultLanguageId, defaultFileExtension);
	}

	@Bean BootLanguageServerParams serverParams(SimpleLanguageServer server, PropertyIndexHarness indexHarness) {
		JavaProjectFinder projectFinder = indexHarness.getProjectFinder();
		TypeUtilProvider typeUtilProvider = (SourceLinks sourceLinks, IDocument doc) -> new TypeUtil(sourceLinks, projectFinder.find(new TextDocumentIdentifier(doc.getUri())));

		return new BootLanguageServerParams(
				projectFinder,
				ProjectObserver.NULL,
				indexHarness.getIndexProvider(),
				typeUtilProvider
		);
	}

	@Bean JavaProjectFinder projectFinder(BootLanguageServerParams serverParams) {
		return serverParams.projectFinder;
	}

	@Bean SourceLinks sourceLinks(CompilationUnitCache cuCache) {
		return SourceLinkFactory.NO_SOURCE_LINKS;
	}

	@Bean DefinitionLinkAsserts definitionLinkAsserts(JavaDocumentUriProvider javaDocumentUriProvider, CompilationUnitCache cuCache) {
		return new DefinitionLinkAsserts(javaDocumentUriProvider, cuCache);
	}

}