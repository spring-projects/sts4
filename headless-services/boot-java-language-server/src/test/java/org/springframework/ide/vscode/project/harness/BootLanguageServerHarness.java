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

import java.util.concurrent.Callable;

import org.junit.Assert;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServer;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServerParams;
import org.springframework.ide.vscode.boot.java.handlers.RunningAppProvider;
import org.springframework.ide.vscode.boot.metadata.SpringPropertyIndexProvider;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.util.LSFactory;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;

public class BootLanguageServerHarness extends LanguageServerHarness<BootJavaLanguageServer> {

	private PropertyIndexHarness indexHarness;
	private final JavaProjectFinder projectFinder = (doc) -> getServer().getProjectFinder().find(doc);

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

		LSFactory<BootJavaLanguageServerParams> defaultsFactory = BootJavaLanguageServerParams.createDefault();
		private JavaProjectFinder projectFinder = null;
		private ProjectObserver projectObserver = null;
		private SpringPropertyIndexProvider indexProvider = null;
		private RunningAppProvider runningAppProvider = null;
		private PropertyIndexHarness indexHarness = null;

		public BootLanguageServerHarness build() throws Exception {
			BootLanguageServerHarness harness = new BootLanguageServerHarness(this);
			return harness;
		}

		public Builder mockDefaults() {
			indexHarness = new PropertyIndexHarness();
			projectFinder = indexHarness.getProjectFinder();
			indexProvider = indexHarness.getIndexProvider();
			projectObserver = ProjectObserver.NULL;
			runningAppProvider = RunningAppProvider.NULL;
			return this;
		}

		public Builder runningAppProvider(RunningAppProvider provider) {
			this.runningAppProvider = provider;
			return this;
		}

		public Builder projectFinder(JavaProjectFinder projectFinder) {
			this.projectFinder = projectFinder;
			return this;
		}
	}

	/**
	 * This constructor is private. Use the builder api instead.
	 */
	private BootLanguageServerHarness(Builder builder) throws Exception {
		super(() -> {
			LSFactory<BootJavaLanguageServerParams> params = (server) -> {
				BootJavaLanguageServerParams defaults = BootJavaLanguageServerParams.createDefault().create(server);
				return new BootJavaLanguageServerParams(
						builder.projectFinder==null?defaults.projectFinder:builder.projectFinder,
						builder.projectObserver==null?defaults.projectObserver:builder.projectObserver,
						builder.indexProvider==null?defaults.indexProvider:builder.indexProvider,
						builder.runningAppProvider==null?defaults.runningAppProvider:builder.runningAppProvider
				);
			};
			return new BootJavaLanguageServer(params);
		});
		this.indexHarness = builder.indexHarness;
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
}
