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

package org.springframework.ide.vscode.concourse;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ide.vscode.concourse.ConcourseLanguageServer;
import org.springframework.ide.vscode.concourse.github.GithubInfoProvider;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;
import static org.springframework.ide.vscode.commons.yaml.completion.YamlCompletionEngineOptions.*;

public class ConcourseLanguageServerTest {

	public static File getTestResource(String name) throws URISyntaxException {
		return Paths.get(ConcourseLanguageServerTest.class.getResource(name).toURI()).toFile();
	}

	@Test
	public void createAndInitializeServerWithWorkspace() throws Exception {
		LanguageServerHarness harness = new LanguageServerHarness(() ->
			new ConcourseLanguageServer(TEST_DEFAULT, Mockito.mock(GithubInfoProvider.class)));
		File workspaceRoot = getTestResource("/workspace/");
		assertExpectedInitResult(harness.intialize(workspaceRoot));
	}

	@Test
	public void createAndInitializeServerWithoutWorkspace() throws Exception {
		File workspaceRoot = null;
		LanguageServerHarness harness = new LanguageServerHarness(() ->
			new ConcourseLanguageServer(TEST_DEFAULT, Mockito.mock(GithubInfoProvider.class)));
		assertExpectedInitResult(harness.intialize(workspaceRoot));
	}

	private void assertExpectedInitResult(InitializeResult initResult) {
		if (Boolean.getBoolean("lsp.lazy.completions.disable")) {
			assertThat(initResult.getCapabilities().getCompletionProvider().getResolveProvider()).isFalse();
		} else {
			assertThat(initResult.getCapabilities().getCompletionProvider().getResolveProvider()).isTrue();
		}
		assertThat(initResult.getCapabilities().getTextDocumentSync().getLeft()).isEqualTo(TextDocumentSyncKind.Incremental);
	}

}
