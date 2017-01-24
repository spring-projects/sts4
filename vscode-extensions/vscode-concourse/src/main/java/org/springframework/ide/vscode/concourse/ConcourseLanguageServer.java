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
package org.springframework.ide.vscode.concourse;

import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.springframework.ide.vscode.commons.languageserver.completion.VscodeCompletionEngine;
import org.springframework.ide.vscode.commons.languageserver.completion.VscodeCompletionEngineAdapter;
import org.springframework.ide.vscode.commons.languageserver.hover.HoverInfoProvider;
import org.springframework.ide.vscode.commons.languageserver.hover.VscodeHoverEngine;
import org.springframework.ide.vscode.commons.languageserver.hover.VscodeHoverEngineAdapter;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.ide.vscode.commons.yaml.ast.YamlASTProvider;
import org.springframework.ide.vscode.commons.yaml.completion.SchemaBasedYamlAssistContextProvider;
import org.springframework.ide.vscode.commons.yaml.completion.YamlAssistContextProvider;
import org.springframework.ide.vscode.commons.yaml.completion.YamlCompletionEngine;
import org.springframework.ide.vscode.commons.yaml.hover.YamlHoverInfoProvider;
import org.springframework.ide.vscode.commons.yaml.reconcile.YamlSchemaBasedReconcileEngine;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureProvider;

public class ConcourseLanguageServer extends SimpleLanguageServer {

	public ConcourseLanguageServer() {
		SimpleTextDocumentService documents = getTextDocumentService();

		ConcourseModel models = new ConcourseModel(documents);
		YamlASTProvider currentAsts = models.getAstProvider(false);

		YamlStructureProvider structureProvider = YamlStructureProvider.DEFAULT;
		PipelineYmlSchema schema = new PipelineYmlSchema(models);
		YamlAssistContextProvider contextProvider = new SchemaBasedYamlAssistContextProvider(schema);
		YamlCompletionEngine yamlCompletionEngine = new YamlCompletionEngine(structureProvider, contextProvider);
		VscodeCompletionEngine completionEngine = new VscodeCompletionEngineAdapter(this, yamlCompletionEngine);
		HoverInfoProvider infoProvider = new YamlHoverInfoProvider(currentAsts, structureProvider, contextProvider);
		VscodeHoverEngine hoverEngine = new VscodeHoverEngineAdapter(this, infoProvider);
		YamlSchemaBasedReconcileEngine reconcileEngine = new YamlSchemaBasedReconcileEngine(currentAsts, schema);
		ConcourseDefinitionFinder definitionFinder = new ConcourseDefinitionFinder(this, models, schema);
		reconcileEngine.setTypeCollector(models.getAstTypeCache());

//		SimpleWorkspaceService workspace = getWorkspaceService();
		documents.onDidChangeContent(params -> {
			TextDocument doc = params.getDocument();
			validateWith(doc, reconcileEngine);
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
		documents.onHover(hoverEngine::getHover);
		documents.onDefinition(definitionFinder);
	}


	@Override
	protected ServerCapabilities getServerCapabilities() {
		ServerCapabilities c = new ServerCapabilities();

		c.setTextDocumentSync(TextDocumentSyncKind.Incremental);
		c.setHoverProvider(true);

		CompletionOptions completionProvider = new CompletionOptions();
		completionProvider.setResolveProvider(false);
		c.setCompletionProvider(completionProvider);

		c.setDefinitionProvider(true);

		return c;
	}
}
