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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.index.SpringMetamodelIndex;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.commons.java.IClasspathUtil;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.java.Version;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.protocol.java.Classpath;
import org.springframework.ide.vscode.commons.protocol.spring.Bean;
import org.springframework.ide.vscode.commons.util.FileObserver;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ModulithService {
	
	private static final Logger log = LoggerFactory.getLogger(ModulithService.class);
	
	private static final List<String> FILE_PATTERNS = List.of("**/*.java");
	
	private Map<IJavaProject, CompletableFuture<AppModules>> cache;

	private SpringMetamodelIndex springIndex;

	private JavaProjectFinder projectFinder;
	
	public ModulithService(ProjectObserver projectObserver, FileObserver fileObserver, JavaProjectFinder projectFinder, SpringMetamodelIndex springIndex) {
		this.projectFinder = projectFinder;
		this.springIndex = springIndex;
		cache = new ConcurrentHashMap<>();
		
		projectObserver.addListener(new ProjectObserver.Listener() {
			
			@Override
			public void deleted(IJavaProject project) {
				invalidate(project);
			}
			
			@Override
			public void created(IJavaProject project) {
				invalidate(project);
			}
			
			@Override
			public void changed(IJavaProject project) {
				invalidate(project);
			}
			
		});
		
		fileObserver.onFilesCreated(FILE_PATTERNS, this::handleFilesChanged);
		fileObserver.onFilesDeleted(FILE_PATTERNS, this::handleFilesChanged);
	}
	
	private void handleFilesChanged(String[] files) {
		for (String f : files) {
			URI uri = URI.create(f);
			TextDocumentIdentifier docId = new TextDocumentIdentifier(uri.toASCIIString());
			projectFinder.find(docId).ifPresent(project -> {
				synchronized (project) {
					if (cache.containsKey(project)) {
						Path filePath = Paths.get(uri);
						if (IClasspathUtil.getProjectJavaSourceFoldersWithoutTests(project.getClasspath()).map(folder -> folder.toPath()).anyMatch(folderPath -> filePath.startsWith(folderPath))) {
							cache.remove(project);
						}
					}
				}
			});
		}
	}
	
	private void invalidate(IJavaProject project) {
		CompletableFuture<AppModules> future = cache.remove(project);
		if (future != null) {
			future.cancel(true);
		}
	}
		
	private CompletableFuture<AppModules> loadModulesMetadata(IJavaProject project) {
		Version v = SpringProjectUtil.getDependencyVersion(project, "spring-modulith-core");
		if (v != null) {
				Set<String> packages = findRootPackages(project);
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
						CompletableFuture<?>[] aggregateFuture = packages
								.stream()
								.map(pkg -> CompletableFuture.supplyAsync(() -> computeAppModules(javaCmd, classpathStr, pkg)).thenAccept(oa -> oa.ifPresent(allAppModules::addAll)))
								.toArray(CompletableFuture[]::new);
						return CompletableFuture.allOf(aggregateFuture).thenApply(r -> new AppModules(allAppModules));
					} catch (Exception e) {
						log.error("", e);
					}
				}
		}
		return CompletableFuture.completedFuture(null);
	}
	
	public CompletableFuture<AppModules> getModulesData(IJavaProject project) {
		synchronized(project) {
			CompletableFuture<AppModules> modules = cache.get(project);
			if (modules == null) {
				modules = loadModulesMetadata(project);
				cache.put(project, modules);
			}
			return modules;
		}
	}
	
	private Set<String> findRootPackages(IJavaProject project) {
		HashSet<String> packages = new HashSet<>();
		for (Bean bean : springIndex.getBeansOfProject(project.getElementName())) {
			String beanType = bean.getType();
			if (beanType != null) {
				if (Arrays.stream(bean.getAnnotations()).anyMatch(Annotations.BOOT_APP::equals)) {
					packages.add(getPackageNameFromTypeFQName(beanType));
				}
			}
		}
		return packages;
	}
	
	static String getPackageNameFromTypeFQName(String fqn) {
		int idx = 0;
		for (; idx < fqn.length() - 1; idx++) {
			char c = fqn.charAt(idx);
			if (c == '.' && Character.isUpperCase(fqn.charAt(idx + 1))) {
				return fqn.substring(0, idx);
			}
		}
		return fqn;
	}
	
	private Optional<List<AppModule>> computeAppModules(String javaCmd, String cp, String pkg) {
		try {
			Process process = Runtime.getRuntime().exec(new String[] { 
					javaCmd, 
					"-cp",
					cp,
					"org.springframework.modulith.core.util.ApplicationModulesExporter",
					pkg
			});
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			StringBuilder builder = new StringBuilder();
			String line = null;
			// skip first line
			boolean skipFirstLine = true;
			while ((line = reader.readLine()) != null) {
				if (skipFirstLine) {
					skipFirstLine = false;
				} else {
					builder.append(line);
					builder.append(System.getProperty("line.separator"));
				}
			}

			String result = builder.toString();
			log.info(result);
			return Optional.ofNullable(loadAppModules(JsonParser.parseString(result).getAsJsonObject()));
		} catch (Exception e) {
			log.error("", e);
		}
		return Optional.empty();
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
}
