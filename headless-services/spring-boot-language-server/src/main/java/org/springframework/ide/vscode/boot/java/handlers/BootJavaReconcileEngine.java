/*******************************************************************************
 * Copyright (c) 2016, 2023 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.handlers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.app.BootJavaConfig;
import org.springframework.ide.vscode.boot.common.IJavaProjectReconcileEngine;
import org.springframework.ide.vscode.boot.common.ProjectReconcileScheduler;
import org.springframework.ide.vscode.boot.java.reconcilers.JavaReconciler;
import org.springframework.ide.vscode.boot.java.rewrite.RewriteRecipeRepository;
import org.springframework.ide.vscode.commons.java.IClasspathUtil;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.UriUtil;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.LazyTextDocument;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class BootJavaReconcileEngine implements IReconcileEngine, IJavaProjectReconcileEngine {
		
	private static final Logger log = LoggerFactory.getLogger(BootJavaReconcileEngine.class);

	private final JavaProjectFinder projectFinder; 
	private final JavaReconciler[] javaReconcilers;
	private final BootJavaProjectReconcilerScheduler projectReconeilerScheduler;
	private final SimpleLanguageServer server;

	public BootJavaReconcileEngine(JavaProjectFinder projectFinder, JavaReconciler[] javaReconcilers,
			SimpleLanguageServer server, BootJavaConfig config, ProjectObserver projectObserver,
			RewriteRecipeRepository recipeRepo) {
		this.projectFinder = projectFinder;
		this.javaReconcilers = javaReconcilers;
		this.server = server;
		this.projectReconeilerScheduler = new BootJavaProjectReconcilerScheduler(
				this,
				server.getWorkspaceService().getFileObserver(),
				projectObserver,
				config,
				recipeRepo,
				server.getTextDocumentService(),
				projectFinder
		);
	}

	@Override
	public void reconcile(final IDocument doc, final IProblemCollector problemCollector) {
		IJavaProject project = projectFinder.find(new TextDocumentIdentifier(doc.getUri())).orElse(null);
		if (project != null) {
			
			try {
				problemCollector.beginCollecting();
				
				AtomicInteger count = new AtomicInteger(0);
				
				IProblemCollector recolerProblemCollector = new IProblemCollector() {
		
					@Override
					public void beginCollecting() {
					}
		
					@Override
					public synchronized void endCollecting() {
						// If count is reached then let the endCollecting be called when all completable futures resolved
						if (count.incrementAndGet() < javaReconcilers.length) {
							problemCollector.checkPointCollecting();
						}
					}
		
					@Override
					public synchronized void accept(ReconcileProblem problem) {
						problemCollector.accept(problem);
					}
					
				};
				
				for (JavaReconciler jr : javaReconcilers) {
					try {
						jr.reconcile(project, doc, recolerProblemCollector);
					} catch (Exception e) {
						log.error("", e);
					}
				}
				
				problemCollector.endCollecting();
				
//				CompletableFuture<?>[] futures = Arrays.stream(javaReconcilers)
//						.map(jr -> CompletableFuture.runAsync(() -> jr.reconcile(project, doc, recolerProblemCollector)).exceptionally(t -> null))
//						.toArray(CompletableFuture[]::new);
//				
//				CompletableFuture.allOf(futures).thenAccept(a -> problemCollector.endCollecting()).get();
				
			} catch (Exception e) {
				log.error("", e);
			} finally {
				problemCollector.endCollecting();
			}
		
		}
	}

	@Override
	public void reconcile(IJavaProject project) {
		Stream<Path> files = IClasspathUtil.getProjectJavaSourceFolders(project.getClasspath()).flatMap(folder -> {
			try {
				return Files.walk(folder.toPath()).filter(Files::isRegularFile);
			} catch (IOException e) {
				return Stream.empty();
			}
		});

		Stream<TextDocumentIdentifier> docIds = files
				.filter(f -> f.getFileName().toString().endsWith(".java"))
				.map(f -> new TextDocumentIdentifier(f.toUri().toASCIIString()));

		List<TextDocument> docs = docIds.filter(docId -> server.getTextDocumentService().getLatestSnapshot(docId.getUri()) == null)
				.map(docId -> new LazyTextDocument(docId.getUri(), LanguageId.JAVA)).collect(Collectors.toList());

		Map<IDocument, IProblemCollector> problemCollectors = docs.stream()
				.collect(Collectors.toMap(d -> d, d -> server.createProblemCollector(d)));

		problemCollectors.values().forEach(c -> c.beginCollecting());

		for (JavaReconciler jr : javaReconcilers) {
			try {
				Map<IDocument, Collection<ReconcileProblem>> problems = jr.reconcile(project, docs);
				problems.entrySet().forEach(e -> {
					IProblemCollector collector = problemCollectors.get(e.getKey());
					e.getValue().forEach(p -> collector.accept(p));
				});
			} catch (Exception e) {
				log.error("", e);
			}
		}

		problemCollectors.values().forEach(c -> c.endCollecting());

	}

	@Override
	public void clear(IJavaProject project) {
		IClasspathUtil.getProjectJavaSourceFolders(project.getClasspath()).flatMap(folder -> {
			try {
				return Files.walk(folder.toPath()).filter(Files::isRegularFile);
			} catch (IOException e) {
				return Stream.empty();
			}
		})
		.filter(f -> f.getFileName().toString().endsWith(".java"))
		.filter(f -> server.getTextDocumentService().getLatestSnapshot(UriUtil.toUri(f.toFile()).toASCIIString()) == null)
		.forEach(p -> server.getTextDocumentService().publishDiagnostics(new TextDocumentIdentifier(p.toUri().toASCIIString()), Collections.emptyList()));
		
	}

	@Override
	public ProjectReconcileScheduler getScheduler() {
		return projectReconeilerScheduler;
	}

}
