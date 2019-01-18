/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.jdt.ls;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.IJavadocProvider;
import org.springframework.ide.vscode.commons.languageserver.java.ls.Classpath.CPE;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.Assert;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;

import reactor.core.Disposable;

public class JavaProjectsServiceWithFallback implements JavaProjectsService {

	private Logger log = LoggerFactory.getLogger(JavaProjectsServiceWithFallback.class);

	final private CompletableFuture<Disposable> mainServiceInitialized;

	final private SimpleLanguageServer server;
	private Supplier<JavaProjectsService> fallback;
	final private InitializableJavaProjectsService main;

	public JavaProjectsServiceWithFallback(SimpleLanguageServer server, InitializableJavaProjectsService main, Supplier<JavaProjectsService> fallback) {
		Assert.isNotNull(fallback);
		this.main = main;
		this.fallback = Suppliers.memoize(fallback);
		this.server = server;
		this.mainServiceInitialized = this.server
				.onInitialized(main.initialize())
				.toFuture();
		this.server.onShutdown(() -> {
			try {
				if (!mainServiceInitialized.isCompletedExceptionally()) {
					// If classpath listener has been added successfully, remove it
					mainServiceInitialized.thenAccept(Disposable::dispose).join();
				}
			} catch (Exception e) {
				// If completable future hasn't completed yet it might complete with exception to add classpath listener.
				// Handle exception gracefully rather than failing LS process to terminate
				log.error("", e);
			}
		}
		);
	}

	@Override
	public Optional<IJavaProject> find(TextDocumentIdentifier doc) {
		if (mainServiceInitialized.isDone()) {
			if (mainServiceInitialized.isCompletedExceptionally()) {
				return fallback.get().find(doc);
			} else {
				return main.find(doc);
			}
		} else {
			log.debug("find => NOT INITIALIZED YET");
		}
		log.debug("NOT FOUND {} ", doc.getUri());
		return Optional.empty();
	}

	@Override
	public void addListener(Listener listener) {
		mainServiceInitialized.handle((success, failed) -> {
			if (failed!=null) {
				fallback.get().addListener(listener);
			} else {
				main.addListener(listener);
			}
			return null;
		});
	}

	@Override
	public void removeListener(Listener listener) {
		mainServiceInitialized.handle((success, failed) -> {
			if (failed!=null) {
				fallback.get().removeListener(listener);
			} else {
				main.removeListener(listener);
			}
			return null;
		});
	}

	@Override
	public IJavadocProvider javadocProvider(String projectUri, CPE classpathEntry) {
		if (mainServiceInitialized.isDone()) {
			if (mainServiceInitialized.isCompletedExceptionally()) {
				return fallback.get().javadocProvider(projectUri, classpathEntry);
			} else {
				return main.javadocProvider(projectUri, classpathEntry);
			}
		} else {
			log.debug("javadoc => NOT INITIALIZED YET");
		}
		return IJavadocProvider.NULL;
	}

	@Override
	public Collection<? extends IJavaProject> all() {
		if (mainServiceInitialized.isDone()) {
			if (mainServiceInitialized.isCompletedExceptionally()) {
				return fallback.get().all();
			} else {
				return main.all();
			}
		} else {
			log.debug("find => NOT INITIALIZED YET");
		}
		return ImmutableList.of();
	}

}
