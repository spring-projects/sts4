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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;
import org.springframework.ide.vscode.manifest.yaml.ManifestYamlLanguageServer;

public class ManifestYamlEditorTest {

	LanguageServerHarness harness;

	@Before public void setup() throws Exception {
		harness = new LanguageServerHarness(ManifestYamlLanguageServer::new);
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
		LanguageServerHarness harness = new LanguageServerHarness(ManifestYamlLanguageServer::new);
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
				"memory: 1G\n" +
				"aplications:\n" +
				"  - buildpack: zbuildpack\n" +
				"    domain: zdomain\n" +
				"    name: foo"
		);
		editor.assertProblems("aplications|Unknown property");

		//mispelled or not allowed at toplevel
		editor = harness.newEditor(
				"name: foo\n" +
				"buildpeck: yahah\n" +
				"memory: 1G\n" +
				"memori: 1G\n"
		);
		editor.assertProblems(
				"name|Unknown property",
				"buildpeck|Unknown property",
				"memori|Unknown property"
		);

		//mispelled or not allowed as nested
		editor = harness.newEditor(
				"applications:\n" +
				"- name: fine\n" +
				"  buildpeck: yahah\n" +
				"  memory: 1G\n" +
				"  memori: 1G\n" +
				"  applications: bad\n"
		);
		editor.assertProblems(
				"buildpeck|Unknown property",
				"memori|Unknown property",
				"applications|Unknown property"
		);
	}

	@Test
	public void reconcileStructuralProblems() throws Exception {
		Editor editor;

		//forgot the 'applications:' heading
		editor = harness.newEditor(
				"- name: foo"
		);
		editor.assertProblems(
				"- name: foo|Expecting a 'Map' but found a 'Sequence'"
		);

		//forgot to make the '-' after applications
		editor = harness.newEditor(
				"applications:\n" +
				"  name: foo"
		);
		editor.assertProblems(
				"name: foo|Expecting a 'Sequence' but found a 'Map'"
		);

		//Using a 'composite' element where a scalar type is expected
		editor = harness.newEditor(
				"memory:\n"+
				"- bad sequence\n" +
				"buildpack:\n" +
				"  bad: map\n"
		);
		editor.assertProblems(
				"- bad sequence|Expecting a 'Memory' but found a 'Sequence'",
				"bad: map|Expecting a 'Buildpack' but found a 'Map'"
		);
	}

	@Test
	public void reconcileSimpleTypes() throws Exception {
		Editor editor;

		//check for 'format' errors:
		editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  instances: not a number\n" +
				"  no-route: notBool\n"+
				"  memory: 1024\n" +
				"  disk_quota: 2048\n"
		);
		editor.assertProblems(
				"not a number|Positive Integer",
				"notBool|boolean",
				"1024|Memory",
				"2048|Memory"
		);

		//check for 'range' errors:
		editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  instances: -3\n" +
				"  memory: -1024M\n" +
				"  disk_quota: -2048M\n"
		);
		editor.assertProblems(
				"-3|Positive Integer",
				"-1024M|Memory",
				"-2048M|Memory"
		);

		//check that correct values are indeed accepted
		editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  instances: 2\n" +
				"  no-route: true\n"+
				"  memory: 1024M\n" +
				"  disk_quota: 2048MB\n"
		);
		editor.assertProblems(/*none*/);

		//check that correct values are indeed accepted
		editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  instances: 2\n" +
				"  no-route: false\n" +
				"  memory: 1024m\n" +
				"  disk_quota: 2048mb\n"
		);
		editor.assertProblems(/*none*/);

		editor = harness.newEditor(
				"applications:\n" +
				"- name: foo\n" +
				"  instances: 2\n" +
				"  memory: 1G\n" +
				"  disk_quota: 2g\n"
		);
		editor.assertProblems(/*none*/);
	}

	@Test
	public void noListIndent() throws Exception {
		Editor editor;
		editor = harness.newEditor("appl<*>");
		editor.assertCompletions(
				"applications:\n"+
				"- <*>"
		);
	}

	@Test
	public void toplevelCompletions() throws Exception {
		Editor editor;
		editor = harness.newEditor("<*>");
		editor.assertCompletions(
				"applications:\n"+
				"- <*>",
				// ---------------
				"buildpack: <*>",
				// ---------------
				"command: <*>",
				// ---------------
				"disk_quota: <*>",
				// ---------------
				"domain: <*>",
				// ---------------
				"domains:\n"+
				"- <*>",
				// ---------------
				"env:\n"+
				"  <*>",
				// ---------------
//				"host: <*>",
				// ---------------
//				"hosts: \n"+
//				"  - <*>",
				// ---------------
				"inherit: <*>",
				// ---------------
				"instances: <*>",
				// ---------------
				"memory: <*>",
				// ---------------
//				"name: <*>",
				// ---------------
				"no-hostname: <*>",
				// ---------------
				"no-route: <*>",
				// ---------------
				"path: <*>",
				// ---------------
				"random-route: <*>",
				// ---------------
				"services:\n"+
				"- <*>",
				// ---------------
				"stack: <*>",
				// ---------------
				"timeout: <*>"
		);

		editor = harness.newEditor("ranro<*>");
		editor.assertCompletions(
				"random-route: <*>"
		);
	}

	@Test
	public void nestedCompletions() throws Exception {
		Editor editor;
		editor = harness.newEditor(
				"applications:\n" +
				"- <*>"
		);
		editor.assertCompletions(
				// ---------------
				"applications:\n" +
				"- buildpack: <*>",
				// ---------------
				"applications:\n" +
				"- command: <*>",
				// ---------------
				"applications:\n" +
				"- disk_quota: <*>",
				// ---------------
				"applications:\n" +
				"- domain: <*>",
				// ---------------
				"applications:\n" +
				"- domains:\n"+
				"  - <*>",
				// ---------------
				"applications:\n" +
				"- env:\n"+
				"    <*>",
				// ---------------
				"applications:\n" +
				"- host: <*>",
				// ---------------
				"applications:\n" +
				"- hosts:\n"+
				"  - <*>",
				// ---------------
				"applications:\n" +
				"- instances: <*>",
				// ---------------
				"applications:\n" +
				"- memory: <*>",
				// ---------------
				"applications:\n" +
				"- name: <*>",
				// ---------------
				"applications:\n" +
				"- no-hostname: <*>",
				// ---------------
				"applications:\n" +
				"- no-route: <*>",
				// ---------------
				"applications:\n" +
				"- path: <*>",
				// ---------------
				"applications:\n" +
				"- random-route: <*>",
				// ---------------
				"applications:\n" +
				"- services:\n"+
				"  - <*>",
				// ---------------
				"applications:\n" +
				"- stack: <*>",
				// ---------------
				"applications:\n" +
				"- timeout: <*>"
		);
	}

	@Test
	public void valueCompletions() throws Exception {
		assertCompletions("disk_quota: <*>",
				"disk_quota: 1024M<*>",
				"disk_quota: 256M<*>",
				"disk_quota: 512M<*>"
		);
		assertCompletions("memory: <*>",
				"memory: 1024M<*>",
				"memory: 256M<*>",
				"memory: 512M<*>"
		);
		assertCompletions("no-hostname: <*>",
				"no-hostname: false<*>",
				"no-hostname: true<*>"
		);
		assertCompletions("no-route: <*>",
				"no-route: false<*>",
				"no-route: true<*>"
		);
		assertCompletions("random-route: <*>",
				"random-route: false<*>",
				"random-route: true<*>"
		);
	}

	@Test @Ignore
	public void hoverInfos() throws Exception {
		Editor editor = harness.newEditor(
				"memory: 1G\n" +
				"applications:\n" +
				"- buildpack: zbuildpack\n" +
				"  domain: zdomain\n" +
				"  name: foo"
		);
		editor.assertIsHoverRegion("memory");
		editor.assertIsHoverRegion("applications");
		editor.assertIsHoverRegion("buildpack");
		editor.assertIsHoverRegion("domain");
		editor.assertIsHoverRegion("name");

		editor.assertHoverContains("memory", "Use the <code>memory</code> attribute to specify the memory limit");
		editor.assertHoverContains("1G", "Use the <code>memory</code> attribute to specify the memory limit");
		editor.assertHoverContains("buildpack", "use the <code>buildpack</code> attribute to specify its URL or name");
	}

	//////////////////////////////////////////////////////////////////////////////

	private void assertCompletions(String textBefore, String... textAfter) throws Exception {
		Editor editor = harness.newEditor(textBefore);
		editor.assertCompletions(textAfter);
	}
}
