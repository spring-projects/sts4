/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.app.BootLanguageServerBootApp;
import org.springframework.ide.vscode.boot.bootiful.SourceLinksTestConf;
import org.springframework.ide.vscode.boot.java.links.SourceLinks;
import org.springframework.ide.vscode.commons.maven.java.MavenJavaProject;
import org.springframework.ide.vscode.languageserver.starter.LanguageServerAutoConf;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

/**
 * @author Alex Boyko
 */
@OverrideAutoConfiguration(enabled=false)
@Import({LanguageServerAutoConf.class, SourceLinksTestConf.class})
@SpringBootTest(classes={
		BootLanguageServerBootApp.class
})
@DirtiesContext(classMode=ClassMode.AFTER_EACH_TEST_METHOD)
public class AdvancedSourceLinksTest {
	
	private ProjectsHarness projects = ProjectsHarness.INSTANCE;
	
	@Autowired
	private MockProjectObserver projectObserver;
	
	@Autowired
	private SourceLinks sourceLinks;
	
	private MavenJavaProject appProject;
	private MavenJavaProject libraryProject;
	
	@BeforeEach
	public void setup() throws Exception {
		// Build parent project
		projects.mavenProject("gs-multi-module-complete");
		appProject = projects.mavenProjectAlreadyBuilt("gs-multi-module-complete/application");
		libraryProject = projects.mavenProjectAlreadyBuilt("gs-multi-module-complete/library");
		projectObserver.doWithListeners(l -> l.created(libraryProject));
		projectObserver.doWithListeners(l -> l.created(appProject));
	}

    @Test
    void linkFromApptoLibrarySource() throws Exception {
        Optional<String> link = sourceLinks.sourceLinkUrlForFQName(appProject, "hello.service.MyService");
        assertTrue(link.isPresent());
        String linkUri = link.get();
        URI uri = URI.create(linkUri);
        assertEquals("file", uri.getScheme());
        assertTrue(linkUri.endsWith("gs-multi-module-complete/library/src/main/java/hello/service/MyService.java#8,14"));
    }

}
