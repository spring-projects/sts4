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
package org.springframework.ide.vscode.languageserver.starter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ide.vscode.commons.languageserver.completion.VscodeCompletionEngineAdapter.CompletionFilter;
import org.springframework.ide.vscode.commons.languageserver.config.LanguageServerInitializer;
import org.springframework.ide.vscode.commons.languageserver.config.LanguageServerProperties;
import org.springframework.ide.vscode.commons.languageserver.reconcile.DiagnosticSeverityProvider;
import org.springframework.ide.vscode.commons.languageserver.util.DefinitionHandler;
import org.springframework.ide.vscode.commons.languageserver.util.LanguageSpecific;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

@Configuration
@EnableConfigurationProperties(LanguageServerProperties.class)
public class LanguageServerAutoConf {
	
	@ConditionalOnMissingBean
	@Bean public SimpleLanguageServer languageServer(
			LanguageServerProperties props, 
			Optional<DiagnosticSeverityProvider> severities,
			Optional<CompletionFilter> completionFilter
	) throws Exception {
		SimpleLanguageServer server = new SimpleLanguageServer(props.getExtensionId());
		server.setCompletionTriggerCharacters(props.getCompletionTriggerCharacters());
		server.setCompletionFilter(completionFilter);
		severities.ifPresent(server::setDiagnosticSeverityProvider);
		return server;
	}
	
	@ConditionalOnBean({LanguageServerInitializer.class, SimpleLanguageServer.class})
	@Bean
	InitializingBean initializer(SimpleLanguageServer server, LanguageServerInitializer serverInit) {
		return () -> {
			serverInit.initialize(server);
		};
	}

	@Bean SimpleTextDocumentService documents(SimpleLanguageServer ls) {
		return ls.getTextDocumentService();
	}

	@ConditionalOnBean(DefinitionHandler.class)
	@Bean
	InitializingBean registerDefintionHandler(SimpleTextDocumentService documents,
			List<DefinitionHandler> definitionHandlers) {
		if (definitionHandlers.size() == 1) {
			return () -> documents.onDefinition(definitionHandlers.get(0));
		} else {
			Map<LanguageId, DefinitionHandler> handlers = new HashMap<>(definitionHandlers.size());
			for (DefinitionHandler h : definitionHandlers) {
				Assert.isInstanceOf(LanguageSpecific.class, h, "Only language specific defintion handlers supported!");
				for (LanguageId l : ((LanguageSpecific)h).supportedLanguages()) {
					Assert.isTrue(!handlers.containsKey(l), "Multiple definition handlers for the same language not supported!");
					handlers.put(l, h);
				}
			}
			ImmutableMap<LanguageId, DefinitionHandler> immutableMap = ImmutableMap.copyOf(handlers);
			return () -> documents.onDefinition((position) -> {
				TextDocument doc = documents.get(position.getTextDocument().getUri());
				if (doc != null) {
					LanguageId language = doc.getLanguageId();
					DefinitionHandler handler = immutableMap.get(language);
					if (handler != null) {
						return handler.handle(position);
					}
				}
				return ImmutableList.of();
 			});
		}
	}
	
}
