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
import org.springframework.ide.vscode.commons.yaml.reconcile.YamlSchemaProblems;
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
				"director_uuid: cf8dc1fc-9c42-4ffc-96f1-fbad983a6ce6\n" +
				"releases:\n" + 
				"- name: redis\n" +
				"  version: 12\n" + 
				"blah: hoooo\n"
		);
		editor.assertProblems(
				"blah|Unknown property"
		);
	}

	@Test public void toplevelV2PropertyHovers() throws Exception {
		Editor editor = harness.newEditor(
				"name: some-name\n" +
				"director_uuid: cf8dc1fc-9c42-4ffc-96f1-fbad983a6ce6\n" +
				"releases:\n" + 
				"- name: redis\n" +
				"  version: 12\n" + 
				"blah: hoooo\n"
		);
		editor.assertHoverContains("name", "The name of the deployment");
		editor.assertHoverContains("director_uuid", "This string must match the UUID of the currently targeted Director");
		editor.assertHoverContains("releases", "The name and version of each release in the deployment");
	}

	@Test public void toplevelV2PropertyCompletions() throws Exception {
		Editor editor = harness.newEditor(
				"<*>"
		);
		editor.assertCompletions(
				"director_uuid: <*>",
				"name: <*>",
				"releases:\n- <*>"
		);
	}

	@Test public void releasesBlockCompletions() throws Exception {
		Editor editor = harness.newEditor(
				"releases:\n" + 
				"- <*>"
		);
		editor.ignoreProblem(YamlSchemaProblems.MISSING_PROPERTY);
		editor.assertCompletions(
				"releases:\n" + 
				"- name: <*>"
				, //========
				"releases:\n" + 
				"- version: <*>"
		);
	}
		
	@Test public void releasesBlockReconcileAndHovers() throws Exception {
		Editor editor = harness.newEditor(
				"releases:\n" + 
				"- name: some-release\n" +
				"  version: some-version\n" +
				"  woot: dunno\n"
		);
		editor.ignoreProblem(YamlSchemaProblems.MISSING_PROPERTY);
		editor.assertProblems(
				"woot|Unknown property"
		);
		
		editor.assertHoverContains("name", "Name of a release used in the deployment");
		editor.assertHoverContains("version", "The version of the release to use");
		
		editor = harness.newEditor(
				"releases:\n" + 
				"- name: some-release\n" +
				"  version: <*>\n" 
		);
		editor.assertCompletionLabels("latest");
	}

}
