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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.Registration;
import org.eclipse.lsp4j.RegistrationParams;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchyAwareLookup;
import org.springframework.ide.vscode.boot.java.autowired.AutowiredHoverProvider;
import org.springframework.ide.vscode.boot.java.beans.BeansSymbolProvider;
import org.springframework.ide.vscode.boot.java.beans.ComponentSymbolProvider;
import org.springframework.ide.vscode.boot.java.conditionals.ConditionalsLiveHoverProvider;
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
import org.springframework.ide.vscode.boot.java.handlers.RunningAppProvider;
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
import org.springframework.ide.vscode.boot.java.livehover.ActiveProfilesProvider;
import org.springframework.ide.vscode.boot.java.livehover.BeanInjectedIntoHoverProvider;
import org.springframework.ide.vscode.boot.java.livehover.ComponentInjectionsHoverProvider;
import org.springframework.ide.vscode.boot.java.metadata.SpringPropertyIndexProvider;
import org.springframework.ide.vscode.boot.java.requestmapping.LiveAppURLSymbolProvider;
import org.springframework.ide.vscode.boot.java.requestmapping.RequestMappingHoverProvider;
import org.springframework.ide.vscode.boot.java.requestmapping.RequestMappingSymbolProvider;
import org.springframework.ide.vscode.boot.java.scope.ScopeCompletionProcessor;
import org.springframework.ide.vscode.boot.java.snippets.JavaSnippet;
import org.springframework.ide.vscode.boot.java.snippets.JavaSnippetContext;
import org.springframework.ide.vscode.boot.java.snippets.JavaSnippetManager;
import org.springframework.ide.vscode.boot.java.utils.CompilationUnitCache;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexer;
import org.springframework.ide.vscode.boot.java.utils.SpringLiveHoverWatchdog;
import org.springframework.ide.vscode.boot.java.value.ValueCompletionProcessor;
import org.springframework.ide.vscode.boot.java.value.ValueHoverProvider;
import org.springframework.ide.vscode.boot.java.value.ValuePropertyReferencesProvider;
import org.springframework.ide.vscode.commons.languageserver.HighlightParams;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionEngine;
import org.springframework.ide.vscode.commons.languageserver.completion.VscodeCompletionEngineAdapter;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.util.LSFactory;
import org.springframework.ide.vscode.commons.languageserver.util.ReferencesHandler;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleWorkspaceService;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

/**
 * Language Server for Spring Boot Application Properties files
 *
 * @author Martin Lippert
 */
public class BootJavaLanguageServer extends SimpleLanguageServer {
	
	public static final String WORKSPACE_FOLDERS_CAPABILITY_NAME = "workspace/didChangeWorkspaceFolders";
	public static final String WORKSPACE_FOLDERS_CAPABILITY_ID = UUID.randomUUID().toString();


	private final VscodeCompletionEngineAdapter completionEngine;
	private final SpringIndexer indexer;
	private final SpringPropertyIndexProvider propertyIndexProvider;
	private final SpringLiveHoverWatchdog liveHoverWatchdog;
	private final ProjectObserver projectObserver;
	private final BootJavaConfig config;
	private final CompilationUnitCache cuCache;

	private final WordHighlighter testHightlighter = null; // new WordHighlighter("foo");

	private JavaProjectFinder projectFinder;

