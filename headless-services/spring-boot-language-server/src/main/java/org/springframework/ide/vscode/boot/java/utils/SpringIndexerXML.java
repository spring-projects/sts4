/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.java.IJavaProject;

/**
 * @author Martin Lippert
 */
public class SpringIndexerXML implements SpringIndexer {

	private static final Logger log = LoggerFactory.getLogger(SpringIndexerJava.class);

	private final SymbolHandler handler;

	public SpringIndexerXML(SymbolHandler handler) {
		this.handler = handler;
	}

	@Override
	public String[] getFileWatchPatterns() {
		return new String[] {"**/*.xml"};
	}

	@Override
	public boolean isInterestedIn(String docURI) {
		return docURI.endsWith(".xml");
	}

	@Override
	public void initializeProject(IJavaProject project) throws Exception {
		List<String> files = Files.walk(Paths.get(project.getLocationUri()))
				.filter(path -> path.getFileName().toString().endsWith(".xml"))
				.filter(Files::isRegularFile)
				.map(path -> path.toAbsolutePath().toString())
				.collect(Collectors.toList());

		scanProject(project, (String[]) files.toArray(new String[files.size()]));
	}

	@Override
	public void updateFile(IJavaProject project, String docURI, String content) throws Exception {
	}

	private void scanProject(IJavaProject project, String[] files) {
		for (String file : files) {
			scanFile(file);
		}
	}

	private void scanFile(String file) {

		System.out.println("XML parsing for: " + file);

		try {
	        InputStream xmlInputStream = new FileInputStream(file);

            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLEventReader eventReader = inputFactory.createXMLEventReader(xmlInputStream);

	        while(eventReader.hasNext()){
	            XMLEvent event = eventReader.nextEvent();
	            switch (event.getEventType()) {
	            case XMLEvent.START_ELEMENT:
	            	StartElement startElement = event.asStartElement();

	                System.out.print("<"+startElement.getName().toString()+">");
	                break;
	            case XMLEvent.CHARACTERS:
	            	Characters characters = event.asCharacters();
	                System.out.print(characters.getData());
	                break;
	            case XMLEvent.END_ELEMENT:
	            	EndElement endElement = event.asEndElement();
	                System.out.println("</"+endElement.getName().toString()+">");
	                break;
	            default:
	                //do nothing
	                break;
	            }
	        }
		}
		catch (Exception e) {
			log.error("error parsing XML file: ", e);
		}

	}

}
