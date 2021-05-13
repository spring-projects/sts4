/*******************************************************************************
 * Copyright (c) 2017, 2021 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.livehover.v2;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServerComponents;
import org.springframework.ide.vscode.boot.java.handlers.BootJavaHoverProvider;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.protocol.HighlightParams;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class SpringProcessLiveHoverUpdater {

	private static final Logger log = LoggerFactory.getLogger(SpringProcessLiveHoverUpdater.class);

	private final SimpleLanguageServer server;
	private final BootJavaHoverProvider hoverProvider;
	private final JavaProjectFinder projectFinder;

	private final Map<String, AtomicReference<IJavaProject>> watchedDocs;
	private boolean highlightsEnabled = true;

	// this update executor puts all the updates to live hovers into a sequence
	// to avoid race conditions among different update operations 
	private final Executor updateExecutor;

	public SpringProcessLiveHoverUpdater(
			SimpleLanguageServer server,
			BootJavaHoverProvider hoverProvider,
			JavaProjectFinder projectFinder,
			SpringProcessLiveDataProvider liveDataProvider) {

		this.server = server;
		this.hoverProvider = hoverProvider;
		this.projectFinder = projectFinder;
		this.watchedDocs = new ConcurrentHashMap<>();
		this.updateExecutor = Executors.newSingleThreadExecutor();

		server.getTextDocumentService().onDidChangeContent(params -> {
			TextDocument doc = params.getDocument();
			if (BootJavaLanguageServerComponents.LANGUAGES.contains(doc.getLanguageId())) {
				watchDocument(doc);
			}
		});
		
		server.getTextDocumentService().onDidClose(doc -> {
			unwatchDocument(doc);
		});
		
		liveDataProvider.addLiveDataChangeListener(event -> {
			CompletableFuture.runAsync(() -> {
				update();
			}, updateExecutor);
		});
	}

	public void cleanup() {
		watchedDocs.keySet().stream()
			.map(uri -> this.server.getTextDocumentService().getLatestSnapshot(uri))
			.filter(Objects::nonNull)
			.forEach(doc -> cleanupLiveHints(doc));
	}

	public void watchDocument(TextDocument doc) {
		this.watchedDocs.putIfAbsent(doc.getUri(), new AtomicReference<IJavaProject>());
		
		CompletableFuture.runAsync(() -> {
			try {
				updateDoc(doc);
			} catch (Throwable t) {
				log.error("", t);
			}
		}, updateExecutor);
	}

	public void unwatchDocument(TextDocument doc) {
		this.watchedDocs.remove(doc.getUri());
		cleanupLiveHints(doc);
	}

	// runs async
	private void updateDoc(TextDocument doc) {
		try {
			IJavaProject project = getCachedProject(doc.getUri());
			update(doc, project);
		}
		catch (Exception e) {
			log.error("", e);
		}
	}

	// runs async
	private void update() {
		if (this.watchedDocs.size() > 0) {
			try {
				for (String docURI : watchedDocs.keySet()) {
					IJavaProject project = getCachedProject(docURI);
					TextDocument doc = this.server.getTextDocumentService().getLatestSnapshot(docURI);
					update(doc, project);
				}
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}

	// runs async
	private void update(TextDocument doc, IJavaProject project) {
		if (highlightsEnabled) {
			try {
				if (doc != null) {
					CodeLens[] infos = this.hoverProvider.getLiveHoverHints(doc, project);
					publishLiveHints(doc, infos);
				}
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}

	// runs async
	private IJavaProject getCachedProject(String docURI) {
		AtomicReference<IJavaProject> reference = this.watchedDocs.get(docURI);
		if (reference != null) {
			IJavaProject project = reference.get();
			if (project == null) {
				project = identifyProject(docURI);
				if (!reference.compareAndSet(null, project)) {
					return reference.get();
				}
			}
			return project;
		}
		return null;
	}

	// runs async
	private IJavaProject identifyProject(String docURI) {
		TextDocument doc = this.server.getTextDocumentService().getLatestSnapshot(docURI);
		if (doc != null) {
			return projectFinder.find(doc.getId()).orElse(null);
		}
		else {
			return null;
		}
	}

	// runs sync or async
	private void publishLiveHints(TextDocument doc, CodeLens[] codeLenses) {
		if (doc != null) {
			int version = doc.getVersion();
			VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier(doc.getUri(), version);
			server.getClient().highlight(new HighlightParams(id, Arrays.asList(codeLenses)));
		}
	}

	// runs sync or async
	private void cleanupLiveHints(TextDocument doc) {
		publishLiveHints(doc, new CodeLens[0]);
	}
	
}
