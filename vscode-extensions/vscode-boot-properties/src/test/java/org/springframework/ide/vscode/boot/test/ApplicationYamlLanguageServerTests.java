package org.springframework.ide.vscode.boot.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageServer;
import org.junit.Test;
import org.springframework.ide.vscode.boot.yaml.ApplicationYamlLanguageServer;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;

public class ApplicationYamlLanguageServerTests {

	public static File getTestResource(String name) throws URISyntaxException {
		return Paths.get(ApplicationYamlLanguageServerTests.class.getResource(name).toURI()).toFile();
	}

	private LanguageServerHarness newHarness() throws Exception {
		Callable<? extends LanguageServer> f = () -> new ApplicationYamlLanguageServer((d) -> null, (d) -> null, (d) -> null);
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
		assertThat(initResult.getCapabilities().getCompletionProvider().getResolveProvider()).isTrue();
		assertThat(initResult.getCapabilities().getTextDocumentSync()).isEqualTo(TextDocumentSyncKind.Full);
	}

}
