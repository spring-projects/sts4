/*******************************************************************************
 * Copyright (c) 2018, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.jdt.ls;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.java.ClasspathData;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.IJavadocProvider;
import org.springframework.ide.vscode.commons.java.JavaProject;
import org.springframework.ide.vscode.commons.java.JdtLsJavaProject;
import org.springframework.ide.vscode.commons.javadoc.JdtLsJavadocProvider;
import org.springframework.ide.vscode.commons.languageserver.java.ls.ClasspathListener;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.protocol.java.Classpath.CPE;
import org.springframework.ide.vscode.commons.util.ExceptionUtil;
import org.springframework.ide.vscode.commons.util.FileObserver;
import org.springframework.ide.vscode.commons.util.UriUtil;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

public class JdtLsProjectCache implements InitializableJavaProjectsService {

	private static final Duration INITIALIZE_TIMEOUT = Duration.ofSeconds(10);
	private static final Object JDT_SCHEME = "jdt";

	private final boolean IS_JANDEX_INDEX;

	private SimpleLanguageServer server;
	private Map<String, IJavaProject> table = new HashMap<String, IJavaProject>();
	private Logger log = LoggerFactory.getLogger(JdtLsProjectCache.class);
	private List<Listener> listeners = new ArrayList<>();

	public JdtLsProjectCache(SimpleLanguageServer server, boolean isJandexIndex) {
		this.server = server;
		this.IS_JANDEX_INDEX = isJandexIndex;
		this.server
			.onInitialized(initialize())
			.doOnSuccess((disposable) -> {
				server.onShutdown(() -> {
					disposable.dispose();
				});
			})
			.doOnError(error -> {
				log.error("JDT-based JavaProject service not available!", error);
			})
			.toFuture();
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
		synchronized (listeners) {
			listeners.add(listener);
			log.info("added listener - now listeners registered: " + listeners.size());
		}
	}

	@Override
	public void removeListener(Listener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
			log.info("removed listener - now listeners registered: " + listeners.size());
		}
	}

	private void notifyCreated(IJavaProject newProject) {
		logEvent("Created", newProject);

		synchronized (listeners) {
			log.info("listeners registered: " + listeners.size());

			for (Listener listener : listeners) {
				try {
					listener.created(newProject);
				}
				catch (Exception e) {
					log.info("listener caused exception: " + e);
				}
			}
		}
	}

	private void notifyDelete(IJavaProject deleted) {
		logEvent("Deleted", deleted);
		synchronized (listeners) {
			for (Listener listener : listeners) {
				listener.deleted(deleted);
			}
		}
		if (deleted instanceof Disposable) {
			((Disposable)deleted).dispose();
		}
	}

	private void notifyChanged(IJavaProject newProject) {
		logEvent("Changed", newProject);
		synchronized (listeners) {
			for (Listener listener : listeners) {
				listener.changed(newProject);
			}
		}
	}

	private void logEvent(String type, IJavaProject project) {
		try {
			log.info("Project {}: {}", type, project.getLocationUri());
			log.info("Classpath has {} entries", project.getClasspath().getClasspathEntries().size());
			if (log.isDebugEnabled()) {
				//Avoid expensive call to countSourceAttachements if possible.
				log.debug("Classpath has {} source attachements",  countSourceAttachments(project.getClasspath().getClasspathEntries()));
			}
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
		// JDT URI has project
		URI docUri = URI.create(doc.getUri());
		if (JDT_SCHEME.equals(docUri.getScheme()) && "contents".equals(docUri.getAuthority())) {
			return findProjectForJDtUri(docUri);
		}
		String uri = UriUtil.normalize(doc.getUri());
		log.debug("find {} ", uri);
		synchronized (table) {
			String foundUri = null;
			IJavaProject foundProject = null;
			for (Entry<String, IJavaProject> e : table.entrySet()) {
				String projectUri = e.getKey();
				log.debug("projectUri = '{}'", projectUri);
				if (UriUtil.contains(projectUri, uri)) {
					if (foundUri==null) {
						log.debug("found {} for {}", e.getValue(), uri);
						foundUri = projectUri;
						foundProject = e.getValue();
					} else if (UriUtil.contains(foundUri, projectUri)) {
						log.debug("found more nested {} for {}", e.getValue(), uri);
						foundUri = projectUri;
						foundProject = e.getValue();
					} else {
						log.debug("found {} for {} but keeping {}", e.getValue(), uri, foundProject);
					}
				}
			}
			return Optional.ofNullable(foundProject);
		}
	}

	private Optional<IJavaProject> findProjectForJDtUri(URI uri) {
		String query = uri.getQuery();
		try {
			String decodedQuery = URLDecoder.decode(query, "UTF-8");
			int lastIdx = decodedQuery.indexOf("/\\/");
			if (lastIdx > 0) {
				String projectName = decodedQuery.substring(1, lastIdx);
				synchronized (table) {
					for (IJavaProject project : table.values()) {
						if (project.getElementName().equals(projectName)) {
							return Optional.of(project);
						}
					}
				}
			}
		} catch (UnsupportedEncodingException e) {
			log.error("{}", e);
		}
		return Optional.empty();
	}

	@Override
	public IJavadocProvider javadocProvider(String projectUri, CPE classpathEntry) {
		return new JdtLsJavadocProvider(server.getClient(), projectUri);
	}

	@Override
	public Mono<Disposable> initialize() {
		return Mono.defer(() -> server.addClasspathListener(new ClasspathListener() {
			@Override
			public void changed(Event event) {
				log.debug("claspath event received {}", event);
				server.doOnInitialized(() -> {
					//log.info("initialized.thenRun block entered");
					try {
						synchronized (table) {
							String uri = UriUtil.normalize(event.projectUri);
							log.debug("uri = {}", uri);
							if (event.deleted) {
								log.debug("event.deleted = true");
								IJavaProject deleted = table.remove(uri);
								if (deleted!=null) {
									log.debug("removed from table = true");
									notifyDelete(deleted);
								} else {
									log.warn("Deleted project not removed because uri {} not found in {}", uri, table.keySet());
								}
							} else {
								log.debug("deleted = false");
								URI projectUri = new URI(uri);
								ClasspathData classpath = new ClasspathData(event.name, event.classpath.getEntries());
								IJavaProject newProject = IS_JANDEX_INDEX
										? new JavaProject(getFileObserver(), projectUri, classpath,
												JdtLsProjectCache.this)
										: new JdtLsJavaProject(server.getClient(), projectUri, classpath, JdtLsProjectCache.this);
								IJavaProject oldProject = table.put(uri, newProject);
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
		})
		.timeout(INITIALIZE_TIMEOUT)
		.doOnSubscribe(x -> log.info("addClasspathListener ..."))
		.doOnSuccess(x -> log.info("addClasspathListener DONE"))
		.doOnError(t -> {
			log.error("Unexpected error registering classpath listener with JDT. Fallback classpath provider will be used instead.", t);
		}));
	}

	@Override
	public Collection<? extends IJavaProject> all() {
		return table.values();
	}
}
