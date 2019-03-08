/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
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
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.ide.vscode.bosh.bootiful.BoshLanguageServerTest;
import org.springframework.ide.vscode.bosh.mocks.MockCloudConfigProvider;
import org.springframework.ide.vscode.bosh.models.CloudConfigModel;
import org.springframework.ide.vscode.bosh.models.DynamicModelProvider;
import org.springframework.ide.vscode.bosh.models.ReleasesModel;
import org.springframework.ide.vscode.bosh.models.StemcellsModel;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@BoshLanguageServerTest
public class BoshLanguageServerInitializerTest {

	public static File getTestResource(String name) throws URISyntaxException {
		return Paths.get(BoshLanguageServerInitializerTest.class.getResource(name).toURI()).toFile();
	}

	@MockBean DynamicModelProvider<CloudConfigModel> cloudConfigProvider;
	@MockBean DynamicModelProvider<StemcellsModel> stemcellsProvider;
	@MockBean DynamicModelProvider<ReleasesModel> releasesProvider;

	@Autowired
	LanguageServerHarness harness;

	@Test
	public void createAndInitializeServerWithWorkspace() throws Exception {
		File workspaceRoot = getTestResource("/workspace/");
		assertExpectedInitResult(harness.intialize(workspaceRoot));
	}

	@Test
	public void createAndInitializeServerWithoutWorkspace() throws Exception {
		File workspaceRoot = null;
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
