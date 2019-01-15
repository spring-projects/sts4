/*******************************************************************************
 * Copyright (c) 2017, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.app.BootLanguageServerParams;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchyAwareLookup;
import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolAddOnInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver.Listener;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleWorkspaceService;
import org.springframework.ide.vscode.commons.util.Futures;
import org.springframework.ide.vscode.commons.util.StringUtil;

import com.google.common.collect.ImmutableList;

/**
 * @author Martin Lippert
 */
public class SpringSymbolIndex {

	private final SimpleLanguageServer server;
	private final BootLanguageServerParams params;
	private final JavaProjectFinder projectFinder;

	private final List<SymbolInformation> symbols;
	private final List<SymbolAddOnInformation> addonInformation;

	private final ConcurrentMap<String, List<SymbolInformation>> symbolsByDoc;
	private final ConcurrentMap<String, List<SymbolAddOnInformation>> addonInformationByDoc;

	private final ConcurrentMap<String, List<SymbolInformation>> symbolsByProject;
	private final ConcurrentMap<String, List<SymbolAddOnInformation>> addonInformationByProject;

	private final ExecutorService updateQueue;
	private final SpringIndexer[] indexer;

	private static final Logger log = LoggerFactory.getLogger(SpringSymbolIndex.class);

	private final Listener projectListener = new Listener() {
		@Override
		public void created(IJavaProject project) {
			log.debug("project created event: {}", project.getElementName());
			initializeProject(project);
		}

		@Override
		public void changed(IJavaProject project) {
			log.debug("project changed event: {}", project.getElementName());
			initializeProject(project);
		}

		@Override
		public void deleted(IJavaProject project) {
			log.debug("project deleted event: {}", project.getElementName());
			deleteProject(project);
		}
	};

	private SimpleWorkspaceService getWorkspaceService() {
		return server.getServer().getWorkspaceService();
	}

	private ProjectObserver getProjectObserver() {
		return params.projectObserver;
	}

	public SpringSymbolIndex(SimpleLanguageServer server, BootLanguageServerParams params, AnnotationHierarchyAwareLookup<SymbolProvider> specificProviders) {
		log.debug("Creating {}", this);
		this.server = server;
		this.params = params;
		this.projectFinder = params.projectFinder;

		this.symbols = Collections.synchronizedList(new ArrayList<>());
		this.symbolsByDoc = new ConcurrentHashMap<>();
		this.symbolsByProject = new ConcurrentHashMap<>();
		this.addonInformation = Collections.synchronizedList(new ArrayList<>());
		this.addonInformationByDoc = new ConcurrentHashMap<>();
		this.addonInformationByProject = new ConcurrentHashMap<>();

		SymbolHandler handler = new SymbolHandler() {
			@Override
			public void addSymbol(IJavaProject project, String docURI, EnhancedSymbolInformation enhancedSymbol) {
				SpringSymbolIndex.this.addSymbol(project, docURI, enhancedSymbol);
			}
		};

//		Map<String, SpringIndexerXMLNamespaceHandler> namespaceHandler = new HashMap<>();
//		namespaceHandler.put("http://www.springframework.org/schema/beans", new SpringIndexerXMLNamespaceHandlerBeans());
//		SpringIndexerXML springIndexerXML = new SpringIndexerXML(handler, namespaceHandler);
//		this.indexer = new SpringIndexer[] {new SpringIndexerJava(handler, specificProviders), springIndexerXML };
		this.indexer = new SpringIndexer[] {new SpringIndexerJava(handler, specificProviders)};

		this.updateQueue = Executors.newSingleThreadExecutor();

		getWorkspaceService().onDidChangeWorkspaceFolders(evt -> {
			log.debug("workspace roots have changed event arrived - added: " + evt.getEvent().getAdded() + " - removed: " + evt.getEvent().getRemoved());
		});

		if (getProjectObserver() != null) {
			getProjectObserver().addListener(projectListener);
		}
	}

