/*******************************************************************************
 * Copyright (c) 2016, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InitializeParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.app.BootLanguageServerParams;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchyAwareLookup;
import org.springframework.ide.vscode.boot.java.autowired.AutowiredHoverProvider;
import org.springframework.ide.vscode.boot.java.beans.BeansSymbolProvider;
import org.springframework.ide.vscode.boot.java.beans.ComponentSymbolProvider;
import org.springframework.ide.vscode.boot.java.conditionals.ConditionalsLiveHoverProvider;
import org.springframework.ide.vscode.boot.java.data.DataRepositoryCompletionProcessor;
import org.springframework.ide.vscode.boot.java.data.DataRepositorySymbolProvider;
import org.springframework.ide.vscode.boot.java.handlers.BootJavaCodeLensEngine;
import org.springframework.ide.vscode.boot.java.handlers.BootJavaCompletionEngine;
import org.springframework.ide.vscode.boot.java.handlers.BootJavaDocumentHighlightEngine;
import org.springframework.ide.vscode.boot.java.handlers.BootJavaDocumentSymbolHandler;
import org.springframework.ide.vscode.boot.java.handlers.BootJavaHoverProvider;
import org.springframework.ide.vscode.boot.java.handlers.BootJavaReferencesHandler;
import org.springframework.ide.vscode.boot.java.handlers.BootJavaWorkspaceSymbolHandler;
import org.springframework.ide.vscode.boot.java.handlers.CodeLensProvider;
import org.springframework.ide.vscode.boot.java.handlers.CompletionProvider;
import org.springframework.ide.vscode.boot.java.handlers.HighlightProvider;
import org.springframework.ide.vscode.boot.java.handlers.HoverProvider;
import org.springframework.ide.vscode.boot.java.handlers.ReferenceProvider;
import org.springframework.ide.vscode.boot.java.handlers.RunningAppProvider;
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
import org.springframework.ide.vscode.boot.java.links.SourceLinks;
import org.springframework.ide.vscode.boot.java.livehover.ActiveProfilesProvider;
import org.springframework.ide.vscode.boot.java.livehover.BeanInjectedIntoHoverProvider;
import org.springframework.ide.vscode.boot.java.livehover.ComponentInjectionsHoverProvider;
import org.springframework.ide.vscode.boot.java.requestmapping.LiveAppURLSymbolProvider;
import org.springframework.ide.vscode.boot.java.requestmapping.RequestMappingHoverProvider;
import org.springframework.ide.vscode.boot.java.requestmapping.RequestMappingSymbolProvider;
import org.springframework.ide.vscode.boot.java.requestmapping.WebfluxHandlerCodeLensProvider;
import org.springframework.ide.vscode.boot.java.requestmapping.WebfluxRouteHighlightProdivder;
import org.springframework.ide.vscode.boot.java.requestmapping.WebfluxRouterSymbolProvider;
import org.springframework.ide.vscode.boot.java.scope.ScopeCompletionProcessor;
import org.springframework.ide.vscode.boot.java.snippets.JavaSnippet;
import org.springframework.ide.vscode.boot.java.snippets.JavaSnippetContext;
import org.springframework.ide.vscode.boot.java.snippets.JavaSnippetManager;
import org.springframework.ide.vscode.boot.java.utils.CompilationUnitCache;
import org.springframework.ide.vscode.boot.java.utils.RestrictedDefaultSymbolProvider;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexer;
import org.springframework.ide.vscode.boot.java.utils.SpringLiveChangeDetectionWatchdog;
import org.springframework.ide.vscode.boot.java.utils.SpringLiveHoverWatchdog;
import org.springframework.ide.vscode.boot.java.value.ValueCompletionProcessor;
import org.springframework.ide.vscode.boot.java.value.ValueHoverProvider;
import org.springframework.ide.vscode.boot.java.value.ValuePropertyReferencesProvider;
import org.springframework.ide.vscode.boot.metadata.ProjectBasedPropertyIndexProvider;
import org.springframework.ide.vscode.boot.metadata.SpringPropertyIndexProvider;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionEngine;
import org.springframework.ide.vscode.commons.languageserver.composable.LanguageServerComponents;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.util.CodeLensHandler;
import org.springframework.ide.vscode.commons.languageserver.util.DocumentHighlightHandler;
import org.springframework.ide.vscode.commons.languageserver.util.HoverHandler;
import org.springframework.ide.vscode.commons.languageserver.util.LspClient;
import org.springframework.ide.vscode.commons.languageserver.util.ReferencesHandler;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleWorkspaceService;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Language Server for Spring Boot Application Properties files
 *
 * @author Martin Lippert
 */
