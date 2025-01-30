/*******************************************************************************
 * Copyright (c) 2025 Broadcom
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.beans.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.boot.bootiful.SymbolProviderTestConf;
import org.springframework.ide.vscode.boot.index.SpringMetamodelIndex;
import org.springframework.ide.vscode.boot.java.events.EventListenerIndexElement;
import org.springframework.ide.vscode.boot.java.events.EventPublisherIndexElement;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.protocol.spring.Bean;
import org.springframework.ide.vscode.commons.protocol.spring.SpringIndexElement;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author Martin Lippert
 */
@ExtendWith(SpringExtension.class)
@BootLanguageServerTest
@Import(SymbolProviderTestConf.class)
public class SpringIndexerEventsTest {

	@Autowired private BootLanguageServerHarness harness;
	@Autowired private JavaProjectFinder projectFinder;
	@Autowired private SpringSymbolIndex indexer;
	@Autowired private SpringMetamodelIndex springIndex;

	private File directory;

	@BeforeEach
	public void setup() throws Exception {
		harness.intialize(null);

		directory = new File(ProjectsHarness.class.getResource("/test-projects/test-events-indexing/").toURI());

		String projectDir = directory.toURI().toString();

		// trigger project creation
		projectFinder.find(new TextDocumentIdentifier(projectDir)).get();

		CompletableFuture<Void> initProject = indexer.waitOperation();
		initProject.get(5, TimeUnit.SECONDS);
	}

    @Test
    void testAnnotationBasedEventListenerSymbol() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/com/example/events/demo/EventListenerPerAnnotation.java").toUri().toString();

        SpringIndexerHarness.assertDocumentSymbols(indexer, docUri,
                SpringIndexerHarness.symbol("@Component", "@+ 'eventListenerPerAnnotation' (@Component) EventListenerPerAnnotation"),
                SpringIndexerHarness.symbol("@EventListener", "@EventListener (ApplicationEvent)"));
    }

    @Test
    void testEventListenerSymbolForEventListenerInterfaceImplementation() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/com/example/events/demo/EventListenerPerInterface.java").toUri().toString();

        SpringIndexerHarness.assertDocumentSymbols(indexer, docUri,
                SpringIndexerHarness.symbol("@Component", "@+ 'eventListenerPerInterface' (@Component) EventListenerPerInterface"));
    }
    
    @Test
    void testEventPublisherSymbol() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/com/example/events/demo/CustomEventPublisher.java").toUri().toString();

        SpringIndexerHarness.assertDocumentSymbols(indexer, docUri,
                SpringIndexerHarness.symbol("@Component", "@+ 'customEventPublisher' (@Component) CustomEventPublisher"),
                SpringIndexerHarness.symbol("this.publisher.publishEvent(new CustomEvent())", "@EventPublisher (CustomEvent)"));
    }

    @Test
    void testAnnotationBasedEventListenerIndexElements() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/com/example/events/demo/EventListenerPerAnnotation.java").toUri().toString();

        Bean[] beans = springIndex.getBeansOfDocument(docUri);
        assertEquals(1, beans.length);
        
        Bean listenerComponentBean = Arrays.stream(beans).filter(bean -> bean.getName().equals("eventListenerPerAnnotation")).findFirst().get();
        assertEquals("com.example.events.demo.EventListenerPerAnnotation", listenerComponentBean.getType());
        
        List<SpringIndexElement> children = listenerComponentBean.getChildren();
        assertEquals(1, children.size());
        assertTrue(children.get(0) instanceof EventListenerIndexElement);
        
        EventListenerIndexElement listenerElement = (EventListenerIndexElement) children.get(0);
        assertEquals("org.springframework.context.ApplicationEvent", listenerElement.getEventType());
        
        Location location = listenerElement.getLocation();
        assertNotNull(location);
        assertEquals(docUri, location.getUri());
        assertEquals(new Range(new Position(10, 13), new Position(10, 24)), location.getRange());
    }
    
    @Test
    void testEventListenerIndexElementForEventListenerInterfaceImplementation() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/com/example/events/demo/EventListenerPerInterface.java").toUri().toString();

        Bean[] beans = springIndex.getBeansOfDocument(docUri);
        assertEquals(1, beans.length);
        
        Bean listenerComponentBean = Arrays.stream(beans).filter(bean -> bean.getName().equals("eventListenerPerInterface")).findFirst().get();
        assertEquals("com.example.events.demo.EventListenerPerInterface", listenerComponentBean.getType());
        
        List<SpringIndexElement> children = listenerComponentBean.getChildren();
        assertEquals(1, children.size());
        assertTrue(children.get(0) instanceof EventListenerIndexElement);
        
        EventListenerIndexElement listenerElement = (EventListenerIndexElement) children.get(0);
        assertEquals("org.springframework.context.ApplicationEvent", listenerElement.getEventType());

        Location location = listenerElement.getLocation();
        assertNotNull(location);
        assertEquals(docUri, location.getUri());
        assertEquals(new Range(new Position(10, 13), new Position(10, 31)), location.getRange());
    }
    
    @Test
    void testEventPublisherIndexElements() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/com/example/events/demo/CustomEventPublisher.java").toUri().toString();

        Bean[] beans = springIndex.getBeansOfDocument(docUri);
        assertEquals(1, beans.length);
        
        Bean listenerComponentBean = Arrays.stream(beans).filter(bean -> bean.getName().equals("customEventPublisher")).findFirst().get();
        assertEquals("com.example.events.demo.CustomEventPublisher", listenerComponentBean.getType());
        
        List<SpringIndexElement> children = listenerComponentBean.getChildren();
        assertEquals(1, children.size());
        assertTrue(children.get(0) instanceof EventPublisherIndexElement);
        
        EventPublisherIndexElement publisherElement = (EventPublisherIndexElement) children.get(0);
        assertEquals("com.example.events.demo.CustomEvent", publisherElement.getEventType());
        
        Location location = publisherElement.getLocation();
        assertNotNull(location);
        assertEquals(docUri, location.getUri());
        assertEquals(new Range(new Position(15, 2), new Position(15, 48)), location.getRange());
    }
    
}
