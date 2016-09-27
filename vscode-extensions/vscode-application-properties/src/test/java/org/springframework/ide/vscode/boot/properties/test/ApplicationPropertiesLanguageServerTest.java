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
package org.springframework.ide.vscode.boot.properties.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.junit.Test;
import org.springframework.ide.vscode.boot.properties.ApplicationPropertiesLanguageServer;
import org.springframework.ide.vscode.testharness.LanguageServerHarness;

import io.typefox.lsapi.InitializeResult;
import io.typefox.lsapi.ServerCapabilities;

/**
 * Boot app properties file language server tests
 * 
 * @author Alex Boyko
 *
 */
public class ApplicationPropertiesLanguageServerTest {

	public static File getTestResource(String name) throws URISyntaxException {
		return Paths.get(ApplicationPropertiesLanguageServer.class.getResource(name).toURI()).toFile();
	}

	@Test
	public void createAndInitializeServerWithWorkspace() throws Exception {
		LanguageServerHarness harness = new LanguageServerHarness(ApplicationPropertiesLanguageServer::new);
		File workspaceRoot = getTestResource("/workspace/");
		assertExpectedInitResult(harness.intialize(workspaceRoot));
	}

	@Test
	public void createAndInitializeServerWithoutWorkspace() throws Exception {
		File workspaceRoot = null;
		LanguageServerHarness harness = new LanguageServerHarness(ApplicationPropertiesLanguageServer::new);
		assertExpectedInitResult(harness.intialize(workspaceRoot));
	}
	
	private void assertExpectedInitResult(InitializeResult initResult) {
		assertThat(initResult.getCapabilities().getTextDocumentSync()).isEqualTo(ServerCapabilities.SYNC_FULL);
	}

}
