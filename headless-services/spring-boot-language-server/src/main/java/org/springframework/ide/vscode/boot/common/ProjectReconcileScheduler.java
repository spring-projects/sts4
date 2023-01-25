/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.common;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public abstract class ProjectReconcileScheduler {
	
	private IJavaProjectReconcileEngine reconciler;
	private JavaProjectFinder projectFinder; 
	private long debounce;
	
	private Scheduler projectReconcileScheduler;
	private Map<URI, Disposable> projectReconcileRequests = new ConcurrentHashMap<>();
	private boolean started;

	public ProjectReconcileScheduler(IJavaProjectReconcileEngine reconciler, JavaProjectFinder projectFinder, long debounce, int numberOFthreads) {
		this.reconciler = reconciler;
		this.debounce = debounce;
		this.projectFinder = projectFinder;
		
		projectReconcileScheduler = Schedulers.newBoundedElastic(numberOFthreads, Integer.MAX_VALUE, "Project-Reconciler", 10);
	}
	
	public ProjectReconcileScheduler(IJavaProjectReconcileEngine reconciler, JavaProjectFinder projectFinder) {
		this(reconciler, projectFinder, 500, 1);
	}

	public final synchronized void start() {
		if (!started) {
			init();
			started = true;
		}
	}
	
	public final synchronized void stop() {
		if (started) {
			dispose();
			started = false;
		}
	}
	
	protected void init() {
		
	}
	
	protected void dispose() {
		for (IJavaProject p : projectFinder.all()) {
			unscheduleValidation(p);
			reconciler.clear(p);
		}
	}
	
	protected final void scheduleValidationForAllProjects() {
		for (IJavaProject p : projectFinder.all()) {
			scheduleValidation(p);
		}
	}
	
	protected final void scheduleValidation(IJavaProject project) {
		URI uri = project.getLocationUri();
		
		Disposable previousRequest = projectReconcileRequests.put(uri, Mono.delay(Duration.ofMillis(debounce))
				.publishOn(projectReconcileScheduler)
				.doOnSuccess(l -> {
					if (projectReconcileRequests.remove(uri) != null) {
						projectFinder.find(new TextDocumentIdentifier(uri.toASCIIString())).ifPresent(p -> {
							reconciler.clear(project);
							reconciler.reconcile(p);
						});
					}
				})
				.subscribe());
		// Dispose previous request to debounce project reconcile requests.
		if (previousRequest != null) {
			previousRequest.dispose();
		}
	}
	
	protected final void unscheduleValidation(IJavaProject project) {
		URI uri = project.getLocationUri();
		Disposable request = projectReconcileRequests.remove(uri);
		if (request != null) {
			request.dispose();
		}		
	}
	
	protected final void clear(IJavaProject project, boolean async) {
		/*
		 * TODO: Look at LanguageServerHarness to fix the deadlock that occurs every 2 second time maven build is ran
		 * If #clear(IJavaProject) is synchronous then the locked LanguageServerHarness instance is attempted to call publishDiagnostic()
		 * which is caused by the #clear(...) call. In the LS reality this will never happen as #publishDiagnsotics() is always a future
		 */
		if (async) {
			Mono.fromRunnable(() -> reconciler.clear(project))
				.publishOn(projectReconcileScheduler)
				.subscribe();
		} else {
			reconciler.clear(project);
		}
	}

	public JavaProjectFinder getProjectFinder() {
		return projectFinder;
	}

}
