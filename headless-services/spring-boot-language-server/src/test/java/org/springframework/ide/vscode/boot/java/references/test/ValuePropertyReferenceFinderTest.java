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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.boot.bootiful.SymbolProviderTestConf;
import org.springframework.ide.vscode.boot.index.SpringMetamodelIndex;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.value.ValuePropertyReferencesProvider;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.protocol.spring.Bean;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.google.common.collect.ImmutableList;

/**
 * @author Martin Lippert
 */
@ExtendWith(SpringExtension.class)
@BootLanguageServerTest
@Import(SymbolProviderTestConf.class)
public class ValuePropertyReferenceFinderTest {

	@Autowired private BootLanguageServerHarness harness;
	@Autowired private JavaProjectFinder projectFinder;
	@Autowired private SpringMetamodelIndex springIndex;
	@Autowired private SpringSymbolIndex indexer;

	private File directory;
	private IJavaProject project;
	private Bean[] indexedBeans;
	private String tempJavaDocUri;
	private Bean bean1;
	private Bean bean2;

	@BeforeEach
	public void setup() throws Exception {
		harness.intialize(null);

		directory = new File(ProjectsHarness.class.getResource("/test-projects/test-spring-indexing/").toURI());

		String projectDir = directory.toURI().toString();
		project = projectFinder.find(new TextDocumentIdentifier(projectDir)).get();
		
        tempJavaDocUri = directory.toPath().resolve("src/main/java/org/test/TempClass.java").toUri().toString();

		CompletableFuture<Void> initProject = indexer.waitOperation();
		initProject.get(5, TimeUnit.SECONDS);
	}
	
	@Test
    void testFindReferenceAtBeginningPropFile() throws Exception {
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
    void testFindReferenceAtBeginningYMLFile() throws Exception {
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
    void testFindReferenceWithinTheDocument() throws Exception {
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
    void testFindReferenceWithinMultipleFiles() throws Exception {
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
    void testFindReferenceWithinMultipleMixedFiles() throws Exception {
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
    
    @Test
    void testFindReferencesToPropertyFromValueAnnotation() throws Exception {
    	harness.getServer().getWorkspaceService().setWorkspaceFolders(List.of(new WorkspaceFolder(directory.toURI().toString())));
    	
    	String completionLine = "@Value(\"${my.<*>prop}\")";
    	
    	String editorContent = """
    			package org.test;

    			import 
    			""" +
    			Annotations.VALUE + ";" +
    			"""

    			@Component
    			""" +
    			completionLine + "\n" +
    					"""
    					public class TestDependsOnClass {
    					}
    					""";

    	Editor editor = harness.newEditor(LanguageId.JAVA, editorContent, tempJavaDocUri);
    	List<? extends Location> references = editor.getReferences();
    	
    	assertEquals(1, references.size());
    	
    	Location location = references.get(0);
    	assertEquals(directory.toPath().resolve("src/main/java/application.properties").toUri().toString(), location.getUri());
    	assertEquals(0, location.getRange().getStart().getLine());
    	assertEquals(0, location.getRange().getStart().getCharacter());
    	assertEquals(0, location.getRange().getEnd().getLine());
    	assertEquals(7, location.getRange().getEnd().getCharacter());
    }
}
