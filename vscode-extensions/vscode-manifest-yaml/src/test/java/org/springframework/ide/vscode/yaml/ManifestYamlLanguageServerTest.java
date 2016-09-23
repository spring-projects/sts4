package org.springframework.ide.vscode.yaml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;
import org.springframework.ide.vscode.cloudfoundry.manifest.editor.ManifestYamlLanguageServer;
import org.springframework.ide.vscode.testharness.LanguageServerHarness;
import org.springframework.ide.vscode.testharness.TextDocumentInfo;

import io.typefox.lsapi.CompletionItem;
import io.typefox.lsapi.CompletionList;
import io.typefox.lsapi.InitializeResult;
import io.typefox.lsapi.ServerCapabilities;

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
	
	
	@Test public void completions() throws Exception {
		LanguageServerHarness harness = new LanguageServerHarness(ManifestYamlLanguageServer::new);
		
		File workspaceRoot = getTestResource("/workspace/");
		assertExpectedInitResult(harness.intialize(workspaceRoot));

		TextDocumentInfo doc = harness.openDocument(getTestResource("/workspace/testfile.yml"));
		
		CompletionList completions = harness.getCompletions(doc, doc.positionOf("foo"));
		assertThat(completions.isIncomplete()).isFalse();
		assertThat(completions.getItems())
			.extracting(CompletionItem::getLabel)
			.containsExactly("TypeScript", "JavaScript");
		
		List<CompletionItem> resolved = harness.resolveCompletions(completions);
		assertThat(resolved)
			.extracting(CompletionItem::getLabel)
			.containsExactly("TypeScript", "JavaScript");
		
		assertThat(resolved)
			.extracting(CompletionItem::getDetail)
			.containsExactly("TypeScript details", "JavaScript details");
		
		assertThat(resolved)
			.extracting(CompletionItem::getDocumentation)
			.containsExactly("TypeScript docs", "JavaScript docs");
	}
	
	private void assertExpectedInitResult(InitializeResult initResult) {
		assertThat(initResult.getCapabilities().getCompletionProvider().getResolveProvider()).isTrue();
		assertThat(initResult.getCapabilities().getTextDocumentSync()).isEqualTo(ServerCapabilities.SYNC_FULL);
	}

}
