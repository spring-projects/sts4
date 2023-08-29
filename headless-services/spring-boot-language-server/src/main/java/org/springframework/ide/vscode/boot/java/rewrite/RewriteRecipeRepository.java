/*******************************************************************************
 * Copyright (c) 2022, 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.rewrite;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.Recipe;
import org.openrewrite.RecipeRun;
import org.openrewrite.Result;
import org.openrewrite.SourceFile;
import org.openrewrite.Validated;
import org.openrewrite.config.DeclarativeRecipe;
import org.openrewrite.config.RecipeDescriptor;
import org.openrewrite.config.YamlResourceLoader;
import org.openrewrite.internal.InMemoryLargeSourceSet;
import org.openrewrite.java.JavaParser;
import org.openrewrite.maven.MavenParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.app.BootJavaConfig;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.IndefiniteProgressTask;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.ListenerList;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.protocol.java.ProjectBuild;
import org.springframework.ide.vscode.commons.rewrite.LoadUtils;
import org.springframework.ide.vscode.commons.rewrite.LoadUtils.DurationTypeConverter;
import org.springframework.ide.vscode.commons.rewrite.ORDocUtils;
import org.springframework.ide.vscode.commons.rewrite.config.StsEnvironment;
import org.springframework.ide.vscode.commons.rewrite.gradle.GradleIJavaProjectParser;
import org.springframework.ide.vscode.commons.rewrite.java.ProjectParser;
import org.springframework.ide.vscode.commons.rewrite.maven.MavenIJavaProjectParser;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public class RewriteRecipeRepository {
	
	enum RecipeFilter {
		ALL,
		BOOT_UPGRADE,
		NON_BOOT_UPGRADE
	}

	private static final Pattern P1 = Pattern.compile("(Upgrade|Migrate)SpringBoot_\\d+_\\d+");
	
	private static final Map<RecipeFilter, Predicate<Recipe>> RECIPE_LIST_FILTERS = new HashMap<>();
	static {
		RECIPE_LIST_FILTERS.put(RecipeFilter.ALL, r -> true);
		RECIPE_LIST_FILTERS.put(RecipeFilter.BOOT_UPGRADE, r -> {
			String n = lastTokenAfterDot(r.getName());
			return P1.matcher(n).matches();
		});
		RECIPE_LIST_FILTERS.put(RecipeFilter.NON_BOOT_UPGRADE, r -> {
			return RECIPE_LIST_FILTERS.get(RecipeFilter.BOOT_UPGRADE).negate().test(r);
		});
	}
		
	private static final String CMD_REWRITE_RELOAD = "sts/rewrite/reload";
	private static final String CMD_REWRITE_EXECUTE = "sts/rewrite/execute";
	private static final String CMD_REWRITE_LIST = "sts/rewrite/list";
	private static final String CMD_REWRITE_RECIPE_EXECUTE = "sts/rewrite/recipe/execute";
	private static final Logger log = LoggerFactory.getLogger(RewriteRecipeRepository.class);
	
	private static final Set<String> UNINITIALIZED_SET = Collections.emptySet();
	
	final private SimpleLanguageServer server;
	
	final private JavaProjectFinder projectFinder;
	
	final private ListenerList<Void> loadListeners;
	
	private CompletableFuture<Map<String, Recipe>> recipesFuture = null;
	
	private Set<String> scanFiles;
	private Set<String> scanDirs;
	private Set<String> recipeFilters;
		
	private static Gson serializationGson = new GsonBuilder()
			.registerTypeAdapter(Duration.class, new DurationTypeConverter())
			.create();
	
	public RewriteRecipeRepository(SimpleLanguageServer server, JavaProjectFinder projectFinder, BootJavaConfig config) {
		this.server = server;
		this.projectFinder = projectFinder;
		this.loadListeners = new ListenerList<>();
		this.scanDirs = UNINITIALIZED_SET;
		this.scanFiles = UNINITIALIZED_SET;
		this.recipeFilters = UNINITIALIZED_SET;
		
		config.addListener(l -> {
			Set<String> recipeFilterFromConfig = config.getRecipesFilters();
			if (recipeFilters == UNINITIALIZED_SET || !recipeFilters.equals(recipeFilterFromConfig)) {
				recipeFilters = recipeFilterFromConfig;
			}
			if (scanDirs == UNINITIALIZED_SET || !scanDirs.equals(config.getRecipeDirectories())
					|| scanFiles == UNINITIALIZED_SET || !scanFiles.equals(config.getRecipeFiles())) {
				// Eclipse client sends one event for init and the other config changed event due to remote app value expr listener.
				// Therefore it is best to store the scanDirs here right after it is received, not during scan process or anything else done async
				scanDirs = config.getRecipeDirectories();
				scanFiles = config.getRecipeFiles();
				clearRecipes();
			}
		});
		
		registerCommands();
	}
		
	private synchronized void clearRecipes() {
		recipesFuture = null;
	}
	
	private synchronized Map<String, Recipe> loadRecipes() {
		IndefiniteProgressTask progressTask = server.getProgressService().createIndefiniteProgressTask(UUID.randomUUID().toString(), "Loading Rewrite Recipes", null);
		Map<String, Recipe> recipes = new HashMap<>();
		try {
			log.info("Loading Rewrite Recipes...");
			Recipe xmlbindRecipe = null;
			StsEnvironment env = createRewriteEnvironment();
			for (Recipe r : env.listRecipes()) {
				if (r.getName() != null) {
					if ("org.openrewrite.java.migrate.jakarta.JavaxXmlBindMigrationToJakartaXmlBind".equals(r.getName())) {
						xmlbindRecipe = r;
					}
					if (recipes.containsKey(r.getName())) {
						log.error("Duplicate ids: '" + r.getName() + "'");
					}
					recipes.put(r.getName(), r);					
				}
			}
			// HACK: add Jakarta XML Bind migration recipe again as there are cases when maven dependency isn't added
			if (xmlbindRecipe != null) {
				for (String id : recipes.keySet()) {
					if (id.startsWith("org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_")) {
						Recipe recipe = recipes.get(id);
						recipe.getDescriptor().getRecipeList().add(xmlbindRecipe.getDescriptor());
						recipe.getRecipeList().add(xmlbindRecipe);
					}
				}
			}
//			codeActionDescriptors.addAll(env.listCodeActionDescriptors());
			log.info("Done loading Rewrite Recipes");
		} catch (Throwable t) {
			log.error("", t);
		} finally {
			progressTask.done();
		}
		return recipes;
	}
	
	private boolean isAcceptableGlobalCommandRecipe(Recipe r) {
		if (recipeFilters.isEmpty()) {
			return isRecipeValid(r);
		} else {
			for (String filter : recipeFilters) {
				if (!filter.isBlank()) {
					// Check if wild-card character present
					if (filter.indexOf('*') < 0) {
						// No wild-card - direct equality 
						if (filter.equals(r.getName())) {
							return isRecipeValid(r);
						}
					} else {
						// Wild-card present - convert to regular expression
						if (Pattern.matches(filter.replaceAll("\\*", "\\.*"), r.getName())) {
							return isRecipeValid(r);
						}
					}
				}
			}
			return false;
		}
	}
	
	private static boolean isRecipeValid(Recipe r) {
		Validated validation = Validated.invalid(null, null, null);
		try {
			validation = r.validate();
		} catch (Exception e) {
			// ignore
		}
		return validation.isValid();
	}
	
	private StsEnvironment createRewriteEnvironment() {
		StsEnvironment.Builder builder = StsEnvironment.builder().scanRuntimeClasspath();
		for (String p : scanFiles) {
			try {
				Path f = Path.of(p);
				String pathStr = f.toString();
				if (pathStr.endsWith(".jar")) {
					URLClassLoader classLoader = new URLClassLoader(new URL[] { f.toUri().toURL() },
							getClass().getClassLoader());
					builder.scanJar(f, classLoader);
				} else if (pathStr.endsWith(".yml") || pathStr.endsWith(".yaml")) {
					builder.load(new YamlResourceLoader(new FileInputStream(f.toFile()), f.toUri(), new Properties()));
				}
			} catch (Exception e) {
				log.error("Skipping folder " + p, e);
			}
		}
//		for (String p : scanDirs) {
//			try {
//				Path d = Path.of(p);
//				if (Files.isDirectory(d)) {
//					URLClassLoader classLoader = new URLClassLoader(new URL[] { d.toUri().toURL()}, getClass().getClassLoader());
//					builder.scanPath(d, Collections.emptyList(), classLoader);
//				}
//			} catch (Exception e) {
//				log.error("Skipping folder " + p, e);
//			}
//		}
		return builder.build();
	}
	
	public CompletableFuture<Map<String, Recipe>> recipes() {
		if (recipesFuture == null) {
			recipesFuture = CompletableFuture.supplyAsync(this::loadRecipes);
		}
		return recipesFuture;
	}
	
	public CompletableFuture<Optional<Recipe>> getRecipe(String name) {
		return recipes().thenApply(recipes -> Optional.ofNullable(recipes.get(name)));
	}
	
	private static JsonElement recipeToJson(Recipe r) {
		JsonElement jsonElement = serializationGson.toJsonTree(r.getDescriptor());
		return jsonElement;
	}
	
	private void registerCommands() {
		server.onCommand(CMD_REWRITE_LIST, params -> {
			JsonElement uri = (JsonElement) params.getArguments().get(0);
			RecipeFilter f = params.getArguments().size() > 1 ? RecipeFilter.valueOf(((JsonElement) params.getArguments().get(1)).getAsString()) : RecipeFilter.ALL;
					return listProjectRefactoringRecipes(uri.getAsString()).thenApply(recipes -> recipes.stream()
							.filter(RECIPE_LIST_FILTERS.get(f))
							.map(RewriteRecipeRepository::recipeToJson)
							.collect(Collectors.toList()));
		});
		
		server.onCommand(CMD_REWRITE_EXECUTE, params -> {
			return recipes().thenCompose(recipes -> {
				String uri = ((JsonElement) params.getArguments().get(0)).getAsString();
				JsonElement recipesJson = ((JsonElement) params.getArguments().get(1));
				
				RecipeDescriptor d = serializationGson.fromJson(recipesJson, RecipeDescriptor.class);
				
				Recipe aggregateRecipe = LoadUtils.createRecipe(d, id -> Optional.ofNullable(recipes.get(id)).map(r -> r.getClass()).orElse(null));
				
				if (aggregateRecipe instanceof DeclarativeRecipe && aggregateRecipe.getRecipeList().isEmpty()) {
					throw new RuntimeException("No recipes found to perform!");
				} else if (aggregateRecipe.getRecipeList().size() == 1) {
					Recipe r = aggregateRecipe.getRecipeList().get(0);
					String progressToken = params.getWorkDoneToken() == null
							|| params.getWorkDoneToken().getLeft() == null
									? (r.getName() == null ? UUID.randomUUID().toString() : r.getName())
									: params.getWorkDoneToken().getLeft();
					return apply(r, uri, progressToken);
				} else {
					String progressToken = params.getWorkDoneToken() == null
							|| params.getWorkDoneToken().getLeft() == null
									? (aggregateRecipe.getName() == null ? UUID.randomUUID().toString()
											: aggregateRecipe.getName())
									: params.getWorkDoneToken().getLeft();
					return apply(aggregateRecipe, uri, progressToken);
				}
			});
		});
		
		server.onCommand(CMD_REWRITE_RELOAD, params -> {
			clearRecipes();
			return CompletableFuture.completedFuture("executed");
		});
		
		server.onCommand(CMD_REWRITE_RECIPE_EXECUTE, params -> {
			String recipeId = ((JsonElement) params.getArguments().get(0)).getAsString();
			return getRecipe(recipeId).thenCompose(optRecipe -> {
				Recipe r = optRecipe.orElseThrow(() -> new IllegalArgumentException("No such recipe exists with name " + recipeId));
				final String progressToken = params.getWorkDoneToken() == null || params.getWorkDoneToken().getLeft() == null ? r.getName() : params.getWorkDoneToken().getLeft();
				String uri = ((JsonElement) params.getArguments().get(1)).getAsString();
				return apply(r, uri, progressToken);	
			});
		});	
	}
	
	CompletableFuture<Object> apply(Recipe r, String uri, String progressToken) {
		final IndefiniteProgressTask progressTask = server.getProgressService().createIndefiniteProgressTask(progressToken, r.getDisplayName(), "Initiated...");
		return CompletableFuture.supplyAsync(() -> {
			return projectFinder.find(new TextDocumentIdentifier(uri));
		}).thenCompose(p -> {
			if (p.isPresent()) {
				try {
					Optional<WorkspaceEdit> edit = computeWorkspaceEdit(r, p.get(), progressTask);
					return CompletableFuture.completedFuture(edit).thenCompose(we -> {
						if (we.isPresent()) {
							progressTask.progressEvent("Applying document changes...");
							return server.getClient().applyEdit(new ApplyWorkspaceEditParams(we.get(), r.getDisplayName())).thenCompose(res -> {
								if (res.isApplied()) {
									progressTask.done();
									return CompletableFuture.completedFuture("success");
								} else {
									progressTask.done();
									return CompletableFuture.completedFuture(null);
								}
							});
						} else {
							progressTask.done();
							return CompletableFuture.completedFuture(null);
						}
					});
				} catch (Throwable t) {
					progressTask.done();
					throw t;
				}
			} else {
				return CompletableFuture.failedFuture(new IllegalArgumentException("Cannot find Spring Boot project for uri: " + uri));
			}
		});
	}
	
	private Optional<WorkspaceEdit> computeWorkspaceEdit(Recipe r, IJavaProject project, IndefiniteProgressTask progressTask) {
		Path absoluteProjectDir = Paths.get(project.getLocationUri());
		progressTask.progressEvent("Parsing files...");
		ProjectParser projectParser = createRewriteProjectParser(project,
				p -> {
					TextDocument doc = server.getTextDocumentService().getLatestSnapshot(p.toUri().toASCIIString());
					if (doc != null) {
						return new Parser.Input(p, () -> new ByteArrayInputStream(doc.get().getBytes()));
					}
					return null;
				});
		List<SourceFile> sources = projectParser.parse(absoluteProjectDir, new InMemoryExecutionContext());
		progressTask.progressEvent("Computing changes...");
		RecipeRun reciperun = r.run(new InMemoryLargeSourceSet(sources), new InMemoryExecutionContext(e -> log.error("Recipe execution failed", e)));
		List<Result> results = reciperun.getChangeset().getAllResults();
		return ORDocUtils.createWorkspaceEdit(absoluteProjectDir, server.getTextDocumentService(), results);
	}
	
	private CompletableFuture<List<Recipe>> listProjectRefactoringRecipes(String uri) {
		if (uri != null) {
			/*
			 * When LS started on listing rewrite recipes project lookup may not find any projects as classpath might still be resolving.
			 * Therefore, it is best probably to list the available recipes and figure out if it is a Spring Boot project recipes is applied to
			 * and if not throw an exception.
			 */