	public void serverInitialized() {
		List<String> globPattern = Arrays.stream(this.indexer).map(indexer ->
				indexer.getFileWatchPatterns()).flatMap(Arrays::stream).collect(Collectors.toList());

		getWorkspaceService().getFileObserver().onFileDeleted(globPattern, (file) -> {
			deleteDocument(new TextDocumentIdentifier(file).getUri());
		});
		getWorkspaceService().getFileObserver().onFileCreated(globPattern, (file) -> {
			createDocument(new TextDocumentIdentifier(file).getUri());
		});
	}

	public void shutdown() {
		try {
			synchronized(this) {
				if (updateQueue != null && !updateQueue.isShutdown()) {
					updateQueue.shutdownNow();
				}

				if (getProjectObserver() != null) {
					getProjectObserver().removeListener(projectListener);
				}
			}
		} catch (Exception e) {
			log.error("{}", e);
		}
	}

	public CompletableFuture<Void> initializeProject(IJavaProject project) {
		try {
			if (SpringProjectUtil.isBootProject(project) || SpringProjectUtil.isSpringProject(project)) {
				if (project.getElementName() == null) {
					// Projects indexed by name. No name  - no index for it
					log.debug("Project with NULL name is being initialized");
					return CompletableFuture.completedFuture(null);
				} else {

					removeSymbolsByProject(project);

					CompletableFuture<Void>[] futures = new CompletableFuture[this.indexer.length];
					for (int i = 0; i < this.indexer.length; i++) {
						InitializeProject initializeItem = new InitializeProject(project, this.indexer[i]);
						futures[i] = CompletableFuture.runAsync(initializeItem, this.updateQueue);
					}

					return CompletableFuture.allOf(futures);
				}
			} else {
				return deleteProject(project);
			}
		} catch (Throwable  e) {
			log.error("", e);
			return Futures.error(e);
		}
	}

	public CompletableFuture<Void> deleteProject(IJavaProject project) {
		try {
			if (project.getElementName() == null) {
				// Projects indexed by name. No name  - no index for it
				log.debug("Project with NULL name is being removed");
				return CompletableFuture.completedFuture(null);
			} else {
				DeleteProject initializeItem = new DeleteProject(project);
				return CompletableFuture.runAsync(initializeItem, this.updateQueue);
			}
		} catch (Throwable  e) {
			log.error("", e);
			return Futures.error(e);
		}
	}

	public CompletableFuture<Void> createDocument(String docURI) {
		synchronized(this) {
			List<CompletableFuture<Void>> futures = new ArrayList<>();

			for (SpringIndexer indexer : this.indexer) {
				if (indexer.isInterestedIn(docURI)) {
					Optional<IJavaProject> maybeProject = projectFinder.find(new TextDocumentIdentifier(docURI));

					if (maybeProject.isPresent()) {
						try {
							String content = FileUtils.readFileToString(new File(new URI(docURI)));
							UpdateItem updateItem = new UpdateItem(maybeProject.get(), docURI, content, indexer);
							futures.add(CompletableFuture.runAsync(updateItem, this.updateQueue));
						}
						catch (Exception e) {
							log.error("", e);
							futures.add(Futures.error(e));
						}
					}
				}
			}

			return CompletableFuture.allOf((CompletableFuture[]) futures.toArray(new CompletableFuture[futures.size()]));
		}
	}

	public CompletableFuture<Void> updateDocument(String docURI, String content) {
		synchronized(this) {
			List<CompletableFuture<Void>> futures = new ArrayList<>();

			for (SpringIndexer indexer : this.indexer) {
				if (indexer.isInterestedIn(docURI)) {
					Optional<IJavaProject> maybeProject = projectFinder.find(new TextDocumentIdentifier(docURI));
					if (maybeProject.isPresent()) {
						try {
							UpdateItem updateItem = new UpdateItem(maybeProject.get(), docURI, content, indexer);
							futures.add(CompletableFuture.runAsync(updateItem, this.updateQueue));
						}
						catch (Exception e) {
							log.error("{}", e);
						}
					}
				}
			}
			return CompletableFuture.allOf((CompletableFuture[]) futures.toArray(new CompletableFuture[futures.size()]));
		}
	}

