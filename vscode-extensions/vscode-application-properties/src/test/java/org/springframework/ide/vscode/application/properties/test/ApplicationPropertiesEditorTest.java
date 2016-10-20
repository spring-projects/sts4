/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.application.properties.test;

import org.junit.Test;
import org.springframework.ide.vscode.application.properties.ApplicationPropertiesLanguageServer;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;

/**
 * Boot App Properties Editor tests
 * 
 * @author Alex Boyko
 *
 */
public class ApplicationPropertiesEditorTest {
	
	private static final String SYNTAX_ERROR__UNEXPECTED_END_OF_INPUT = "Unexpected end of input, value identifier is expected";
	private static final String SYNTAX_ERROR__UNEXPECTED_END_OF_LINE = "Unexpected end of line, value identifier is expected";

	@Test
	public void testReconcileCatchesParseError() throws Exception {
		LanguageServerHarness harness = new LanguageServerHarness(ApplicationPropertiesLanguageServer::new);
		harness.intialize(null);
		
		Editor editor = harness.newEditor("key\n");
		editor.assertProblems("key|" + SYNTAX_ERROR__UNEXPECTED_END_OF_LINE);
	}
	
	@Test public void linterRunsOnDocumentOpenAndChange() throws Exception {
		LanguageServerHarness harness = new LanguageServerHarness(ApplicationPropertiesLanguageServer::new);
		harness.intialize(null);

		Editor editor = harness.newEditor("key");

		editor.assertProblems("key|" + SYNTAX_ERROR__UNEXPECTED_END_OF_INPUT);
		
		editor.setText(
				"problem\n" +
				"key=value\n" +
				"another"		
		);

		editor.assertProblems("problem|" + SYNTAX_ERROR__UNEXPECTED_END_OF_LINE, "another|" + SYNTAX_ERROR__UNEXPECTED_END_OF_INPUT);
	}

}
