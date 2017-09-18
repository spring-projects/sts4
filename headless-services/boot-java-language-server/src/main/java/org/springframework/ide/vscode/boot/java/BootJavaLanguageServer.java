/*******************************************************************************
 * Copyright (c) 2016, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java;

import java.util.HashMap;
import java.util.Map;

import org.springframework.ide.vscode.boot.java.beans.BeansSymbolProvider;
import org.springframework.ide.vscode.boot.java.beans.ComponentSymbolProvider;
import org.springframework.ide.vscode.boot.java.handlers.BootJavaCodeLensEngine;
import org.springframework.ide.vscode.boot.java.handlers.BootJavaCompletionEngine;
import org.springframework.ide.vscode.boot.java.handlers.BootJavaDocumentSymbolHandler;
import org.springframework.ide.vscode.boot.java.handlers.BootJavaHoverProvider;
import org.springframework.ide.vscode.boot.java.handlers.BootJavaReconcileEngine;
import org.springframework.ide.vscode.boot.java.handlers.BootJavaReferencesHandler;
import org.springframework.ide.vscode.boot.java.handlers.BootJavaWorkspaceSymbolHandler;
import org.springframework.ide.vscode.boot.java.handlers.CompletionProvider;
import org.springframework.ide.vscode.boot.java.handlers.HoverProvider;
import org.springframework.ide.vscode.boot.java.handlers.ReferenceProvider;
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
import org.springframework.ide.vscode.boot.java.requestmapping.RequestMappingHoverProvider;
import org.springframework.ide.vscode.boot.java.requestmapping.RequestMappingSymbolProvider;
import org.springframework.ide.vscode.boot.java.scope.ScopeCompletionProcessor;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexer;
import org.springframework.ide.vscode.boot.java.value.ValueCompletionProcessor;
import org.springframework.ide.vscode.boot.java.value.ValueHoverProvider;
import org.springframework.ide.vscode.boot.java.value.ValuePropertyReferencesProvider;
import org.springframework.ide.vscode.boot.metadata.SpringPropertyIndexProvider;
import org.springframework.ide.vscode.commons.gradle.GradleCore;
import org.springframework.ide.vscode.commons.gradle.GradleProjectFinderStrategy;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionEngine;
import org.springframework.ide.vscode.commons.languageserver.completion.VscodeCompletionEngineAdapter;
import org.springframework.ide.vscode.commons.languageserver.java.DefaultJavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.IJavaProjectFinderStrategy;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.util.HoverHandler;
import org.springframework.ide.vscode.commons.languageserver.util.ReferencesHandler;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleWorkspaceService;
import org.springframework.ide.vscode.commons.maven.JavaProjectWithClasspathFileFinderStrategy;
import org.springframework.ide.vscode.commons.maven.MavenCore;
import org.springframework.ide.vscode.commons.maven.MavenProjectFinderStrategy;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * Language Server for Spring Boot Application Properties files
 *
 * @author Martin Lippert
 */
public class BootJavaLanguageServer extends SimpleLanguageServer {

	public static final JavaProjectFinder DEFAULT_PROJECT_FINDER = new DefaultJavaProjectFinder(new IJavaProjectFinderStrategy[] {
			new MavenProjectFinderStrategy(MavenCore.getDefault()),
			new GradleProjectFinderStrategy(GradleCore.getDefault()),
			new JavaProjectWithClasspathFileFinderStrategy()
	});

	private final VscodeCompletionEngineAdapter completionEngine;

	public BootJavaLanguageServer(JavaProjectFinder javaProjectFinder, SpringPropertyIndexProvider indexProvider) {
		super("vscode-boot-java");
		SimpleWorkspaceService workspaceService = getWorkspaceService();
		SimpleTextDocumentService documents = getTextDocumentService();

		IReconcileEngine reconcileEngine = new BootJavaReconcileEngine();
		documents.onDidChangeContent(params -> {
			TextDocument doc = params.getDocument();
			validateWith(doc.getId(), reconcileEngine);
		});

		ICompletionEngine bootCompletionEngine = createCompletionEngine(javaProjectFinder, indexProvider);
		completionEngine = createCompletionEngineAdapter(this, bootCompletionEngine);
		completionEngine.setMaxCompletions(100);
		documents.onCompletion(completionEngine::getCompletions);
		documents.onCompletionResolve(completionEngine::resolveCompletion);

		HoverHandler hoverInfoProvider = createHoverHandler(javaProjectFinder);
		documents.onHover(hoverInfoProvider);

		ReferencesHandler referencesHandler = createReferenceHandler(this, javaProjectFinder);
		documents.onReferences(referencesHandler);

		SpringIndexer indexer = createAnnotationIndexer(this, javaProjectFinder);
		documents.onDocumentSymbol(new BootJavaDocumentSymbolHandler(indexer));
		workspaceService.onWorkspaceSymbol(new BootJavaWorkspaceSymbolHandler(indexer));

		BootJavaCodeLensEngine codeLensHandler = createCodeLensEngine(this, javaProjectFinder);
		documents.onCodeLens(codeLensHandler::createCodeLenses);
		documents.onCodeLensResolve(codeLensHandler::resolveCodeLens);

	}

