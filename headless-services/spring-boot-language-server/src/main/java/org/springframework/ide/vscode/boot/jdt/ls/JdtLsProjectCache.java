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
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.jandex.JandexClasspath;
import org.springframework.ide.vscode.commons.java.ClasspathData;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.IJavadocProvider;
import org.springframework.ide.vscode.commons.languageserver.jdt.ls.Classpath;
import org.springframework.ide.vscode.commons.languageserver.jdt.ls.Classpath.CPE;
import org.springframework.ide.vscode.commons.languageserver.jdt.ls.ClasspathListener;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.Assert;
import org.springframework.ide.vscode.commons.util.CollectorUtil;
import org.springframework.ide.vscode.commons.util.ExceptionUtil;
import org.springframework.ide.vscode.commons.util.UriUtil;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import reactor.core.Disposable;

public class JdtLsProjectCache implements JavaProjectsService {
	
	private CompletableFuture<Void> initialized = new CompletableFuture<Void>();

	private SimpleLanguageServer server;
	private Map<String, JdtLsProject> table = new HashMap<String, JdtLsProject>();
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
						initialized.thenRun(() -> {
							synchronized (table) {
								String uri = UriUtil.normalize(event.projectUri);
								if (event.deleted) {
									JdtLsProject deleted = table.remove(uri);
									notifyDelete(deleted);
								} else {
									JdtLsProject newProject = new JdtLsProject(event.name, uri, event.classpath);
									JdtLsProject oldProject = table.put(uri, newProject);
									if (oldProject != null) {
										notifyChanged(newProject);
									} else {
										notifyCreated(newProject);
									}
								}
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

	private void notifyCreated(JdtLsProject newProject) {
		logEvent("Created", newProject);
		synchronized (listeners) {
			for (Listener listener : listeners) {
				listener.created(newProject);
			}
		}
	}

	private void notifyDelete(JdtLsProject deleted) {
		logEvent("Deleted", deleted);
		synchronized (listeners) {
			for (Listener listener : listeners) {
				listener.deleted(deleted);
			}
		}
	}
	
	private void notifyChanged(JdtLsProject newProject) {
		logEvent("Changed", newProject);
		synchronized (listeners) {
			for (Listener listener : listeners) {
				listener.changed(newProject);
			}
		}
	}

	private void logEvent(String type, JdtLsProject newProject) {
		try {
			log.info("Project "+type+": " + newProject.getLocationUri());
			log.info("Classpath has "+newProject.getClasspath().getClasspathEntries().size()+" entries");
		} catch (Exception e) {
		}
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
				for (Entry<String, JdtLsProject> e : table.entrySet()) {
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
	
	private class JdtLsProject implements IJavaProject {

		private final JdtClasspath classpath;

		public JdtLsProject(String name, String projectUri, Classpath classpath) {
			this.classpath = new JdtClasspath(name, projectUri, classpath);
		}

		@Override
		public IClasspath getClasspath() {
			return classpath;
		}
		
		public String getLocationUri() {
			return classpath.projectUri;
		}

	}
	
	private class JdtClasspath extends JandexClasspath {

		private Classpath classpath;
		private String name;
		private String projectUri;


		public JdtClasspath(String name, String uri, Classpath classpath) {
			this.name = name;
			this.projectUri = uri;
			this.classpath = classpath;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public boolean exists() {
			return new File(projectUri).exists();
		}

		@Override
		public Path getOutputFolder() {
			return Paths.get(classpath.getDefaultOutputFolder());
		}

		@Override
		public Collection<CPE> getClasspathEntries() throws Exception {
			return classpath.getEntries();
		}

		@Override
		public ImmutableList<String> getClasspathResources() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ImmutableList<String> getSourceFolders() {
			ImmutableList.Builder<String> sourceEntries = ImmutableList.builder();
			try {
				for (CPE e : getClasspathEntries()) {
					if (Classpath.isSource(e)) {
						sourceEntries.add(e.getPath());
					}
				}
			} catch (Exception e) {
				log.error("", e);
			}
			return sourceEntries.build();

		}

		@Override
		public ClasspathData createClasspathData() throws Exception {
			// We should not be needing this as we dont use DelegatingCachedClasspath
			throw new UnsupportedOperationException("Not supported for JDT classpath: ");
		}

		@Override
		public Optional<URL> sourceContainer(File classpathResource) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected IJavadocProvider createHtmlJavdocProvider(File classpathResource) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}

}
