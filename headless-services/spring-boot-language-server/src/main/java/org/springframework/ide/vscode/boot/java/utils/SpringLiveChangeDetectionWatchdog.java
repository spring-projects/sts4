/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServerComponents;
import org.springframework.ide.vscode.boot.java.links.SourceLinks;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver.Listener;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;

/**
 * @author Martin Lippert
 */
public class SpringLiveChangeDetectionWatchdog {

	public static final Duration DEFAULT_INTERVAL = Duration.ofMillis(5000);

	Logger logger = LoggerFactory.getLogger(SpringLiveChangeDetectionWatchdog.class);

	private final long POLLING_INTERVAL_MILLISECONDS;

	private final SimpleLanguageServer server;
	private final SourceLinks sourceLinks;

	private final ChangeDetectionHistory changeHistory;
	private final Set<IJavaProject> observedProjects;

	private boolean changeDetectionEnabled = false;
	private ScheduledThreadPoolExecutor timer;

	public SpringLiveChangeDetectionWatchdog(
			BootJavaLanguageServerComponents bootJavaLanguageServerComponents,
			SimpleLanguageServer server,
			ProjectObserver projectObserver,
			JavaProjectFinder projectFinder,
			Duration pollingInterval,
			SourceLinks sourceLinks
	) {
		this.observedProjects = new HashSet<>();

		this.server = server;

		this.POLLING_INTERVAL_MILLISECONDS = pollingInterval == null ? DEFAULT_INTERVAL.toMillis() : pollingInterval.toMillis();

		this.changeHistory = new ChangeDetectionHistory();
		this.sourceLinks = sourceLinks;

		if (projectObserver != null) {
			projectObserver.addListener(new Listener() {

				@Override
				public void deleted(IJavaProject project) {
					observedProjects.remove(project);
				}

				@Override
				public void created(IJavaProject project) {
					observedProjects.add(project);
				}

				@Override
				public void changed(IJavaProject project) {
					// do nothing
				}
			});
		}
	}

	public synchronized void start() {
		if (changeDetectionEnabled && timer == null) {
			logger.debug("Starting SpringLiveChangeDetectionWatchdog");
			this.timer = new ScheduledThreadPoolExecutor(1);
			this.timer.scheduleWithFixedDelay(() -> this.update(), 0, POLLING_INTERVAL_MILLISECONDS, TimeUnit.MILLISECONDS);
		}
	}

	public synchronized void shutdown() {
		if (timer != null) {
			logger.info("Shutting down SpringLiveChangeDetectionWatchdog");
			timer.shutdown();
			timer = null;
		}
	}

	public void update() {
		if (changeDetectionEnabled) {
//			try {
//				SpringBootApp[] runningBootApps = runningAppProvider.getAllRunningSpringApps().toArray(new SpringBootApp[0]);
//				Change[] changes = changeHistory.checkForChanges(runningBootApps);
//				if (changes != null && changes.length > 0) {
//					for (Change change : changes) {
//						publishDetectedChange(change);
//					}
//				}
//				for (SpringBootApp app : runningBootApps) {
//					updateApp(app);
//				}
//			} catch (Exception e) {
//				logger.error("", e);
//			}
		}
	}

