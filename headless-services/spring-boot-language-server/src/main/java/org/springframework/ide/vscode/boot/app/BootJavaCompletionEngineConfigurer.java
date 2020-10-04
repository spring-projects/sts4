/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.app;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.lsp4j.CompletionItemKind;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.data.DataRepositoryCompletionProcessor;
import org.springframework.ide.vscode.boot.java.handlers.BootJavaCompletionEngine;
import org.springframework.ide.vscode.boot.java.handlers.CompletionProvider;
import org.springframework.ide.vscode.boot.java.scope.ScopeCompletionProcessor;
import org.springframework.ide.vscode.boot.java.snippets.JavaSnippet;
import org.springframework.ide.vscode.boot.java.snippets.JavaSnippetContext;
import org.springframework.ide.vscode.boot.java.snippets.JavaSnippetManager;
import org.springframework.ide.vscode.boot.java.utils.CompilationUnitCache;
import org.springframework.ide.vscode.boot.java.value.ValueCompletionProcessor;
import org.springframework.ide.vscode.boot.metadata.ProjectBasedPropertyIndexProvider;
import org.springframework.ide.vscode.boot.metadata.SpringPropertyIndexProvider;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.LspClient;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;

import com.google.common.collect.ImmutableList;

@Configuration
public class BootJavaCompletionEngineConfigurer {
	

	@Bean JavaSnippetManager javaSnippetManager(SimpleLanguageServer server) {
		JavaSnippetManager snippetManager = new JavaSnippetManager(server::createSnippetBuilder);

		// PT 160529904: Eclipse templates are duplicated, due to templates in Eclipse also being contributed by
		// STS3 bundle. Therefore do not include templates if client is Eclipse
		// TODO: REMOVE this check once STS3 is no longer supported
		if (LspClient.currentClient() != LspClient.Client.ECLIPSE) {
			snippetManager.add(
					new JavaSnippet("RequestMapping method", JavaSnippetContext.BOOT_MEMBERS, CompletionItemKind.Method,
							ImmutableList.of("org.springframework.web.bind.annotation.RequestMapping",
									"org.springframework.web.bind.annotation.RequestMethod",
									"org.springframework.web.bind.annotation.RequestParam"),
							"@RequestMapping(value=\"${path}\", method=RequestMethod.${GET})\n"
									+ "public ${SomeData} ${requestMethodName}(@RequestParam ${String} ${param}) {\n"
									+ "	return new ${SomeData}(${cursor});\n" + "}\n"));
			snippetManager
					.add(new JavaSnippet("GetMapping method", JavaSnippetContext.BOOT_MEMBERS, CompletionItemKind.Method,
							ImmutableList.of("org.springframework.web.bind.annotation.GetMapping",
									"org.springframework.web.bind.annotation.RequestParam"),
							"@GetMapping(value=\"${path}\")\n"
									+ "public ${SomeData} ${getMethodName}(@RequestParam ${String} ${param}) {\n"
									+ "	return new ${SomeData}(${cursor});\n" + "}\n"));
			snippetManager.add(new JavaSnippet("PostMapping method", JavaSnippetContext.BOOT_MEMBERS,
					CompletionItemKind.Method,
					ImmutableList.of("org.springframework.web.bind.annotation.PostMapping",
							"org.springframework.web.bind.annotation.RequestBody"),
					"@PostMapping(value=\"${path}\")\n"
							+ "public ${SomeEnityData} ${postMethodName}(@RequestBody ${SomeEnityData} ${entity}) {\n"
							+ "	//TODO: process POST request\n" + "	${cursor}\n" + "	return ${entity};\n" + "}\n"));
			snippetManager.add(new JavaSnippet("PutMapping method", JavaSnippetContext.BOOT_MEMBERS,
					CompletionItemKind.Method,
					ImmutableList.of("org.springframework.web.bind.annotation.PutMapping",
							"org.springframework.web.bind.annotation.RequestBody",
							"org.springframework.web.bind.annotation.PathVariable"),
					"@PutMapping(value=\"${path}/{${id}}\")\n"
							+ "public ${SomeEnityData} ${putMethodName}(@PathVariable ${pvt:String} ${id}, @RequestBody ${SomeEnityData} ${entity}) {\n"
							+ "	//TODO: process PUT request\n" + "	${cursor}\n" + "	return ${entity};\n" + "}"));
		}

		return snippetManager;
	}

	@Bean
	BootJavaCompletionEngine javaCompletionEngine(
			BootLanguageServerParams params,
			@Qualifier("adHocProperties") ProjectBasedPropertyIndexProvider adHocProperties,
			JavaSnippetManager snippetManager, 
			CompilationUnitCache cuCache
	) {
		SpringPropertyIndexProvider indexProvider = params.indexProvider;
		JavaProjectFinder javaProjectFinder = params.projectFinder;
		Map<String, CompletionProvider> providers = new HashMap<>();
		providers.put(org.springframework.ide.vscode.boot.java.scope.Constants.SPRING_SCOPE,
				new ScopeCompletionProcessor());
		providers.put(org.springframework.ide.vscode.boot.java.value.Constants.SPRING_VALUE,
				new ValueCompletionProcessor(javaProjectFinder, indexProvider, adHocProperties));
		providers.put(Annotations.REPOSITORY, new DataRepositoryCompletionProcessor());
		return new BootJavaCompletionEngine(cuCache, providers, snippetManager);
	}
}
