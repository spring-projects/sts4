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

import org.eclipse.lsp4j.CompletionList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ide.vscode.commons.languageserver.completion.VscodeCompletionEngineAdapter;
import org.springframework.ide.vscode.commons.languageserver.hover.HoverInfoProvider;
import org.springframework.ide.vscode.commons.languageserver.hover.VscodeHoverEngineAdapter;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
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
import org.springframework.ide.vscode.commons.yaml.reconcile.TypeBasedYamlSymbolHandler;
import org.springframework.ide.vscode.commons.yaml.reconcile.YamlSchemaBasedReconcileEngine;
import org.springframework.ide.vscode.commons.yaml.schema.YType;
import org.springframework.ide.vscode.commons.yaml.schema.YamlSchema;
import org.springframework.ide.vscode.commons.yaml.snippet.SchemaBasedSnippetGenerator;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureProvider;
import org.springframework.ide.vscode.concourse.github.GithubInfoProvider;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

import reactor.core.publisher.Mono;

@Component
public class ConcourseLanguageServerInitializer implements InitializingBean {

	private final YamlCompletionEngineOptions COMPLETION_OPTIONS = YamlCompletionEngineOptions.DEFAULT;
	private final YamlStructureProvider structureProvider = YamlStructureProvider.DEFAULT;

	private ConcourseModel models;

	private SchemaSpecificPieces forPipelines;
	private SchemaSpecificPieces forTasks;
	private YamlQuickfixes yamlQuickfixes;
	private YamlASTProvider currentAsts;

	@Autowired private SimpleLanguageServer server;
	@Autowired private GithubInfoProvider github;

	private class SchemaSpecificPieces {

		final VscodeCompletionEngineAdapter completionEngine;
		final VscodeHoverEngineAdapter hoverEngine;
		final YamlSchemaBasedReconcileEngine reconcileEngine;
		final DocumentSymbolHandler symbolHandler;

		SchemaSpecificPieces(YamlSchema schema, List<YType> definitionTypes) {
			SchemaBasedYamlAssistContextProvider contextProvider = new SchemaBasedYamlAssistContextProvider(schema);
			YamlCompletionEngine yamlCompletionEngine = new YamlCompletionEngine(structureProvider, contextProvider, COMPLETION_OPTIONS);
			this.completionEngine = server.createCompletionEngineAdapter(yamlCompletionEngine);

			HoverInfoProvider infoProvider = new YamlHoverInfoProvider(currentAsts, structureProvider, contextProvider);
			this.hoverEngine = new VscodeHoverEngineAdapter(server, infoProvider);

			this.reconcileEngine = new YamlSchemaBasedReconcileEngine(currentAsts, schema, yamlQuickfixes);
			reconcileEngine.setTypeCollector(models.getAstTypeCache());

			this.symbolHandler = CollectionUtil.hasElements(definitionTypes)
					? new TypeBasedYamlSymbolHandler(server.getTextDocumentService(), models.getAstTypeCache(), definitionTypes, server::hasHierarchicalDocumentSymbolSupport)
					: DocumentSymbolHandler.NO_SYMBOLS;
		}

		public void setMaxCompletions(int max) {
			completionEngine.setMaxCompletions(max);
		}
	}

	public void enableSnippets(PipelineYmlSchema schema, boolean enable) {
		if (enable) {
			schema.f.setSnippetProvider(new SchemaBasedSnippetGenerator(schema.getTypeUtil(), server::createSnippetBuilder));
		} else {
			schema.f.setSnippetProvider(null);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.models = new ConcourseModel(server);
		this.currentAsts = models.getAstCache().getAstProvider(false);
		PipelineYmlSchema pipelineSchema = new PipelineYmlSchema(models, github);
		enableSnippets(pipelineSchema, true);
		SimpleTextDocumentService documents = server.getTextDocumentService();
		this.yamlQuickfixes = new YamlQuickfixes(server.getQuickfixRegistry(), documents, structureProvider);

		this.forPipelines = new SchemaSpecificPieces(pipelineSchema, pipelineSchema.getDefinitionTypes());
		this.forTasks = new SchemaSpecificPieces(pipelineSchema.getTaskSchema(), null);
		ConcourseDefinitionFinder definitionFinder = new ConcourseDefinitionFinder(server, models, pipelineSchema);

//		SimpleWorkspaceService workspace = getWorkspaceService();
		documents.onDidChangeContent(params -> {
			TextDocument doc = params.getDocument();
			if (LanguageId.CONCOURSE_PIPELINE.equals(doc.getLanguageId())) {
				server.validateWith(doc.getId(), forPipelines.reconcileEngine);
			} else if (LanguageId.CONCOURSE_TASK.equals(doc.getLanguageId())) {
				server.validateWith(doc.getId(), forTasks.reconcileEngine);
			} else {
				server.validateWith(doc.getId(), IReconcileEngine.NULL);
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
			return Mono.just(new CompletionList(false, ImmutableList.of()));
		});
		documents.onCompletionResolve(item -> {
			server.completionResolver.resolveNow(item);
			return item;
		});
		documents.onHover(params -> {
			TextDocument doc = documents.get(params);
			if (doc!=null) {
				if (LanguageId.CONCOURSE_PIPELINE.equals(doc.getLanguageId())) {
					return forPipelines.hoverEngine.handle(params);
				} else if (LanguageId.CONCOURSE_TASK.equals(doc.getLanguageId())) {
					return forTasks.hoverEngine.handle(params);
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

//	@Override
//	protected DiagnosticSeverity getDiagnosticSeverity(ReconcileProblem problem) {
//		ProblemType type = problem.getType();
//		if (YamlSchemaProblems.PROPERTY_CONSTRAINT.contains(type)) {
//			return DiagnosticSeverity.Warning;
//		}
//		return server.getDiagnosticSeverity(problem);
//	}

	public void setMaxCompletions(int max) {
		forPipelines.setMaxCompletions(max);
		forTasks.setMaxCompletions(max);
	}

}
