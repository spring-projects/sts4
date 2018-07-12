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
package org.springframework.ide.vscode.project.harness;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import org.junit.Assert;
import org.springframework.ide.vscode.boot.BootLanguageServer;
import org.springframework.ide.vscode.boot.BootLanguageServerParams;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServerComponents;
import org.springframework.ide.vscode.boot.java.handlers.ProjectAwareRunningAppProvider;
import org.springframework.ide.vscode.boot.java.handlers.RunningAppProvider;
import org.springframework.ide.vscode.boot.metadata.SpringPropertyIndexProvider;
import org.springframework.ide.vscode.boot.metadata.types.TypeUtilProvider;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.composable.ComposableLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.jdt.ls.Classpath;
import org.springframework.ide.vscode.commons.languageserver.jdt.ls.Classpath.CPE;
import org.springframework.ide.vscode.commons.languageserver.util.LSFactory;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;

public class BootJavaLanguageServerHarness extends LanguageServerHarness<ComposableLanguageServer<BootJavaLanguageServerComponents>> {

	private PropertyIndexHarness indexHarness;
	private final JavaProjectFinder projectFinder = (doc) -> getServerWrapper().getComponents().getProjectFinder().find(doc);

	/**
	 * Creates a builder and initializes it so that it sets up a test harness with
	 * the 'real stuff'. I.e project finder and other injected components are like
	 * they would be in 'production' environment.
	 * <p>
	 * Builder methods can still be called to replace some of the components with
	 * mocks selectively.
	 */
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		LSFactory<BootLanguageServerParams> defaultsFactory = BootLanguageServerParams.createTestDefault();
		private JavaProjectFinder projectFinder = null;
		private ProjectObserver projectObserver = null;
		private SpringPropertyIndexProvider indexProvider = null;
		private ProjectAwareRunningAppProvider runningAppProvider = null;
		private PropertyIndexHarness indexHarness = null;
		private Duration watchDogInterval = null;
		private TypeUtilProvider typeUtilProvider = null;

		public BootJavaLanguageServerHarness build() throws Exception {
			BootJavaLanguageServerHarness harness = new BootJavaLanguageServerHarness(this);
			return harness;
		}

		public Builder mockDefaults() {
			indexHarness = new PropertyIndexHarness();
			projectFinder = indexHarness.getProjectFinder();
			indexProvider = indexHarness.getIndexProvider();
			projectObserver = ProjectObserver.NULL;
			runningAppProvider = ProjectAwareRunningAppProvider.NULL;
			return this;
		}

		public Builder runningAppProvider(RunningAppProvider provider) {
			this.runningAppProvider = (project) -> provider.getAllRunningSpringApps();
			return this;
		}

		public Builder projectFinder(JavaProjectFinder projectFinder) {
			this.projectFinder = projectFinder;
			return this;
		}

		public Builder propertyIndexProvider(SpringPropertyIndexProvider propertyIndexProvider) {
			this.indexProvider = propertyIndexProvider;
			return this;
		}

		public Builder watchDogInterval(Duration watchDogInterval) {
			this.watchDogInterval = watchDogInterval;
			return this;
		}
	}

	/**
	 * This constructor is private. Use the builder api instead.
	 */
	private BootJavaLanguageServerHarness(Builder builder) throws Exception {
		super(() -> {
			LSFactory<BootLanguageServerParams> params = (server) -> {
				BootLanguageServerParams defaults = BootLanguageServerParams.createTestDefault().create(server);
				return new BootLanguageServerParams(
						builder.projectFinder==null?defaults.projectFinder:builder.projectFinder,
						builder.projectObserver==null?defaults.projectObserver:builder.projectObserver,
						builder.indexProvider==null?defaults.indexProvider:builder.indexProvider,
						builder.typeUtilProvider==null?defaults.typeUtilProvider:builder.typeUtilProvider,
						builder.runningAppProvider==null?defaults.runningAppProvider:builder.runningAppProvider,
						builder.watchDogInterval==null?defaults.watchDogInterval:builder.watchDogInterval
				);
			};
			return BootLanguageServer.createJava(params);
		});
		this.indexHarness = builder.indexHarness;
	}

	public BootLanguageServerParams getServerParams() {
		return getServerWrapper().getComponents().getServerParams();
	}



	@Override
	protected String getFileExtension() {
		return ".java";
	}

	public JavaProjectFinder getProjectFinder() {
		return projectFinder;
	}

	public PropertyIndexHarness getPropertyIndexHarness() {
		Assert.assertNotNull(indexHarness); //only supported in some types of instantations of the harness (i.e. when indexer is controlled by indexer harness.
		return indexHarness;
	}

	public void useProject(IJavaProject p) throws Exception {
		indexHarness.useProject(p);
	}

	public Path getOutputFolder() throws Exception {
		IClasspath classpath = getProjectFinder().find(null).get().getClasspath();
		for (CPE cpe : classpath.getClasspathEntries()) {
			if (Classpath.isSource(cpe)) {
				if (cpe.getPath().endsWith("main/java")) {
					return Paths.get(cpe.getOutputFolder());
				}
			}
		}
		return null;
	}

}
