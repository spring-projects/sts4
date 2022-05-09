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
package org.springframework.ide.vscode.boot.java.rewrite.codeaction;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionCapabilities;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionResolveSupportCapabilities;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.openrewrite.Recipe;
import org.openrewrite.Result;
import org.openrewrite.java.tree.J;
import org.springframework.ide.vscode.boot.java.handlers.JavaCodeAction;
import org.springframework.ide.vscode.boot.java.rewrite.ORCompilationUnitCache;
import org.springframework.ide.vscode.boot.java.rewrite.ORDocUtils;
import org.springframework.ide.vscode.boot.java.rewrite.RewriteRefactorings;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.LspClient;
import org.springframework.ide.vscode.commons.languageserver.util.LspClient.Client;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.text.IRegion;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public abstract class AbstractRewriteJavaCodeAction implements JavaCodeAction {

	protected ORCompilationUnitCache orCuCache;
	protected String codeActionId;
	protected SimpleLanguageServer server;
	protected JavaProjectFinder projectFinder;

	public AbstractRewriteJavaCodeAction(SimpleLanguageServer server, JavaProjectFinder projectFinder,
			RewriteRefactorings rewriteRefactorings, ORCompilationUnitCache orCuCache, String codeActionId) {
		this.server = server;
		this.projectFinder = projectFinder;
		this.orCuCache = orCuCache;
		this.codeActionId = codeActionId;
		rewriteRefactorings.addRefactoring(codeActionId, this::perform);
	}

	protected CodeAction createCodeAction(String kind, String title, List<?> arguments) {
		CodeAction ca = new CodeAction();
		ca.setKind(kind);
		ca.setTitle(title);
		ca.setData(new RewriteRefactorings.Data(codeActionId, arguments));
		return ca;
	}
	
	protected WorkspaceEdit applyRecipe(Recipe r, IJavaProject project, List<J.CompilationUnit> cus) {
		List<Result> results = r.run(cus);
		List<Either<TextDocumentEdit, ResourceOperation>> edits = results.stream().filter(res -> res.getAfter() != null).map(res -> {
			URI docUri = project.getLocationUri().resolve(res.getAfter().getSourcePath().toString());
			TextDocument doc = server.getTextDocumentService().getLatestSnapshot(docUri.toString());
			if (doc == null) {
				doc = new TextDocument(docUri.toString(), LanguageId.JAVA, 0, res.getBefore() == null ? "" : res.getBefore().printAll());
			}
			return ORDocUtils.computeTextDocEdit(doc, res);
		}).filter(e -> e.isPresent()).map(e -> e.get()).map(e -> Either.<TextDocumentEdit, ResourceOperation>forLeft(e)).collect(Collectors.toList());
		if (edits.isEmpty()) {
			return null;
		}
		WorkspaceEdit workspaceEdit = new WorkspaceEdit();
		workspaceEdit.setDocumentChanges(edits);
		return workspaceEdit;
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
	
	protected boolean isSupported(CodeActionCapabilities capabilities, CodeActionContext context) {
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
	final public List<Either<Command, CodeAction>> getCodeActions(CodeActionCapabilities capabilities,
			CodeActionContext context, TextDocument doc, IRegion region, IJavaProject project, CompilationUnit cu,
			ASTNode node) {
		if (isSupported(capabilities, context)) {
			return provideCodeActions(context, doc, region, project, cu, node);
		}
		return null;
	}
	
	final protected WorkspaceEdit perform(List<?> args, Supplier<Recipe> recipeSupplier) {
		String docUri = (String) args.get(0);
		boolean projectWide = ((Boolean) args.get(1)).booleanValue();
		
		Optional<IJavaProject> project = projectFinder.find(new TextDocumentIdentifier(docUri));
		
		if (project.isPresent()) {
			if (projectWide) {
				return applyRecipe(recipeSupplier.get(), project.get(), orCuCache.getCompiulationUnits(project.get()));
			} else {
				return orCuCache.withCompilationUnit(project.get(), URI.create(docUri), cu -> {
					if (cu == null) {
						throw new IllegalStateException("Cannot parse Java file: " + docUri);
					}
					return applyRecipe(recipeSupplier.get(), project.get(), List.of(cu));
				});
			}
		}
		return null;
	}


	protected abstract List<Either<Command, CodeAction>> provideCodeActions(CodeActionContext context, TextDocument doc,
			IRegion region, IJavaProject project, CompilationUnit cu, ASTNode node);	

}
