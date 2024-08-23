/*******************************************************************************
 * Copyright (c) 2017, 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.spel.AnnotationParamSpelExtractor;
import org.springframework.ide.vscode.boot.java.spel.AnnotationParamSpelExtractor.Snippet;
import org.springframework.ide.vscode.boot.java.spel.SpelSemanticTokens;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokenData;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonPrimitive;

/**
 * @author Udayani V
 */
public class QueryCodeLensProvider implements CodeLensProvider {
	
	protected static Logger logger = LoggerFactory.getLogger(QueryCodeLensProvider.class);

	public static final String CMD_ENABLE_COPILOT_FEATURES = "sts/enable/copilot/features";

	private static final String QUERY = "Query";
	private static final String FQN_QUERY = "org.springframework.data.jpa.repository." + QUERY;
	private static final String CMD = "vscode-spring-boot.query.explain";

	private final AnnotationParamSpelExtractor[] spelExtractors = AnnotationParamSpelExtractor.SPEL_EXTRACTORS;

	private final JavaProjectFinder projectFinder;
	
	private SpelSemanticTokens spelSemanticTokens;

	private static boolean showCodeLenses;

	public QueryCodeLensProvider(JavaProjectFinder projectFinder, SimpleLanguageServer server, SpelSemanticTokens spelSemanticTokens) {
		this.projectFinder = projectFinder;
		this.spelSemanticTokens = spelSemanticTokens;
		server.onCommand(CMD_ENABLE_COPILOT_FEATURES, params -> {
			if (params.getArguments().get(0) instanceof JsonPrimitive) {
				QueryCodeLensProvider.showCodeLenses = ((JsonPrimitive) params.getArguments().get(0)).getAsBoolean();
			}
			return CompletableFuture.completedFuture(showCodeLenses);
		});
	}

	@Override
	public void provideCodeLenses(CancelChecker cancelToken, TextDocument document, CompilationUnit cu,
			List<CodeLens> resultAccumulator) {
		if (!showCodeLenses) {
			return;
		}
		cu.accept(new ASTVisitor() {

			@Override
			public boolean visit(SingleMemberAnnotation node) {
				Arrays.stream(spelExtractors).map(e -> e.getSpelRegion(node)).filter(o -> o.isPresent())
						.map(o -> o.get()).forEach(snippet -> {
							String additionalContext = parseSpelAndFetchContext(cu, snippet.text());
							provideCodeLensForSpelExpression(cancelToken, node, document, snippet,
									additionalContext, resultAccumulator);
						});

				if (isQueryAnnotation(node)) {
					String queryPrompt = determineQueryPrompt(document);
					provideCodeLensForQuery(cancelToken, node, document, node.getValue(), queryPrompt,
							resultAccumulator);
				}

				return super.visit(node);
			}

			@Override
			public boolean visit(NormalAnnotation node) {
				

				Arrays.stream(spelExtractors).map(e -> e.getSpelRegion(node)).filter(o -> o.isPresent())
						.map(o -> o.get()).forEach(snippet -> {
							String additionalContext = parseSpelAndFetchContext(cu, snippet.text());
							provideCodeLensForSpelExpression(cancelToken, node, document, snippet, additionalContext,
									resultAccumulator);
						});

				if (isQueryAnnotation(node)) {
					String queryPrompt = determineQueryPrompt(document);
					for (Object value : node.values()) {
						if (value instanceof MemberValuePair) {
							MemberValuePair pair = (MemberValuePair) value;
							if ("value".equals(pair.getName().getIdentifier())) {
								provideCodeLensForQuery(cancelToken, node, document, pair.getValue(), queryPrompt,
										resultAccumulator);
								break;
							}
						}
					}
				}

				return super.visit(node);
			}
		});
	}

