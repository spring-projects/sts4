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
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.openrewrite.Recipe;
import org.openrewrite.RecipeRun;
import org.openrewrite.Result;
import org.openrewrite.config.DeclarativeRecipe;
import org.openrewrite.internal.RecipeIntrospectionUtils;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.CompilationUnit;
import org.openrewrite.marker.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixEdit;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixHandler;
import org.springframework.ide.vscode.commons.languageserver.util.CodeActionResolver;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.rewrite.ORDocUtils;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeScope;
import org.springframework.ide.vscode.commons.rewrite.java.FixDescriptor;
import org.springframework.ide.vscode.commons.rewrite.java.ORAstUtils;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

public class RewriteRefactorings implements CodeActionResolver, QuickfixHandler {
	
	public static final String REWRITE_RECIPE_QUICKFIX = "org.openrewrite.rewrite";
		
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
		FixDescriptor data = gson.fromJson(o, FixDescriptor.class);
		if (data != null && data.getRecipeId() != null) {
			return perform(data);
		}
		return null;
	}
	
	private WorkspaceEdit applyRecipe(Recipe r, IJavaProject project, List<J.CompilationUnit> cus) {
		RecipeRun reciperun = r.run(cus);
		List<Result> results = reciperun.getResults();
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
	
	private WorkspaceEdit perform(FixDescriptor data) {
		Optional<IJavaProject> project = projectFinder.find(new TextDocumentIdentifier(data.getDocUris().get(0)));
		if (project.isPresent()) {
			boolean projectWide = data.getRecipeScope() == RecipeScope.PROJECT;
			Recipe r = createRecipe(data); 
			if (projectWide) {
				return applyRecipe(r, project.get(), ORAstUtils.parse(documents, project.get()));
			} else {
				List<CompilationUnit> cus = data.getDocUris().stream().map(docUri -> cuCache.getCU(project.get(), URI.create(docUri))).filter(Objects::nonNull).collect(Collectors.toList());
				return applyRecipe(r, project.get(), cus);
			}
		}
		return null;
	}
	

	private Recipe createRecipe(FixDescriptor d) {
		Recipe r = recipeRepo.getRecipe(d.getRecipeId()).orElse(null);
		if (!(r instanceof DeclarativeRecipe)) {
			r = RecipeIntrospectionUtils.constructRecipe(r.getClass());
		}
		if (d.getParameters() != null) {
			for (Entry<String, Object> entry : d.getParameters().entrySet()) {
				try {
					Field f = r.getClass().getDeclaredField(entry.getKey());
					f.setAccessible(true);
					f.set(r, entry.getValue());
				} catch (Exception e) {
					log.error("", e);;
				}
			}
		}
		if (d.getRecipeScope() == RecipeScope.NODE) {
			if (d.getRangeScope() == null) {
				throw new IllegalArgumentException("Missing scope AST node!");
			} else {
				r = ORAstUtils.nodeRecipe(r, j -> {
					if (j != null) {
						 Range range = j.getMarkers().findFirst(Range.class).orElse(null);
						 if (range != null) {
							 return d.getRangeScope().getStart().getOffset() <= range.getStart().getOffset() && range.getEnd().getOffset() <= d.getRangeScope().getEnd().getOffset();  
						 }
					}
					return false;
				});
			}
		}
		return r;
	}
}
