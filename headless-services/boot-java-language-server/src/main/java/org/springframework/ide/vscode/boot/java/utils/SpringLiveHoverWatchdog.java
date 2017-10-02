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

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentSkipListSet;

import org.eclipse.lsp4j.Range;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServer;
import org.springframework.ide.vscode.boot.java.handlers.BootJavaHoverProvider;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
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

	private final Timer timer;

	public SpringLiveHoverWatchdog(SimpleLanguageServer server, BootJavaHoverProvider hoverProvider) {
		this.server = server;
		this.hoverProvider = hoverProvider;
		this.watchedDocs = new ConcurrentSkipListSet<>();

		this.timer = new Timer();
	}

	public void start() {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				update();
			}
		};

		timer.scheduleAtFixedRate(task, 0, POLLING_INTERVAL_MILLISECONDS);
	}

	public void shutdown() {
		timer.cancel();
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
		try {
			SpringBootApp[] runningBootApps = SpringBootApp.getAllRunningSpringApps().values().stream()
					.filter((app) -> !app.containsSystemProperty(BootJavaLanguageServer.LANGUAGE_SERVER_PROCESS_PROPERTY))
					.toArray(SpringBootApp[]::new);

			TextDocument doc = this.server.getTextDocumentService().get(docURI);
			if (doc != null) {
				Range[] ranges = this.hoverProvider.getLiveHoverHints(doc, runningBootApps);
				publishLiveHints(ranges);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void update() {
		if (this.watchedDocs.size() > 0) {
			for (String docURI : watchedDocs) {
				update(docURI);
			}
		}
	}

	private void publishLiveHints(Range[] ranges) {
		for (Range range : ranges) {
			int startLine = range.getStart().getLine();
			int startChar = range.getStart().getCharacter();
			int endLine = range.getEnd().getLine();
			int endChar = range.getEnd().getCharacter();

			System.out.println("live hover information at: " + startLine + ":" + startChar + " - " + endLine + ":" + endChar);
		}
	}

	private void cleanupLiveHints(String docURI) {
		// TODO: send client a message to cleanup live hover diagnostics data
	}

	private void cleanupResources() {
		// TODO: close and cleanup open JMX connections and cached data
	}

}