//			Optional<IJavaProject> projectOpt = projectFinder.find(new TextDocumentIdentifier(uri));
//			if (projectOpt.isPresent()) {
				return recipes().thenApply(recipes -> recipes.values().stream().filter(this::isAcceptableGlobalCommandRecipe).collect(Collectors.toList()));
//			}
		}
		return CompletableFuture.completedFuture(Collections.emptyList());
	}
	
    private static ProjectParser createRewriteProjectParser(IJavaProject jp, Function<Path, Parser.Input> inputProvider) {
		switch (jp.getProjectBuild().getType()) {
    	case ProjectBuild.MAVEN_PROJECT_TYPE:
    		Path absoluteProjectDir = Paths.get(jp.getLocationUri()).toAbsolutePath();
            MavenParser.Builder mavenParserBuilder = MavenParser.builder()
            	.mavenConfig(absoluteProjectDir.resolve(".mvn/maven.config"));
    		return new MavenIJavaProjectParser(jp, JavaParser.fromJavaVersion(), inputProvider, mavenParserBuilder);
    	case ProjectBuild.GRADLE_PROJECT_TYPE:
    		return new GradleIJavaProjectParser(jp, JavaParser.fromJavaVersion(), inputProvider);
    	default:
    		throw new IllegalStateException("The project is neither Maven nor Gradle!");
    	}
    }
    
	public void onRecipesLoaded(Consumer<Void> l) {
		loadListeners.add(l);
	}

	private static String lastTokenAfterDot(String s) {
		int idx = s.lastIndexOf('.');
		if (idx >= 0 && idx < s.length() - 1) {
			return s.substring(idx + 1);
		}
		return s;
	}
	
