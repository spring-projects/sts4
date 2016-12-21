package org.springframework.ide.vscode.concourse;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.junit.Test;
import org.springframework.ide.vscode.concourse.ConcourseLanguageServer;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;

public class ConcourseLanguageServerTest {

	public static File getTestResource(String name) throws URISyntaxException {
		return Paths.get(ConcourseLanguageServerTest.class.getResource(name).toURI()).toFile();
	}

	@Test
	public void createAndInitializeServerWithWorkspace() throws Exception {
		LanguageServerHarness harness = new LanguageServerHarness(ConcourseLanguageServer::new);
		File workspaceRoot = getTestResource("/workspace/");
		assertExpectedInitResult(harness.intialize(workspaceRoot));
	}

	@Test
	public void createAndInitializeServerWithoutWorkspace() throws Exception {
		File workspaceRoot = null;
		LanguageServerHarness harness = new LanguageServerHarness(ConcourseLanguageServer::new);
		assertExpectedInitResult(harness.intialize(workspaceRoot));
	}
	
	private void assertExpectedInitResult(InitializeResult initResult) {
		assertThat(initResult.getCapabilities().getCompletionProvider().getResolveProvider()).isFalse();
		assertThat(initResult.getCapabilities().getTextDocumentSync()).isEqualTo(TextDocumentSyncKind.Incremental);
	}

}
