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
package org.springframework.ide.vscode.project.harness;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.springframework.ide.vscode.boot.app.BootLanguageServerParams;
import org.springframework.ide.vscode.boot.editor.harness.PropertyIndexHarness;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.protocol.java.Classpath;
import org.springframework.ide.vscode.commons.protocol.java.Classpath.CPE;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;

public class BootLanguageServerHarness extends LanguageServerHarness {

	private final PropertyIndexHarness indexHarness;
	private final JavaProjectFinder projectFinder;
	private final BootLanguageServerParams serverParams;
	private final String defaultFileExtension;

//	/**
//	 * Creates a builder and initializes it so that it sets up a test harness with
//	 * the 'real stuff'. I.e project finder and other injected components are like
//	 * they would be in 'production' environment.
//	 * <p>
//	 * Builder methods can still be called to replace some of the components with
//	 * mocks selectively.
//	 */
//	public static Builder builder() {
//		return new Builder();
//	}
//
//	public static class Builder {
//
//		LSFactory<BootLanguageServerParams> defaultsFactory = BootLanguageServerParams.createTestDefault();
//		private JavaProjectFinder projectFinder = null;
//		private ProjectObserver projectObserver = null;
//		private SpringPropertyIndexProvider indexProvider = null;
//		private SpringPropertyIndexProvider adHocIndexProvider = null;
//		private RunningAppProvider runningAppProvider = null;
//		private PropertyIndexHarness indexHarness = null;
//		private Duration watchDogInterval = null;
//		private TypeUtilProvider typeUtilProvider = null;
//
//		public BootJavaLanguageServerHarness build() throws Exception {
//			BootJavaLanguageServerHarness harness = new BootJavaLanguageServerHarness(this);
//			return harness;
//		}
//
//		public Builder mockDefaults() {
//			indexHarness = new PropertyIndexHarness();
//			projectFinder = indexHarness.getProjectFinder();
//			indexProvider = indexHarness.getIndexProvider();
//			adHocIndexProvider = indexHarness.adHocIndexProvider;
//			projectObserver = ProjectObserver.NULL;
//			runningAppProvider = RunningAppProvider.NULL;
//			return this;
//		}
//
//		public Builder runningAppProvider(RunningAppProvider provider) {
//			this.runningAppProvider = () -> provider.getAllRunningSpringApps();
//			return this;
//		}
//
//		public Builder projectFinder(JavaProjectFinder projectFinder) {
//			this.projectFinder = projectFinder;
//			return this;
//		}
//
//		public Builder propertyIndexProvider(SpringPropertyIndexProvider propertyIndexProvider) {
//			this.indexProvider = propertyIndexProvider;
//			return this;
//		}
//
//		public Builder watchDogInterval(Duration watchDogInterval) {
//			this.watchDogInterval = watchDogInterval;
//			return this;
//		}
//	}

	/**
	 * This constructor is private. Use the builder api instead.
	 * @param projectFinder
	 */
	public BootLanguageServerHarness(
			SimpleLanguageServer server,
			BootLanguageServerParams serverParams,
			PropertyIndexHarness indexHarness,
			JavaProjectFinder projectFinder,
			LanguageId defaultLanguageId,
			String defaultFileExtension
	) throws Exception {
		super(server, defaultLanguageId);
		this.serverParams = serverParams;
		this.indexHarness = indexHarness;
		this.projectFinder = projectFinder;
		this.defaultFileExtension = defaultFileExtension;
	}

	public BootLanguageServerParams getServerParams() {
		return serverParams;
	}

	@Override
	protected String getFileExtension() {
		return defaultFileExtension;
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
