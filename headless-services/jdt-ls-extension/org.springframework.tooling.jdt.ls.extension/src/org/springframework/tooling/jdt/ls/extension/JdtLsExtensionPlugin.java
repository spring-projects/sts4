/*******************************************************************************
 * Copyright (c) 2022, 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.jdt.ls.extension;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.osgi.framework.BundleContext;
import org.springframework.tooling.jdt.ls.commons.BootProjectTracker;
import org.springframework.tooling.jdt.ls.commons.Logger;

public class JdtLsExtensionPlugin extends Plugin {

	private boolean bootProjectPresent = false;
	
	private static JdtLsExtensionPlugin instance = null;

	@Override
	public void start(BundleContext context) throws Exception {
		instance = this;
		super.start(context);
		Logger logger = Logger.forEclipsePlugin(() -> JdtLsExtensionPlugin.getInstance());
		initializationFuture().thenAccept(v -> {
			Consumer<Set<IJavaProject>> l = bootProjects -> {
				boolean currentBootProjectsPresent = !bootProjects.isEmpty();
				if (bootProjectPresent != currentBootProjectsPresent) {
					bootProjectPresent = currentBootProjectsPresent;
					try {
						if (bootProjectPresent) {
							logger.log("About to START Boot LS since Boot project has been detected in the workspace");
							JavaLanguageServerPlugin.getInstance().getClientConnection()
									.executeClientCommand("vscode-spring-boot.ls.start");
							logger.log("Boot LS START command executed successfully");
						} else {
							logger.log("About to STOP Boot LS since no Boot projects have been detected in the workspace");
							JavaLanguageServerPlugin.getInstance().getClientConnection()
									.executeClientCommand("vscode-spring-boot.ls.stop");
							logger.log("Boot LS STOP command executed successfully");
						}
					} catch (Exception e) {
						logger.log(e);
					}
				}
			};
			new BootProjectTracker(logger, Arrays.asList(l));
		});

	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		instance = null;
	}

	private CompletableFuture<Void> initializationFuture() {
		CompletableFuture<Void> initFuture = new CompletableFuture<>();
		Job.getJobManager().addJobChangeListener(new IJobChangeListener() {

			@Override
			public void aboutToRun(IJobChangeEvent event) {
			}

			@Override
			public void awake(IJobChangeEvent event) {
			}

			@Override
			public void done(IJobChangeEvent event) {
				if (event.getJob().belongsTo(
						org.eclipse.jdt.ls.core.internal.handlers.BaseInitHandler.JAVA_LS_INITIALIZATION_JOBS)) {
					initFuture.complete(null);
					Job.getJobManager().removeJobChangeListener(this);
				}
			}

			@Override
			public void running(IJobChangeEvent event) {
			}

			@Override
			public void scheduled(IJobChangeEvent event) {
			}

			@Override
			public void sleeping(IJobChangeEvent event) {
			}

		});
		return initFuture;
	}
	
	static JdtLsExtensionPlugin getInstance() {
		return instance;
	}

}
