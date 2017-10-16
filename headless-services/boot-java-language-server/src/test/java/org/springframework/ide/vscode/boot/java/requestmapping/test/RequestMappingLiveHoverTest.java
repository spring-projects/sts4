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
package org.springframework.ide.vscode.boot.java.requestmapping.test;

import org.junit.Before;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.java.CompositeJavaProjectFinder;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;
import org.springframework.ide.vscode.project.harness.MockRunningAppProvider;
import org.springframework.ide.vscode.project.harness.PropertyIndexHarness;


public class RequestMappingLiveHoverTest {

	private CompositeJavaProjectFinder projectManager;
	private LanguageServerHarness<BootJavaLanguageServer> harness;
	private PropertyIndexHarness indexHarness;
	private MockRunningAppProvider mockAppProvider;

	@Before
	public void setup() throws Exception {

		projectManager = new CompositeJavaProjectFinder();
        mockAppProvider = new MockRunningAppProvider();
		indexHarness = new PropertyIndexHarness();
		harness = new LanguageServerHarness<BootJavaLanguageServer>(() -> {
			BootJavaLanguageServer server = new BootJavaLanguageServer(projectManager, indexHarness.getIndexProvider(), mockAppProvider.provider);
			return server;
		}) {
			@Override
			protected String getFileExtension() {
				return ".java";
			}
		};
	}


}
