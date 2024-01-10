/*******************************************************************************
 * Copyright (c) 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.xml.test;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.lsp4j.CompletionItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.app.BootLanguageServerBootApp;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.boot.bootiful.XmlBeansTestConf;
import org.springframework.ide.vscode.boot.java.utils.test.MockProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.util.Settings;
import org.springframework.ide.vscode.commons.maven.java.MavenJavaProject;
import org.springframework.ide.vscode.commons.util.UriUtil;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.languageserver.starter.LanguageServerAutoConf;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import com.google.gson.Gson;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * @author Martin Lippert
 */
@OverrideAutoConfiguration(enabled=false)
@Import({LanguageServerAutoConf.class, XmlBeansTestConf.class})
@SpringBootTest(classes={
		BootLanguageServerBootApp.class
})
@DirtiesContext(classMode=ClassMode.AFTER_EACH_TEST_METHOD)
public class XMLContentAssistTest {
	
	
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(XMLContentAssistTest.class);
	
	@Autowired private BootLanguageServerHarness harness;
	@Autowired private SpringSymbolIndex indexer;
	@Autowired private MockProjectObserver projectObserver;
	
	private ProjectsHarness projects = ProjectsHarness.INSTANCE;
	private MavenJavaProject project;
	
	private Level originalLevel;

	@BeforeEach
	public void setup() throws Exception {
	    final Logger logger = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
	    originalLevel = logger.getLevel();
	    logger.setLevel(Level.INFO);
	    
		log.info("-------------------------------------------------");
		harness.intialize(null);
		
		Map<String, Object> supportXML = new HashMap<>();
		supportXML.put("on", true);
		supportXML.put("hyperlinks", true);
		supportXML.put("content-assist", true);
		supportXML.put("scan-folders", "/src/main/");
		Map<String, Object> bootJavaObj = new HashMap<>();
		bootJavaObj.put("support-spring-xml-config", supportXML);
		Map<String, Object> settings = new HashMap<>();
		settings.put("boot-java", bootJavaObj);
		
		harness.changeConfiguration(new Settings(new Gson().toJsonTree(settings)));

		project = projects.mavenProject("test-xml-hyperlinks");
		
		harness.useProject(project);
		
		projectObserver.doWithListeners(l -> l.created(project));

		CompletableFuture<Void> initProject = indexer.waitOperation();
		initProject.get(1500, TimeUnit.SECONDS);
	}
	
	@AfterEach
	public void tearDown() {
		log.debug("-------------------------------------------------");
	    final Logger logger = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
	    logger.setLevel(originalLevel);
	}

    @Test
    void testEmptyXMLFileCompletions() throws Exception {
        Path xmlFilePath = Paths.get(project.getLocationUri()).resolve("beans.xml");
        Editor editor = harness.newEditor(LanguageId.XML, "",
                UriUtil.toUri(xmlFilePath.toFile()).toString()
        );
        
        List<CompletionItem> completions = editor.getCompletions();
        assertEquals(1, completions.size());
        assertEquals("Spring XML config file skeleton", completions.get(0).getLabel());
    }

    @Test
    void testNoSkeletonSnippetForNonEmptyXMLFile() throws Exception {
        Path xmlFilePath = Paths.get(project.getLocationUri()).resolve("beans.xml");
        Editor editor = harness.newEditor(LanguageId.XML, "<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\"?>\\n",
                UriUtil.toUri(xmlFilePath.toFile()).toString()
        );
        
        List<CompletionItem> completions = editor.getCompletions();
        assertEquals(0, completions.size());
    }

}
