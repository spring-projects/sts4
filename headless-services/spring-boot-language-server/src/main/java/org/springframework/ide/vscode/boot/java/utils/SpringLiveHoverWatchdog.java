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

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.handlers.BootJavaHoverProvider;
import org.springframework.ide.vscode.boot.java.handlers.RunningAppMatcher;
import org.springframework.ide.vscode.boot.java.handlers.RunningAppProvider;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.protocol.HighlightParams;
import org.springframework.ide.vscode.commons.util.MemoizingProxy;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

/**
 * @author Martin Lippert
 */
public class SpringLiveHoverWatchdog {

	public static final Duration DEFAULT_INTERVAL = Duration.ofMillis(5000);

	Logger logger = LoggerFactory.getLogger(SpringLiveHoverWatchdog.class);

	private final long POLLING_INTERVAL_MILLISECONDS;

	private final SimpleLanguageServer server;
	private final BootJavaHoverProvider hoverProvider;
	private final RunningAppProvider runningAppProvider;

	private boolean highlightsEnabled = true;
	private ScheduledThreadPoolExecutor timer;
	private JavaProjectFinder projectFinder;
	private final Map<String, AtomicReference<IJavaProject>> watchedDocs;


	public SpringLiveHoverWatchdog(
			SimpleLanguageServer server,
			BootJavaHoverProvider hoverProvider,
			RunningAppProvider runningAppProvider,
			JavaProjectFinder projectFinder,
			ProjectObserver projectChanges,
			Duration pollingInterval
	) {
		this.POLLING_INTERVAL_MILLISECONDS = pollingInterval == null ? DEFAULT_INTERVAL.toMillis() : pollingInterval.toMillis();
		this.server = server;
		this.hoverProvider = hoverProvider;
		this.runningAppProvider = runningAppProvider;
		this.projectFinder = projectFinder;
		this.watchedDocs = new ConcurrentHashMap<>();

		projectChanges.addListener(new ProjectObserver.Listener() {

			@Override
			public void deleted(IJavaProject project) {
				logger.info("project deleted event: {}", project.getElementName());
				refreshEnablement();
			}

			@Override
			public void created(IJavaProject project) {
				logger.info("project created event: {}", project.getElementName());
				refreshEnablement();
			}

			@Override
			public void changed(IJavaProject project) {
				logger.info("project changed event: {}", project.getElementName());
				refreshEnablement();
			}
		});
	}

	public synchronized void enableHighlights() {
		if (!highlightsEnabled) {
			highlightsEnabled = true;
			refreshEnablement();
		}
	}

	public synchronized void disableHighlights() {
		if (highlightsEnabled) {
			highlightsEnabled = false;
			refreshEnablement();
		}
	}

	private synchronized void start() {
		if (highlightsEnabled && timer == null) {
			logger.debug("Starting SpringLiveHoverWatchdog");
			this.timer = new ScheduledThreadPoolExecutor(1);
			this.timer.scheduleWithFixedDelay(() -> this.update(), 0, POLLING_INTERVAL_MILLISECONDS, TimeUnit.MILLISECONDS);
		}
	}

	public synchronized void shutdown() {
		if (timer != null) {
			logger.info("Shutting down SpringLiveHoverWatchdog");
			timer.shutdown();
			timer = null;
			watchedDocs.keySet().forEach(uri -> cleanupLiveHints(uri));
		}
	}

	public synchronized void watchDocument(String docURI) {
		this.watchedDocs.putIfAbsent(docURI, new AtomicReference<IJavaProject>());
		refreshEnablement();
	}

	public synchronized void unwatchDocument(String docURI) {
		this.watchedDocs.remove(docURI);
		cleanupLiveHints(docURI);

		if (watchedDocs.size() == 0) {
			cleanupResources();
		}
		refreshEnablement();
	}

	public void update(String docURI) {
		ScheduledThreadPoolExecutor scheduler = this.timer;
		if (scheduler != null) {
			scheduler.execute(() -> {
				updateDoc(docURI);
			});
		}
	}

