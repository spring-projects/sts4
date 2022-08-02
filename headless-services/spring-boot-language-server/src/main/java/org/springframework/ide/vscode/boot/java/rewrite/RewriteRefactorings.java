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

import java.lang.reflect.Field;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.openrewrite.Recipe;
import org.openrewrite.Result;
import org.openrewrite.config.DeclarativeRecipe;
import org.openrewrite.internal.RecipeIntrospectionUtils;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.CompilationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixEdit;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixHandler;
import org.springframework.ide.vscode.commons.languageserver.util.CodeActionResolver;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.rewrite.ORDocUtils;
import org.springframework.ide.vscode.commons.rewrite.java.ORAstUtils;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

public class RewriteRefactorings implements CodeActionResolver, QuickfixHandler {
		
	private static final Logger log = LoggerFactory.getLogger(RewriteRefactorings.class);
	
	private RewriteRecipeRepository recipeRepo;
	
	private SimpleTextDocumentService documents;

	private RewriteCompilationUnitCache cuCache;

	private JavaProjectFinder projectFinder;

	private Gson gson;
		
	public RewriteRefactorings(SimpleTextDocumentService documents, JavaProjectFinder projectFinder, RewriteRecipeRepository recipeRepo, RewriteCompilationUnitCache cuCache) {
		this.documents = documents;
		this.projectFinder = projectFinder;
		this.recipeRepo = recipeRepo;
		this.cuCache = cuCache;
		this.gson = new GsonBuilder()
				.registerTypeAdapter(RecipeScope.class, (JsonDeserializer<RecipeScope>) (json, type, context) -> {
					try {
						return RecipeScope.values()[json.getAsInt()];
					} catch (Exception e) {
						return null;
					}
				})
				.create();
	}

	@Override
	public QuickfixEdit createEdits(Object p) {
		if (p instanceof JsonElement) {
			return new QuickfixEdit(createEdit((JsonElement) p), null);
		}
		return null;
	}

	
	@Override
	public void resolve(CodeAction codeAction) {
		if (codeAction.getData() instanceof JsonElement) {
			try {
				WorkspaceEdit edit = createEdit((JsonElement) codeAction.getData());
				if (edit != null) {
					codeAction.setEdit(edit);
				}
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}
	
	public WorkspaceEdit createEdit(JsonElement o) {
		Data data = gson.fromJson(o, Data.class);
		if (data != null && data.id != null) {
			return perform(data);
		}
		return null;
	}
	
	private WorkspaceEdit applyRecipe(Recipe r, IJavaProject project, List<J.CompilationUnit> cus) {
		List<Result> results = r.run(cus);
		List<Either<TextDocumentEdit, ResourceOperation>> edits = results.stream().filter(res -> res.getAfter() != null).map(res -> {
			URI docUri = res.getAfter().getSourcePath().isAbsolute() ? res.getAfter().getSourcePath().toUri() : project.getLocationUri().resolve(res.getAfter().getSourcePath().toString());
			TextDocument doc = documents.getLatestSnapshot(docUri.toString());
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
	
	private WorkspaceEdit perform(Data data) {
		Optional<IJavaProject> project = projectFinder.find(new TextDocumentIdentifier(data.docUri));
		if (project.isPresent()) {
			boolean projectWide = data.recipeScope == RecipeScope.PROJECT;
			Recipe r = createRecipe(data); 
			if (projectWide) {
				return applyRecipe(r, project.get(), cuCache.getCompiulationUnits(project.get()));
			} else {
				CompilationUnit cu = cuCache.getCU(project.get(), URI.create(data.docUri));
				if (cu == null) {
					throw new IllegalStateException("Cannot parse Java file: " + data.docUri);
				}
				return applyRecipe(r, project.get(), List.of(cu));
			}
		}
		return null;
	}

	private Recipe createRecipe(Data d) {
		Recipe r = recipeRepo.getRecipe(d.id).orElse(null);
		if (!(r instanceof DeclarativeRecipe)) {
			r = RecipeIntrospectionUtils.constructRecipe(r.getClass());
		}
		if (d.params != null) {
			for (Entry<String, Object> entry : d.params.entrySet()) {
				try {
					Field f = r.getClass().getDeclaredField(entry.getKey());
					f.setAccessible(true);
					f.set(r, entry.getValue());
				} catch (Exception e) {
					log.error("", e);;
				}
			}
		}
		if (d.recipeScope == RecipeScope.NODE) {
			UUID astNodeId = UUID.fromString(d.scope);
			if (astNodeId == null) {
				throw new IllegalArgumentException("Missing scope AST node!");
			} else {
				r = ORAstUtils.nodeRecipe(r, j -> j != null && astNodeId.equals(j.getId()));
			}
		}
		return r;
	}

	public static class Data {
		public String id;
		public String docUri;
		public RecipeScope recipeScope;
		public String scope;
		public Map<String, Object> params;
		public Data(String id, String docUri, RecipeScope recipeScope, String scope, Map<String, Object> params) {
			this.id = id;
			this.docUri = docUri;
			this.recipeScope = recipeScope;
			this.scope = scope;
			this.params = params;
		}
		
	}
	
}
