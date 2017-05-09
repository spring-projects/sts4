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

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.springframework.ide.vscode.commons.languageserver.completion.VscodeCompletionEngineAdapter;
import org.springframework.ide.vscode.commons.languageserver.hover.HoverInfoProvider;
import org.springframework.ide.vscode.commons.languageserver.hover.VscodeHoverEngineAdapter;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;
import org.springframework.ide.vscode.commons.languageserver.util.DocumentSymbolHandler;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.CollectionUtil;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.ide.vscode.commons.yaml.ast.YamlASTProvider;
import org.springframework.ide.vscode.commons.yaml.completion.SchemaBasedYamlAssistContextProvider;
import org.springframework.ide.vscode.commons.yaml.completion.YamlCompletionEngine;
import org.springframework.ide.vscode.commons.yaml.completion.YamlCompletionEngineOptions;
import org.springframework.ide.vscode.commons.yaml.hover.YamlHoverInfoProvider;
import org.springframework.ide.vscode.commons.yaml.quickfix.YamlQuickfixes;
import org.springframework.ide.vscode.commons.yaml.reconcile.YamlSchemaBasedReconcileEngine;
import org.springframework.ide.vscode.commons.yaml.reconcile.YamlSchemaProblems;
import org.springframework.ide.vscode.commons.yaml.schema.YType;
import org.springframework.ide.vscode.commons.yaml.schema.YamlSchema;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureProvider;

import com.google.common.collect.ImmutableList;

public class ConcourseLanguageServer extends SimpleLanguageServer {

	private final YamlCompletionEngineOptions COMPLETION_OPTIONS;
	YamlStructureProvider structureProvider = YamlStructureProvider.DEFAULT;
	SimpleTextDocumentService documents = getTextDocumentService();
	ConcourseModel models = new ConcourseModel(this);
	YamlASTProvider currentAsts = models.getAstProvider(false);
	private SchemaSpecificPieces forPipelines;
	private SchemaSpecificPieces forTasks;
	private final YamlQuickfixes yamlQuickfixes;

	private class SchemaSpecificPieces {

		final VscodeCompletionEngineAdapter completionEngine;
		final VscodeHoverEngineAdapter hoverEngine;
		final YamlSchemaBasedReconcileEngine reconcileEngine;
		final DocumentSymbolHandler symbolHandler;

		SchemaSpecificPieces(YamlSchema schema, List<YType> definitionTypes) {
			SchemaBasedYamlAssistContextProvider contextProvider = new SchemaBasedYamlAssistContextProvider(schema);
			YamlCompletionEngine yamlCompletionEngine = new YamlCompletionEngine(structureProvider, contextProvider, COMPLETION_OPTIONS);
			this.completionEngine = new VscodeCompletionEngineAdapter(ConcourseLanguageServer.this, yamlCompletionEngine);

			HoverInfoProvider infoProvider = new YamlHoverInfoProvider(currentAsts, structureProvider, contextProvider);
			this.hoverEngine = new VscodeHoverEngineAdapter(ConcourseLanguageServer.this, infoProvider);

			this.reconcileEngine = new YamlSchemaBasedReconcileEngine(currentAsts, schema, yamlQuickfixes);
			reconcileEngine.setTypeCollector(models.getAstTypeCache());

			this.symbolHandler = CollectionUtil.hasElements(definitionTypes)
					? new ConcourseDocumentSymbolHandler(documents, models.getAstTypeCache(), definitionTypes)
					: DocumentSymbolHandler.NO_SYMBOLS;
		}

		public void setMaxCompletions(int max) {
			completionEngine.setMaxCompletionsNumber(max);
		}
	}

	public ConcourseLanguageServer(YamlCompletionEngineOptions completionOptions) {
		super("vscode-concourse");
		this.COMPLETION_OPTIONS = completionOptions;
		PipelineYmlSchema pipelineSchema = new PipelineYmlSchema(models);
		this.yamlQuickfixes = new YamlQuickfixes(getQuickfixRegistry(), documents, structureProvider);

		this.forPipelines = new SchemaSpecificPieces(pipelineSchema, pipelineSchema.getDefinitionTypes());
		this.forTasks = new SchemaSpecificPieces(pipelineSchema.getTaskSchema(), null);
		ConcourseDefinitionFinder definitionFinder = new ConcourseDefinitionFinder(this, models, pipelineSchema);

//		SimpleWorkspaceService workspace = getWorkspaceService();
		documents.onDidChangeContent(params -> {
			TextDocument doc = params.getDocument();
			if (LanguageId.CONCOURSE_PIPELINE.equals(doc.getLanguageId())) {
				validateWith(doc.getId(), forPipelines.reconcileEngine);
			} else if (LanguageId.CONCOURSE_TASK.equals(doc.getLanguageId())) {
				validateWith(doc.getId(), forTasks.reconcileEngine);
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

		documents.onCompletion(params -> {
			TextDocument doc = documents.get(params);
			if (doc!=null) {
				if (LanguageId.CONCOURSE_PIPELINE.equals(doc.getLanguageId())) {
					return forPipelines.completionEngine.getCompletions(params);
				} else if (LanguageId.CONCOURSE_TASK.equals(doc.getLanguageId())) {
					return forTasks.completionEngine.getCompletions(params);
				}
			}
			return CompletableFuture.completedFuture(new CompletionList(false, ImmutableList.of()));
		});
		documents.onCompletionResolve(params -> {
			//this is a bogus implementation. But its not currently used.
			throw new IllegalStateException("Not implemented");

		});
		documents.onHover(params -> {
			TextDocument doc = documents.get(params);
			if (doc!=null) {
				if (LanguageId.CONCOURSE_PIPELINE.equals(doc.getLanguageId())) {
					return forPipelines.hoverEngine.getHover(params);
				} else if (LanguageId.CONCOURSE_TASK.equals(doc.getLanguageId())) {
					return forTasks.hoverEngine.getHover(params);
				}
			}
			return SimpleTextDocumentService.NO_HOVER;
		});
		documents.onDefinition(definitionFinder);
		documents.onDocumentSymbol((params) -> {
			DocumentSymbolHandler handler = DocumentSymbolHandler.NO_SYMBOLS;
			TextDocument doc = documents.getDocument(params.getTextDocument().getUri());
			if (doc!=null) {
				if (LanguageId.CONCOURSE_PIPELINE.equals(doc.getLanguageId())) {
					handler = forPipelines.symbolHandler;
				} else if (LanguageId.CONCOURSE_TASK.equals(doc.getLanguageId())) {
					handler = forTasks.symbolHandler;
				}
			}
			return handler.handle(params);
		});
	}

	@Override
	protected DiagnosticSeverity getDiagnosticSeverity(ReconcileProblem problem) {
		ProblemType type = problem.getType();
		if (YamlSchemaProblems.PROPERTY_CONSTRAINT.contains(type)) {
			return DiagnosticSeverity.Warning;
		}
		return super.getDiagnosticSeverity(problem);
	}
	public SimpleLanguageServer setMaxCompletions(int max) {
		forPipelines.setMaxCompletions(max);
		forTasks.setMaxCompletions(max);
		return this;
	}

}
