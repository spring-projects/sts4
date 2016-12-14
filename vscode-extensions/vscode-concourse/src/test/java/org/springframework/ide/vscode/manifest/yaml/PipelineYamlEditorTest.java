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
package org.springframework.ide.vscode.manifest.yaml;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.vscode.commons.util.IOUtil;
import org.springframework.ide.vscode.concourse.ConcourseLanguageServer;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;

public class PipelineYamlEditorTest {

	LanguageServerHarness harness;

	@Before public void setup() throws Exception {
		harness = new LanguageServerHarness(ConcourseLanguageServer::new);
		harness.intialize(null);
	}

	@Test public void testReconcileCatchesParseError() throws Exception {
		Editor editor = harness.newEditor(
				"somemap: val\n"+
				"- sequence"
		);
		editor.assertProblems(
				"-|expected <block end>"
		);
	}

	@Test public void reconcileRunsOnDocumentOpenAndChange() throws Exception {
		LanguageServerHarness harness = new LanguageServerHarness(ConcourseLanguageServer::new);
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

	@Test
	public void reconcileMisSpelledPropertyNames() throws Exception {
		Editor editor;

		editor = harness.newEditor(
				"resorces:\n" +
				"- name: git\n" +
				"  type: git\n"
		);
		editor.assertProblems("resorces|Unknown property");
	}

	@Test
	public void reconcileAcceptsSensiblePipelineFile() throws Exception {
		Editor editor;

		editor = harness.newEditor(
				getClasspathResourceText("workspace/pipeline.yml")
		);
		editor.assertProblems(/*NONE*/);;
	}

	private String getClasspathResourceText(String resourceName) throws Exception {
		InputStream stream = PipelineYamlEditorTest.class.getClassLoader().getResourceAsStream(resourceName);
		return IOUtil.toString(stream);
	}

	@Test
	public void reconcileStructuralProblems() throws Exception {
		Editor editor;

		//resources should be a sequence not a map, even if there's only one entry
		editor = harness.newEditor(
				"resources:\n" +
				"  name: git\n" +
				"  type: git\n" 
		);
		editor.assertProblems(
				"name: git\n  type: git|Expecting a 'Sequence' but found a 'Map'"
		);

		//TODO: Add more test cases for structural problem?
	}

	@Test
	public void reconcileSimpleTypes() throws Exception {
		Editor editor;

		//check for 'format' errors:
		editor = harness.newEditor(
				"jobs:\n" +
				"- name: foo\n" +
				"  serial: boohoo\n" +
				"  max_in_flight: 0\n" +
				"  plan:\n" +
				"  - get: git\n" +
				"    trigger: yohoho"
		);
		editor.assertProblems(
				"boohoo|boolean",
				"0|Positive Integer",
				"yohoho|boolean"
		);

		//check that correct values are indeed accepted
		editor = harness.newEditor(
				"jobs:\n" +
				"- name: foo\n" +
				"  serial: true\n" +
				"  max_in_flight: 2\n" +
				"  plan:\n" +
				"  - get: git\n" +
				"    trigger: true"
		);
		editor.assertProblems(/*none*/);

	}

	@Test
	public void noListIndent() throws Exception {
		Editor editor;
		editor = harness.newEditor("jo<*>");
		editor.assertCompletions(
				"jobs:\n"+
				"- <*>"
		);
	}

	@Test
	public void toplevelCompletions() throws Exception {
		Editor editor;
		editor = harness.newEditor("<*>");
		editor.assertCompletions(
				"resources:\n"+
				"- <*>",
				// ---------------
				"resource-types:\n" +
				"- <*>",
				// ---------------
				"jobs:\n" +
				"- <*>"
		);

		editor = harness.newEditor("ranro<*>");
		editor.assertCompletions(
				"random-route: <*>"
		);
	}

	@Test
	public void completionDetailsAndDocs() throws Exception {
		Editor editor = harness.newEditor(
				"applications:\n" +
				"- build<*>"
		);
		editor.assertCompletionDetails("buildpack", "Buildpack", "If your application requires a custom buildpack");
	}

	@Test
	public void valueCompletions() throws Exception {
		assertCompletions(
				"resources:\n" +
				"- type: <*>"
				, //=>
				"resources:\n" +
				"- type: archive<*>",
				"resources:\n" +
				"- type: docker-image<*>",
				"resources:\n" +
				"- type: git<*>",
				"resources:\n" +
				"- type: s3<*>",
				"resources:\n" +
				"- type: semver<*>",
				"resources:\n" +
				"- type: time<*>"
		);
		assertCompletions(
				"jobs:\n" +
				"- name: foo\n" +
				"  serial: <*>"
				, // =>
				"jobs:\n" +
				"- name: foo\n" +
				"  serial: false<*>"
				, // --
				"jobs:\n" +
				"- name: foo\n" +
				"  serial: true<*>"
		);
	}

	@Test
	public void topLevelHoverInfos() throws Exception {
		Editor editor = harness.newEditor(
			"resource_types:\n" + 
			"- name: s3-multi\n" + 
			"  type: docker-image\n" + 
			"  source:\n" + 
			"    repository: kdvolder/s3-resource-simple\n" + 
			"resources:\n" + 
			"- name: docker-git\n" + 
			"  type: git\n" + 
			"  source:\n" + 
			"    uri: git@github.com:spring-projects/sts4.git\n" + 
			"    branch: {{branch}}\n" + 
			"    username: kdvolder\n" + 
			"    private_key: {{rsa_id}}\n" + 
			"    paths:\n" + 
			"    - concourse/docker\n" + 
			"jobs:\n" + 
			"- name: build-docker-image\n" + 
			"  serial: true\n" + 
			"  plan:\n" + 
			"  - get: docker-git\n" + 
			"    trigger: true\n" + 
			"  - put: docker-image\n" + 
			"    params:\n" + 
			"      build: docker-git/concourse/docker\n" + 
			"    get_params: \n" + 
			"      skip_download: true\n"
		);
		
		editor.assertHoverContains("resource_types", "each pipeline can configure its own custom types by specifying `resource_types` at the top level.");
		editor.assertHoverContains("resources", "A resource is any entity that can be checked for new versions");
		editor.assertHoverContains("jobs", "At a high level, a job describes some actions to perform");
	}
	
	//////////////////////////////////////////////////////////////////////////////

	private void assertCompletions(String textBefore, String... textAfter) throws Exception {
		Editor editor = harness.newEditor(textBefore);
		editor.assertCompletions(textAfter);
	}
}
