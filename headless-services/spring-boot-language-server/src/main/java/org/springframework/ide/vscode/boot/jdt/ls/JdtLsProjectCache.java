/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.jdt.ls;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.jandex.JandexIndex.JavadocProviderFactory;
import org.springframework.ide.vscode.commons.java.ClasspathData;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.JavaProject;
import org.springframework.ide.vscode.commons.javadoc.JdtLsJavadocProvider;
import org.springframework.ide.vscode.commons.languageserver.JavadocParams;
import org.springframework.ide.vscode.commons.languageserver.jdt.ls.Classpath.CPE;
import org.springframework.ide.vscode.commons.languageserver.jdt.ls.ClasspathListener;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.Assert;
import org.springframework.ide.vscode.commons.util.ExceptionUtil;
import org.springframework.ide.vscode.commons.util.FileObserver;
import org.springframework.ide.vscode.commons.util.UriUtil;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import reactor.core.Disposable;

public class JdtLsProjectCache implements JavaProjectsService {
	
	private CompletableFuture<Void> initialized = new CompletableFuture<Void>();

	private SimpleLanguageServer server;
	private Map<String, JavaProject> table = new HashMap<String, JavaProject>();
	private Logger log = LoggerFactory.getLogger(JdtLsProjectCache.class);
	private List<Listener> listeners = new ArrayList<>();

	private final Supplier<JavaProjectsService> fallback;
	
	public JdtLsProjectCache(SimpleLanguageServer server, Supplier<JavaProjectsService> fallback) {
		Assert.isNotNull(fallback);
		this.fallback = Suppliers.memoize(fallback);
		this.server = server;
		CompletableFuture<Disposable> disposable = new CompletableFuture<Disposable>();
		this.server.onInitialized(() -> {
			try {
				disposable.complete(server.addClasspathListener(new ClasspathListener() {
					@Override
					public void changed(Event event) {
						log.debug("claspath event received {}", event);
						initialized.thenRun(() -> {
							//log.info("initialized.thenRun block entered");
							try {
								synchronized (table) {
									String uri = UriUtil.normalize(event.projectUri);
									log.debug("uri = {}", uri);
									if (event.deleted) {
										log.debug("event.deleted = true");
										JavaProject deleted = table.remove(uri);
										if (deleted!=null) {
											log.debug("removed from table = true");
											notifyDelete(deleted);
										} else {
											log.warn("Deleted project not removed because uri {} not found in {}", uri, table.keySet());
										}
									} else {
										log.debug("deleted = false");
										JdtLsJavadocProvider javadocProvider = new JdtLsJavadocProvider(server.getClient(), uri);
										JavaProject newProject = new JavaProject(getFileObserver(), new URI(uri), new ClasspathData(event.name, event.classpath.getEntries()), classpathResource -> javadocProvider);
										JavaProject oldProject = table.put(uri, newProject);
										if (oldProject != null) {
											notifyChanged(newProject);
										} else {
											notifyCreated(newProject);
										}
									}
								}
							} catch (Exception e) {
								log.error("", e);
							}
						});
					}
				}));
				initialized.complete(null);
			} catch (Throwable e) {
				if (isNoJdtError(e)) {
					log.info("JDT Language Server not available. Fallback classpath provider will be used instead.");
				} else if (isOldJdt(e)) {
					log.info("JDT Lanuage Server too old. Fallback classpath provider will be used instead.");
				} else {
					log.error("Unexpected error registering classpath listener with JDT. Fallback classpath provider will be used instead.", e);
				}
				disposable.complete(()-> {});
				initialized.completeExceptionally(e);
			}
		});
		this.server.onShutdown(() -> 
			disposable.thenAccept(Disposable::dispose).join()
		);
	}

	private FileObserver getFileObserver() {
		return server.getWorkspaceService().getFileObserver();
	}

	private boolean isOldJdt(Throwable e) {
		return ExceptionUtil.getMessage(e).contains("'sts.java.addClasspathListener' not supported");
	}

	private boolean isNoJdtError(Throwable e) {
		return ExceptionUtil.getMessage(e).contains("command 'java.execute.workspaceCommand' not found");
	}

	@Override
	public void addListener(Listener listener) {
		initialized.handle((success, failed) -> {
			if (failed!=null) {
				fallback.get().addListener(listener);
			} else {
				synchronized (listeners) {
					listeners.add(listener);
				}
			}
			return null;
		});
	}

	@Override
	public void removeListener(Listener listener) {
		initialized.handle((success, failed) -> {
			if (failed!=null) {
				fallback.get().removeListener(listener);
			} else {
				synchronized (listeners) {
					listeners.remove(listener);
				}
			}
			return null;
		});
	}

	private void notifyCreated(JavaProject newProject) {
		logEvent("Created", newProject);
		synchronized (listeners) {
			for (Listener listener : listeners) {
				listener.created(newProject);
			}
		}
	}

	private void notifyDelete(JavaProject deleted) {
		logEvent("Deleted", deleted);
		synchronized (listeners) {
			for (Listener listener : listeners) {
				listener.deleted(deleted);
			}
		}
		deleted.dispose();
	}
	
	private void notifyChanged(JavaProject newProject) {
		logEvent("Changed", newProject);
		synchronized (listeners) {
			for (Listener listener : listeners) {
				listener.changed(newProject);
			}
		}
	}

	private void logEvent(String type, JavaProject project) {
		try {
			log.info("Project "+type+": " + project.getLocationUri());
			log.info("Classpath has "+project.getClasspath().getClasspathEntries().size()+" entries "
					+countSourceAttachments(project.getClasspath().getClasspathEntries()) + " source attachements");
		} catch (Exception e) {
		}
	}
	
	private static int countSourceAttachments(Collection<CPE> classpathEntries) {
		int count = 0;
		for (CPE cpe : classpathEntries) {
			URL sourceJar = cpe.getSourceContainerUrl();
			if (sourceJar!=null) {
				try {
					if (new File(sourceJar.toURI()).exists()) {
						count++;
					}
				} catch (URISyntaxException e) {
				}
			}
		}
		return count;
	}

	@Override
	public Optional<IJavaProject> find(TextDocumentIdentifier doc) {
		String uri = UriUtil.normalize(doc.getUri());
		log.debug("find {} ", uri);
		if (initialized.isDone()) {
			if (initialized.isCompletedExceptionally()) {
				log.debug("find {} delegating to fallback", uri);
				Optional<IJavaProject> result = fallback.get().find(doc);
				log.debug("find => {}", result);
				return result;
			}
	
			synchronized (table) {
				for (Entry<String, JavaProject> e : table.entrySet()) {
					String projectUri = e.getKey();
					log.debug("projectUri = '{}'", projectUri);
					if (UriUtil.contains(projectUri, uri) ) {
						log.debug("found {} for {}", e.getValue(), uri);
						return Optional.of(e.getValue());
					} 
				}
			}
		} else {
			log.debug("find => NOT INITIALIZED YET");
		}
		log.debug("NOT FOUND {} ", uri);
		return Optional.empty();
	}
}
