/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot;

import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.springframework.ide.vscode.application.properties.metadata.PropertyInfo;
import org.springframework.ide.vscode.application.properties.metadata.SpringPropertyIndexProvider;
import org.springframework.ide.vscode.application.properties.metadata.completions.PropertyCompletionFactory;
import org.springframework.ide.vscode.application.properties.metadata.completions.RelaxedNameConfig;
import org.springframework.ide.vscode.application.properties.metadata.types.TypeUtilProvider;
import org.springframework.ide.vscode.application.properties.metadata.util.FuzzyMap;
import org.springframework.ide.vscode.boot.properties.completions.SpringPropertiesCompletionEngine;
import org.springframework.ide.vscode.boot.properties.hover.PropertiesHoverInfoProvider;
import org.springframework.ide.vscode.boot.properties.reconcile.SpringPropertiesReconcileEngine;
import org.springframework.ide.vscode.boot.yaml.completions.ApplicationYamlAssistContext;
import org.springframework.ide.vscode.boot.yaml.reconcile.ApplicationYamlReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionEngine;
import org.springframework.ide.vscode.commons.languageserver.completion.VscodeCompletionEngineAdapter;
import org.springframework.ide.vscode.commons.languageserver.hover.HoverInfoProvider;
import org.springframework.ide.vscode.commons.languageserver.hover.VscodeHoverEngineAdapter;
import org.springframework.ide.vscode.commons.languageserver.hover.VscodeHoverEngineAdapter.HoverType;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.util.IDocument;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.languageserver.util.TextDocument;
import org.springframework.ide.vscode.commons.yaml.ast.YamlASTProvider;
import org.springframework.ide.vscode.commons.yaml.ast.YamlParser;
import org.springframework.ide.vscode.commons.yaml.completion.YamlAssistContext;
import org.springframework.ide.vscode.commons.yaml.completion.YamlAssistContextProvider;
import org.springframework.ide.vscode.commons.yaml.completion.YamlCompletionEngine;
import org.springframework.ide.vscode.commons.yaml.hover.YamlHoverInfoProvider;
import org.springframework.ide.vscode.commons.yaml.structure.YamlDocument;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureProvider;
import org.yaml.snakeyaml.Yaml;

import com.google.common.collect.ImmutableList;

/**
 * Language Server for Spring Boot Application Properties files
 * 
 * @author Alex Boyko
 *
 */
public class BootPropertiesLanguageServer extends SimpleLanguageServer {

	private static final String YML = ".yml";
	private static final String PROPERTIES = ".properties";
	// Shared:
	private final JavaProjectFinder javaProjectFinder;
	private final SpringPropertyIndexProvider indexProvider;
	private final TypeUtilProvider typeUtilProvider;
	private final VscodeCompletionEngineAdapter completionEngine;
	private final VscodeHoverEngineAdapter hoverEngine;
	private final RelaxedNameConfig relaxedNameConfig = RelaxedNameConfig.COMPLETION_DEFAULTS;
	
	private final PropertyCompletionFactory completionFactory;

	// For yaml
	private final Yaml yaml = new Yaml();
	private final YamlASTProvider parser = new YamlParser(yaml);
	private final YamlStructureProvider yamlStructureProvider= YamlStructureProvider.DEFAULT;
	private YamlAssistContextProvider yamlAssistContextProvider;
	