//	private static Recipe convert(Recipe r, RecipeDescriptor d) {
//		try {
//			if (d.selected) {
//				if (d.children != null && !d.children.isEmpty()) {
//					Recipe recipe = r instanceof DeclarativeRecipe ? new DeclarativeRecipe(r.getName(), r.getDisplayName(), r.getDescription(), r.getTags(), r.getEstimatedEffortPerOccurrence(), null)
//							: r.getClass().getDeclaredConstructor().newInstance();
//					int i = 0;
//					for (Recipe sr : r.getRecipeList()) {
//						Recipe convertedSubRecipe = convert(sr, d.children.get(i++));
//						if (convertedSubRecipe != null) {
//							recipe.doNext(convertedSubRecipe);
//						}
//					}
//					return recipe;
//				} else {
//					return r;
//				}
//			}
//		} catch (Exception e) {
//			log.error("", e);
//		}
//		return null;
//	}

//	@SuppressWarnings("unused")
//	private static class RecipeDescriptor {
//		String id;
//		String label;
//		String detail;
//		List<RecipeDescriptor> children;
//		boolean selected;
//		
//		RecipeDescriptor(Recipe r) {
//			this.id = r.getName();
//			this.label = r.getDisplayName();
//			this.detail = r.getDescription();
//			List<Recipe> subRecipes = r.getRecipeList();
//			if (r instanceof DeclarativeRecipe && !subRecipes.isEmpty() && (subRecipes.size() > 1 || subRecipes.get(0) instanceof DeclarativeRecipe)) {
//				this.children = r.getRecipeList().stream().map(sr -> new RecipeDescriptor(sr)).collect(Collectors.toList());
//			}
//		}
//	}
	
}
