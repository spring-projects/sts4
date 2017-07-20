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
package org.springframework.ide.vscode.bosh;

import org.springframework.ide.vscode.bosh.models.CloudConfigModel;
import org.springframework.ide.vscode.bosh.models.DynamicModelProvider;
import org.springframework.ide.vscode.commons.languageserver.completion.VscodeCompletionEngineAdapter;
import org.springframework.ide.vscode.commons.languageserver.hover.HoverInfoProvider;
import org.springframework.ide.vscode.commons.languageserver.hover.VscodeHoverEngine;
import org.springframework.ide.vscode.commons.languageserver.hover.VscodeHoverEngineAdapter;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.ide.vscode.commons.yaml.ast.YamlAstCache;
import org.springframework.ide.vscode.commons.yaml.completion.SchemaBasedYamlAssistContextProvider;
import org.springframework.ide.vscode.commons.yaml.completion.YamlAssistContextProvider;
import org.springframework.ide.vscode.commons.yaml.completion.YamlCompletionEngine;
import org.springframework.ide.vscode.commons.yaml.completion.YamlCompletionEngineOptions;
import org.springframework.ide.vscode.commons.yaml.hover.YamlHoverInfoProvider;
import org.springframework.ide.vscode.commons.yaml.quickfix.YamlQuickfixes;
import org.springframework.ide.vscode.commons.yaml.reconcile.ASTTypeCache;
import org.springframework.ide.vscode.commons.yaml.reconcile.TypeBasedYamlSymbolHandler;
import org.springframework.ide.vscode.commons.yaml.reconcile.YamlSchemaBasedReconcileEngine;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureProvider;

public class BoshLanguageServer extends SimpleLanguageServer {

	private final VscodeCompletionEngineAdapter completionEngine;

	public BoshLanguageServer(DynamicModelProvider<CloudConfigModel> cloudConfigProvider) {
		super("vscode-bosh");
		YamlAstCache asts = new YamlAstCache();
		SimpleTextDocumentService documents = getTextDocumentService();

		ASTTypeCache astTypeCache = new ASTTypeCache();
		BoshDeploymentManifestSchema schema = new BoshDeploymentManifestSchema(astTypeCache, cloudConfigProvider);

		YamlStructureProvider structureProvider = YamlStructureProvider.DEFAULT;
		YamlAssistContextProvider contextProvider = new SchemaBasedYamlAssistContextProvider(schema);
		YamlCompletionEngine yamlCompletionEngine = new YamlCompletionEngine(structureProvider, contextProvider, YamlCompletionEngineOptions.DEFAULT);
		completionEngine = createCompletionEngineAdapter(this, yamlCompletionEngine);
		HoverInfoProvider infoProvider = new YamlHoverInfoProvider(asts.getAstProvider(true), structureProvider, contextProvider);
		VscodeHoverEngine hoverEngine = new VscodeHoverEngineAdapter(this, infoProvider);
		YamlQuickfixes quickfixes = new YamlQuickfixes(getQuickfixRegistry(), getTextDocumentService(), structureProvider);
		YamlSchemaBasedReconcileEngine engine = new YamlSchemaBasedReconcileEngine(asts.getAstProvider(false), schema, quickfixes);
		engine.setTypeCollector(astTypeCache);
		documents.onDocumentSymbol(new TypeBasedYamlSymbolHandler(documents, astTypeCache, schema.getDefinitionTypes()));

		documents.onDidChangeContent(params -> {
			validateOnDocumentChange(engine, params.getDocument());
		});
		documents.onCompletion(completionEngine::getCompletions);
		documents.onCompletionResolve(completionEngine::resolveCompletion);
		documents.onHover(hoverEngine ::getHover);
		documents.onDefinition(new BoshDefintionFinder(this, schema, asts, astTypeCache));
	}


	private void validateOnDocumentChange(IReconcileEngine engine, TextDocument doc) {
		if (LanguageId.BOSH_DEPLOYMENT.equals(doc.getLanguageId())) {
			validateWith(doc.getId(), engine);
		} else {
			validateWith(doc.getId(), IReconcileEngine.NULL);
		}
	}

	public BoshLanguageServer setMaxCompletions(int maxCompletions) {
		completionEngine.setMaxCompletions(maxCompletions);
		return this;
	}

}