	public BootJavaLanguageServer(LSFactory<BootJavaLanguageServerParams> _params) {
		super("boot-java");
		BootJavaLanguageServerParams serverParams = _params.create(this);

		this.config = new BootJavaConfig();

		projectFinder = serverParams.projectFinder;
		projectObserver = serverParams.projectObserver;
		cuCache = new CompilationUnitCache(projectFinder, getTextDocumentService(), projectObserver);

		propertyIndexProvider = serverParams.indexProvider;

		JavaProjectFinder javaProjectFinder = serverParams.projectFinder;
		SimpleWorkspaceService workspaceService = getWorkspaceService();
		SimpleTextDocumentService documents = getTextDocumentService();

		IReconcileEngine reconcileEngine = new BootJavaReconcileEngine();
		documents.onDidChangeContent(params -> {
			TextDocument doc = params.getDocument();
			validateWith(doc.getId(), reconcileEngine);
		});

		ICompletionEngine bootCompletionEngine = createCompletionEngine(javaProjectFinder, propertyIndexProvider);
		completionEngine = createCompletionEngineAdapter(this, bootCompletionEngine);
		completionEngine.setMaxCompletions(100);
		documents.onCompletion(completionEngine::getCompletions);
		documents.onCompletionResolve(completionEngine::resolveCompletion);

		BootJavaHoverProvider hoverInfoProvider = createHoverHandler(javaProjectFinder,
				serverParams.runningAppProvider);
		documents.onHover(hoverInfoProvider);

		ReferencesHandler referencesHandler = createReferenceHandler(this, javaProjectFinder);
		documents.onReferences(referencesHandler);

		indexer = createAnnotationIndexer(this, javaProjectFinder);
		documents.onDidSave(params -> {
			String docURI = params.getDocument().getId().getUri();
			String content = params.getDocument().get();
			indexer.updateDocument(docURI, content);
		});

		documents.onDocumentSymbol(new BootJavaDocumentSymbolHandler(indexer));
		workspaceService.onWorkspaceSymbol(new BootJavaWorkspaceSymbolHandler(indexer,
				new LiveAppURLSymbolProvider(serverParams.runningAppProvider)));

		BootJavaCodeLensEngine codeLensHandler = createCodeLensEngine(this, javaProjectFinder);
		documents.onCodeLens(codeLensHandler::createCodeLenses);
		documents.onCodeLensResolve(codeLensHandler::resolveCodeLens);

		liveHoverWatchdog = new SpringLiveHoverWatchdog(this, hoverInfoProvider, serverParams.runningAppProvider,
				projectFinder, projectObserver, serverParams.watchDogInterval);
		documents.onDidChangeContent(params -> {
			TextDocument doc = params.getDocument();
			if (testHightlighter != null) {
				getClient().highlight(new HighlightParams(params.getDocument().getId(), testHightlighter.apply(doc)));
			} else {
				liveHoverWatchdog.watchDocument(doc.getUri());
				liveHoverWatchdog.update(doc.getUri(), null);
			}
		});

		documents.onDidClose(doc -> {
			if (testHightlighter != null) {
				getClient().highlight(new HighlightParams(doc.getId(), testHightlighter.apply(doc)));
			} else {
				liveHoverWatchdog.unwatchDocument(doc.getUri());
			}
		});

		workspaceService.onDidChangeConfiguraton(settings -> {
			config.handleConfigurationChange(settings);
			if (config.isBootHintsEnabled()) {
				liveHoverWatchdog.enableHighlights();
			} else {
				liveHoverWatchdog.disableHighlights();
			}
		});

	}

	public void setMaxCompletionsNumber(int number) {
		completionEngine.setMaxCompletions(number);
	}

	@Override
	public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		CompletableFuture<InitializeResult> result = super.initialize(params);

		this.indexer.initialize(getWorkspaceRoots());

