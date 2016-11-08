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
package org.springframework.ide.vscode.application.properties.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageServer;
import org.junit.Test;
import org.springframework.ide.vscode.application.properties.ApplicationPropertiesLanguageServer;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;

/**
 * Boot app properties file language server tests
 * 
 * @author Alex Boyko
 *
 */
public class ApplicationPropertiesLanguageServerTest {

	public static File getTestResource(String name) throws URISyntaxException {
		return Paths.get(ApplicationPropertiesLanguageServer.class.getResource(name).toURI()).toFile();
	}

	private LanguageServerHarness newHarness() throws Exception {
		Callable<? extends LanguageServer> f = () -> new ApplicationPropertiesLanguageServer((d) -> null, (d) -> null);
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
		assertThat(initResult.getCapabilities().getTextDocumentSync()).isEqualTo(TextDocumentSyncKind.Full);
	}

}
