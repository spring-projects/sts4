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

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.springframework.ide.vscode.commons.languageserver.LanguageIds;
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
import org.springframework.ide.vscode.commons.yaml.completion.SchemaBasedYamlAssistContextProvider;
import org.springframework.ide.vscode.commons.yaml.completion.YamlAssistContextProvider;
import org.springframework.ide.vscode.commons.yaml.completion.YamlCompletionEngine;
import org.springframework.ide.vscode.commons.yaml.hover.YamlHoverInfoProvider;
import org.springframework.ide.vscode.commons.yaml.reconcile.YamlSchemaBasedReconcileEngine;
import org.springframework.ide.vscode.commons.yaml.schema.YamlSchema;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureProvider;

import com.google.common.collect.ImmutableList;

public class ConcourseLanguageServer extends SimpleLanguageServer {

	YamlStructureProvider structureProvider = YamlStructureProvider.DEFAULT;
	SimpleTextDocumentService documents = getTextDocumentService();
	ConcourseModel models = new ConcourseModel(documents);
	YamlASTProvider currentAsts = models.getAstProvider(false);

	private class SchemaSpecificPieces {

		final YamlSchema schema;
		final VscodeCompletionEngine completionEngine;
		final VscodeHoverEngineAdapter hoverEngine;
		final YamlSchemaBasedReconcileEngine reconcileEngine;

		SchemaSpecificPieces(YamlSchema schema) {
			this.schema = schema;
			SchemaBasedYamlAssistContextProvider contextProvider = new SchemaBasedYamlAssistContextProvider(schema);
			YamlCompletionEngine yamlCompletionEngine = new YamlCompletionEngine(structureProvider, contextProvider);
			this.completionEngine = new VscodeCompletionEngineAdapter(ConcourseLanguageServer.this, yamlCompletionEngine);

			HoverInfoProvider infoProvider = new YamlHoverInfoProvider(currentAsts, structureProvider, contextProvider);
			this.hoverEngine = new VscodeHoverEngineAdapter(ConcourseLanguageServer.this, infoProvider);

			this.reconcileEngine = new YamlSchemaBasedReconcileEngine(currentAsts, schema);
			reconcileEngine.setTypeCollector(models.getAstTypeCache());
		}

	}

	public ConcourseLanguageServer() {
		PipelineYmlSchema pipelineSchema = new PipelineYmlSchema(models);

		SchemaSpecificPieces forPipelines = new SchemaSpecificPieces(pipelineSchema);
		SchemaSpecificPieces forTasks = new SchemaSpecificPieces(pipelineSchema.getTaskSchema());
		ConcourseDefinitionFinder definitionFinder = new ConcourseDefinitionFinder(this, models, pipelineSchema);

//		SimpleWorkspaceService workspace = getWorkspaceService();
		documents.onDidChangeContent(params -> {
			TextDocument doc = params.getDocument();
			if (LanguageIds.CONCOURSE_PIPELINE.equals(doc.getLanguageId())) {
				validateWith(doc, forPipelines.reconcileEngine);
			} else if (LanguageIds.CONCOURSE_TASK.equals(doc.getLanguageId())) {
				validateWith(doc, forTasks.reconcileEngine);
			} else {
				validateWith(doc, IReconcileEngine.NULL);
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

		documents.onCompletion(params -> {
			TextDocument doc = documents.get(params);
			if (doc!=null) {
				if (LanguageIds.CONCOURSE_PIPELINE.equals(doc.getLanguageId())) {
					return forPipelines.completionEngine.getCompletions(params);
				} else if (LanguageIds.CONCOURSE_TASK.equals(doc.getLanguageId())) {
					return forTasks.completionEngine.getCompletions(params);
				}
			}
			return CompletableFuture.completedFuture(new CompletionList(false, ImmutableList.of()));
		});
		documents.onCompletionResolve(params -> {
			//this is a bogus implementation. But its not currently used.
			throw new IllegalStateException("Not implemented");

		});
		documents.onHover(forPipelines.hoverEngine::getHover); //TODO: dispatch based on language id
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
