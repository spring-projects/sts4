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
package org.springframework.ide.vscode.boot.app;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.ide.vscode.boot.common.PropertyCompletionFactory;
import org.springframework.ide.vscode.boot.common.RelaxedNameConfig;
import org.springframework.ide.vscode.boot.java.handlers.RunningAppProvider;
import org.springframework.ide.vscode.boot.java.links.DefaultJavaElementLocationProvider;
import org.springframework.ide.vscode.boot.java.links.EclipseJavaDocumentUriProvider;
import org.springframework.ide.vscode.boot.java.links.JavaDocumentUriProvider;
import org.springframework.ide.vscode.boot.java.links.JavaElementLocationProvider;
import org.springframework.ide.vscode.boot.java.links.JavaServerElementLocationProvider;
import org.springframework.ide.vscode.boot.java.links.JdtJavaDocumentUriProvider;
import org.springframework.ide.vscode.boot.java.links.SourceLinkFactory;
import org.springframework.ide.vscode.boot.java.links.SourceLinks;
import org.springframework.ide.vscode.boot.java.utils.CompilationUnitCache;
import org.springframework.ide.vscode.boot.java.utils.SymbolCache;
import org.springframework.ide.vscode.boot.java.utils.SymbolCacheOnDisc;
import org.springframework.ide.vscode.boot.java.utils.SymbolCacheVoid;
import org.springframework.ide.vscode.boot.metadata.AdHocSpringPropertyIndexProvider;
import org.springframework.ide.vscode.boot.metadata.ClassReferenceProvider;
import org.springframework.ide.vscode.boot.metadata.LoggerNameProvider;
import org.springframework.ide.vscode.boot.metadata.ProjectBasedPropertyIndexProvider;
import org.springframework.ide.vscode.boot.metadata.PropertyInfo;
import org.springframework.ide.vscode.boot.metadata.ValueProviderRegistry;
import org.springframework.ide.vscode.boot.yaml.completions.ApplicationYamlAssistContext;
import org.springframework.ide.vscode.commons.languageserver.LanguageServerRunner;
import org.springframework.ide.vscode.commons.languageserver.util.DocumentEventListenerManager;
import org.springframework.ide.vscode.commons.languageserver.util.LspClient;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.FileObserver;
import org.springframework.ide.vscode.commons.util.FuzzyMap;
import org.springframework.ide.vscode.commons.util.LogRedirect;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.yaml.ast.YamlASTProvider;
import org.springframework.ide.vscode.commons.yaml.ast.YamlParser;
import org.springframework.ide.vscode.commons.yaml.completion.YamlAssistContext;
import org.springframework.ide.vscode.commons.yaml.completion.YamlAssistContextProvider;
import org.springframework.ide.vscode.commons.yaml.structure.YamlDocument;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureProvider;
import org.yaml.snakeyaml.Yaml;

@SpringBootApplication(proxyBeanMethods = false)
public class BootLanguagServerBootApp {
	
	private static final String SERVER_NAME = "boot-language-server";

	public static void main(String[] args) throws Exception {
		System.setProperty(LanguageServerRunner.SYSPROP_LANGUAGESERVER_NAME, SERVER_NAME); //makes it easy to recognize language server processes - and set this as early as possible
		
		LogRedirect.bootRedirectToFile(SERVER_NAME); //TODO: use boot (or logback realy) to configure logging instead.
		SpringApplication.run(BootLanguagServerBootApp.class, args);
	}

	@ConditionalOnMissingClass("org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness")
	@Bean
	SymbolCache symbolCache(BootLsConfigProperties props) {
		if (props.isSymbolCacheEnabled()) {
			return new SymbolCacheOnDisc();
		} else {
			return new SymbolCacheVoid();
		}
	}

	@ConditionalOnMissingClass("org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness")
	@Bean
	RunningAppProvider runningAppProvider(SimpleLanguageServer server) {
		return RunningAppProvider.createDefault(server);
	}

