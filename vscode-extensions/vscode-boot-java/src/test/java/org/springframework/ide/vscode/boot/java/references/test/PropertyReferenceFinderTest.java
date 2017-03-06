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
package org.springframework.ide.vscode.boot.java.references.test;

import static org.junit.Assert.assertNotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Location;
import org.junit.Test;
import org.springframework.ide.vscode.boot.java.references.ValuePropertyReferencesProvider;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;

/**
 * @author Martin Lippert
 */
public class PropertyReferenceFinderTest {
	
	@Test
	public void testFindReferences() throws Exception {
		ValuePropertyReferencesProvider provider = new ValuePropertyReferencesProvider(null);

		Path root = Paths.get(ProjectsHarness.class.getResource("/test-property-files/simple-case/").toURI());
		CompletableFuture<List<? extends Location>> locations = provider.findReferencesFromPropertyFiles(root, "test.property");
		
		assertNotNull(locations);
	}

}
