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

import org.junit.Assert;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServer;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServerParams;
import org.springframework.ide.vscode.boot.java.handlers.RunningAppProvider;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;

public class BootLanguageServerHarness extends LanguageServerHarness<BootJavaLanguageServer> {

	private PropertyIndexHarness indexHarness;
	private JavaProjectFinder projectFinder = (doc) -> getServer().getProjectFinder().find(doc);

	/**
	 * Creates a harness with a 'realistic' language server. I.e. no fake or mocked components.
	 */
	public BootLanguageServerHarness() throws Exception {
		super(() -> new BootJavaLanguageServer(BootJavaLanguageServerParams.createDefault()));
	}

	/**
	 * Creates a harness with custom (potentially mocked) params.
	 */
	public BootLanguageServerHarness(BootJavaLanguageServerParams params) throws Exception {
		super(() -> {
			return new BootJavaLanguageServer((server) -> params);
		});
		this.projectFinder = params.projectFinder;
	}


	public static BootLanguageServerHarness createMocked() throws Exception {
		PropertyIndexHarness indexHarness = new PropertyIndexHarness();
		BootLanguageServerHarness harness = new BootLanguageServerHarness(new BootJavaLanguageServerParams(indexHarness.getProjectFinder(), ProjectObserver.NULL, indexHarness.getIndexProvider(), RunningAppProvider.DEFAULT));
		harness.indexHarness = indexHarness;
		return harness;
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