public class BootJavaLanguageServerComponents implements LanguageServerComponents {

	private static final Set<LanguageId> LANGUAGES = ImmutableSet.of(LanguageId.JAVA);

	private static final Logger log = LoggerFactory.getLogger(BootJavaLanguageServerComponents.class);

	private final SimpleLanguageServer server;
	private final BootLanguageServerParams serverParams;
	private final SpringIndexer indexer;
	private final SpringPropertyIndexProvider propertyIndexProvider;
	private final ProjectBasedPropertyIndexProvider adHocPropertyIndexProvider;
	private final SpringLiveHoverWatchdog liveHoverWatchdog;
	private final SpringLiveChangeDetectionWatchdog liveChangeDetectionWatchdog;
	private final ProjectObserver projectObserver;
	private final BootJavaConfig config;
	private final CompilationUnitCache cuCache;

	private JavaProjectFinder projectFinder;
	private BootJavaHoverProvider hoverProvider;
	private CodeLensHandler codeLensHandler;
	private DocumentHighlightHandler highlightsEngine;

	public BootJavaLanguageServerComponents(
			SimpleLanguageServer server,
			BootLanguageServerParams serverParams,
			SourceLinks sourceLinks,
			CompilationUnitCache cuCache,
			ProjectBasedPropertyIndexProvider adHocIndexProvider
	) {
		this.server = server;
		this.serverParams = serverParams;

		this.config = new BootJavaConfig();

		projectFinder = serverParams.projectFinder;
		projectObserver = serverParams.projectObserver;
		this.cuCache = cuCache;

		propertyIndexProvider = serverParams.indexProvider;
		this.adHocPropertyIndexProvider = adHocIndexProvider;

		SimpleWorkspaceService workspaceService = server.getWorkspaceService();
		SimpleTextDocumentService documents = server.getTextDocumentService();

		ReferencesHandler referencesHandler = createReferenceHandler(server, projectFinder);
		documents.onReferences(referencesHandler);

		indexer = createAnnotationIndexer(server, serverParams);
		documents.onDidSave(params -> {
			TextDocument document = params.getDocument();
			// Spring Boot LS get events from boot properties files as well, so filter them out
			if (getInterestingLanguages().contains(document.getLanguageId())) {
				String docURI = document.getId().getUri();
				String content = document.get();
				indexer.updateDocument(docURI, content);
			}
		});

		documents.onDocumentSymbol(new BootJavaDocumentSymbolHandler(indexer));
		workspaceService.onWorkspaceSymbol(new BootJavaWorkspaceSymbolHandler(indexer,
				new LiveAppURLSymbolProvider(serverParams.runningAppProvider)));

//		BootJavaCodeLensEngine codeLensHandler = createCodeLensEngine(server, projectFinder);
//		documents.onCodeLens(codeLensHandler::createCodeLenses);
//		documents.onCodeLensResolve(codeLensHandler::resolveCodeLens);

		hoverProvider = createHoverHandler(projectFinder, serverParams.runningAppProvider, sourceLinks);
		liveHoverWatchdog = new SpringLiveHoverWatchdog(server, hoverProvider, serverParams.runningAppProvider,
				projectFinder, projectObserver, serverParams.watchDogInterval);
		documents.onDidChangeContent(params -> {
			TextDocument doc = params.getDocument();
			if (getInterestingLanguages().contains(doc.getLanguageId())) {
//				if (testHightlighter != null) {
//					getClient()
//							.highlight(new HighlightParams(params.getDocument().getId(), testHightlighter.apply(doc)));
//				} else {
					try {
						liveHoverWatchdog.watchDocument(doc.getUri());
						liveHoverWatchdog.update(doc.getUri());
					} catch (Throwable t) {
						log.error("", t);
					}
//				}
			}
		});

		documents.onDidClose(doc -> {
//			if (testHightlighter != null) {
//				getClient().highlight(new HighlightParams(doc.getId(), testHightlighter.apply(doc)));
//			} else {
				liveHoverWatchdog.unwatchDocument(doc.getUri());
//			}
		});

		liveChangeDetectionWatchdog = new SpringLiveChangeDetectionWatchdog(
				this,
				server,
				serverParams.projectObserver,
				serverParams.runningAppProvider,
				projectFinder,
				serverParams.watchDogInterval,
				sourceLinks);

		codeLensHandler = createCodeLensEngine();
		documents.onCodeLens(codeLensHandler);

		highlightsEngine = createDocumentHighlightEngine();
		documents.onDocumentHighlight(highlightsEngine);

		workspaceService.onDidChangeConfiguraton(settings -> {
			config.handleConfigurationChange(settings);

			// live hover watchdog
			if (config.isBootHintsEnabled()) {
				liveHoverWatchdog.enableHighlights();
			} else {
				liveHoverWatchdog.disableHighlights();
			}

			// live change detection watchdog
			if (config.isChangeDetectionEnabled()) {
				liveChangeDetectionWatchdog.enableHighlights();
			}
			else {
				liveChangeDetectionWatchdog.disableHighlights();
			}
		});

		server.onInitialize(this::initialize);
		server.doOnInitialized(this::initialized);
		server.onShutdown(this::shutdown);
	}