	private void publishDetectedChange(Change change) {
//		Map<String, List<Diagnostic>> diagnostics = new HashMap<>();
//
//		IJavaProject[] projects = findProjectsFor(change.getRunningApp());
//
//		List<LiveBean> deletedBeans = change.getDeletedBeans();
//		if (deletedBeans != null) {
//			for (LiveBean liveBean : deletedBeans) {
//				Diagnostic diag = new Diagnostic();
//				diag.setSeverity(DiagnosticSeverity.Information);
//				diag.setRange(new Range(new Position(0, 0), new Position(0, 0)));
//				diag.setSource("Spring Boot Change Detection Mechanism");
//
//				diag.setMessage("bean removed from app: " + liveBean.getId());
//
//				String docURI = getDocURI(liveBean, projects);
//				if (docURI != null) {
//					List<Diagnostic> diags = diagnostics.computeIfAbsent(docURI, (s) -> new ArrayList<>());
//					diags.add(diag);
//				}
//				else {
//					logger.info("deleted bean could not be associated with a doc URI: " + liveBean.getId());
//				}
//			}
//		}
//
//		List<LiveBean> newBeans = change.getNewBeans();
//		if (newBeans != null) {
//			for (LiveBean liveBean : newBeans) {
//				Diagnostic diag = new Diagnostic();
//				diag.setSeverity(DiagnosticSeverity.Information);
//				diag.setRange(new Range(new Position(0, 0), new Position(0, 0)));
//				diag.setSource("Spring Boot Change Detection Mechanism");
//
//				diag.setMessage("new bean detected: " + liveBean.getId());
//
//				String docURI = getDocURI(liveBean, projects);
//				if (docURI != null) {
//					List<Diagnostic> diags = diagnostics.computeIfAbsent(docURI, (s) -> new ArrayList<>());
//					diags.add(diag);
//				}
//				else {
//					logger.info("new bean could not be associated with a doc URI: " + liveBean.getId());
//				}
//			}
//		}
//
//		for (String docURI : diagnostics.keySet()) {
//			PublishDiagnosticsParams params = new PublishDiagnosticsParams(docURI, diagnostics.get(docURI));
//			server.getClient().publishDiagnostics(params);
//		}

	}

//	private IJavaProject[] findProjectsFor(SpringBootApp app) {
//		List<IJavaProject> result = new ArrayList<>();
//
//		try {
//			Set<String> runningClasspath = new HashSet<>();
//			Collections.addAll(runningClasspath, app.getClasspath());
//
//			for (IJavaProject project : this.observedProjects) {
//				if (RunningAppMatcher.doesClasspathMatch(runningClasspath, project)) {
//					result.add(project);
//					break;
//				}
//			}
//		}
//		catch (Exception e) {
//			logger.error("find projects failed with: ", e);
//		}
//
//		return (IJavaProject[]) result.toArray(new IJavaProject[result.size()]);
//	}
//
//	private String getDocURI(LiveBean liveBean, IJavaProject[] projects) {
//		String result = null;
//
//		String resource = liveBean.getResource();
//		if (resource != null) {
//
//			Pattern BRACKETS = Pattern.compile("\\[[^\\]]*\\]");
//
//			Matcher matcher = BRACKETS.matcher(resource);
//			if (matcher.find()) {
//				String type = resource.substring(0, matcher.start()).trim();
//				String path = resource.substring(matcher.start()+1, matcher.end()-1);
//
//				for (IJavaProject project : projects) {
//					if (SpringResource.FILE.equals(type) || SpringResource.URL.equals(type)) {
//						result = sourceLinks.sourceLinkUrlForClasspathResource(path).get();
//						if (result == null) {
//							result = sourceLinks.sourceLinkForResourcePath(Paths.get(path)).get();
//						}
//						break;
//					}
//					else if (SpringResource.CLASS_PATH_RESOURCE.equals(type)) {
//						int idx = path.lastIndexOf(SourceLinks.CLASS);
//						if (idx >= 0) {
//							Path p = Paths.get(path.substring(0, idx));
//							result = sourceLinks.sourceLinkUrlForFQName(project, p.toString().replace(File.separator, ".")).get();
//						}
//						break;
//					}
//				}
//			}
//
//			if (result != null) {
//				int position = result.lastIndexOf('#');
//				if (position > 0) {
//					result = result.substring(0, position);
//				}
//			}
//		}
//		return result;
//	}

	public synchronized void enableHighlights() {
		if (!changeDetectionEnabled) {
			changeDetectionEnabled = true;
			refreshEnablement();
		}
	}

	public synchronized void disableHighlights() {
		if (changeDetectionEnabled) {
			changeDetectionEnabled = false;
			refreshEnablement();
		}
	}

	private void refreshEnablement() {
		if (changeDetectionEnabled) {
			start();
		} else {
			shutdown();
		}
	}

}
