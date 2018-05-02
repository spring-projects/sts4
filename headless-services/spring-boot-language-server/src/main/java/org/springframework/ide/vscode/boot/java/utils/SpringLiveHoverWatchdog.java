/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
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
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Stream;

import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.handlers.BootJavaHoverProvider;
import org.springframework.ide.vscode.boot.java.handlers.RunningAppProvider;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.HighlightParams;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class SpringLiveHoverWatchdog {


	public static final Duration DEFAULT_INTERVAL = Duration.ofMillis(5000);
	
	Logger logger = LoggerFactory.getLogger(SpringLiveHoverWatchdog.class);

	private final long POLLING_INTERVAL_MILLISECONDS;
	private final Set<String> watchedDocs;
	private final SimpleLanguageServer server;
	private final BootJavaHoverProvider hoverProvider;
	private RunningAppProvider runningAppProvider;

	private boolean highlightsEnabled = true;
//	private boolean hadPreviousRunningBootApps = false;

	private Timer timer;

	private JavaProjectFinder projectFinder;

	private void refreshEnablement() {
		boolean shouldEnable = highlightsEnabled && hasInterestingProject(watchedDocs.stream());
		if (shouldEnable) {
			start();
		} else {
			shutdown();
		}
	}

	private boolean hasInterestingProject(Stream<String> uris) {
		return uris.anyMatch(uri -> projectFinder.find(new TextDocumentIdentifier(uri)).isPresent());
	}

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
		this.watchedDocs = new ConcurrentSkipListSet<>();
		projectChanges.addListener(new ProjectObserver.Listener() {

			@Override
			public void deleted(IJavaProject project) {
				refreshEnablement();
			}

			@Override
			public void created(IJavaProject project) {
				refreshEnablement();
			}

			@Override
			public void changed(IJavaProject project) {
				refreshEnablement();
			}
		});
	}

	private synchronized void start() {
		if (highlightsEnabled && timer == null) {
			logger.debug("Starting SpringLiveHoverWatchdog");
			this.timer = new Timer();

			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					update();
				}
			};

			timer.schedule(task, 0, POLLING_INTERVAL_MILLISECONDS);
		}
	}

	public synchronized void shutdown() {
		if (timer != null) {
			logger.info("Shutting down SpringLiveHoverWatchdog");
			timer.cancel();
			timer = null;
			watchedDocs.forEach(uri -> cleanupLiveHints(uri));
		}
	}

	public synchronized void watchDocument(String docURI) {
		this.watchedDocs.add(docURI);
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

	public void update(String docURI, SpringBootApp[] runningBootApps) {
		if (highlightsEnabled) {

			try {
				if (runningBootApps == null) {
					runningBootApps = runningAppProvider.getAllRunningSpringApps().toArray(new SpringBootApp[0]);
				}

				boolean hasCurrentRunningBootApps = runningBootApps != null && runningBootApps.length > 0;
				if (hasCurrentRunningBootApps) {
					TextDocument doc = this.server.getTextDocumentService().get(docURI);
					if (doc != null) {
						Range[] ranges = this.hoverProvider.getLiveHoverHints(doc, runningBootApps);
						publishLiveHints(docURI, ranges);
					}
				}
				else 
//					if (this.hadPreviousRunningBootApps) 
					{
					// PT 156688501:
					// Only clean up live hovers if there were running boot apps in the previous update, but not
					// in the current one.
					// This is to avoid unnecessary publishing of live hovers when there have been no running apps
					// at all between consecutive updates.
					cleanupLiveHints(docURI);
				}
//				this.hadPreviousRunningBootApps = hasCurrentRunningBootApps;
			} catch (Exception e) {
				logger.error("", e);
			}
		}
	}

	protected void update() {
		if (this.watchedDocs.size() > 0) {
			try {
				SpringBootApp[] runningBootApps = runningAppProvider.getAllRunningSpringApps().toArray(new SpringBootApp[0]);
				for (String docURI : watchedDocs) {
					update(docURI, runningBootApps);
				}
			} catch (Exception e) {
				logger.error("", e);
			}
		}
	}

	private void publishLiveHints(String docURI, Range[] ranges) {
		server.getClient().highlight(new HighlightParams(new TextDocumentIdentifier(docURI), Arrays.asList(ranges)));
	}

	private void cleanupLiveHints(String docURI) {
		publishLiveHints(docURI, new Range[0]);
	}

	private void cleanupResources() {
		// TODO: close and cleanup open JMX connections and cached data
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

}