		return result;
	}

	@Override
	public void initialized() {
		Registration registration = new Registration(WORKSPACE_FOLDERS_CAPABILITY_ID, WORKSPACE_FOLDERS_CAPABILITY_NAME, null);
		RegistrationParams registrationParams = new RegistrationParams(Collections.singletonList(registration));
		getClient().registerCapability(registrationParams);

		this.indexer.serverInitialized();

		// TODO: due to a missing message from lsp4e this "initialized" is not called in
		// the LSP4E case
		// if this gets fixed, the code should move here (from "initialize" above)

		// this.indexer.initialize(this.getWorkspaceRoot());
		// this.liveHoverWatchdog.start();
	}

	@Override
	public CompletableFuture<Object> shutdown() {
		this.liveHoverWatchdog.shutdown();
		this.indexer.shutdown();
		this.cuCache.dispose();

		return super.shutdown();
	}

	protected ICompletionEngine createCompletionEngine(JavaProjectFinder javaProjectFinder,
			SpringPropertyIndexProvider indexProvider) {
		Map<String, CompletionProvider> providers = new HashMap<>();
		providers.put(org.springframework.ide.vscode.boot.java.scope.Constants.SPRING_SCOPE,
				new ScopeCompletionProcessor());
		providers.put(org.springframework.ide.vscode.boot.java.value.Constants.SPRING_VALUE,
				new ValueCompletionProcessor(indexProvider));

		JavaSnippetManager snippetManager = new JavaSnippetManager(this::createSnippetBuilder);
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
		return new BootJavaCompletionEngine(this, providers, snippetManager);
	}

	protected BootJavaHoverProvider createHoverHandler(JavaProjectFinder javaProjectFinder,
			RunningAppProvider runningAppProvider) {
		AnnotationHierarchyAwareLookup<HoverProvider> providers = new AnnotationHierarchyAwareLookup<>();

		providers.put(org.springframework.ide.vscode.boot.java.value.Constants.SPRING_VALUE, new ValueHoverProvider());

		providers.put(Annotations.SPRING_REQUEST_MAPPING, new RequestMappingHoverProvider());
		providers.put(Annotations.SPRING_GET_MAPPING, new RequestMappingHoverProvider());
		providers.put(Annotations.SPRING_POST_MAPPING, new RequestMappingHoverProvider());
		providers.put(Annotations.SPRING_PUT_MAPPING, new RequestMappingHoverProvider());
		providers.put(Annotations.SPRING_DELETE_MAPPING, new RequestMappingHoverProvider());
		providers.put(Annotations.SPRING_PATCH_MAPPING, new RequestMappingHoverProvider());
		providers.put(Annotations.PROFILE, new ActiveProfilesProvider());

		providers.put(Annotations.AUTOWIRED, new AutowiredHoverProvider());
		providers.put(Annotations.COMPONENT, new ComponentInjectionsHoverProvider());
		providers.put(Annotations.BEAN, new BeanInjectedIntoHoverProvider());

		providers.put(Annotations.CONDITIONAL, new ConditionalsLiveHoverProvider());
		providers.put(Annotations.CONDITIONAL_ON_BEAN, new ConditionalsLiveHoverProvider());
		providers.put(Annotations.CONDITIONAL_ON_MISSING_BEAN, new ConditionalsLiveHoverProvider());
		providers.put(Annotations.CONDITIONAL_ON_PROPERTY, new ConditionalsLiveHoverProvider());
		providers.put(Annotations.CONDITIONAL_ON_RESOURCE, new ConditionalsLiveHoverProvider());
		providers.put(Annotations.CONDITIONAL_ON_CLASS, new ConditionalsLiveHoverProvider());
		providers.put(Annotations.CONDITIONAL_ON_MISSING_CLASS, new ConditionalsLiveHoverProvider());
		providers.put(Annotations.CONDITIONAL_ON_CLOUD_PLATFORM, new ConditionalsLiveHoverProvider());
		providers.put(Annotations.CONDITIONAL_ON_WEB_APPLICATION, new ConditionalsLiveHoverProvider());
		providers.put(Annotations.CONDITIONAL_ON_NOT_WEB_APPLICATION, new ConditionalsLiveHoverProvider());
		providers.put(Annotations.CONDITIONAL_ON_ENABLED_INFO_CONTRIBUTOR, new ConditionalsLiveHoverProvider());
		providers.put(Annotations.CONDITIONAL_ON_ENABLED_RESOURCE_CHAIN, new ConditionalsLiveHoverProvider());
		providers.put(Annotations.CONDITIONAL_ON_ENABLED_ENDPOINT, new ConditionalsLiveHoverProvider());
		providers.put(Annotations.CONDITIONAL_ON_ENABLED_HEALTH_INDICATOR, new ConditionalsLiveHoverProvider());
		providers.put(Annotations.CONDITIONAL_ON_EXPRESSION, new ConditionalsLiveHoverProvider());
		providers.put(Annotations.CONDITIONAL_ON_JAVA, new ConditionalsLiveHoverProvider());
		providers.put(Annotations.CONDITIONAL_ON_JNDI, new ConditionalsLiveHoverProvider());
		providers.put(Annotations.CONDITIONAL_ON_SINGLE_CANDIDATE, new ConditionalsLiveHoverProvider());

		return new BootJavaHoverProvider(this, javaProjectFinder, providers, runningAppProvider);
	}

	protected SpringIndexer createAnnotationIndexer(SimpleLanguageServer server, JavaProjectFinder projectFinder) {
		AnnotationHierarchyAwareLookup<SymbolProvider> providers = new AnnotationHierarchyAwareLookup<>();
		providers.put(Annotations.SPRING_REQUEST_MAPPING, new RequestMappingSymbolProvider());
		providers.put(Annotations.SPRING_GET_MAPPING, new RequestMappingSymbolProvider());
		providers.put(Annotations.SPRING_POST_MAPPING, new RequestMappingSymbolProvider());
		providers.put(Annotations.SPRING_PUT_MAPPING, new RequestMappingSymbolProvider());
		providers.put(Annotations.SPRING_DELETE_MAPPING, new RequestMappingSymbolProvider());
		providers.put(Annotations.SPRING_PATCH_MAPPING, new RequestMappingSymbolProvider());

		providers.put(Annotations.BEAN, new BeansSymbolProvider());
		providers.put(Annotations.COMPONENT, new ComponentSymbolProvider());

		return new SpringIndexer(this, projectFinder, providers);
	}

	protected ReferencesHandler createReferenceHandler(SimpleLanguageServer server, JavaProjectFinder projectFinder) {
		Map<String, ReferenceProvider> providers = new HashMap<>();
		providers.put(org.springframework.ide.vscode.boot.java.value.Constants.SPRING_VALUE,
				new ValuePropertyReferencesProvider(server));

		return new BootJavaReferencesHandler(server, projectFinder, providers);
	}

	protected BootJavaCodeLensEngine createCodeLensEngine(SimpleLanguageServer server,
			JavaProjectFinder projectFinder) {
		return new BootJavaCodeLensEngine(server, projectFinder);
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

}
