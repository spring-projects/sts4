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

import org.springframework.ide.vscode.commons.languageserver.completion.VscodeCompletionEngineAdapter;
import org.springframework.ide.vscode.commons.languageserver.completion.VscodeCompletionEngineAdapter.LazyCompletionResolver;
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
import org.springframework.ide.vscode.commons.yaml.schema.YamlSchema;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureProvider;
import org.yaml.snakeyaml.Yaml;

public class BoshLanguageServer extends SimpleLanguageServer {

	private Yaml yaml = new Yaml();
	private final LazyCompletionResolver completionResolver = new LazyCompletionResolver(); //Set to null to disable lazy resolving
	private final VscodeCompletionEngineAdapter completionEngine;

	public BoshLanguageServer() {
		super("vscode-bosh");
		YamlASTProvider parser = new YamlParser(yaml);
		SimpleTextDocumentService documents = getTextDocumentService();
		YamlSchema schema = new BoshDeploymentManifestSchema();

		YamlStructureProvider structureProvider = YamlStructureProvider.DEFAULT;
		YamlAssistContextProvider contextProvider = new SchemaBasedYamlAssistContextProvider(schema);
		YamlCompletionEngine yamlCompletionEngine = new YamlCompletionEngine(structureProvider, contextProvider, YamlCompletionEngineOptions.DEFAULT);
		completionEngine = new VscodeCompletionEngineAdapter(this, yamlCompletionEngine);
		completionEngine.setLazyCompletionResolver(completionResolver);
		HoverInfoProvider infoProvider = new YamlHoverInfoProvider(parser, structureProvider, contextProvider);
		VscodeHoverEngine hoverEngine = new VscodeHoverEngineAdapter(this, infoProvider);
		YamlQuickfixes quickfixes = new YamlQuickfixes(getQuickfixRegistry(), getTextDocumentService(), structureProvider);
		IReconcileEngine engine = new YamlSchemaBasedReconcileEngine(parser, schema, quickfixes);

		documents.onDidChangeContent(params -> {
			validateOnDocumentChange(engine, params.getDocument());
		});
		documents.onCompletion(completionEngine::getCompletions);
		documents.onCompletionResolve(completionEngine::resolveCompletion);
		documents.onHover(hoverEngine ::getHover);
	}

	private void validateOnDocumentChange(IReconcileEngine engine, TextDocument doc) {
		if (LanguageId.BOSH_DEPLOYMENT.equals(doc.getLanguageId())) {
			validateWith(doc.getId(), engine);
		} else {
			validateWith(doc.getId(), IReconcileEngine.NULL);
		}
	}

	@Override
	public boolean hasLazyCompletionResolver() {
		return completionResolver!=null;
	}

	public BoshLanguageServer setMaxCompletions(int maxCompletions) {
		completionEngine.setMaxCompletions(maxCompletions);
		return this;
	}

}
