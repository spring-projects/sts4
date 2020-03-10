/*******************************************************************************
 * Copyright (c) 2017, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.app;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServerComponents;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchyAwareLookup;
import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolAddOnInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
import org.springframework.ide.vscode.boot.java.utils.DocumentDescriptor;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexer;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexerJava;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexerXML;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexerXMLNamespaceHandler;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexerXMLNamespaceHandlerBeans;
import org.springframework.ide.vscode.boot.java.utils.SymbolCache;
import org.springframework.ide.vscode.boot.java.utils.SymbolHandler;
import org.springframework.ide.vscode.boot.java.utils.SymbolIndexConfig;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver.Listener;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleWorkspaceService;
import org.springframework.ide.vscode.commons.util.Futures;
import org.springframework.ide.vscode.commons.util.StringUtil;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

/**
 * @author Martin Lippert
 */
@Component
public class SpringSymbolIndex implements InitializingBean {

	@Autowired SimpleLanguageServer server;
	@Autowired BootJavaConfig config;
	@Autowired BootLanguageServerParams params;
	@Autowired AnnotationHierarchyAwareLookup<SymbolProvider> specificProviders;
	@Autowired SymbolCache cache;

	private static final String QUERY_PARAM_LOCATION_PREFIX = "locationPrefix:";
	private static final int MAX_NUMBER_OF_SYMBOLS_IN_RESPONSE = 50;

	private final List<EnhancedSymbolInformation> symbols = new ArrayList<>();

	private final ConcurrentMap<String, List<EnhancedSymbolInformation>> symbolsByDoc = new ConcurrentHashMap<>();

	private final ConcurrentMap<String, List<EnhancedSymbolInformation>> symbolsByProject = new ConcurrentHashMap<>();

	private final ExecutorService updateQueue = Executors.newSingleThreadExecutor();
	private SpringIndexer[] indexers;


	private static final Logger log = LoggerFactory.getLogger(SpringSymbolIndex.class);

	private final Listener projectListener = new Listener() {
		@Override
		public void created(IJavaProject project) {
			log.info("project created event: {}", project.getElementName());
			initializeProject(project);
		}

		@Override
		public void changed(IJavaProject project) {
			log.info("project changed event: {}", project.getElementName());
			initializeProject(project);
		}

		@Override
		public void deleted(IJavaProject project) {
			log.info("project deleted event: {}", project.getElementName());
			deleteProject(project);
		}
	};

	private SpringIndexerXML springIndexerXML;
	private SpringIndexerJava springIndexerJava;

	private String watchXMLDeleteRegistration;
	private String watchXMLCreatedRegistration;
	private String watchXMLChangedRegistration;


	private SimpleWorkspaceService getWorkspaceService() {
		return server.getServer().getWorkspaceService();
	}