	public BootPropertiesLanguageServer(SpringPropertyIndexProvider indexProvider, TypeUtilProvider typeUtilProvider, JavaProjectFinder javaProjectFinder) {
		this.indexProvider = indexProvider;
		this.typeUtilProvider = typeUtilProvider;
		this.javaProjectFinder = javaProjectFinder;
		this.completionFactory = new PropertyCompletionFactory(javaProjectFinder);
		this.yamlAssistContextProvider = new YamlAssistContextProvider() {
			@Override
			public YamlAssistContext getGlobalAssistContext(YamlDocument ydoc) {
				IDocument doc = ydoc.getDocument();
				FuzzyMap<PropertyInfo> index = indexProvider.getIndex(doc);
				return ApplicationYamlAssistContext.global(index, completionFactory, typeUtilProvider.getTypeUtil(doc), relaxedNameConfig);
			}
		};

		SimpleTextDocumentService documents = getTextDocumentService();

		IReconcileEngine reconcileEngine = getReconcileEngine();
		documents.onDidChangeContent(params -> {
			TextDocument doc = params.getDocument();
			validateWith(doc, reconcileEngine);
		});
		
		ICompletionEngine propertiesCompletionEngine = getCompletionEngine();
		completionEngine = new VscodeCompletionEngineAdapter(this, propertiesCompletionEngine);
		completionEngine.setMaxCompletionsNumber(100);
		documents.onCompletion(completionEngine::getCompletions);
		documents.onCompletionResolve(completionEngine::resolveCompletion);
		
		HoverInfoProvider hoverInfoProvider = getHoverProvider();
		hoverEngine = new VscodeHoverEngineAdapter(this, hoverInfoProvider);
		documents.onHover(hoverEngine::getHover);
	}

	private ICompletionEngine getCompletionEngine() {
		ICompletionEngine propertiesCompletions = new SpringPropertiesCompletionEngine(indexProvider, typeUtilProvider, javaProjectFinder);
		ICompletionEngine yamlCompletions = new YamlCompletionEngine(yamlStructureProvider, yamlAssistContextProvider);
		return (IDocument document, int offset) -> {
			String uri = document.getUri();
			if (uri!=null) {
				if (uri.endsWith(PROPERTIES)) {
					return propertiesCompletions.getCompletions(document, offset);
				} else if (uri.endsWith(YML)) {
					return yamlCompletions.getCompletions(document, offset);
				}
			}
			return ImmutableList.of();
		};
	}

	protected HoverInfoProvider getHoverProvider() {
		HoverInfoProvider propertiesHovers = new PropertiesHoverInfoProvider(indexProvider, typeUtilProvider, javaProjectFinder);
		HoverInfoProvider ymlHovers = new YamlHoverInfoProvider(parser, yamlStructureProvider, yamlAssistContextProvider);
		
		return (IDocument document, int offset) -> {
			String uri = document.getUri();
			if (uri!=null) {
				if (uri.endsWith(PROPERTIES)) {
					return propertiesHovers.getHoverInfo(document, offset);
				} else if (uri.endsWith(YML)) {
					return ymlHovers.getHoverInfo(document, offset);
				}
			}
			return null;
		};
	}
	
	public void setMaxCompletionsNumber(int number) {
		completionEngine.setMaxCompletionsNumber(number);
	}
	
	public void setHoverType(HoverType type) {
		hoverEngine.setHoverType(type);
	}
	
	@Override
	protected ServerCapabilities getServerCapabilities() {
		ServerCapabilities c = new ServerCapabilities();
		
		c.setTextDocumentSync(TextDocumentSyncKind.Incremental);
		CompletionOptions completionProvider = new CompletionOptions();
		completionProvider.setResolveProvider(false);
		c.setCompletionProvider(completionProvider);
		
		c.setHoverProvider(true);
		
		return c;
	}
	
	protected IReconcileEngine getReconcileEngine() {
		IReconcileEngine propertiesReconciler = new SpringPropertiesReconcileEngine(indexProvider, typeUtilProvider);
		IReconcileEngine ymlReconciler = new ApplicationYamlReconcileEngine(parser, indexProvider, typeUtilProvider);
		
		return (doc, problemCollector) -> {
			String uri = doc.getUri();
			if (uri!=null) {
				if (uri.endsWith(PROPERTIES)) {
					propertiesReconciler.reconcile(doc, problemCollector);
					return;
				} else if (uri.endsWith(YML)) {
					ymlReconciler.reconcile(doc, problemCollector);
					return;
				}
			}
			//No real reconciler is applicable. So tell the problemCollector there are no problems.
			problemCollector.beginCollecting();
			problemCollector.endCollecting();
		};
	}


}
