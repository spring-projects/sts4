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
package org.springframework.ide.vscode.boot.java.utils.test;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServer;
import org.springframework.ide.vscode.boot.metadata.DefaultSpringPropertyIndexProvider;
import org.springframework.ide.vscode.commons.languageserver.ProgressService;
import org.springframework.ide.vscode.commons.maven.MavenCore;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;

/**
 * Tests for Spring properties index in Boot Java server
 *
 * @author Alex Boyko
 *
 */
public class SpringPropertyIndexTest {

	private LanguageServerHarness<BootJavaLanguageServer> harness;

	private DefaultSpringPropertyIndexProvider propertyIndexProvider;

	@Before
	public void setup() throws Exception {
		harness = BootLanguageServerHarness.builder().build();
	}

	@Test
	public void testScanningAllAnnotationsSimpleProjectUpfront() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI()));
		propertyIndexProvider = (DefaultSpringPropertyIndexProvider) harness.getServer().getSpringPropertyIndexProvider();

		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-parent/test-annotation-indexing/").toURI());

		File javaFile = new File(directory, "/src/main/java/org/test/SimpleMappingClass.java");

		TextDocument doc = new TextDocument(javaFile.toURI().toString(), LanguageId.JAVA);

		// Not cached yet, hence progress service invoked
		ProgressService progressService = mock(ProgressService.class);
		propertyIndexProvider.setProgressService(progressService);
		propertyIndexProvider.getIndex(doc);
		verify(progressService, atLeastOnce()).progressEvent(anyObject(), anyObject());

		// Should be cached now, so progress service should not be touched
		progressService = mock(ProgressService.class);
		propertyIndexProvider.setProgressService(progressService);
		propertyIndexProvider.getIndex(doc);
		verify(progressService, never()).progressEvent(anyObject(), anyObject());

		// Change POM file for the project
		harness.changeFile(new File(directory, MavenCore.POM_XML).toURI().toString());

		// POM has changed, hence project needs to be reloaded, cached value is cleared
		progressService = mock(ProgressService.class);
		propertyIndexProvider.setProgressService(progressService);
		propertyIndexProvider.getIndex(doc);
		verify(progressService, atLeastOnce()).progressEvent(anyObject(), anyObject());
	}

}
