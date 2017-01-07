/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
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

public class ManifestYamlEditorCFTest {
	LanguageServerHarness harness;

	@Before
	public void setup() throws Exception {
		harness = new LanguageServerHarness(ManifestYamlLanguageServer::new);
		harness.intialize(null);
	}

	/*
	 * Optional test that is here only to be run in certain conditions. Tests if
	 * buildpack completion values are actually fetched from PWS if the test env
	 * also has a CLI that is alread connected to PWS. Enable only if underlying
	 * conditions for CF connection from vscode are present. For example, CLI is
	 * installed.
	 */
	@Ignore
	@Test
	public void optionalDynamicBuildpacksPWSUsingCliParams() throws Exception {
		// No special test harness setup to use CLI. It is a "default" CF client params
		// provider in the CF vscode framework.
		// Just have to make sure the test env has a CLI that is connected to
		// PWS
		assertContainsCompletions("buildpack: <*>", "buildpack: java_buildpack<*>");
	}
	
	@Ignore
	@Test
	public void optionalDynamicServicesPWSUsingCliParams() throws Exception {
		// No special test harness setup to use CLI. It is a "default" CF client params
		// provider in the CF vscode framework.
		// Just have to make sure the test env has a CLI that is connected to
		// PWS
		assertContainsCompletions(	"services:\n"+
				"  - <*>", "sql");
	}

	//////////////////////////////////////////////////////////////////////////////

	private void assertContainsCompletions(String textBefore, String... textAfter) throws Exception {
		Editor editor = harness.newEditor(textBefore);
		editor.assertContainsCompletions(textAfter);
	}

}
