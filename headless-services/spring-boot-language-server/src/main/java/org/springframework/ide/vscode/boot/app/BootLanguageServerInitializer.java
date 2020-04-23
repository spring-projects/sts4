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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServerComponents;
import org.springframework.ide.vscode.boot.java.links.JavaElementLocationProvider;
import org.springframework.ide.vscode.boot.java.links.SourceLinks;
import org.springframework.ide.vscode.boot.java.livehover.v2.SpringProcessLiveDataProvider;
import org.springframework.ide.vscode.boot.java.utils.CompilationUnitCache;
import org.springframework.ide.vscode.boot.java.utils.SymbolCache;
import org.springframework.ide.vscode.boot.metadata.ProjectBasedPropertyIndexProvider;
import org.springframework.ide.vscode.boot.properties.BootPropertiesLanguageServerComponents;
import org.springframework.ide.vscode.boot.xml.SpringXMLLanguageServerComponents;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionEngine;
import org.springframework.ide.vscode.commons.languageserver.completion.VscodeCompletionEngineAdapter;
import org.springframework.ide.vscode.commons.languageserver.composable.CompositeLanguageServerComponents;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.util.HoverHandler;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.ide.vscode.commons.yaml.ast.YamlASTProvider;
import org.springframework.ide.vscode.commons.yaml.completion.YamlAssistContextProvider;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class BootLanguageServerInitializer implements InitializingBean {

	@Autowired SimpleLanguageServer server;
	@Autowired BootLanguageServerParams params;
	@Autowired SourceLinks sourceLinks;
	@Autowired CompilationUnitCache cuCache;
	@Autowired JavaElementLocationProvider javaElementLocationProvider;
	@Autowired YamlASTProvider parser;
	@Autowired YamlStructureProvider yamlStructureProvider;
	@Autowired YamlAssistContextProvider yamlAssistContextProvider;
	@Autowired SymbolCache symbolCache;
	@Autowired SpringProcessLiveDataProvider liveDataProvider;
	@Autowired BootJavaConfig config;
	@Autowired SpringSymbolIndex springIndexer;
	@Autowired(required = false) List<ICompletionEngine> completionEngines;

	@Qualifier("adHocProperties") @Autowired ProjectBasedPropertyIndexProvider adHocProperties;

	private CompositeLanguageServerComponents components;
	private VscodeCompletionEngineAdapter completionEngineAdapter;

	private static final Logger log = LoggerFactory.getLogger(BootLanguageServerInitializer.class);

	private static ProjectObserver.Listener reconcileOpenDocuments(SimpleLanguageServer s, CompositeLanguageServerComponents c, JavaProjectFinder projectFinder) {
		return ProjectObserver.onAny(project -> {
			c.getReconcileEngine().ifPresent(reconciler -> {
				log.info("A project changed {}, triggering reconcile on all project's open documents", project.getElementName());
				for (TextDocument doc : s.getTextDocumentService().getAll()) {
					if (projectFinder.find(doc.getId()).orElse(null) == project) {
						s.validateWith(doc.getId(), reconciler);
					}
				}
			});
		});
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		//TODO: ComposableLanguageServer object instance serves no purpose anymore. The constructor really just contains
		// some server intialization code. Migrate that code and get rid of the ComposableLanguageServer class
		CompositeLanguageServerComponents.Builder builder = new CompositeLanguageServerComponents.Builder();
		builder.add(new BootPropertiesLanguageServerComponents(server, params, javaElementLocationProvider, parser, yamlStructureProvider, yamlAssistContextProvider, sourceLinks));
		builder.add(new BootJavaLanguageServerComponents(server, params, sourceLinks, cuCache, adHocProperties, symbolCache, liveDataProvider, config, springIndexer));
		builder.add(new SpringXMLLanguageServerComponents(server, springIndexer, params, config));
		builder.completionEngines(completionEngines);
		components = builder.build(server);
		params.projectObserver.addListener(reconcileOpenDocuments(server, components, params.projectFinder));

		SimpleTextDocumentService documents = server.getTextDocumentService();

		components.getReconcileEngine().ifPresent(reconcileEngine -> {
			documents.onDidChangeContent(params -> {
				TextDocument doc = params.getDocument();
				server.validateWith(doc.getId(), reconcileEngine);
			});
		});

		ICompletionEngine completionEngine = components.getCompletionEngine();
		if (completionEngine!=null) {
			completionEngineAdapter = server.createCompletionEngineAdapter(completionEngine);
			completionEngineAdapter.setMaxCompletions(100);
			documents.onCompletion(completionEngineAdapter::getCompletions);
			documents.onCompletionResolve(completionEngineAdapter::resolveCompletion);
		}

		HoverHandler hoverHandler = components.getHoverProvider();
		documents.onHover(hoverHandler);
	}

	public CompositeLanguageServerComponents getComponents() {
		Assert.notNull(components, "Not yet initialized, can't get components yet.");
		return components;
	}

	public void setMaxCompletions(int number) {
		if (completionEngineAdapter!=null) {
			completionEngineAdapter.setMaxCompletions(number);
		}
	}

}