	public void setMaxCompletionsNumber(int number) {
		completionEngine.setMaxCompletions(number);
	}

	protected ICompletionEngine createCompletionEngine(JavaProjectFinder javaProjectFinder, SpringPropertyIndexProvider indexProvider) {
		Map<String, CompletionProvider> providers = new HashMap<>();
		providers.put(org.springframework.ide.vscode.boot.java.scope.Constants.SPRING_SCOPE, new ScopeCompletionProcessor());
		providers.put(org.springframework.ide.vscode.boot.java.value.Constants.SPRING_VALUE, new ValueCompletionProcessor(indexProvider));

		return new BootJavaCompletionEngine(javaProjectFinder, providers);
	}

	protected HoverHandler createHoverHandler(JavaProjectFinder javaProjectFinder) {
		HashMap<String, HoverProvider> providers = new HashMap<>();
		providers.put(org.springframework.ide.vscode.boot.java.value.Constants.SPRING_VALUE, new ValueHoverProvider());
		providers.put(org.springframework.ide.vscode.boot.java.requestmapping.Constants.SPRING_REQUEST_MAPPING, new RequestMappingHoverProvider());
		providers.put(org.springframework.ide.vscode.boot.java.requestmapping.Constants.SPRING_GET_MAPPING, new RequestMappingHoverProvider());
		providers.put(org.springframework.ide.vscode.boot.java.requestmapping.Constants.SPRING_POST_MAPPING, new RequestMappingHoverProvider());
		providers.put(org.springframework.ide.vscode.boot.java.requestmapping.Constants.SPRING_PUT_MAPPING, new RequestMappingHoverProvider());
		providers.put(org.springframework.ide.vscode.boot.java.requestmapping.Constants.SPRING_DELETE_MAPPING, new RequestMappingHoverProvider());
		providers.put(org.springframework.ide.vscode.boot.java.requestmapping.Constants.SPRING_PATCH_MAPPING, new RequestMappingHoverProvider());

		return new BootJavaHoverProvider(this, javaProjectFinder, providers);
	}

	protected SpringIndexer createAnnotationIndexer(SimpleLanguageServer server, JavaProjectFinder projectFinder) {
		HashMap<String, SymbolProvider> providers = new HashMap<>();
		providers.put(org.springframework.ide.vscode.boot.java.requestmapping.Constants.SPRING_REQUEST_MAPPING, new RequestMappingSymbolProvider());
		providers.put(org.springframework.ide.vscode.boot.java.requestmapping.Constants.SPRING_GET_MAPPING, new RequestMappingSymbolProvider());
		providers.put(org.springframework.ide.vscode.boot.java.requestmapping.Constants.SPRING_POST_MAPPING, new RequestMappingSymbolProvider());
		providers.put(org.springframework.ide.vscode.boot.java.requestmapping.Constants.SPRING_PUT_MAPPING, new RequestMappingSymbolProvider());
		providers.put(org.springframework.ide.vscode.boot.java.requestmapping.Constants.SPRING_DELETE_MAPPING, new RequestMappingSymbolProvider());
		providers.put(org.springframework.ide.vscode.boot.java.requestmapping.Constants.SPRING_PATCH_MAPPING, new RequestMappingSymbolProvider());

		providers.put(org.springframework.ide.vscode.boot.java.beans.Constants.SPRING_BEAN, new BeansSymbolProvider());
		providers.put(org.springframework.ide.vscode.boot.java.beans.Constants.SPRING_COMPONENT, new ComponentSymbolProvider());

		return new SpringIndexer(this, projectFinder, providers);
	}

	protected ReferencesHandler createReferenceHandler(SimpleLanguageServer server, JavaProjectFinder projectFinder) {
		Map<String, ReferenceProvider> providers = new HashMap<>();
		providers.put(org.springframework.ide.vscode.boot.java.value.Constants.SPRING_VALUE, new ValuePropertyReferencesProvider(server));

		return new BootJavaReferencesHandler(server, projectFinder, providers);
	}

	protected BootJavaCodeLensEngine createCodeLensEngine(SimpleLanguageServer server, JavaProjectFinder projectFinder) {
		return new BootJavaCodeLensEngine(server, projectFinder);
	}


}