	private ProjectObserver getProjectObserver() {
		return params.projectObserver;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		log.debug("Setting up {}", this);

		SymbolHandler handler = new SymbolHandler() {
			@Override
			public void addSymbol(IJavaProject project, String docURI, EnhancedSymbolInformation enhancedSymbol) {
				SpringSymbolIndex.this.addSymbol(project, docURI, enhancedSymbol);
			}

			@Override
			public void removeSymbols(IJavaProject project, String docURI) {
				SpringSymbolIndex.this.removeSymbolsByDoc(project, docURI);
			}
			
		};

		Map<String, SpringIndexerXMLNamespaceHandler> namespaceHandler = new HashMap<>();
		namespaceHandler.put("http://www.springframework.org/schema/beans", new SpringIndexerXMLNamespaceHandlerBeans());
		springIndexerXML = new SpringIndexerXML(handler, namespaceHandler, this.cache, projectFinder());
		springIndexerJava = new SpringIndexerJava(handler, specificProviders, this.cache, projectFinder());

		this.indexers = new SpringIndexer[] {springIndexerJava};


		getWorkspaceService().onDidChangeWorkspaceFolders(evt -> {
			log.debug("workspace roots have changed event arrived - added: " + evt.getEvent().getAdded() + " - removed: " + evt.getEvent().getRemoved());
		});

		if (getProjectObserver() != null) {
			getProjectObserver().addListener(projectListener);
			log.info("project listener registered");
		}

		SimpleTextDocumentService documents = server.getTextDocumentService();
		documents.onDidSave(params -> {
			TextDocument document = params.getDocument();
			// Spring Boot LS get events from boot properties files as well, so filter them out
			if (BootJavaLanguageServerComponents.LANGUAGES.contains(document.getLanguageId())) {
				String docURI = document.getId().getUri();
				String content = document.get();
				this.updateDocument(docURI, content, "didSave event");
			}
		});

		config.addListener(evt -> {
			log.info("update settings of spring indexer - start");
			
			server.getAsync().execute(() -> 
			configureIndexer(SymbolIndexConfig.builder()
					.scanXml(config.isSpringXMLSupportEnabled())
					.xmlScanFolders(config.xmlBeansFoldersToScan())
					.scanTestJavaSources(config.isScanJavaTestSourcesEnabled())
					.build()
			));
			
			log.info("update settings of spring indexer - done");
		});
		
		server.doOnInitialized(this::serverInitialized);
		server.onShutdown(this::shutdown);
	}

	public void serverInitialized() {
		List<String> globPattern = Arrays.asList(springIndexerJava.getFileWatchPatterns());

		getWorkspaceService().getFileObserver().onFilesDeleted(globPattern, (files) -> {
			deleteDocuments(files);
		});
		getWorkspaceService().getFileObserver().onFilesCreated(globPattern, (files) -> {
			createDocuments(files);
		});
		getWorkspaceService().getFileObserver().onFilesChanged(globPattern, (files) -> {
			updateDocuments(files, "file changed");
		});
	}

	public void configureIndexer(SymbolIndexConfig config) {
		synchronized (this) {
			if (config.isScanXml() && !(Arrays.asList(this.indexers).contains(springIndexerXML))) {
				this.indexers = new SpringIndexer[] { springIndexerJava, springIndexerXML };
				springIndexerXML.updateScanFolders(config.getXmlScanFolders());
				addXmlFileListeners(Arrays.asList(springIndexerXML.getFileWatchPatterns()));
			} else if (!config.isScanXml() && Arrays.asList(this.indexers).contains(springIndexerXML)) {
				this.indexers = new SpringIndexer[] { springIndexerJava };
				springIndexerXML.updateScanFolders(new String[0]);
				removeXmlFileListeners();
			} else if (config.isScanXml()) {
				if (springIndexerXML.updateScanFolders(config.getXmlScanFolders())) {
					// should remove the old listeners before adding the new ones
					addXmlFileListeners(Arrays.asList(springIndexerXML.getFileWatchPatterns()));
				}
			}
			springIndexerJava.setScanTestJavaSources(config.isScanTestJavaSources());
		}
	}
	
	private void addXmlFileListeners(List<String> globPattern) {
		removeXmlFileListeners();
		watchXMLDeleteRegistration = getWorkspaceService().getFileObserver().onFilesDeleted(globPattern,
				(files) -> {
					deleteDocuments(files);
				});
		watchXMLCreatedRegistration = getWorkspaceService().getFileObserver().onFilesCreated(globPattern,
				(files) -> {
					createDocuments(files);
				});
		watchXMLChangedRegistration = getWorkspaceService().getFileObserver().onFilesChanged(globPattern,
				(files) -> {
					updateDocuments(files, "xml changed");
				});
	}
	
