/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.references.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.junit.Test;
import org.springframework.ide.vscode.boot.java.value.ValuePropertyReferencesProvider;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;

import com.google.common.collect.ImmutableList;

/**
 * @author Martin Lippert
 */
public class PropertyReferenceFinderTest {

	@Test
	public void testFindReferenceAtBeginningPropFile() throws Exception {
		ValuePropertyReferencesProvider provider = new ValuePropertyReferencesProvider(null);

		Path root = Paths.get(ProjectsHarness.class.getResource("/test-property-files/simple-case/").toURI());
		List<? extends Location> locations = provider.findReferencesFromPropertyFiles(wsFolder(root), "test.property");

		assertNotNull(locations);
		assertEquals(1, locations.size());
		Location location = locations.get(0);

		URI docURI = Paths.get(root.toString(), "application.properties").toUri();
		assertEquals(docURI.toString(), location.getUri());
		assertEquals(0, location.getRange().getStart().getLine());
		assertEquals(0, location.getRange().getStart().getCharacter());
		assertEquals(0, location.getRange().getEnd().getLine());
		assertEquals(13, location.getRange().getEnd().getCharacter());
	}

	private Collection<WorkspaceFolder> wsFolder(Path directory) {
		if (directory != null) {
			WorkspaceFolder folder = new WorkspaceFolder();
			folder.setName(directory.getFileName().toString());
			folder.setUri(directory.toUri().toString());
			return ImmutableList.of(folder);
		}
		return ImmutableList.of();
	}

	@Test
	public void testFindReferenceAtBeginningYMLFile() throws Exception {
		ValuePropertyReferencesProvider provider = new ValuePropertyReferencesProvider(null);

		Path root = Paths.get(ProjectsHarness.class.getResource("/test-property-files/simple-yml/").toURI());
		List<? extends Location> locations  = provider.findReferencesFromPropertyFiles(wsFolder(root), "test.property");

		assertNotNull(locations);
		assertEquals(1, locations.size());
		Location location = locations.get(0);

		URI docURI = Paths.get(root.toString(), "application.yml").toUri();
		assertEquals(docURI.toString(), location.getUri());
		assertEquals(3, location.getRange().getStart().getLine());
		assertEquals(2, location.getRange().getStart().getCharacter());
		assertEquals(3, location.getRange().getEnd().getLine());
		assertEquals(10, location.getRange().getEnd().getCharacter());
	}

	@Test
	public void testFindReferenceWithinTheDocument() throws Exception {
		ValuePropertyReferencesProvider provider = new ValuePropertyReferencesProvider(null);

		Path root = Paths.get(ProjectsHarness.class.getResource("/test-property-files/simple-case/").toURI());
		List<? extends Location> locations = provider.findReferencesFromPropertyFiles(wsFolder(root), "server.port");

		assertNotNull(locations);
		assertEquals(1, locations.size());
		Location location = locations.get(0);

		URI docURI = Paths.get(root.toString(), "application.properties").toUri();
		assertEquals(docURI.toString(), location.getUri());
		assertEquals(2, location.getRange().getStart().getLine());
		assertEquals(0, location.getRange().getStart().getCharacter());
		assertEquals(2, location.getRange().getEnd().getLine());
		assertEquals(11, location.getRange().getEnd().getCharacter());
	}

	@Test
	public void testFindReferenceWithinMultipleFiles() throws Exception {
		ValuePropertyReferencesProvider provider = new ValuePropertyReferencesProvider(null);

		Path root = Paths.get(ProjectsHarness.class.getResource("/test-property-files/multiple-files/").toURI());
		List<? extends Location> locations = provider.findReferencesFromPropertyFiles(wsFolder(root), "appl1.prop");

		assertNotNull(locations);
		assertEquals(3, locations.size());

		Location location = getLocation(locations, Paths.get(root.toString(), "application-dev.properties").toUri());
		assertNotNull(location);
		assertEquals(1, location.getRange().getStart().getLine());
		assertEquals(0, location.getRange().getStart().getCharacter());
		assertEquals(1, location.getRange().getEnd().getLine());
		assertEquals(10, location.getRange().getEnd().getCharacter());

		location = getLocation(locations, Paths.get(root.toString(), "application.properties").toUri());
		assertNotNull(location);
		assertEquals(1, location.getRange().getStart().getLine());
		assertEquals(0, location.getRange().getStart().getCharacter());
		assertEquals(1, location.getRange().getEnd().getLine());
		assertEquals(10, location.getRange().getEnd().getCharacter());

		location = getLocation(locations, Paths.get(root.toString(), "prod-application.properties").toUri());
		assertNotNull(location);
		assertEquals(1, location.getRange().getStart().getLine());
		assertEquals(0, location.getRange().getStart().getCharacter());
		assertEquals(1, location.getRange().getEnd().getLine());
		assertEquals(10, location.getRange().getEnd().getCharacter());
	}

	private Location getLocation(List<? extends Location> locations, URI docURI) {
		for (Location location : locations) {
			if (docURI.toString().equals(location.getUri())) {
				return location;
			}
		}

		return null;
	}

	@Test
	public void testFindReferenceWithinMultipleMixedFiles() throws Exception {
		ValuePropertyReferencesProvider provider = new ValuePropertyReferencesProvider(null);

		Path root = Paths.get(ProjectsHarness.class.getResource("/test-property-files/mixed-multiple-files/").toURI());
		List<? extends Location> locations = provider.findReferencesFromPropertyFiles(wsFolder(root), "appl1.prop");

		assertNotNull(locations);
		assertEquals(2, locations.size());

		Location location = getLocation(locations, Paths.get(root.toString(), "application-dev.properties").toUri());
		assertNotNull(location);
		assertEquals(1, location.getRange().getStart().getLine());
		assertEquals(0, location.getRange().getStart().getCharacter());
		assertEquals(1, location.getRange().getEnd().getLine());
		assertEquals(10, location.getRange().getEnd().getCharacter());

		location = getLocation(locations, Paths.get(root.toString(), "application.yml").toUri());
		assertNotNull(locations);
		assertEquals(3, location.getRange().getStart().getLine());
		assertEquals(2, location.getRange().getStart().getCharacter());
		assertEquals(3, location.getRange().getEnd().getLine());
		assertEquals(6, location.getRange().getEnd().getCharacter());
	}
}
