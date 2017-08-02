/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.bosh;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.junit.Test;
import org.springframework.ide.vscode.bosh.mocks.MockCloudConfigProvider;
import org.springframework.ide.vscode.bosh.models.DynamicModelProvider;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;

@SuppressWarnings("unchecked")
public class BoshLanguageServerTest {

	public static File getTestResource(String name) throws URISyntaxException {
		return Paths.get(BoshLanguageServerTest.class.getResource(name).toURI()).toFile();
	}

	private BoshCliConfig cliConfig = new BoshCliConfig();

	@Test
	public void createAndInitializeServerWithWorkspace() throws Exception {
		LanguageServerHarness harness = new LanguageServerHarness(() ->
			new BoshLanguageServer(cliConfig, new MockCloudConfigProvider(cliConfig), mock(DynamicModelProvider.class), mock(DynamicModelProvider.class))
		);
		File workspaceRoot = getTestResource("/workspace/");
		assertExpectedInitResult(harness.intialize(workspaceRoot));
	}

	@Test
	public void createAndInitializeServerWithoutWorkspace() throws Exception {
		File workspaceRoot = null;
		LanguageServerHarness harness = new LanguageServerHarness(() ->
			new BoshLanguageServer(cliConfig, new MockCloudConfigProvider(cliConfig), mock(DynamicModelProvider.class), mock(DynamicModelProvider.class))
		);
		assertExpectedInitResult(harness.intialize(workspaceRoot));
	}

	private void assertExpectedInitResult(InitializeResult initResult) {
		assertThat(initResult.getCapabilities().getCompletionProvider().getResolveProvider()).isTrue();
		assertThat(initResult.getCapabilities().getTextDocumentSync().getLeft()).isEqualTo(TextDocumentSyncKind.Incremental);
	}

}
