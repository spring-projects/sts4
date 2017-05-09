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

import java.util.Collection;
import java.util.concurrent.Callable;

import org.springframework.ide.vscode.commons.cloudfoundry.client.ClientTimeouts;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CloudFoundryClientFactory;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFTargetCache;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CfCliParamsProvider;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.ClientParamsProvider;
import org.springframework.ide.vscode.commons.cloudfoundry.client.v2.DefaultCloudFoundryClientFactoryV2;
import org.springframework.ide.vscode.commons.languageserver.completion.VscodeCompletionEngine;
import org.springframework.ide.vscode.commons.languageserver.completion.VscodeCompletionEngineAdapter;
import org.springframework.ide.vscode.commons.languageserver.hover.HoverInfoProvider;
import org.springframework.ide.vscode.commons.languageserver.hover.VscodeHoverEngine;
import org.springframework.ide.vscode.commons.languageserver.hover.VscodeHoverEngineAdapter;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.ide.vscode.commons.yaml.ast.YamlASTProvider;
import org.springframework.ide.vscode.commons.yaml.ast.YamlParser;
import org.springframework.ide.vscode.commons.yaml.completion.SchemaBasedYamlAssistContextProvider;
import org.springframework.ide.vscode.commons.yaml.completion.YamlAssistContextProvider;
import org.springframework.ide.vscode.commons.yaml.completion.YamlCompletionEngine;
import org.springframework.ide.vscode.commons.yaml.completion.YamlCompletionEngineOptions;
import org.springframework.ide.vscode.commons.yaml.hover.YamlHoverInfoProvider;
import org.springframework.ide.vscode.commons.yaml.quickfix.YamlQuickfixes;
import org.springframework.ide.vscode.commons.yaml.reconcile.YamlSchemaBasedReconcileEngine;
import org.springframework.ide.vscode.commons.yaml.schema.YValueHint;
import org.springframework.ide.vscode.commons.yaml.schema.YamlSchema;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureProvider;
import org.yaml.snakeyaml.Yaml;

public class ManifestYamlLanguageServer extends SimpleLanguageServer {

	private Yaml yaml = new Yaml();
	private YamlSchema schema;
	private CFTargetCache cfTargetCache;
	private final CloudFoundryClientFactory cfClientFactory;
	private final ClientParamsProvider cfParamsProvider;

	public ManifestYamlLanguageServer() {
		this(DefaultCloudFoundryClientFactoryV2.INSTANCE, new CfCliParamsProvider());
	}

	public ManifestYamlLanguageServer(CloudFoundryClientFactory cfClientFactory, ClientParamsProvider cfParamsProvider) {
		super("vscode-manifest-yaml");
		this.cfClientFactory = cfClientFactory;
		this.cfParamsProvider=cfParamsProvider;
		SimpleTextDocumentService documents = getTextDocumentService();

		YamlASTProvider parser = new YamlParser(yaml);

		schema = new ManifestYmlSchema(getHintProviders());

		YamlStructureProvider structureProvider = YamlStructureProvider.DEFAULT;
		YamlAssistContextProvider contextProvider = new SchemaBasedYamlAssistContextProvider(schema);
		YamlCompletionEngine yamlCompletionEngine = new YamlCompletionEngine(structureProvider, contextProvider, YamlCompletionEngineOptions.DEFAULT);
		VscodeCompletionEngine completionEngine = new VscodeCompletionEngineAdapter(this, yamlCompletionEngine);
		HoverInfoProvider infoProvider = new YamlHoverInfoProvider(parser, structureProvider, contextProvider);
		VscodeHoverEngine hoverEngine = new VscodeHoverEngineAdapter(this, infoProvider);
		YamlQuickfixes quickfixes = new YamlQuickfixes(getQuickfixRegistry(), getTextDocumentService(), structureProvider);
		IReconcileEngine engine = new YamlSchemaBasedReconcileEngine(parser, schema, quickfixes);

//		SimpleWorkspaceService workspace = getWorkspaceService();
		documents.onDidChangeContent(params -> {
			TextDocument doc = params.getDocument();
			if (LanguageId.CF_MANIFEST.equals(doc.getLanguageId())) {
				validateWith(doc.getId(), engine);
			} else {
				validateWith(doc.getId(), IReconcileEngine.NULL);
			}
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

	protected ManifestYmlHintProviders getHintProviders() {
		Callable<Collection<YValueHint>> buildPacksProvider = new ManifestYamlCFBuildpacksProvider(getCfTargetCache());
		Callable<Collection<YValueHint>> servicesProvider = new ManifestYamlCFServicesProvider(getCfTargetCache());
		Callable<Collection<YValueHint>> domainsProvider = new ManifestYamlCFDomainsProvider(getCfTargetCache());
		Callable<Collection<YValueHint>> stacksProvider = new ManifestYamlStacksProvider(getCfTargetCache());

		return new ManifestYmlHintProviders() {

			@Override
			public Callable<Collection<YValueHint>> getServicesProvider() {
				return servicesProvider;
			}

			@Override
			public Callable<Collection<YValueHint>> getDomainsProvider() {
				return domainsProvider;
			}

			@Override
			public Callable<Collection<YValueHint>> getBuildpackProviders() {
				return buildPacksProvider;
			}

			@Override
			public Callable<Collection<YValueHint>> getStacksProvider() {
				return stacksProvider;
			}
		};
	}

	private CFTargetCache getCfTargetCache() {
		if (cfTargetCache == null) {
			ClientParamsProvider paramsProvider = cfParamsProvider;
			CloudFoundryClientFactory clientFactory = cfClientFactory;
			cfTargetCache = new CFTargetCache(paramsProvider, clientFactory, new ClientTimeouts());
		}
		return cfTargetCache;
	}

}