	protected void provideCodeLensForSpelExpression(CancelChecker cancelToken, Annotation node, TextDocument document,
			Snippet snippet, String additionalContext, List<CodeLens> resultAccumulator) {
		cancelToken.checkCanceled();

		if (snippet != null) {
			try {
				String context = additionalContext != null && !additionalContext.isEmpty() ? String.format(
								"""
								   Finally, provide a brief summary of what the following method does, focusing on its role within the SpEL expression.
								   The summary should mention key criteria the method checks but avoid detailed implementation steps.
								   Please include this summary as an appendix to the main explanation, and avoid repeating information covered earlier.\n\n%s
								""",additionalContext) : "";

				CodeLens codeLens = new CodeLens();
				codeLens.setRange(document.toRange(snippet.offset(), snippet.text().length()));

				Command cmd = new Command();
				cmd.setTitle(QueryType.SPEL.getTitle());
				cmd.setCommand(CMD);
				cmd.setArguments(ImmutableList.of(QueryType.SPEL.getPrompt() + snippet.text() + "\n\n" + context));
				codeLens.setCommand(cmd);

				resultAccumulator.add(codeLens);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

	protected void provideCodeLensForQuery(CancelChecker cancelToken, Annotation node, TextDocument document,
			Expression valueExp, String query, List<CodeLens> resultAccumulator) {
		cancelToken.checkCanceled();

		if (valueExp != null) {
			try {

				CodeLens codeLens = new CodeLens();
				codeLens.setRange(document.toRange(valueExp.getStartPosition(), valueExp.getLength()));

				Command cmd = new Command();
				cmd.setTitle(QueryType.DEFAULT.getTitle());
				cmd.setCommand(CMD);
				cmd.setArguments(ImmutableList.of(query + valueExp.toString()));
				codeLens.setCommand(cmd);

				resultAccumulator.add(codeLens);
			} catch (BadLocationException e) {
				logger.error("Error providing code lens: " + e.getMessage());
			}
		}
	}

	private static boolean isQueryAnnotation(Annotation a) {
		return FQN_QUERY.equals(a.getTypeName().getFullyQualifiedName())
				|| QUERY.equals(a.getTypeName().getFullyQualifiedName());
	}

	private String determineQueryPrompt(TextDocument document) {
		Optional<IJavaProject> optProject = projectFinder.find(document.getId());
		if (optProject.isPresent()) {
			IJavaProject jp = optProject.get();
			return SpringProjectUtil.hasDependencyStartingWith(jp, "hibernate-core", null) ? QueryType.HQL.getPrompt()
					: QueryType.JPQL.getPrompt();
		}
		return QueryType.DEFAULT.getPrompt();
	}

	private String parseSpelAndFetchContext(CompilationUnit cu, String spelExpression) {
		List<SemanticTokenData> tokens = parseSpelExpression(spelExpression);
		Set<String> methodNames = extractMethodNames(tokens, spelExpression);
		List<String> context = collectMethodContexts(methodNames, cu);
		return String.join("\n", context);
	}

	private List<SemanticTokenData> parseSpelExpression(String spelText) {
		try {
			return spelSemanticTokens.computeTokens(spelText, 0);
		} catch (Exception e) {
			logger.error("Error computing tokens: " + e.getMessage());
			return Collections.emptyList();
		}
	}

	private static Set<String> extractMethodNames(List<SemanticTokenData> tokens, String spelText) {
		return tokens.stream().filter(token -> "method".equals(token.type()))
				.map(token -> spelText.substring(token.start(), token.end())).collect(Collectors.toSet());
	}

	private List<String> collectMethodContexts(Set<String> methodNames, CompilationUnit cu) {
		List<String> methodContext = new ArrayList<>();
		for (String methodName : methodNames) {
			cu.accept(new ASTVisitor() {
				@Override
				public boolean visit(MethodDeclaration node) {
					if (node.getName().getIdentifier().equals(methodName)) {
						methodContext.add(node.toString());
					}
					return super.visit(node);
				}
			});
		}
		return methodContext;
	}

}