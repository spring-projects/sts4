/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import java.util.Arrays;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentSkipListSet;

import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServer;
import org.springframework.ide.vscode.boot.java.handlers.BootJavaHoverProvider;
import org.springframework.ide.vscode.boot.java.handlers.RunningAppProvider;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.languageserver.HighlightParams;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class SpringLiveHoverWatchdog {

	private static final int POLLING_INTERVAL_MILLISECONDS = 5000;

	private final Set<String> watchedDocs;
	private final SimpleLanguageServer server;
	private final BootJavaHoverProvider hoverProvider;
	private RunningAppProvider runningAppProvider;

	private boolean highlightsEnabled = true;

	private Timer timer;


	public SpringLiveHoverWatchdog(SimpleLanguageServer server, BootJavaHoverProvider hoverProvider, RunningAppProvider runningAppProvider) {
		this.server = server;
		this.hoverProvider = hoverProvider;
		this.runningAppProvider = runningAppProvider;
		this.watchedDocs = new ConcurrentSkipListSet<>();
	}

	public synchronized void start() {
		if (highlightsEnabled && timer == null) {
			this.timer = new Timer();

			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					update();
				}
			};

			timer.scheduleAtFixedRate(task, 0, POLLING_INTERVAL_MILLISECONDS);
		}
	}

	public synchronized void shutdown() {
		if (timer != null) {
			timer.cancel();
			timer = null;
			watchedDocs.forEach(uri -> cleanupLiveHints(uri));
		}
	}

	public void watchDocument(String docURI) {
		this.watchedDocs.add(docURI);
	}

	public void unwatchDocument(String docURI) {
		this.watchedDocs.remove(docURI);
		cleanupLiveHints(docURI);

		if (watchedDocs.size() == 0) {
			cleanupResources();
		}
	}

	public void update(String docURI) {
		if (highlightsEnabled) {
			try {
				SpringBootApp[] runningBootApps = runningAppProvider.getAllRunningSpringApps().stream()
						.filter((app) -> !app.containsSystemProperty(BootJavaLanguageServer.LANGUAGE_SERVER_PROCESS_PROPERTY))
						.toArray(SpringBootApp[]::new);

				TextDocument doc = this.server.getTextDocumentService().get(docURI);
				if (doc != null) {
					Range[] ranges = this.hoverProvider.getLiveHoverHints(doc, runningBootApps);
					publishLiveHints(docURI, ranges);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected void update() {
		if (this.watchedDocs.size() > 0) {
			for (String docURI : watchedDocs) {
				update(docURI);
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
			start();
		}
	}

	public synchronized void disableHighlights() {
		if (highlightsEnabled) {
			highlightsEnabled = false;
			shutdown();
		}
	}

}
