package org.springframework.ide.vscode.yaml;

import org.junit.Test;
import org.springframework.ide.vscode.testharness.Editor;
import org.springframework.ide.vscode.testharness.LanguageServerHarness;
import org.springframework.ide.vscode.yaml.YamlLanguageServer;

/**
 * This class is a placeholder where we will attempt to copy and port
 * as many tests a possible from 
 * org.springframework.ide.eclipse.boot.properties.editor.test.YamlEditorTests
 * 
 * @author Kris De Volder
 */
public class ApplicationYamlEditorTest {
		
	@Test public void testReconcileCatchesParseError() throws Exception {
		LanguageServerHarness harness = new LanguageServerHarness(YamlLanguageServer::new);
		harness.intialize(null);
		
		Editor editor = harness.newEditor(
				"somemap: val\n"+
				"- sequence"
		);
		editor.assertProblems(
				"-|expected <block end>"
		);
	}
	
	@Test public void linterRunsOnDocumentOpenAndChange() throws Exception {
		LanguageServerHarness harness = new LanguageServerHarness(YamlLanguageServer::new);
		harness.intialize(null);

		Editor editor = harness.newEditor(
				"somemap: val\n"+
				"- sequence"
		);

		editor.assertProblems(
				"-|expected <block end>"
		);
		
		editor.setText(
				"- sequence\n" +
				"zomemap: val"
		);

		editor.assertProblems(
				"z|expected <block end>"
		);
	}
}
