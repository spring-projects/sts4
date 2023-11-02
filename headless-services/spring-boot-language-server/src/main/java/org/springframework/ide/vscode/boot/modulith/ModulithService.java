/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.modulith;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.app.BootJavaConfig;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.Boot3JavaProblemType;
import org.springframework.ide.vscode.boot.java.handlers.BootJavaReconcileEngine;
import org.springframework.ide.vscode.commons.java.IClasspathUtil;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemCategory.Toggle.Option;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.protocol.java.Classpath;
import org.springframework.ide.vscode.commons.protocol.spring.Bean;
import org.springframework.ide.vscode.commons.protocol.spring.BeansParams;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ModulithService {
		
	private static final Logger log = LoggerFactory.getLogger(ModulithService.class);
	
	private static final String CMD_MODULITH_REFRESH = "sts/modulith/metadata/refresh";
	private static final String CMD_LIST_MODULITH_PROJECTS = "sts/modulith/projects";
	
	private SimpleLanguageServer server;
	private SpringSymbolIndex springIndex;
	private BootJavaReconcileEngine reconciler;
	private BootJavaConfig config;
	
	private Map<URI, AppModules> cache;
	private Map<URI, CompletableFuture<Boolean>> metadataRequested;

	public ModulithService(
			SimpleLanguageServer server,
			JavaProjectFinder projectFinder,
			ProjectObserver projectObserver,
			SpringSymbolIndex springIndex,
			BootJavaReconcileEngine reconciler,
			BootJavaConfig config
	) {
		this.config = config;
		this.cache = new ConcurrentHashMap<>();
		this.metadataRequested = new ConcurrentHashMap<>();
		this.server = server;
		this.springIndex = springIndex;
		this.reconciler = reconciler;
		
		projectObserver.addListener(new ProjectObserver.Listener() {
			
			@Override
			public void deleted(IJavaProject project) {
				removeFromCache(project);
			}
			
			@Override
			public void created(IJavaProject project) {
				if (isModulithDependentProject(project)) {
					if (anyClassFilesPresent(project)) {
						requestMetadata(project);
					} else {
						waitForClassFilesCreatedInTargetFolder(project);
					}
				}
			}
			
			@Override
			public void changed(IJavaProject project) {
				if (!isModulithDependentProject(project)) {
					removeFromCache(project);
				} else if (anyClassFilesPresent(project)) {
					requestMetadata(project);
				} else {
					waitForClassFilesCreatedInTargetFolder(project);
				}
			}
		});
		
		server.onCommand(CMD_MODULITH_REFRESH, params -> {
			String uri = ((JsonElement) params.getArguments().get(0)).getAsString();
			return projectFinder.find(new TextDocumentIdentifier(uri)).map(this::refreshMetadata).orElse(CompletableFuture.completedFuture(false)).thenApply(String::valueOf);
		});
		
		server.onCommand(CMD_LIST_MODULITH_PROJECTS, params -> {
			return CompletableFuture.completedFuture(projectFinder.all()
					.stream()
					.filter(ModulithService::isModulithDependentProject)
					.collect(Collectors.toMap(p -> p.getElementName(), p -> p.getLocationUri().toASCIIString()))
			);
		});
	}
	
	private void waitForClassFilesCreatedInTargetFolder(IJavaProject project) {
		final AtomicReference<String> subscription = new AtomicReference<>();
		subscription.set(server.getWorkspaceService().getFileObserver().onFilesCreated(getNonTestClassOutputFolders(project).map(p -> p.toString() + "/**/*.class").collect(Collectors.toList()), files -> {
			if (subscription.get() != null) {
				server.getWorkspaceService().getFileObserver().unsubscribe(subscription.get());
				requestMetadata(project);
			}
		}));
	}
	
	public AppModules getModulesData(IJavaProject project) {
		return cache.get(project.getLocationUri());
	}
	
	private CompletableFuture<Boolean> refreshMetadata(IJavaProject project) {
		if (!isModulithDependentProject(project)) {
			server.getClient().showMessage(new MessageParams(MessageType.Error, "Project '" + project.getElementName() + "' does not depend on spring-modulith."));
			return CompletableFuture.completedFuture(false);
		}
		if (!anyClassFilesPresent(project)) {
			server.getClient().showMessage(new MessageParams(MessageType.Error, "Project '" + project.getElementName() + "' output folder does not contain any '.class' files. Consider re-building."));
			return CompletableFuture.completedFuture(false);
		}
		clearMetadataRequest(project);
		return requestMetadata(project).whenComplete((refreshed, throwable) -> {
			if (throwable != null) {
				server.getClient().showMessage(new MessageParams(MessageType.Error, "Project '" + project.getElementName() + "' Modulith metadata refresh has failed. " + throwable.getMessage()));
			} else {
				if (refreshed) {
					server.getClient().showMessage(new MessageParams(MessageType.Info, "Project '" + project.getElementName() + "' Modulith metadata has been changed."));
				} else {
					server.getClient().showMessage(new MessageParams(MessageType.Info, "Project '" + project.getElementName() + "' Modulith metadata has been refreshed but it has not changed."));
				}
			}
		});
	}
	
	CompletableFuture<Boolean> requestMetadata(IJavaProject p) {
		URI uri = p.getLocationUri();
		CompletableFuture<Boolean> f = metadataRequested.get(uri);
		if (f == null) {
			f = loadModulesMetadata(p).thenApply(appModules -> updateAppModulesCache(p, appModules));
			metadataRequested.put(uri, f);
		}
		return f;
	}
	
	private boolean updateAppModulesCache(IJavaProject project, AppModules modules) {
		URI uri = project.getLocationUri();
		AppModules oldModules = modules == null ? cache.remove(uri) : cache.put(uri, modules);
		if (!Objects.equals(modules, oldModules)) {
			validate(project);
			return true;
		}
		return false;
	}
	
	private AppModules removeFromCache(IJavaProject project) {
		clearMetadataRequest(project);
		return cache.remove(project.getLocationUri());
	}
	
	private void clearMetadataRequest(IJavaProject project) {
		CompletableFuture<Boolean> f = metadataRequested.remove(project.getLocationUri());
		if (f != null && !f.isDone()) {
			f.cancel(true);
		}
	}
	
	private void validate(IJavaProject project) {
		if (server.getDiagnosticSeverityProvider().getDiagnosticSeverity(Boot3JavaProblemType.MODULITH_TYPE_REF_VIOLATION) != null
				&& config.getProblemApplicability(Boot3JavaProblemType.MODULITH_TYPE_REF_VIOLATION) != Option.OFF) {
			
			List<Path> javaSources = IClasspathUtil.getProjectJavaSourceFoldersWithoutTests(project.getClasspath())
				.flatMap(sourceFolder -> {
					try {
						return Files.walk(sourceFolder.toPath()).filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".java"));
					} catch (IOException e) {
						log.error("", e);
						return Stream.empty();
					}
				}).collect(Collectors.toList());
			List<String> fileUriToUpdate = new ArrayList<>(javaSources.size());
			for (Path javaSource : javaSources) {
				String docUri = javaSource.toUri().toASCIIString();
				TextDocument doc = server.getTextDocumentService().getLatestSnapshot(docUri);
				if (doc == null) {
					fileUriToUpdate.add(docUri);
				} else {
					server.validateWith(doc.getId(), reconciler);
				}
			}
			String[] uris = fileUriToUpdate.toArray(new String[fileUriToUpdate.size()]);
			springIndex.deleteDocuments(uris).thenAccept(v -> springIndex.updateDocuments(uris, "Modulith Metadata Changed"));
		}
	}

	private CompletableFuture<AppModules> loadModulesMetadata(IJavaProject project) {
		log.info("Loading Modulith metadata for project '" + project.getElementName() + "'...");
		return findRootPackages(project).thenComposeAsync(packages -> {
			if (!packages.isEmpty()) {
				try {
					String javaCmd = ProcessHandle.current().info().command().orElseThrow();
					String classpathStr = project.getClasspath().getClasspathEntries().stream().map(cpe -> {
						if (Classpath.ENTRY_KIND_SOURCE.equals(cpe.getKind())) {
							return cpe.getOutputFolder();
						} else {
							return cpe.getPath();
						}
					}).collect(Collectors.joining(System.getProperty("path.separator")));
					List<AppModule> allAppModules = new ArrayList<>();
					CompletableFuture<?>[] aggregateFuture = packages.stream()
							.map(pkg -> computeAppModules(project.getElementName(), javaCmd, classpathStr, pkg)
									.thenAccept(allAppModules::addAll))
							.toArray(CompletableFuture[]::new);
					return CompletableFuture.allOf(aggregateFuture).thenApply(r -> new AppModules(allAppModules));
				} catch (Exception e) {
					log.error("", e);
				}
			}
			return CompletableFuture.completedFuture(null);
		});
	}
	
	private CompletableFuture<List<AppModule>> computeAppModules(String projectName, String javaCmd,
			String cp, String pkg) {
		try {
			File outputFile = File.createTempFile(projectName + "-" + pkg, "json");
			Process process = Runtime.getRuntime()
					.exec(new String[] { 
							javaCmd,
							"-cp",
							cp,
							"org.springframework.modulith.core.util.ApplicationModulesExporter",
							pkg,
							outputFile.toString()
					});
			StringBuilder builder = new StringBuilder();
			String line = null;
			while ((line = process.errorReader().readLine()) != null) {
				builder.append(line);
				builder.append(System.getProperty("line.separator"));
			}
			return process.onExit().thenApply(p -> {
					if (p.exitValue() == 0) {
						try {
							log.info("Updating Modulith metadata for project '" + projectName + "'");
							JsonObject json = JsonParser.parseReader(new FileReader(outputFile)).getAsJsonObject();
							log.info("Modulith metadata: " + new GsonBuilder().setPrettyPrinting().create().toJson(json));
							return loadAppModules(json);
						} catch (Exception e) {
							log.error("", e);
						}
					} else {
						log.error("Failed to generate modulith metadata for project '" + projectName + "'. Modulith Exporter process exited with code " + process.exitValue() + "\n" + builder.toString());
					}
					return Collections.emptyList();
				});
		} catch (IOException e) {
			log.error("", e);
		}
		return CompletableFuture.completedFuture(Collections.emptyList());
	}
	
	private CompletableFuture<Set<String>> findRootPackages(IJavaProject project) {
		BeansParams params = new BeansParams();
		params.setProjectName(project.getElementName());
		return springIndex.beans(params).thenApply(beansOfProject -> {
			HashSet<String> packages = new HashSet<>();
			if (beansOfProject != null) {
				for (Bean bean : beansOfProject) {
					String beanType = bean.getType();
					if (beanType != null) {
						if (Arrays.stream(bean.getAnnotations()).anyMatch(Annotations.BOOT_APP::equals)) {
							packages.add(getPackageNameFromTypeFQName(beanType));
						}
					}
				}
			}
			return packages;
		});
	}
	
	public static String getPackageNameFromTypeFQName(String fqn) {
		int idx = 0;
		for (; idx < fqn.length() - 1; idx++) {
			char c = fqn.charAt(idx);
			if (c == '.' && Character.isUpperCase(fqn.charAt(idx + 1))) {
				return fqn.substring(0, idx);
			}
		}
		return fqn;
	}
	
	private static List<AppModule> loadAppModules(JsonObject json) {
		return json.keySet()
				.stream()
				.map(name -> loadAppModule(name, json.get(name).getAsJsonObject()))
				.collect(Collectors.toList());
	}
	
	private static AppModule loadAppModule(String name, JsonObject json) {
		String basePackage = json.get("basePackage").getAsString();
		JsonObject nameInterfacesJson = json.get("namedInterfaces").getAsJsonObject();
		List<String> namedInterfaces = nameInterfacesJson.keySet()
				.stream()
				.flatMap(k -> nameInterfacesJson.get(k).getAsJsonArray().asList().stream().map(js -> js.getAsString()))
				.collect(Collectors.toList());
		return new AppModule(name, basePackage, namedInterfaces);
	}

	private static boolean anyClassFilesPresent(IJavaProject p) {
		return getNonTestClassOutputFolders(p).anyMatch(path -> {
			try {
				return Files.exists(path) && Files.walk(path).anyMatch(f -> Files.isRegularFile(f) && f.toFile().getName().endsWith(".class"));
			} catch (IOException e) {
				log.error("", e);
				return false;
			}
		});
	}
	
	private static Stream<Path> getNonTestClassOutputFolders(IJavaProject p) {
		try {
			return p.getClasspath().getClasspathEntries()
				.stream()
				.filter(cpe -> Classpath.ENTRY_KIND_SOURCE.equals(cpe.getKind()) && cpe.isJavaContent() && cpe.isOwn() && !cpe.isTest())
				.map(cpe -> Paths.get(cpe.getOutputFolder()));
		} catch (Exception e) {
			log.error("", e);
			return Stream.empty();
		}
	}
	
	public static boolean isModulithDependentProject(IJavaProject project) {
		return SpringProjectUtil.hasDependencyStartingWith(project, "spring-modulith-core", cpe -> !cpe.isSystem() && !cpe.isTest() && !cpe.isOwn());
	}
}
