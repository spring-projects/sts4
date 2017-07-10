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
package org.springframework.ide.vscode.concourse;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.vscode.bosh.BoshLanguageServer;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;

public class BoshEditorTest {

	LanguageServerHarness harness;

	@Before public void setup() throws Exception {
		harness = new LanguageServerHarness(() -> {
				return new BoshLanguageServer()
						.setMaxCompletions(100);
			},
			LanguageId.BOSH_DEPLOYMENT
		);
		harness.intialize(null);
	}	

	@Test public void toplevelV2PropertyNamesKnown() throws Exception {
		Editor editor = harness.newEditor(
				"name: some-name\n" +
				"blah: hoooo\n"
		);
		editor.assertProblems(
				"blah|Unknown property"
		);
	}

	@Test public void toplevelV2PropertyHovers() throws Exception {
		Editor editor = harness.newEditor(
				"name: some-name\n" +
				"blah: hoooo\n"
		);
		editor.assertHoverContains("name", "The name of the deployment");
	}

	@Test public void toplevelV2PropertyCompletions() throws Exception {
		Editor editor = harness.newEditor(
				"<*>\n"
		);
		editor.assertCompletions(
				"name: <*>\n"
		);
	}

}
