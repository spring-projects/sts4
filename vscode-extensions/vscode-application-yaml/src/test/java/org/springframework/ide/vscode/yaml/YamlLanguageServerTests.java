package org.springframework.ide.vscode.yaml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;
import org.springframework.ide.vscode.testharness.LanguageServerHarness;
import org.springframework.ide.vscode.testharness.TextDocumentInfo;
import org.springframework.ide.vscode.yaml.YamlLanguageServer;

import io.typefox.lsapi.CompletionItem;
import io.typefox.lsapi.CompletionList;
import io.typefox.lsapi.InitializeResult;
import io.typefox.lsapi.ServerCapabilities;
import io.typefox.lsapi.TextDocumentSyncKind;

public class YamlLanguageServerTests {

	public static File getTestResource(String name) throws URISyntaxException {
		return Paths.get(YamlLanguageServerTests.class.getResource(name).toURI()).toFile();
	}

	@Test
	public void createAndInitializeServerWithWorkspace() throws Exception {
		LanguageServerHarness harness = new LanguageServerHarness(YamlLanguageServer::new);
		File workspaceRoot = getTestResource("/workspace/");
		assertExpectedInitResult(harness.intialize(workspaceRoot));
	}

	@Test
	public void createAndInitializeServerWithoutWorkspace() throws Exception {
		File workspaceRoot = null;
		LanguageServerHarness harness = new LanguageServerHarness(YamlLanguageServer::new);
		assertExpectedInitResult(harness.intialize(workspaceRoot));
	}
	
	
	private void assertExpectedInitResult(InitializeResult initResult) {
		assertThat(initResult.getCapabilities().getCompletionProvider().getResolveProvider()).isTrue();
		assertThat(initResult.getCapabilities().getTextDocumentSync()).isEqualTo(TextDocumentSyncKind.Full);
	}

}