	public CompletableFuture<Void> deleteDocument(String deletedDocURI) {
		synchronized(this) {
			try {
				Optional<IJavaProject> maybeProject = projectFinder.find(new TextDocumentIdentifier(deletedDocURI));
				if (maybeProject.isPresent()) {
					DeleteItem deleteItem = new DeleteItem(maybeProject.get(), deletedDocURI);
					return CompletableFuture.runAsync(deleteItem, this.updateQueue);
				}
			}
			catch (Exception e) {
				log.error("", e);
				return Futures.error(e);
			}
		}

		return null;
	}

	public List<SymbolInformation> getAllSymbols(String query) {
		if (query != null && query.length() > 0) {
			List<SymbolInformation> foundSymbols = searchMatchingSymbols(this.symbols, query);
			return foundSymbols.subList(0,  Math.min(50, foundSymbols.size()));
		} else {
			return this.symbols.subList(0, Math.min(50, this.symbols.size()));
		}
	}

	public List<? extends SymbolInformation> getSymbols(String docURI) {
		return this.symbolsByDoc.get(docURI);
	}

	public List<SymbolAddOnInformation> getAllAdditionalInformation(Predicate<SymbolAddOnInformation> filter) {
		if (filter != null) {
			return addonInformation.stream().filter(filter).collect(Collectors.toList());
		}
		else {
			return null;
		}
	}

	public List<? extends SymbolAddOnInformation> getAdditonalInformation(String docURI) {
		List<SymbolAddOnInformation> info = this.addonInformationByDoc.get(docURI);
		return info == null ? ImmutableList.of() : info;
	}

	/**
	 * inserts a noop operation into the worker/update quene, which allows invokers to use the
	 * returned future to wait for the queue items in the queue to be completed which got inserted before
	 * this noop.
	 */
	public CompletableFuture<Void> waitOperation() {
		return CompletableFuture.runAsync(new Runnable() {
			@Override
			public void run() {
			}
		}, this.updateQueue);
	}

	private List<SymbolInformation> searchMatchingSymbols(List<SymbolInformation> allsymbols, String query) {
		return allsymbols.stream()
				.filter(symbol -> StringUtil.containsCharactersCaseInsensitive(symbol.getName(), query))
				.collect(Collectors.toList());
	}



	//
	//
	// worker queue items to initialize, update, or delete symbols for files and projects
	//
	//

	private class InitializeProject implements Runnable {

		private final IJavaProject project;
		private final SpringIndexer indexer;

		public InitializeProject(IJavaProject project, SpringIndexer indexer) {
			this.project = project;
			this.indexer = indexer;
			log.debug("{} created ", this);
		}

		@Override
		public void run() {
			log.debug("{} starting...", this);
			try {
				indexer.initializeProject(project);

				log.debug("{} completed", this);
			} catch (Throwable e) {
				log.error("{} threw exception", this, e);
			}
		}
	}

	private class UpdateItem implements Runnable {

		private final String docURI;
		private final String content;
		private final IJavaProject project;
		private final SpringIndexer indexer;

		public UpdateItem(IJavaProject project, String docURI, String content, SpringIndexer indexer) {
			this.project = project;
			this.docURI = docURI;
			this.content = content;
			this.indexer = indexer;
		}

		@Override
		public void run() {
			try {
				removeSymbolsByDoc(project, docURI);
				indexer.updateFile(project, docURI, content);
			} catch (Exception e) {
				log.error("{}", e);
			}
		}
	}

	private class DeleteItem implements Runnable {

		private final String docURI;
		private IJavaProject project;

		public DeleteItem(IJavaProject project, String docURI) {
			this.project = project;
			this.docURI = docURI;
		}

