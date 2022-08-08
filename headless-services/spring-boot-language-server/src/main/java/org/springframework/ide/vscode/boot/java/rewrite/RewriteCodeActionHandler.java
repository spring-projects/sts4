/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.rewrite;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionCapabilities;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionResolveSupportCapabilities;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Tree;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.CompilationUnit;
import org.openrewrite.marker.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.app.BootJavaConfig;
import org.springframework.ide.vscode.boot.java.handlers.JavaCodeActionHandler;
import org.springframework.ide.vscode.boot.java.rewrite.reconcile.RecipeSpringJavaProblemDescriptor;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.util.LspClient;
import org.springframework.ide.vscode.commons.languageserver.util.LspClient.Client;
import org.springframework.ide.vscode.commons.rewrite.java.FixAssistMarker;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.util.text.IRegion;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class RewriteCodeActionHandler implements JavaCodeActionHandler {
		
	private static final Logger log = LoggerFactory.getLogger(RewriteCodeActionHandler.class);
	
	final private RewriteCompilationUnitCache cuCache;
	final private RewriteRecipeRepository recipeRepo;

	private BootJavaConfig config;

	public RewriteCodeActionHandler(RewriteCompilationUnitCache cuCache, RewriteRecipeRepository recipeRepo, BootJavaConfig config) {
		this.cuCache = cuCache;
		this.recipeRepo = recipeRepo;
		this.config = config;
	}

	protected static boolean isResolve(CodeActionCapabilities capabilities, String property) {
		if (capabilities != null) {
			CodeActionResolveSupportCapabilities resolveSupport = capabilities.getResolveSupport();
			if (resolveSupport != null) {
				List<String> properties = resolveSupport.getProperties();
				return properties != null && properties.contains(property);
			}
		}
		return false;
	}
	
	private boolean isSupported(CodeActionCapabilities capabilities, CodeActionContext context) {
		// Default case is anything non-quick fix related.
		if (isResolve(capabilities, "edit")) {
			if (context.getOnly() != null) {
				return context.getOnly().contains(CodeActionKind.Refactor);
			} else {
				if (LspClient.currentClient() == Client.ECLIPSE) {
					// Eclipse would have no diagnostics in the context for QuickAssists refactoring. Diagnostics will be around for QuickFix only
					return context.getDiagnostics().isEmpty();
				} else {
					return true;
				}
				
			}
		}
		return false;
	}

	@Override
	public List<Either<Command, CodeAction>> handle(IJavaProject project, CancelChecker cancelToken,
			CodeActionCapabilities capabilities, CodeActionContext context, TextDocument doc, IRegion region) {
		if (!config.isRewriteReconcileEnabled()) {
			return Collections.emptyList();
		}
		
		try {
			
			// Wait for recipe repo to load if not loaded - should be loaded by the time we get here.
			recipeRepo.loaded.get();
			
			List<RecipeCodeActionDescriptor> descriptors = recipeRepo.getCodeActionRecipeDescriptors().stream()
				// If Recipe not present - don't show quick assist as it won't be handled without the Rewrite recipe present	
				.filter(d -> recipeRepo.getRecipe(d.getRecipeId()).isPresent())
				.filter(d -> d.isApplicable(project))
				.collect(Collectors.toList());

			
			if (isSupported(capabilities, context)) {
				CompilationUnit cu = cuCache.getCU(project, URI.create(doc.getUri()));
				if (cu != null) {
	
					cu = recipeRepo.mark(descriptors, cu);
	
					List<CodeAction> codeActions = new ArrayList<>();
					new JavaIsoVisitor<ExecutionContext>() {
						public J visit(Tree tree, ExecutionContext ctx) {
							if (tree == null) {
								return null;
							}
							if (tree instanceof J) {
								J node = (J) tree;
								Range range = node.getMarkers().findFirst(Range.class).orElse(null);
								if (range != null
										&& range.getStart().getOffset() <= region.getOffset()
										&& region.getOffset() + region.getLength() <= range.getEnd().getOffset()) {
									
									for (FixAssistMarker m : node.getMarkers().findAll(FixAssistMarker.class)) {
										for (CodeAction ca : createCodeActions(doc, m, node)) {
											codeActions.add(ca);
										}
									}
									return super.visit(tree, ctx);
								} else {
									return (J) tree;
								}
							}
							return (J) tree;
						};
					}.visitNonNull(cu, new InMemoryExecutionContext());
					return codeActions.stream().map(ca -> Either.<Command, CodeAction>forRight(ca)).collect(Collectors.toList());
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return Collections.emptyList();
	}

	private CodeAction[] createCodeActions(IDocument doc, FixAssistMarker m, J astNode) {
		if (astNode != null) {
			Range range = astNode.getMarkers().findFirst(Range.class).orElse(null);
			RecipeCodeActionDescriptor descriptor = recipeRepo.getCodeActionRecipeDescriptor(m.getRecipeId());
			if (descriptor != null && descriptor.getScopes() != null) {
				// Fix descriptor code actions may have the overlapping scopes with assist descriptors. Quick fixes are provided separately hence overlapping scopes will produces duplicates quick assist.
				// Therefore, need to compute recipe scopes that don't overlap with quick fix descriptor recipe scopes. 
				RecipeSpringJavaProblemDescriptor fixDescriptor = recipeRepo.getProblemRecipeDescriptor(m.getRecipeId());
				return Arrays.stream(descriptor.getScopes())
						.filter(s -> fixDescriptor == null || !Arrays.asList(fixDescriptor.getScopes()).contains(s))
						.map(s -> createCodeActionFromScope(doc, descriptor, s, m, range))
						.filter(Objects::nonNull)
						.toArray(CodeAction[]::new);
			}
		}
		return new CodeAction[0];
	}

	private CodeAction createCodeActionFromScope(IDocument doc, RecipeCodeActionDescriptor descriptor,
			RecipeScope s, FixAssistMarker m, Range range) {
		CodeAction ca = new CodeAction();
		ca.setKind(CodeActionKind.Refactor);
		ca.setTitle(descriptor.getLabel(s));
		ca.setData(new RewriteRefactorings.Data(
				m.getRecipeId(),
				doc.getUri(), 
				s,
				m.getScope() == null ? null : m.getScope().toString(),
				m.getParameters() == null ? Collections.emptyMap() : m.getParameters()
		));
		return ca;
	}
	
}