	@ConditionalOnMissingClass("org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness")
	@Bean AdHocSpringPropertyIndexProvider adHocProperties(BootLanguageServerParams params, FileObserver fileObserver, DocumentEventListenerManager documentEvents) {
		return new AdHocSpringPropertyIndexProvider(params.projectFinder, params.projectObserver, fileObserver, documentEvents);
	}

	@Bean FileObserver fileObserver(SimpleLanguageServer server) {
		return server.getWorkspaceService().getFileObserver();
	}

	@Bean ValueProviderRegistry valueProviders() {
		return new ValueProviderRegistry();
	}

	@Bean InitializingBean initializeValueProviders(ValueProviderRegistry r, @Qualifier("adHocProperties") ProjectBasedPropertyIndexProvider adHocProperties, SourceLinks sourceLinks) {
		return () -> {
			r.def("logger-name", LoggerNameProvider.factory(adHocProperties, sourceLinks));
			r.def("class-reference", ClassReferenceProvider.factory(sourceLinks));
		};
	}

	@ConditionalOnMissingClass("org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness")
	@Bean BootLanguageServerParams serverParams(SimpleLanguageServer server, ValueProviderRegistry valueProviders, BootLsConfigProperties configProperties) {
		return BootLanguageServerParams.createDefault(server, valueProviders, configProperties.isEnableJandexIndex());
	}

	@ConditionalOnMissingClass("org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness")
	@Bean SourceLinks sourceLinks(SimpleLanguageServer server, CompilationUnitCache cuCache, BootLanguageServerParams params) {
		return SourceLinkFactory.createSourceLinks(server, cuCache, params.projectFinder);
	}

	@Bean CompilationUnitCache cuCache(BootLanguageServerParams params, SimpleTextDocumentService documents) {
		return new CompilationUnitCache(params.projectFinder, documents, params.projectObserver);
	}

	@Bean JavaDocumentUriProvider javaDocumentUriProvider() {
		switch (LspClient.currentClient()) {
		case ECLIPSE:
			// LSP4E doesn't support JDT java doc URIs. Only supports file, eclipse intro and http URIs for docs
			return new EclipseJavaDocumentUriProvider();
		default:
			return new JdtJavaDocumentUriProvider();
		}
	}

	@Bean JavaElementLocationProvider javaElementLocationProvider(SimpleLanguageServer server, CompilationUnitCache cuCache, JavaDocumentUriProvider javaDocUriProvider) {
		switch (LspClient.currentClient()) {
		case ECLIPSE:
		case VSCODE:
		case THEIA:
			return new JavaServerElementLocationProvider(server);
		default:
			return new DefaultJavaElementLocationProvider(cuCache, javaDocUriProvider);
		}
	}

	@Bean Yaml yaml() {
		//TODO: Yaml is not re-entrant. So its a bit fishy to create a 're-usable' bean for this!
		return new Yaml();
	}

	@Bean YamlASTProvider yamlAstProvider() {
		return new YamlParser();
	}

	@Bean YamlStructureProvider yamlStructureProvider() {
		return YamlStructureProvider.DEFAULT;
	}

	@Bean YamlAssistContextProvider yamlAssistContextProvider(BootLanguageServerParams params, JavaElementLocationProvider javaElementLocationProvider, SourceLinks sourceLinks) {
		return new YamlAssistContextProvider() {
			@Override
			public YamlAssistContext getGlobalAssistContext(YamlDocument ydoc) {
				IDocument doc = ydoc.getDocument();
				FuzzyMap<PropertyInfo> index = params.indexProvider.getIndex(doc);
				return ApplicationYamlAssistContext.global(ydoc, index, new PropertyCompletionFactory(), params.typeUtilProvider.getTypeUtil(sourceLinks, doc), RelaxedNameConfig.COMPLETION_DEFAULTS, javaElementLocationProvider);
			}
		};
	}

}