		@Override
		public void run() {
			try {
				removeSymbolsByDoc(project, docURI);
			} catch (Exception e) {
				log.error("{}", e);
			}
		}
	}

	private class DeleteProject implements Runnable {

		private final IJavaProject project;

		public DeleteProject(IJavaProject project) {
			this.project = project;
			log.debug("{} created ", this);
		}

		@Override
		public void run() {
			log.debug("{} starting...", this);
			try {
				removeSymbolsByProject(project);
				log.debug("{} completed", this);
			} catch (Throwable e) {
				log.error("{} threw exception", this, e);
			}
		}

	}

	private void addSymbol(IJavaProject project, String docURI, EnhancedSymbolInformation enhancedSymbol) {
		symbols.add(enhancedSymbol.getSymbol());
		symbolsByDoc.computeIfAbsent(docURI, s -> new ArrayList<SymbolInformation>()).add(enhancedSymbol.getSymbol());
		symbolsByProject.computeIfAbsent(project.getElementName(), s -> new ArrayList<SymbolInformation>()).add(enhancedSymbol.getSymbol());

		if (enhancedSymbol.getAdditionalInformation() != null) {
			addonInformation.addAll(Arrays.asList(enhancedSymbol.getAdditionalInformation()));
			addonInformationByDoc.computeIfAbsent(docURI, s -> new ArrayList<SymbolAddOnInformation>()).addAll(Arrays.asList(enhancedSymbol.getAdditionalInformation()));
			addonInformationByProject.computeIfAbsent(project.getElementName(), s -> new ArrayList<SymbolAddOnInformation>()).addAll(Arrays.asList(enhancedSymbol.getAdditionalInformation()));
		}
	}

	private void removeSymbolsByDoc(IJavaProject project, String docURI) {
		List<SymbolInformation> oldSymbols = symbolsByDoc.remove(docURI);
		if (oldSymbols != null) {
			symbols.removeAll(oldSymbols);

			List<SymbolInformation> projectSymbols = symbolsByProject.get(project.getElementName());
			if (projectSymbols != null) {
				projectSymbols.removeAll(oldSymbols);
			}
		}

		List<SymbolAddOnInformation> oldAddInInformation = addonInformationByDoc.remove(docURI);
		if (oldAddInInformation != null) {
			addonInformation.removeAll(oldAddInInformation);

			List<SymbolAddOnInformation> projectAddOns = addonInformationByProject.get(project.getElementName());
			if (projectAddOns != null) {
				projectAddOns.removeAll(oldAddInInformation);
			}
		}

	}

	private void removeSymbolsByProject(IJavaProject project) {
		// If project name is null it cannot be in the cache
		if (project.getElementName() == null) {
			return;
		}
		List<SymbolInformation> oldSymbols = symbolsByProject.remove(project.getElementName());
		if (oldSymbols != null) {
			symbols.removeAll(oldSymbols);

			Set<String> keySet = symbolsByDoc.keySet();
			Iterator<String> docIter = keySet.iterator();
			while (docIter.hasNext()) {
				String docURI = docIter.next();
				List<SymbolInformation> docSymbols = symbolsByDoc.get(docURI);
				docSymbols.removeAll(oldSymbols);

				if (docSymbols.isEmpty()) {
					docIter.remove();
				}
			}
		}

		List<SymbolAddOnInformation> oldAddInInformation = addonInformationByProject.remove(project.getElementName());
		if (oldAddInInformation != null) {
			addonInformation.removeAll(oldAddInInformation);

			Set<String> keySet = addonInformationByDoc.keySet();
			Iterator<String> docIter = keySet.iterator();
			while (docIter.hasNext()) {
				String docURI = docIter.next();
				List<SymbolAddOnInformation> docAddons = addonInformationByDoc.get(docURI);
				docAddons.removeAll(oldAddInInformation);

				if (docAddons.isEmpty()) {
					docIter.remove();
				}
			}
		}

	}

}
