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

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.java.ClasspathData;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.IJavadocProvider;
import org.springframework.ide.vscode.commons.java.JavaProject;
import org.springframework.ide.vscode.commons.javadoc.JdtLsJavadocProvider;
import org.springframework.ide.vscode.commons.languageserver.jdt.ls.Classpath.CPE;
import org.springframework.ide.vscode.commons.languageserver.jdt.ls.ClasspathListener;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.ExceptionUtil;
import org.springframework.ide.vscode.commons.util.FileObserver;
import org.springframework.ide.vscode.commons.util.UriUtil;

import reactor.core.Disposable;

public class JdtLsProjectCache implements InitializableJavaProjectsService {

	private SimpleLanguageServer server;
	private Map<String, JavaProject> table = new HashMap<String, JavaProject>();
	private Logger log = LoggerFactory.getLogger(JdtLsProjectCache.class);
	private List<Listener> listeners = new ArrayList<>();

	public JdtLsProjectCache(SimpleLanguageServer server) {
		this.server = server;
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
		}
	}

	@Override
	public void removeListener(Listener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
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
		synchronized (table) {
			String foundUri = null;
			IJavaProject foundProject = null;
			for (Entry<String, JavaProject> e : table.entrySet()) {
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

	@Override
	public IJavadocProvider javadocProvider(String projectUri, CPE classpathEntry) {
		return new JdtLsJavadocProvider(server.getClient(), projectUri);
	}

	@Override
	public Disposable initialize() throws Exception {
		try {
			return server.addClasspathListener(new ClasspathListener() {
				@Override
				public void changed(Event event) {
					log.debug("claspath event received {}", event);
					server.onInitialized(() -> {
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
									JavaProject newProject = new JavaProject(getFileObserver(), new URI(uri), new ClasspathData(event.name, event.classpath.getEntries()), JdtLsProjectCache.this);
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
			});
		} catch (Throwable t) {
			if (isNoJdtError(t)) {
				log.info("JDT Language Server not available. Fallback classpath provider will be used instead.");
			} else if (isOldJdt(t)) {
				log.info("JDT Lanuage Server too old. Fallback classpath provider will be used instead.");
			} else {
				log.error("Unexpected error registering classpath listener with JDT. Fallback classpath provider will be used instead.", t);
			}
			throw t;
		}
	}
}
