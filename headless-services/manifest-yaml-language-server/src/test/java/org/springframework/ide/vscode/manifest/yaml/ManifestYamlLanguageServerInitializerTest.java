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

package org.springframework.ide.vscode.manifest.yaml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFClientParams;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.ClientParamsProvider;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;
import org.springframework.ide.vscode.manifest.yaml.bootiful.ManifestLanguageServerTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.gson.JsonParser;

@RunWith(SpringRunner.class)
@ManifestLanguageServerTest
public class ManifestYamlLanguageServerInitializerTest {

	public static File getTestResource(String name) throws URISyntaxException {
		return Paths.get(ManifestYamlLanguageServerInitializerTest.class.getResource(name).toURI()).toFile();
	}

	@Autowired LanguageServerHarness harness;
	@Autowired ManifestYamlLanguageServerInitializer serverInitializer;
	@Autowired SimpleLanguageServer server;

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
		harness.intialize(null);

		// This is an initial target, for example from cf CLI
		assertEquals(1, getAllParams(serverInitializer.getParamsProvider()).size());
		assertEquals(Arrays.asList("test.io"), serverInitializer.getCfTargets());

		// This tests a change in workspace (e.g. boot dash) that results in two more targets created.
		DidChangeConfigurationParams params = new DidChangeConfigurationParams();

		JsonParser parser = new JsonParser();
		params.setSettings(parser.parse(new InputStreamReader(getClass().getResourceAsStream("/cf-targets1.json"))));

		server.getWorkspaceService().didChangeConfiguration(params);
		assertEquals(3, getAllParams(serverInitializer.getParamsProvider()).size());

		// End result should have the initial target as well as the two additional targets obtained on workspace change
		assertEquals(Arrays.asList("test.io", "api.system.demo-gcp.springapps.io", "api.run.pivotal.io"), serverInitializer.getCfTargets());
	}

	private List<CFClientParams> getAllParams(List<ClientParamsProvider> providers) throws Exception {
		List<CFClientParams> all = new ArrayList<>();
		for (ClientParamsProvider clientParamsProvider : providers) {
			Collection<CFClientParams> params = clientParamsProvider.getParams();
			all.addAll(params);
		}
		return all;
	}

}