	public SimpleLanguageServer getServer() {
		return server;
	}

	@Override
	public ICompletionEngine getCompletionEngine() {
		return createCompletionEngine(projectFinder, propertyIndexProvider, adHocPropertyIndexProvider);
	}

	@Override
	public HoverHandler getHoverProvider() {
		return hoverProvider;
	}

	public CodeLensHandler getCodeLensHandler() {
		return codeLensHandler;
	}

	public DocumentHighlightHandler getDocumentHighlightHandler() {
		return highlightsEngine;
	}

	private void initialize(InitializeParams params) {
//		this.indexer.initialize(server.getWorkspaceRoots());
	}

	private void initialized() {
		this.indexer.serverInitialized();
		this.liveChangeDetectionWatchdog.start();

		// TODO: due to a missing message from lsp4e this "initialized" is not called in
		// the LSP4E case
		// if this gets fixed, the code should move here (from "initialize" above)

		// this.indexer.initialize(this.getWorkspaceRoot());
		// this.liveHoverWatchdog.start();
	}

	private void shutdown() {
		this.liveHoverWatchdog.shutdown();
		this.liveChangeDetectionWatchdog.shutdown();
		this.indexer.shutdown();
		this.cuCache.dispose();
	}

	protected ICompletionEngine createCompletionEngine(
			JavaProjectFinder javaProjectFinder,
			SpringPropertyIndexProvider indexProvider,
			ProjectBasedPropertyIndexProvider adHocIndexProvider) {

		Map<String, CompletionProvider> providers = new HashMap<>();
		providers.put(org.springframework.ide.vscode.boot.java.scope.Constants.SPRING_SCOPE,
				new ScopeCompletionProcessor());
		providers.put(org.springframework.ide.vscode.boot.java.value.Constants.SPRING_VALUE,
				new ValueCompletionProcessor(javaProjectFinder, indexProvider, adHocIndexProvider));
		providers.put(Annotations.REPOSITORY, new DataRepositoryCompletionProcessor());

		JavaSnippetManager snippetManager = getSnippets();
		return new BootJavaCompletionEngine(this, providers, snippetManager);
	}

