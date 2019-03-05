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
package org.springframework.ide.vscode.boot.java.utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.FileUtils;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.dom.DOMParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.UriUtil;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class SpringIndexerXML implements SpringIndexer {

	private static final Logger log = LoggerFactory.getLogger(SpringIndexerJava.class);

	private final SymbolHandler symbolHandler;
	private final Map<String, SpringIndexerXMLNamespaceHandler> namespaceHandler;
	private final SymbolCache cache;

	public SpringIndexerXML(SymbolHandler handler, Map<String, SpringIndexerXMLNamespaceHandler> namespaceHandler, SymbolCache cache) {
		this.symbolHandler = handler;
		this.namespaceHandler = namespaceHandler;
		this.cache = cache;
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
		String[] files = this.getFiles(project);

		log.info("scan xml files for symbols for project: " + project.getElementName() + " - no. of files: " + files.length);

		long startTime = System.currentTimeMillis();
		for (String file : files) {
			scanFile(project, file);
		}
		long endTime = System.currentTimeMillis();

		log.info("scan xml files for symbols for project: " + project.getElementName() + " took ms: " + (endTime - startTime));
	}

	@Override
	public void removeProject(IJavaProject project) throws Exception {
	}

	@Override
	public void updateFile(IJavaProject project, String docURI, long lastModified, String content) throws Exception {
		scanFile(project, content, docURI);
	}

	@Override
	public void removeFile(IJavaProject project, String docURI) throws Exception {
	}

	private void scanFile(IJavaProject project, String fileName) {
		log.debug("starting to parse XML file for Spring symbol indexing: ", fileName);

		try {
			File file = new File(fileName);
			long lastModified = file.lastModified();

			String docURI = UriUtil.toUri(file).toString();
			String fileContent = FileUtils.readFileToString(file);

	        scanFile(project, fileContent, docURI);
		}
		catch (Exception e) {
			log.error("error parsing XML file: ", e);
		}
	}

	private void scanFile(IJavaProject project, String fileContent, String docURI) throws Exception {
		DOMParser parser = DOMParser.getInstance();
		DOMDocument document = parser.parse(fileContent, "", null);

		AtomicReference<TextDocument> docRef = new AtomicReference<>();
		scanNode(document, project, docURI, docRef, fileContent);
	}

	private void scanNode(DOMNode node, IJavaProject project, String docURI, AtomicReference<TextDocument> docRef, String content) throws Exception {
		String namespaceURI = node.getNamespaceURI();

		if (namespaceURI != null && this.namespaceHandler.containsKey(namespaceURI)) {
			SpringIndexerXMLNamespaceHandler namespaceHandler = this.namespaceHandler.get(namespaceURI);

			TextDocument document = DocumentUtils.getTempTextDocument(docURI, docRef, content);
			namespaceHandler.processNode(node, project, docURI, document, this.symbolHandler);
		}


//		if ("http://www.springframework.org/schema/beans".equals(namespaceURI)) {
//			List<DOMAttr> attributeNodes = node.getAttributeNodes();
//			if (attributeNodes != null) {
//				for (DOMAttr attribute : attributeNodes) {
//					System.out.println(attribute.getName() + " - " + attribute.getValue());
//				}
//			}
//		}

		List<DOMNode> children = node.getChildren();
		for (DOMNode child : children) {
			scanNode(child, project, docURI, docRef, content);
		}


	}

	private String[] getFiles(IJavaProject project) throws Exception {
		return Files.walk(Paths.get(project.getLocationUri()))
				.filter(path -> path.getFileName().toString().endsWith(".xml"))
				.filter(Files::isRegularFile)
				.map(path -> path.toAbsolutePath().toString())
				.toArray(String[]::new);
	}

}
