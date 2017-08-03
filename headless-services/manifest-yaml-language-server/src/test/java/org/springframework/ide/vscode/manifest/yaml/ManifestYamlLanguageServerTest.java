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

package org.springframework.ide.vscode.manifest.yaml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.junit.Test;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ManifestYamlLanguageServerTest {

	public static File getTestResource(String name) throws URISyntaxException {
		return Paths.get(ManifestYamlLanguageServerTest.class.getResource(name).toURI()).toFile();
	}

	@Test
	public void createAndInitializeServerWithWorkspace() throws Exception {
		LanguageServerHarness harness = new LanguageServerHarness(ManifestYamlLanguageServer::new);
		File workspaceRoot = getTestResource("/workspace/");
		assertExpectedInitResult(harness.intialize(workspaceRoot));
	}

	@Test
	public void createAndInitializeServerWithoutWorkspace() throws Exception {
		File workspaceRoot = null;
		LanguageServerHarness harness = new LanguageServerHarness(ManifestYamlLanguageServer::new);
		assertExpectedInitResult(harness.intialize(workspaceRoot));
	}

//	@Test public void completions() throws Exception {
//		LanguageServerHarness harness = new LanguageServerHarness(ManifestYamlLanguageServer::new);
//
//		File workspaceRoot = getTestResource("/workspace/");
//		assertExpectedInitResult(harness.intialize(workspaceRoot));
//
//		TextDocumentInfo doc = harness.openDocument(getTestResource("/workspace/testfile.yml"));
//
//		CompletionList completions = harness.getCompletions(doc, doc.positionOf("foo"));
//		assertThat(completions.isIncomplete()).isFalse();
//		assertThat(completions.getItems())
//			.extracting(CompletionItem::getLabel)
//			.containsExactly("TypeScript", "JavaScript");
//
//		List<CompletionItem> resolved = harness.resolveCompletions(completions);
//		assertThat(resolved)
//			.extracting(CompletionItem::getLabel)
//			.containsExactly("TypeScript", "JavaScript");
//
//		assertThat(resolved)
//			.extracting(CompletionItem::getDetail)
//			.containsExactly("TypeScript details", "JavaScript details");
//
//		assertThat(resolved)
//			.extracting(CompletionItem::getDocumentation)
//			.containsExactly("TypeScript docs", "JavaScript docs");
//	}

	private void assertExpectedInitResult(InitializeResult initResult) {
		if (Boolean.getBoolean("lsp.lazy.completions.disable")) {
			assertThat(initResult.getCapabilities().getCompletionProvider().getResolveProvider()).isFalse();
		} else {
			assertThat(initResult.getCapabilities().getCompletionProvider().getResolveProvider()).isTrue();
		}
		assertThat(initResult.getCapabilities().getTextDocumentSync().getLeft()).isEqualTo(TextDocumentSyncKind.Incremental);
	}

	@Test public void changeCfClientParams() throws Exception {
		MockCloudfoundry cloudfoundry = new MockCloudfoundry();
		ManifestYamlLanguageServer manifestYamlLanguageServer = new ManifestYamlLanguageServer(cloudfoundry.factory, cloudfoundry.defaultParamsProvider);

		LanguageServerHarness harness = new LanguageServerHarness(
				() -> manifestYamlLanguageServer,
				LanguageId.CF_MANIFEST
		);
		harness.intialize(null);

		assertEquals(1, manifestYamlLanguageServer.getCfClientConfig().getClientParamsProvider().getParams().size());
		assertEquals(Arrays.asList("test.io"), manifestYamlLanguageServer.getCfTargets());

		DidChangeConfigurationParams params = new DidChangeConfigurationParams();
		params.setSettings(new ObjectMapper().readValue(getClass().getResourceAsStream("/cf-targets1.json"), Map.class));
		manifestYamlLanguageServer.getWorkspaceService().didChangeConfiguration(params);
		assertEquals(3, manifestYamlLanguageServer.getCfClientConfig().getClientParamsProvider().getParams().size());
		assertEquals(Arrays.asList("test.io", "api.system.demo-gcp.springapps.io", "api.run.pivotal.io"), manifestYamlLanguageServer.getCfTargets());
	}



}