	protected JavaSnippetManager getSnippets() {
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

	protected BootJavaHoverProvider createHoverHandler(JavaProjectFinder javaProjectFinder,
			RunningAppProvider runningAppProvider, SourceLinks sourceLinks) {
		AnnotationHierarchyAwareLookup<HoverProvider> providers = new AnnotationHierarchyAwareLookup<>();

		ValueHoverProvider valueHoverProvider = new ValueHoverProvider();
		RequestMappingHoverProvider requestMappingHoverProvider = new RequestMappingHoverProvider();
		AutowiredHoverProvider autowiredHoverProvider = new AutowiredHoverProvider(sourceLinks);
		ComponentInjectionsHoverProvider componentInjectionsHoverProvider = new ComponentInjectionsHoverProvider(sourceLinks);
		BeanInjectedIntoHoverProvider beanInjectedIntoHoverProvider = new BeanInjectedIntoHoverProvider(sourceLinks);
		ConditionalsLiveHoverProvider conditionalsLiveHoverProvider = new ConditionalsLiveHoverProvider();

		providers.put(org.springframework.ide.vscode.boot.java.value.Constants.SPRING_VALUE, valueHoverProvider);

		providers.put(Annotations.SPRING_REQUEST_MAPPING, requestMappingHoverProvider);
		providers.put(Annotations.SPRING_GET_MAPPING, requestMappingHoverProvider);
		providers.put(Annotations.SPRING_POST_MAPPING, requestMappingHoverProvider);
		providers.put(Annotations.SPRING_PUT_MAPPING, requestMappingHoverProvider);
		providers.put(Annotations.SPRING_DELETE_MAPPING, requestMappingHoverProvider);
		providers.put(Annotations.SPRING_PATCH_MAPPING, requestMappingHoverProvider);
		providers.put(Annotations.PROFILE, new ActiveProfilesProvider());

		providers.put(Annotations.AUTOWIRED, autowiredHoverProvider);
		providers.put(Annotations.INJECT, autowiredHoverProvider);
		providers.put(Annotations.COMPONENT, componentInjectionsHoverProvider);
		providers.put(Annotations.BEAN, beanInjectedIntoHoverProvider);

		providers.put(Annotations.CONDITIONAL, conditionalsLiveHoverProvider);
		providers.put(Annotations.CONDITIONAL_ON_BEAN, conditionalsLiveHoverProvider);
		providers.put(Annotations.CONDITIONAL_ON_MISSING_BEAN, conditionalsLiveHoverProvider);
		providers.put(Annotations.CONDITIONAL_ON_PROPERTY, conditionalsLiveHoverProvider);
		providers.put(Annotations.CONDITIONAL_ON_RESOURCE, conditionalsLiveHoverProvider);
		providers.put(Annotations.CONDITIONAL_ON_CLASS, conditionalsLiveHoverProvider);
		providers.put(Annotations.CONDITIONAL_ON_MISSING_CLASS, conditionalsLiveHoverProvider);
		providers.put(Annotations.CONDITIONAL_ON_CLOUD_PLATFORM, conditionalsLiveHoverProvider);
		providers.put(Annotations.CONDITIONAL_ON_WEB_APPLICATION, conditionalsLiveHoverProvider);
		providers.put(Annotations.CONDITIONAL_ON_NOT_WEB_APPLICATION, conditionalsLiveHoverProvider);
		providers.put(Annotations.CONDITIONAL_ON_ENABLED_INFO_CONTRIBUTOR, conditionalsLiveHoverProvider);
		providers.put(Annotations.CONDITIONAL_ON_ENABLED_RESOURCE_CHAIN, conditionalsLiveHoverProvider);
		providers.put(Annotations.CONDITIONAL_ON_ENABLED_ENDPOINT, conditionalsLiveHoverProvider);
		providers.put(Annotations.CONDITIONAL_ON_ENABLED_HEALTH_INDICATOR, conditionalsLiveHoverProvider);
		providers.put(Annotations.CONDITIONAL_ON_EXPRESSION, conditionalsLiveHoverProvider);
		providers.put(Annotations.CONDITIONAL_ON_JAVA, conditionalsLiveHoverProvider);
		providers.put(Annotations.CONDITIONAL_ON_JNDI, conditionalsLiveHoverProvider);
		providers.put(Annotations.CONDITIONAL_ON_SINGLE_CANDIDATE, conditionalsLiveHoverProvider);

		return new BootJavaHoverProvider(this, javaProjectFinder, providers, runningAppProvider);
	}

	protected SpringIndexer createAnnotationIndexer(SimpleLanguageServer server, BootLanguageServerParams params) {
		AnnotationHierarchyAwareLookup<SymbolProvider> providers = new AnnotationHierarchyAwareLookup<>();
		RequestMappingSymbolProvider requestMappingSymbolProvider = new RequestMappingSymbolProvider();
		BeansSymbolProvider beansSymbolProvider = new BeansSymbolProvider();
		ComponentSymbolProvider componentSymbolProvider = new ComponentSymbolProvider();
		RestrictedDefaultSymbolProvider restrictedDefaultSymbolProvider = new RestrictedDefaultSymbolProvider();
		DataRepositorySymbolProvider dataRepositorySymbolProvider = new DataRepositorySymbolProvider();
		WebfluxRouterSymbolProvider webfluxRouterSymbolProvider = new WebfluxRouterSymbolProvider();

		providers.put(Annotations.SPRING_REQUEST_MAPPING, requestMappingSymbolProvider);
		providers.put(Annotations.SPRING_GET_MAPPING, requestMappingSymbolProvider);
		providers.put(Annotations.SPRING_POST_MAPPING, requestMappingSymbolProvider);
		providers.put(Annotations.SPRING_PUT_MAPPING, requestMappingSymbolProvider);
		providers.put(Annotations.SPRING_DELETE_MAPPING, requestMappingSymbolProvider);
		providers.put(Annotations.SPRING_PATCH_MAPPING, requestMappingSymbolProvider);

		providers.put(Annotations.BEAN, beansSymbolProvider);
		providers.put(Annotations.COMPONENT, componentSymbolProvider);

		providers.put(Annotations.PROFILE, restrictedDefaultSymbolProvider);

		providers.put(Annotations.CONDITIONAL, restrictedDefaultSymbolProvider);
		providers.put(Annotations.CONDITIONAL_ON_BEAN, restrictedDefaultSymbolProvider);
		providers.put(Annotations.CONDITIONAL_ON_MISSING_BEAN, restrictedDefaultSymbolProvider);
		providers.put(Annotations.CONDITIONAL_ON_PROPERTY, restrictedDefaultSymbolProvider);
		providers.put(Annotations.CONDITIONAL_ON_RESOURCE, restrictedDefaultSymbolProvider);
		providers.put(Annotations.CONDITIONAL_ON_CLASS, restrictedDefaultSymbolProvider);
		providers.put(Annotations.CONDITIONAL_ON_MISSING_CLASS, restrictedDefaultSymbolProvider);
		providers.put(Annotations.CONDITIONAL_ON_CLOUD_PLATFORM, restrictedDefaultSymbolProvider);
		providers.put(Annotations.CONDITIONAL_ON_WEB_APPLICATION, restrictedDefaultSymbolProvider);
		providers.put(Annotations.CONDITIONAL_ON_NOT_WEB_APPLICATION, restrictedDefaultSymbolProvider);
		providers.put(Annotations.CONDITIONAL_ON_ENABLED_INFO_CONTRIBUTOR, restrictedDefaultSymbolProvider);
		providers.put(Annotations.CONDITIONAL_ON_ENABLED_RESOURCE_CHAIN, restrictedDefaultSymbolProvider);
		providers.put(Annotations.CONDITIONAL_ON_ENABLED_ENDPOINT, restrictedDefaultSymbolProvider);
		providers.put(Annotations.CONDITIONAL_ON_ENABLED_HEALTH_INDICATOR, restrictedDefaultSymbolProvider);
		providers.put(Annotations.CONDITIONAL_ON_EXPRESSION, restrictedDefaultSymbolProvider);
		providers.put(Annotations.CONDITIONAL_ON_JAVA, restrictedDefaultSymbolProvider);
		providers.put(Annotations.CONDITIONAL_ON_JNDI, restrictedDefaultSymbolProvider);
		providers.put(Annotations.CONDITIONAL_ON_SINGLE_CANDIDATE, restrictedDefaultSymbolProvider);

		providers.put(Annotations.REPOSITORY, dataRepositorySymbolProvider);
		providers.put("", webfluxRouterSymbolProvider);

		return new SpringIndexer(server, params, providers);
	}

	protected ReferencesHandler createReferenceHandler(SimpleLanguageServer server, JavaProjectFinder projectFinder) {
		Map<String, ReferenceProvider> providers = new HashMap<>();
		providers.put(org.springframework.ide.vscode.boot.java.value.Constants.SPRING_VALUE,
				new ValuePropertyReferencesProvider(server));

		return new BootJavaReferencesHandler(this, projectFinder, providers);
	}

	protected BootJavaCodeLensEngine createCodeLensEngine() {
		Collection<CodeLensProvider> codeLensProvider = new ArrayList<>();
		codeLensProvider.add(new WebfluxHandlerCodeLensProvider(this));

		return new BootJavaCodeLensEngine(this, codeLensProvider);
	}

	protected BootJavaDocumentHighlightEngine createDocumentHighlightEngine() {
		Collection<HighlightProvider> highlightProvider = new ArrayList<>();
		highlightProvider.add(new WebfluxRouteHighlightProdivder(this));

		return new BootJavaDocumentHighlightEngine(this, highlightProvider);
	}

	public ProjectObserver getProjectObserver() {
		return projectObserver;
	}

	public JavaProjectFinder getProjectFinder() {
		return projectFinder;
	}

	public SpringIndexer getSpringIndexer() {
		return indexer;
	}

	public SpringPropertyIndexProvider getSpringPropertyIndexProvider() {
		return propertyIndexProvider;
	}

	public BootJavaConfig getConfig() {
		return config;
	}

	public CompilationUnitCache getCompilationUnitCache() {
		return cuCache;
	}

	public SimpleTextDocumentService getTextDocumentService() {
		return server.getTextDocumentService();
	}

	public BootLanguageServerParams getServerParams() {
		return this.serverParams;
	}

	@Override
	public Set<LanguageId> getInterestingLanguages() {
		return LANGUAGES;
	}

}
