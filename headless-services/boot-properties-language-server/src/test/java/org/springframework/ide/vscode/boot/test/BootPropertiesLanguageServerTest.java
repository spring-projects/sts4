/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.junit.Test;
import org.springframework.ide.vscode.boot.BootPropertiesLanguageServer;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.AbstractJavaProjectManager;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectManager;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;

/**
 * Boot app properties file language server tests
 *
 * @author Alex Boyko
 *
 */
public class BootPropertiesLanguageServerTest {

	public static File getTestResource(String name) throws URISyntaxException {
		return Paths.get(BootPropertiesLanguageServer.class.getResource(name).toURI()).toFile();
	}

	private LanguageServerHarness newHarness() throws Exception {
		JavaProjectManager nullJavaProjectFinder = new AbstractJavaProjectManager() {
			@Override
			public boolean isProjectRoot(File file) {
				return false;
			}
			@Override
			public IJavaProject find(File file) {
				return null;
			}
			@Override
			public IJavaProject find(IDocument doc) {
				return null;
			}
		};
		
		
		Callable<? extends SimpleLanguageServer> f = () -> new BootPropertiesLanguageServer((d) -> null, (d) -> null, nullJavaProjectFinder);
		return new LanguageServerHarness(f);
	}

	@Test
	public void createAndInitializeServerWithWorkspace() throws Exception {
		LanguageServerHarness harness = newHarness();
		File workspaceRoot = getTestResource("/workspace/");
		assertExpectedInitResult(harness.intialize(workspaceRoot));
	}

	@Test
	public void createAndInitializeServerWithoutWorkspace() throws Exception {
		File workspaceRoot = null;
		LanguageServerHarness harness = newHarness();
		assertExpectedInitResult(harness.intialize(workspaceRoot));
	}

	private void assertExpectedInitResult(InitializeResult initResult) {
		assertThat(initResult.getCapabilities().getTextDocumentSync().getLeft()).isEqualTo(TextDocumentSyncKind.Incremental);
	}

}