	private void removeXmlFileListeners() {
		if (watchXMLChangedRegistration != null) {
			getWorkspaceService().getFileObserver().unsubscribe(watchXMLChangedRegistration);
			watchXMLChangedRegistration = null;
		}
		if (watchXMLCreatedRegistration != null) {
			getWorkspaceService().getFileObserver().unsubscribe(watchXMLCreatedRegistration);
			watchXMLCreatedRegistration = null;
		}
		if (watchXMLDeleteRegistration != null) {
			getWorkspaceService().getFileObserver().unsubscribe(watchXMLDeleteRegistration);
			watchXMLDeleteRegistration = null;
		}
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

					@SuppressWarnings("unchecked")
					CompletableFuture<Void>[] futures = new CompletableFuture[this.indexers.length];
					for (int i = 0; i < this.indexers.length; i++) {
						InitializeProject initializeItem = new InitializeProject(project, this.indexers[i]);
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

	public SpringIndexerJava getJavaIndexer() {
		return springIndexerJava;
	}
	
	public CompletableFuture<Void> deleteProject(IJavaProject project) {
		try {
			if (project.getElementName() == null) {
				// Projects indexed by name. No name  - no index for it
				log.debug("Project with NULL name is being removed");
				return CompletableFuture.completedFuture(null);
			} else {
				DeleteProject initializeItem = new DeleteProject(project, this.indexers);
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

			for (SpringIndexer indexer : this.indexers) {
				if (indexer.isInterestedIn(docURI)) {
					Optional<IJavaProject> maybeProject = projectFinder().find(new TextDocumentIdentifier(docURI));

					if (maybeProject.isPresent()) {
						try {
							DocumentDescriptor newDoc = createUpdatedDoc(docURI);
							futures.add(updateItems(maybeProject.get(), new DocumentDescriptor[] {newDoc}, indexer));
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

	public CompletableFuture<Void> createDocuments(String[] docURIs) {
		synchronized(this) {
			List<CompletableFuture<Void>> futures = new ArrayList<>();

			for (SpringIndexer indexer : this.indexers) {
				String[] interestingDocs = getDocumentsInterestingForIndexer(indexer, docURIs);
				Map<String, IJavaProject> projectsForDocs = getProjectsForDocs(interestingDocs);
				Map<IJavaProject, List<String>> projectMapping = getProjectMapping(projectsForDocs);
				
				for (IJavaProject project : projectMapping.keySet()) {
					List<String> docs = projectMapping.get(project);
					
					try {
						DocumentDescriptor[] updatedDocs = docs.stream().map(doc -> createUpdatedDoc(doc)).toArray(DocumentDescriptor[]::new);
						futures.add(updateItems(project, updatedDocs, indexer));
					}
					catch (Exception e) {
						log.error("{}", e);
					}
				}
			}

			return CompletableFuture.allOf((CompletableFuture[]) futures.toArray(new CompletableFuture[futures.size()]));
		}
	}

	private JavaProjectFinder projectFinder() {
		return params.projectFinder;
	}

	public CompletableFuture<Void> updateDocument(String docURI, String content, String reason) {
		log.info("Update document [{}]: {}", reason, docURI);
		
		synchronized(this) {
			List<CompletableFuture<Void>> futures = new ArrayList<>();

			for (SpringIndexer indexer : this.indexers) {
				if (indexer.isInterestedIn(docURI)) {
					Optional<IJavaProject> maybeProject = projectFinder().find(new TextDocumentIdentifier(docURI));
					if (maybeProject.isPresent()) {
						try {
							DocumentDescriptor updatedDoc = createUpdatedDoc(docURI);
							futures.add(updateItem(maybeProject.get(), updatedDoc, content, indexer));
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

	public CompletableFuture<Void> updateDocuments(String[] docURIs, String reason) {
		for (String docURI : docURIs) {
			log.info("Update document [{}]: {}", reason, docURI);
		}
		
		synchronized(this) {
			List<CompletableFuture<Void>> futures = new ArrayList<>();

			for (SpringIndexer indexer : this.indexers) {
				String[] interestingDocs = getDocumentsInterestingForIndexer(indexer, docURIs);
				Map<String, IJavaProject> projectsForDocs = getProjectsForDocs(interestingDocs);
				Map<IJavaProject, List<String>> projectMapping = getProjectMapping(projectsForDocs);
				
				for (IJavaProject project : projectMapping.keySet()) {
					List<String> docs = projectMapping.get(project);
					
					try {
						DocumentDescriptor[] updatedDocs = docs.stream().map(doc -> createUpdatedDoc(doc)).toArray(DocumentDescriptor[]::new);
						futures.add(updateItems(project, updatedDocs, indexer));
					}
					catch (Exception e) {
						log.error("{}", e);
					}
				}
			}
			return CompletableFuture.allOf((CompletableFuture[]) futures.toArray(new CompletableFuture[futures.size()]));
		}
	}
	
	private Map<String, IJavaProject> getProjectsForDocs(String[] docURIs) {
		Map<String, IJavaProject> result = new HashMap<>();
		
		for (String docURI : docURIs) {
			Optional<IJavaProject> project = projectFinder().find(new TextDocumentIdentifier(docURI));
			if (project.isPresent()) {
				result.put(docURI, project.get());
			}
		}
		return result;
	}

	private String[] getDocumentsInterestingForIndexer(SpringIndexer indexer, String[] docURIs) {
		return Arrays.stream(docURIs).filter(docURI -> indexer.isInterestedIn(docURI)).toArray(String[]::new);
	}

	private Map<IJavaProject, List<String>> getProjectMapping(Map<String, IJavaProject> docsToProject) {
		return docsToProject.keySet().stream().collect(Collectors.groupingBy(docURI -> docsToProject.get(docURI)));
	}
	
	private DocumentDescriptor createUpdatedDoc(String docURI) throws RuntimeException {
		try {
			File file = new File(new URI(docURI));
			long lastModified = file.lastModified();
			return new DocumentDescriptor(docURI, lastModified);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public CompletableFuture<Void> deleteDocument(String deletedDocURI) {
		synchronized(this) {
			try {
				Optional<IJavaProject> maybeProject = projectFinder().find(new TextDocumentIdentifier(deletedDocURI));
				if (maybeProject.isPresent()) {
					DeleteItems deleteItem = new DeleteItems(maybeProject.get(), new String[] {deletedDocURI}, this.indexers);
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

	public CompletableFuture<Void> deleteDocuments(String[] deletedDocURIs) {
		synchronized(this) {
			try {
				List<CompletableFuture<Void>> futures = new ArrayList<>();

				Map<String, IJavaProject> projectsForDocs = getProjectsForDocs(deletedDocURIs);
				Map<IJavaProject, List<String>> projectMapping = getProjectMapping(projectsForDocs);
				
				for (IJavaProject project : projectMapping.keySet()) {
					List<String> docURIs = projectMapping.get(project);

					DeleteItems deleteItems = new DeleteItems(project, (String[]) docURIs.toArray(new String[docURIs.size()]), this.indexers);
					futures.add(CompletableFuture.runAsync(deleteItems, this.updateQueue));
				}

				return CompletableFuture.allOf((CompletableFuture[]) futures.toArray(new CompletableFuture[futures.size()]));
			}
			catch (Exception e) {
				log.error("", e);
				return Futures.error(e);
			}
		}
	}

	public List<SymbolInformation> getAllSymbols(String query) {
		if (query != null && query.length() > 0) {
			synchronized(this.symbols) {
				return searchMatchingSymbols(this.symbols, query, MAX_NUMBER_OF_SYMBOLS_IN_RESPONSE);
			}
		} else {
			synchronized(this.symbols) {
				return this.symbols.stream().map(s -> s.getSymbol()).limit(Math.min(MAX_NUMBER_OF_SYMBOLS_IN_RESPONSE, this.symbols.size())).collect(Collectors.toList());
			}
		}
	}
	
	public Stream<SymbolInformation> getSymbols(Predicate<EnhancedSymbolInformation> filter) {
		return symbols.parallelStream()
			.filter(filter)
			.map(enhanced -> enhanced.getSymbol());
	}

	public List<? extends SymbolInformation> getSymbols(String docURI) {
		List<EnhancedSymbolInformation> docSymbols = this.symbolsByDoc.get(docURI);
		if (docSymbols != null) {
			synchronized(docSymbols) {
				ImmutableList.Builder<SymbolInformation> builder = ImmutableList.builder();
				for (EnhancedSymbolInformation enhanced : docSymbols) {
					builder.add(enhanced.getSymbol());
				}
				return builder.build();
			}
		}
		else {
			return Collections.emptyList();
		}
	}

	public List<SymbolAddOnInformation> getAllAdditionalInformation(Predicate<SymbolAddOnInformation> filter) {
		if (filter != null) {
			synchronized(symbols) {
				return symbols.stream()
						.map(s -> s.getAdditionalInformation())
						.filter(Objects::nonNull)
						.flatMap(i -> Arrays.stream(i))
						.filter(filter)
						.collect(Collectors.toList());
			}
		}
		else {
			return Collections.emptyList();
		}
	}

	public List<? extends SymbolAddOnInformation> getAdditonalInformation(String docURI) {
		List<EnhancedSymbolInformation> info = this.symbolsByDoc.get(docURI);

		if (info != null) {
			synchronized(info) {
				ImmutableList.Builder<SymbolAddOnInformation> builder = ImmutableList.builder();
				for (EnhancedSymbolInformation enhanced : info) {
					SymbolAddOnInformation[] additionalInformation = enhanced.getAdditionalInformation();
					if (additionalInformation != null) {
						builder.add(additionalInformation);
					}
				}
				return builder.build();
			}
		}
		else {
			return Collections.emptyList();
		}
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

	private List<SymbolInformation> searchMatchingSymbols(List<EnhancedSymbolInformation> allsymbols, String query, int maxNumberOfSymbolsInResponse) {
		long limit = maxNumberOfSymbolsInResponse;
		String locationPrefix = "";

		if (query.startsWith(QUERY_PARAM_LOCATION_PREFIX)) {

			int separatorIndex = query.indexOf("?");
			if (separatorIndex > 0) {
				locationPrefix = query.substring(QUERY_PARAM_LOCATION_PREFIX.length(), separatorIndex);
				query = query.substring(separatorIndex + 1);
			}
			else {
				locationPrefix = query.substring(QUERY_PARAM_LOCATION_PREFIX.length());
				query = query.substring(QUERY_PARAM_LOCATION_PREFIX.length() + locationPrefix.length());
			}
		}

		if (query.startsWith("*")) {
			limit = Long.MAX_VALUE;
			query = query.substring(1);
		}

		String finalQuery = query;
		String finalLocationPrefix = locationPrefix;

		return allsymbols.stream()
				.map(enhanced -> enhanced.getSymbol())
				.filter(symbol -> symbol.getLocation().getUri().startsWith(finalLocationPrefix))
				.filter(symbol -> StringUtil.containsCharactersCaseInsensitive(symbol.getName(), finalQuery))
				.limit(limit)
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

	CompletableFuture<Void> updateItem(IJavaProject project, DocumentDescriptor updatedDoc, String content, SpringIndexer indexer) {
		log.debug("scheduling updateItem {}. {},  {}, {}", project.getElementName(), updatedDoc.getDocURI(), updatedDoc.getLastModified(), indexer);

		return CompletableFuture.runAsync(() -> {
			
			try {
				log.debug("updateItem {}. {},  {}, {}", project.getElementName(), updatedDoc.getDocURI(), updatedDoc.getLastModified(), indexer);
				indexer.updateFile(project, updatedDoc, content);
			} catch (Exception e) {
				log.error("{}", e);
			}
		}, this.updateQueue);
	}

	CompletableFuture<Void> updateItems(IJavaProject project, DocumentDescriptor[] updatedDoc, SpringIndexer indexer) {
		for (DocumentDescriptor doc : updatedDoc) {
			log.debug("scheduling updateItem {}. {},  {}, {}", project.getElementName(), doc.getDocURI(), doc.getLastModified(), indexer);
		}

		return CompletableFuture.runAsync(() -> {
			
			try {
				for (DocumentDescriptor doc : updatedDoc) {
					log.debug("updateItem {}. {},  {}, {}", project.getElementName(), doc.getDocURI(), doc.getLastModified(), indexer);
				}

				indexer.updateFiles(project, updatedDoc);
			} catch (Exception e) {
				log.error("{}", e);
			}
		}, this.updateQueue);
	}

	private class DeleteItems implements Runnable {

		private final String[] docURIs;
		private final IJavaProject project;
		private final SpringIndexer[] indexer;

		public DeleteItems(IJavaProject project, String[] docURIs, SpringIndexer[] indexer) {
			this.project = project;
			this.docURIs = docURIs;
			this.indexer = indexer;
		}

		@Override
		public void run() {
			try {
				for (String doc : this.docURIs) {
					removeSymbolsByDoc(project, doc);
				}
				
				for (SpringIndexer index : this.indexer) {
					index.removeFiles(project, docURIs);
				}
			} catch (Exception e) {
				log.error("{}", e);
			}
		}
	}

	private class DeleteProject implements Runnable {

		private final IJavaProject project;
		private final SpringIndexer[] indexer;

		public DeleteProject(IJavaProject project, SpringIndexer[] indexer) {
			this.project = project;
			this.indexer = indexer;
			log.debug("{} created ", this);
		}

		@Override
		public void run() {
			log.debug("{} starting...", this);
			try {
				removeSymbolsByProject(project);
				for (SpringIndexer index : this.indexer) {
					index.removeProject(project);
				}
				log.debug("{} completed", this);
			} catch (Throwable e) {
				log.error("{} threw exception", this, e);
			}
		}

	}

	private void addSymbol(IJavaProject project, String docURI, EnhancedSymbolInformation enhancedSymbol) {
		synchronized(this.symbols) {
			symbols.add(enhancedSymbol);
		}

		List<EnhancedSymbolInformation> docSymbols = symbolsByDoc.computeIfAbsent(docURI, s -> new ArrayList<EnhancedSymbolInformation>());
		synchronized(docSymbols) {
			docSymbols.add(enhancedSymbol);
		}

		List<EnhancedSymbolInformation> projectSymbols = symbolsByProject.computeIfAbsent(project.getElementName(), s -> new ArrayList<EnhancedSymbolInformation>());
		synchronized(projectSymbols) {
			projectSymbols.add(enhancedSymbol);
		}
	}

	private void removeSymbolsByDoc(IJavaProject project, String docURI) {
		List<EnhancedSymbolInformation> oldSymbols = symbolsByDoc.remove(docURI);
		if (oldSymbols != null) {

			List<EnhancedSymbolInformation> copy = null;
			synchronized(oldSymbols) {
				copy = new ArrayList<>(oldSymbols);
			}

			synchronized(this.symbols) {
				this.symbols.removeAll(copy);
			}

			List<EnhancedSymbolInformation> projectSymbols = symbolsByProject.get(project.getElementName());
			if (projectSymbols != null) {
				synchronized(projectSymbols) {
					projectSymbols.removeAll(copy);
				}
			}
		}
	}

	private void removeSymbolsByProject(IJavaProject project) {
		// If project name is null it cannot be in the cache
		if (project.getElementName() == null) {
			return;
		}
		List<EnhancedSymbolInformation> oldSymbols = symbolsByProject.remove(project.getElementName());
		if (oldSymbols != null) {

			List<EnhancedSymbolInformation> copy = null;
			synchronized(oldSymbols) {
				copy = new ArrayList<>(oldSymbols);
			}

			synchronized(this.symbols) {
				symbols.removeAll(copy);
			}

			Set<String> keySet = symbolsByDoc.keySet();
			Iterator<String> docIter = keySet.iterator();
			while (docIter.hasNext()) {
				String docURI = docIter.next();
				List<EnhancedSymbolInformation> docSymbols = symbolsByDoc.get(docURI);
				synchronized(docSymbols) {
					docSymbols.removeAll(copy);

					if (docSymbols.isEmpty()) {
						docIter.remove();
					}
				}
			}
		}

	}
}