	// internal method, need to run on the scheduled executor pool, do not call outside of that
	protected void updateDoc(String docURI) {
		try {
			IJavaProject project = getCachedProject(docURI);
			SpringBootApp[] runningBootApps = RunningAppMatcher.getAllMatchingApps(runningAppProvider.getAllRunningSpringApps(), project).toArray(new SpringBootApp[0]);
			update(docURI, project, runningBootApps);
		}
		catch (Exception e) {
			logger.error("", e);
		}
	}

	// internal method, need to run on the scheduled executor pool, do not call outside of that
	protected void update() {
		if (this.watchedDocs.size() > 0) {
			try {
				Collection<SpringBootApp> runningBootApps = runningAppProvider.getAllRunningSpringApps();
				Collection<SpringBootApp> cachedApps = createAppCaches(runningBootApps);

				for (String docURI : watchedDocs.keySet()) {
					IJavaProject project = getCachedProject(docURI);
					SpringBootApp[] matchingApps = RunningAppMatcher.getAllMatchingApps(cachedApps, project).toArray(new SpringBootApp[0]);
					update(docURI, project, matchingApps);
				}

				this.hoverProvider.setRunningSpringApps(cachedApps);

			} catch (Exception e) {
				logger.error("", e);
			}
		}
	}

	// internal method, need to run on the scheduled executor pool, do not call outside of that
	protected void update(String docURI, IJavaProject project, SpringBootApp[] runningBootApps) {
		if (highlightsEnabled) {
			try {
				boolean hasCurrentRunningBootApps = runningBootApps != null && runningBootApps.length > 0;
				if (hasCurrentRunningBootApps) {
					TextDocument doc = this.server.getTextDocumentService().get(docURI);
					if (doc != null) {
						CodeLens[] infos = this.hoverProvider.getLiveHoverHints(doc, project, runningBootApps);
						publishLiveHints(docURI, infos);
					}
				}
				else  {
					cleanupLiveHints(docURI);
				}
			} catch (Exception e) {
				logger.error("", e);
			}
		}
	}

	private Collection<SpringBootApp> createAppCaches(Collection<SpringBootApp> runningBootApps) {
		return runningBootApps.stream().map(app -> {
		    MethodInterceptor handler = new MemoizingProxy.MemoizingProxyHandler(app, Duration.ofMillis(20000));
		    SpringBootApp proxied = (SpringBootApp) Enhancer.create(SpringBootApp.class, handler);

		    try {
		    	proxied.getProcessName();
		    	proxied.getProcessID();
		    }
		    catch (Exception e) {
		    }

		    return proxied;
		}).filter(app -> app != null).collect(Collectors.toList());
	}

	private void refreshEnablement() {
		boolean shouldEnable = highlightsEnabled && hasInterestingProject(watchedDocs.keySet().stream());
		if (shouldEnable) {
			start();
		} else {
			shutdown();
		}
	}

	private boolean hasInterestingProject(Stream<String> uris) {
		return uris.anyMatch(uri -> projectFinder.find(new TextDocumentIdentifier(uri)).isPresent());
	}

	private IJavaProject getCachedProject(String docURI) {
		AtomicReference<IJavaProject> reference = this.watchedDocs.get(docURI);
		IJavaProject project = reference.get();
		if (project == null) {
			project = identifyProject(docURI);
			if (!reference.compareAndSet(null, project)) {
				return reference.get();
			}
		}
		return project;
	}

	private IJavaProject identifyProject(String docURI) {
		TextDocument doc = this.server.getTextDocumentService().get(docURI);
		if (doc != null) {
			return projectFinder.find(doc.getId()).orElse(null);
		}
		else {
			return null;
		}
	}

	private void publishLiveHints(String docURI, CodeLens[] codeLenses) {
		int version = server.getTextDocumentService().get(docURI).getVersion();
		VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier(docURI, version);
		server.getClient().highlight(new HighlightParams(id, Arrays.asList(codeLenses)));
	}

	private void cleanupLiveHints(String docURI) {
		publishLiveHints(docURI, new CodeLens[0]);
	}

	private void cleanupResources() {
		// TODO: close and cleanup open JMX connections and cached data
	}

}
