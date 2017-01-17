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
package org.springframework.ide.vscode.manifest.yaml;

import java.time.Duration;
import java.util.Collection;

import javax.inject.Provider;

import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CfCliParamsProvider;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.ClientParamsProvider;
import org.springframework.ide.vscode.commons.cloudfoundry.client.ClientTimeouts;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFTargetCache;
import org.springframework.ide.vscode.commons.cloudfoundry.client.v2.CloudFoundryClientFactory;
import org.springframework.ide.vscode.commons.cloudfoundry.client.v2.DefaultCloudFoundryClientFactoryV2;
import org.springframework.ide.vscode.commons.languageserver.completion.VscodeCompletionEngine;
import org.springframework.ide.vscode.commons.languageserver.completion.VscodeCompletionEngineAdapter;
import org.springframework.ide.vscode.commons.languageserver.hover.HoverInfoProvider;
import org.springframework.ide.vscode.commons.languageserver.hover.VscodeHoverEngine;
import org.springframework.ide.vscode.commons.languageserver.hover.VscodeHoverEngineAdapter;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.ide.vscode.commons.yaml.ast.YamlASTProvider;
import org.springframework.ide.vscode.commons.yaml.ast.YamlParser;
import org.springframework.ide.vscode.commons.yaml.completion.SchemaBasedYamlAssistContextProvider;
import org.springframework.ide.vscode.commons.yaml.completion.YamlAssistContextProvider;
import org.springframework.ide.vscode.commons.yaml.completion.YamlCompletionEngine;
import org.springframework.ide.vscode.commons.yaml.hover.YamlHoverInfoProvider;
import org.springframework.ide.vscode.commons.yaml.reconcile.YamlSchemaBasedReconcileEngine;
import org.springframework.ide.vscode.commons.yaml.schema.YValueHint;
import org.springframework.ide.vscode.commons.yaml.schema.YamlSchema;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureProvider;
import org.yaml.snakeyaml.Yaml;

public class ManifestYamlLanguageServer extends SimpleLanguageServer {

	
	private Yaml yaml = new Yaml();
	private YamlSchema schema;
	private CFTargetCache cfTargetCache;
	
	private static final ClientTimeouts VSCODE_CF_CLIENT_TIMEOUTS = new ClientTimeouts() {

		@Override
		public Duration getServicesTimeout() {
			// Use shorter timeouts for services because it is used in dynamic
			// values for content assist
			return Duration.ofSeconds(15);
		}	
	};

	
	public ManifestYamlLanguageServer() {
		SimpleTextDocumentService documents = getTextDocumentService();
		
		YamlASTProvider parser = new YamlParser(yaml);
				
		Provider<Collection<YValueHint>> buildPacksProvider = getBuildpacksProvider();
		Provider<Collection<YValueHint>> servicesProvider = getServicesProvider();

		schema = new ManifestYmlSchema(buildPacksProvider, servicesProvider);

		YamlStructureProvider structureProvider = YamlStructureProvider.DEFAULT;
		YamlAssistContextProvider contextProvider = new SchemaBasedYamlAssistContextProvider(schema);
		YamlCompletionEngine yamlCompletionEngine = new YamlCompletionEngine(structureProvider, contextProvider);
		VscodeCompletionEngine completionEngine = new VscodeCompletionEngineAdapter(this, yamlCompletionEngine);
		HoverInfoProvider infoProvider = new YamlHoverInfoProvider(parser, structureProvider, contextProvider);
		VscodeHoverEngine hoverEngine = new VscodeHoverEngineAdapter(this, infoProvider);
		IReconcileEngine engine = new YamlSchemaBasedReconcileEngine(parser, schema);

//		SimpleWorkspaceService workspace = getWorkspaceService();
		documents.onDidChangeContent(params -> {
			TextDocument doc = params.getDocument();
			validateWith(doc, engine);
		});
		
//		workspace.onDidChangeConfiguraton(settings -> {
//			System.out.println("Config changed: "+params);
//			Integer val = settings.getInt("languageServerExample", "maxNumberOfProblems");
//			if (val!=null) {
//				maxProblems = ((Number) val).intValue();
//				for (TextDocument doc : documents.getAll()) {
//					validateDocument(documents, doc);
//				}
//			}
//		});
		
		documents.onCompletion(completionEngine::getCompletions);
		documents.onCompletionResolve(completionEngine::resolveCompletion);
		documents.onHover(hoverEngine ::getHover);
	}
	
	private CFTargetCache getCfTargetCache() {
		if (cfTargetCache == null) {
			ClientParamsProvider paramsProvider = new CfCliParamsProvider();
			CloudFoundryClientFactory clientFactory = DefaultCloudFoundryClientFactoryV2.INSTANCE;
			cfTargetCache = new CFTargetCache(paramsProvider, clientFactory, VSCODE_CF_CLIENT_TIMEOUTS);
		}
		return cfTargetCache;
	}

	private Provider<Collection<YValueHint>> getBuildpacksProvider() {
		return new ManifestYamlCFBuildpacksProvider(getCfTargetCache());
	}
	
	private Provider<Collection<YValueHint>> getServicesProvider() {
		return new ManifestYamlCFServicesProvider(getCfTargetCache());
	}
	
	@Override
	protected ServerCapabilities getServerCapabilities() {
		ServerCapabilities c = new ServerCapabilities();
		
		c.setTextDocumentSync(TextDocumentSyncKind.Incremental);
		c.setHoverProvider(true);
		
		CompletionOptions completionProvider = new CompletionOptions();
		completionProvider.setResolveProvider(false);
		c.setCompletionProvider(completionProvider);
		
		return c;
	}
}
