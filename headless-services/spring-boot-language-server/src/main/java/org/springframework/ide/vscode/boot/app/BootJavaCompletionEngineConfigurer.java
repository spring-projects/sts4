/*******************************************************************************
 * Copyright (c) 2020, 2023 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.app;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.CompletionItemKind;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchies;
import org.springframework.ide.vscode.boot.java.data.DataRepositoryCompletionProcessor;
import org.springframework.ide.vscode.boot.java.handlers.BootJavaCompletionEngine;
import org.springframework.ide.vscode.boot.java.handlers.CompletionProvider;
import org.springframework.ide.vscode.boot.java.scope.ScopeCompletionProcessor;
import org.springframework.ide.vscode.boot.java.snippets.JavaSnippet;
import org.springframework.ide.vscode.boot.java.snippets.JavaSnippetContext;
import org.springframework.ide.vscode.boot.java.snippets.JavaSnippetManager;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
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
			
			JavaSnippetContext webControllerContext = new CompositeJavaSnippetContext(
					JavaSnippetContext.AT_ROOT_LEVEL,
					new AnnotatedTypeDeclarationContext(Annotations.CONTROLLER));
			
			snippetManager.add(
					new JavaSnippet("@RequestMapping(..) {..}", webControllerContext, CompletionItemKind.Method,
							ImmutableList.of("org.springframework.web.bind.annotation.RequestMapping",
									"org.springframework.web.bind.annotation.RequestMethod",
									"org.springframework.web.bind.annotation.RequestParam"),
							"@RequestMapping(\"${path}\", method=RequestMethod.${GET})\n"
									+ "public ${SomeData} ${requestMethodName}(@RequestParam ${String} ${param}) {\n"
									+ "	return new ${SomeData}(${cursor});\n" + "}\n",
							"RequestMapping"));
			snippetManager.add(
					new JavaSnippet("@GetMapping(..) {..}", webControllerContext, CompletionItemKind.Method,
							ImmutableList.of("org.springframework.web.bind.annotation.GetMapping",
									"org.springframework.web.bind.annotation.RequestParam"),
							"@GetMapping(\"${path}\")\n"
									+ "public ${SomeData} ${getMethodName}(@RequestParam ${String} ${param}) {\n"
									+ "	return new ${SomeData}(${cursor});\n" + "}\n",
							"GetMapping"));
			snippetManager.add(
					new JavaSnippet("@PostMapping(..) {..}", webControllerContext, CompletionItemKind.Method,
							ImmutableList.of("org.springframework.web.bind.annotation.PostMapping",
									"org.springframework.web.bind.annotation.RequestBody"),
							"@PostMapping(\"${path}\")\n"
									+ "public ${SomeEnityData} ${postMethodName}(@RequestBody ${SomeEnityData} ${entity}) {\n"
									+ "	//TODO: process POST request\n" + "	${cursor}\n" + "	return ${entity};\n" + "}\n",
							"PostMapping"));
			snippetManager.add(
					new JavaSnippet("@PutMapping(..) {..}", webControllerContext, CompletionItemKind.Method,
							ImmutableList.of("org.springframework.web.bind.annotation.PutMapping",
									"org.springframework.web.bind.annotation.RequestBody",
									"org.springframework.web.bind.annotation.PathVariable"),
							"@PutMapping(\"${path}/{${id}}\")\n"
									+ "public ${SomeEnityData} ${putMethodName}(@PathVariable ${pvt:String} ${id}, @RequestBody ${SomeEnityData} ${entity}) {\n"
									+ "	//TODO: process PUT request\n" + "	${cursor}\n" + "	return ${entity};\n" + "}",
							"PutMapping"));
		}

		return snippetManager;
	}

	@Bean
	BootJavaCompletionEngine javaCompletionEngine(
			BootLanguageServerParams params,
			@Qualifier("adHocProperties") ProjectBasedPropertyIndexProvider adHocProperties,
			JavaSnippetManager snippetManager, 
			CompilationUnitCache cuCache) {
		SpringPropertyIndexProvider indexProvider = params.indexProvider;
		JavaProjectFinder javaProjectFinder = params.projectFinder;
		Map<String, CompletionProvider> providers = new HashMap<>();
		
		providers.put(Annotations.SCOPE, new ScopeCompletionProcessor());
		providers.put(Annotations.VALUE, new ValueCompletionProcessor(javaProjectFinder, indexProvider, adHocProperties));
		providers.put(Annotations.REPOSITORY, new DataRepositoryCompletionProcessor());

		return new BootJavaCompletionEngine(cuCache, providers, snippetManager);
	}
	
	/**
	 * checks if the type declaration that belongs to the node is annotated with the required annotation
	 */
	private class AnnotatedTypeDeclarationContext implements JavaSnippetContext {
		
		private final String requiredAnnotation;

		public AnnotatedTypeDeclarationContext(String requiredAnnotation) {
			this.requiredAnnotation = requiredAnnotation;
		}

		@Override
		public boolean appliesTo(ASTNode node) {
			TypeDeclaration type = ASTUtils.findDeclaringType(node);
			if (type != null) {
				ITypeBinding binding = type.resolveBinding();
				if (binding != null) {
					IAnnotationBinding[] annotations = binding.getAnnotations();
					if (annotations != null) {
						for (int i = 0; i < annotations.length; i++) {
							ITypeBinding annotationType = annotations[i].getAnnotationType();
							if (AnnotationHierarchies.isMetaAnnotation(annotationType, (name) -> requiredAnnotation.equals(name))) {
								return true;
							}
						}
					}
				}
			}
			return false;
		}
	}
	
	/**
	 * composite implementation for snippet contexts that allows you to combine multiple context checks
	 * The check only passes if ALL individual context checks are successful
	 */
	private class CompositeJavaSnippetContext implements JavaSnippetContext {
		
		private final JavaSnippetContext[] contexts;

		public CompositeJavaSnippetContext(JavaSnippetContext... contexts) {
			this.contexts = contexts;
		}

		@Override
		public boolean appliesTo(ASTNode node) {
			for (JavaSnippetContext context : contexts) {
				if (!context.appliesTo(node)) {
					return false;
				}
			}
			return contexts.length > 0;
		}
	}
	
}
